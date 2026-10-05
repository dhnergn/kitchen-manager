package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val ingredientListType = Types.newParameterizedType(List::class.java, RecipeIngredient::class.java)
    private val adapter = moshi.adapter<List<RecipeIngredient>>(ingredientListType)

    @TypeConverter
    fun fromIngredientsList(ingredients: List<RecipeIngredient>?): String {
        return adapter.toJson(ingredients ?: emptyList())
    }

    @TypeConverter
    fun toIngredientsList(ingredientsJson: String?): List<RecipeIngredient> {
        if (ingredientsJson.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(ingredientsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
