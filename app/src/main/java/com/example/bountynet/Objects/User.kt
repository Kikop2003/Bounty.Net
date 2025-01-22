package com.example.bountynet.Objects

data class User(
    val username: String = "Anonymous",
    val completedBounties: Int = 0,
    val creds: Int = 10000,
    private val password: String = "ERROR", // Passwords should be securely handled
    val currentBountyId: String = "ERROR",
    var profilePictureIndex: Int = 1 // Validated to be within 1-4
) {
    init {
        require(profilePictureIndex in 1..4) {
            "Profile picture index must be between 1 and 4"
        }
    }
}
