package com.example.bountynet

import com.example.bountynet.Objects.User
import com.google.firebase.database.*
import com.example.bountynet.Objects.Bounty
import android.util.Log


object FirebaseHelper {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun updateObjectAttribute(
        path: String,
        id: String,
        attribute: String,
        attributeValue: Any,
        onFailure: (String) -> Unit
    ) {
        database.child(path).child(id).child(attribute).setValue(attributeValue)
            .addOnFailureListener { exception ->
                onFailure("Error updating attribute: ${exception.message}")
            }

    }

    fun getUserCreatedBountys(userId: String, onSucess: (List<Pair<String, Bounty>>) -> Unit){
        retrieveList(
            path = "bountys",
            type = Bounty::class.java,
            onSuccess = {

                onSucess(it.filter { it.second.createdBy == userId } )
                        },
            onFailure = {}
        )
    }


    fun concludeBounty(
        userId: String
    ){
        val refUser = database.child("users").child(userId)

        refUser.child("currentBountyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                val currentBountyId = userSnapshot.getValue(String::class.java) ?: "ERROR"

                if (currentBountyId != "ERROR") {
                    val refBounty = database.child("bountys").child(currentBountyId)

                    refBounty.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(bountySnapshot: DataSnapshot) {
                            val reward = bountySnapshot.child("reward").getValue(Int::class.java) ?: 0

                            refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(fullUserSnapshot: DataSnapshot) {
                                    val currentCreds = fullUserSnapshot.child("creds").getValue(Int::class.java) ?: 0
                                    val bountiesCompleted = fullUserSnapshot.child("completedBounties").getValue(Int::class.java) ?: 0

                                    refUser.child("currentBountyId").setValue("ERROR")
                                    refUser.child("creds").setValue(currentCreds + reward)
                                    refUser.child("completedBounties").setValue(bountiesCompleted + 1)

                                    // Update bounty data
                                    refBounty.child("concluida").setValue(true)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Failed to fetch user data: ${error.message}")
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Failed to fetch bounty data: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch current bounty ID: ${error.message}")
            }
        })
    }

    fun getObjectAttribute(
        path: String,
        id: String,
        attribute: String,
        onSuccess: (Any) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.child(path).child(id).child(attribute).get()
            .addOnSuccessListener { snapshot ->
            val attributeValue = snapshot.value
            if (attributeValue != null) {
                onSuccess(attributeValue)
            } else {
                onFailure("Attribute not found")
            }
        }.addOnFailureListener { exception ->
            onFailure("Error retrieving attribute: ${exception.message}")
        }
    }

    fun checkAndCreateUser(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userRef = database.child("users")
        // Check if the username already exists
        userRef.orderByChild("username").equalTo(username).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    // Username already exists, check password
                    val userSnapshot = dataSnapshot.children.first()
                    val existingPassword = userSnapshot.child("password").value as? String
                    if (existingPassword == password) {
                        // Return the existing user's ID
                        onSuccess(userSnapshot.key ?: "Unknown ID")
                    } else {
                        onFailure("Invalid password")
                    }
                } else {
                    // Create a new user with a Firebase-generated ID
                    val newUser = User(username = username, password = password)
                    addToDatabase(
                        path = "users",
                        item = newUser,
                        onSuccess = { id -> onSuccess(id) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            } else {
                onFailure("Database error: ${task.exception?.message}")
            }
        }
    }

    fun getUserBounty(
        userId: String,
        onSuccess: (Bounty) -> Unit,
        onFailure: (String) -> Unit
    ) {
        getObjectById(
            path = "users",
            id = userId,
            type = User::class.java,
            onSuccess = { userRet ->
                val bountyId = userRet.currentBountyId.toString()
                // Early return if no bounty
                if (bountyId == "ERROR") {
                    onFailure("User has no bounty")
                }else{
                    getObjectById(
                        path = "bountys",
                        id = bountyId,
                        type = Bounty::class.java,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                }
            },
            onFailure = onFailure
        )
    }

    fun <T : Any> retrieveList(
        path: String,
        type: Class<T>,
        onSuccess: (List<Pair<String, T>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference(path)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { child ->
                    val key = child.key // This might be null
                    val item = child.getValue(type)
                    if (key != null && item != null) {
                        key to item // Create a non-nullable pair
                    } else {
                        null // Skip this entry if key or item is null
                    }
                }
                onSuccess(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
    fun <T> addToDatabase(
        path: String,
        item: T,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference(path)
        val newItemRef = dbRef.push() // Generate a new ID
        newItemRef.setValue(item)
            .addOnSuccessListener { onSuccess(newItemRef.key ?: "Unknown ID") }
            .addOnFailureListener { exception -> onFailure(exception.message ?: "Unknown error") }
    }

    fun <T> getObjectById(
        path: String,
        id: String,
        type: Class<T>,
        onSuccess: (T) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference(path).child(id)
        ref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    // Convert the snapshot into the object of type T
                    val obj = dataSnapshot.getValue(type)
                    if (obj != null) {
                        onSuccess(obj)
                    } else {
                        onFailure("Failed to map data to the object")
                    }
                } else {
                    onFailure("No data found for the given ID")
                }
            } else {
                onFailure("Database error: ${task.exception?.message}")
            }
        }
    }
    fun acceptBounty(
        bountyId: String,
        userId: String,
        callback: (String) -> Unit
    ) {
        val refUser = database.child("users").child(userId)
        refUser.child("currentBountyId").get().addOnSuccessListener { snapshot ->
            val currentBountyId = snapshot.getValue(String::class.java)

            if (currentBountyId != "ERROR") {
                callback("User already has an active bounty")
            }else{
                val refBounty = database.child("bountys").child(bountyId)
                refBounty.child("hunter").setValue(userId)
                refUser.child("currentBountyId").setValue(bountyId)
                callback("Bounty accepted successfully")
            }
        }
    }

    fun releaseBounty(
        bountyId: String,
        userId: String,
        callback: (String) -> Unit
    ) {
        val refUser = database.child("users").child(userId)
        val refBounty = database.child("bountys").child(bountyId)

        refBounty.child("hunter").get().addOnSuccessListener { snapshot ->
            val hunterId = snapshot.getValue(String::class.java)

            if (hunterId != userId) {
                callback("User is not assigned to this bounty")
            } else {
                refBounty.child("hunter").setValue("None")// Remove the hunter from the bounty
                refUser.child("currentBountyId").setValue("ERROR") // Reset user's current bounty
                callback("Bounty released successfully")
            }
        }.addOnFailureListener {
            callback("Failed to release bounty: ${it.message}")
        }
    }

    fun changeProfilePictureIndex(userId: String, newIndex: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userRef = database.child("users").child(userId)
        userRef.child("profilePictureIndex").setValue(newIndex)
            .addOnSuccessListener {
                onSuccess() // Notify success
            }
            .addOnFailureListener { exception ->
                onFailure("Error updating profile picture index: ${exception.message}") // Handle failure
            }
    }
}
