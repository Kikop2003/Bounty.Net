package com.example.bountynet.Objects

data class User(
    val username: String = "Anonymous",
    val completedBounties: Int = 0,
    val creds: Int = 10000,
    val password : String = "ERROR",
    val currentBountyId : String = "ERROR",
)
