package com.example.bountynet.Objects

data class User(
    val name: String = "Anonymous",
    val CompletedBounties: Int = 0,
    val Averagetime: String = "0:00",
    val Creds: Int = 0
)
