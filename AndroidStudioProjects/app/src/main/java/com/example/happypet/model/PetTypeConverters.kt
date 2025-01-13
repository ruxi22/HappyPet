package com.example.happypet.model

import androidx.room.TypeConverter

class PetTypeConverters {

    // Convert pet image string (e.g., "dog", "cat") to a valid image resource reference
    @TypeConverter
    fun fromPetImageToString(petImage: String?): String? {
        return petImage // Return the pet image name (e.g., "dog", "cat", etc.)
    }

    // (The following method can be removed as it's redundant)
    // @TypeConverter
    // fun fromStringToPetImage(imageName: String?): String? {
    //     return imageName // Just return the image name as a string
    // }
}
