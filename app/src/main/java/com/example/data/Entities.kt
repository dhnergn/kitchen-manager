package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    val name: String,
    val amount: Double,
    val unit: String
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Mains", "Appetizers", "Desserts", "Sauces", etc.
    val servings: Int,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val instructions: String, // Newline separated steps
    val ingredients: List<RecipeIngredient> // Converted to/from JSON
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentStock: Double,
    val minThreshold: Double,
    val unit: String, // "kg", "g", "l", "ml", "pcs", "cans", etc.
    val category: String // "Produce", "Meat & Poultry", "Seafood", "Dairy", "Dry Goods", etc.
) {
    val isLowStock: Boolean
        get() = currentStock <= minThreshold

    val stockStatus: String
        get() = when {
            currentStock <= 0 -> "Out of Stock"
            isLowStock -> "Low Stock"
            else -> "In Stock"
        }
}

@Entity(tableName = "staff")
data class StaffMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // "Head Chef", "Sous Chef", "Line Cook", "Prep Cook", "Dishwasher"
    val contactInfo: String
)

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val staffMemberId: Int,
    val staffMemberName: String,
    val staffMemberRole: String,
    val date: String, // "YYYY-MM-DD"
    val startTime: String, // "HH:MM"
    val endTime: String, // "HH:MM"
    val station: String, // "Sauté", "Grill", "Pastry", "Prep", "Dishwashing", "Expo"
    val notes: String = ""
)
