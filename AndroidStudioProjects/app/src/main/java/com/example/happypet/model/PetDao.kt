package com.example.happypet.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PetDao {

    @Insert
    suspend fun insertPet(pet: Pet): Long

    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<Pet>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: Int): Pet?
}

