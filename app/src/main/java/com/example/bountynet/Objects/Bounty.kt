package com.example.bountynet.Objects

data class Bounty(
    val name: String = "Unnamed Bounty",
    val reward: Int = 0,
    val concluida: Boolean = false,
    val tempo: String = "Unfinished",
    val hunter: String = "None",
    val planeta: String = "Not Defined",
    val createdBy: String = "Undefined",
    val lat: Double = 0.0,
    val lon: Double = 0.0,

) {
    companion object {
        val possiblePlanets = listOf("Earth", "Mars", "Jupiter", "Venus", "Saturn")
    }
}
