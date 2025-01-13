package com.example.happypet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val username: String,
    val password: String,
    var petId: Int? = null // Change to var to allow reassignment
)

