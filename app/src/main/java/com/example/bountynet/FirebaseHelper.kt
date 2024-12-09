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
        val userRef = database.child("users").child(username)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val existingPassword = dataSnapshot.child("password").value as? String
                    if (existingPassword == password) {
                        onSuccess(username)
                    } else {
                        onFailure("Invalid password")
                    }
                } else {
                    val newUser = mapOf("password" to password)
                    userRef.setValue(newUser).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful) {
                            onSuccess(username)
                        } else {
                            onFailure("Failed to create user")
                        }
                    }
                }
            } else {
                onFailure("Database error: ${task.exception?.message}")
            }
        }
    }

    fun getUser(
        username: String?,
        onSuccess: (User?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (username == null) {
            onFailure("Username is null")
            return
        }

        val userRef = database.child("users").child(username)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    // Map the data snapshot to a User object
                    val user = dataSnapshot.getValue(User::class.java)
                    onSuccess(user)
                } else {
                    onSuccess(null) // User not found
                }
            } else {
                onFailure("Error fetching user: ${task.exception?.message}")
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
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference(path)
        val newItemRef = dbRef.push() // Generate a new ID
        newItemRef.setValue(item)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception.message ?: "Unknown error") }
    }
}
