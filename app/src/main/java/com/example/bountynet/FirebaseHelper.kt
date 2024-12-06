package com.example.bountynet

import com.google.firebase.database.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextFieldDefaults.colors

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
}
