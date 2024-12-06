package com.example.bountynet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> get() = _username

    fun setUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun logout() {
        _username.value = null // Clear the username
    }
}