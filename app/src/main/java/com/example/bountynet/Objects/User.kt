package com.example.bountynet.Objects

data class User(
    val username: String = "Anonymous",
    val completedBounties: Int = 0,
    val averageTime: String = "0:00",
    val creds: Int = 0,
    val password : String = "ERROR",
    val currentBountyId : String = "ERROR",
)
