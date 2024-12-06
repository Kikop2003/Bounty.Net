package com.example.bountynet

data class Bounty(
    val id: Long = 0,
    val name: String = "",
    val reward: Double = 0.0
) {
    override fun toString(): String {
        return "Bounty ID: $id, Name: $name, Reward: $reward"
    }
}
