package com.example.bountynet

import com.example.bountynet.Objects.User
import com.google.firebase.database.*


object FirebaseHelper {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference



    fun checkAndCreateUser(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (username.isEmpty() || password.isEmpty()) {
            onFailure("Username and password cannot be empty")
            return
        }
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





    fun <T : Any> retrieveList(
        path: String,
        type: Class<T>,
        onSuccess: (List<T>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference(path)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(type) }
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

}
