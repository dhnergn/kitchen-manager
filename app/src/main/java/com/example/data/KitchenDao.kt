package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Int)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItemEntity)

    @Query("UPDATE inventory SET currentStock = :quantity WHERE id = :id")
    suspend fun updateStock(id: Int, quantity: Double)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffMember(staff: StaffMemberEntity)

    @Delete
    suspend fun deleteStaffMember(staff: StaffMemberEntity)
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY date ASC, startTime ASC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE date = :date ORDER BY startTime ASC")
    fun getShiftsByDate(date: String): Flow<List<ShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity)

    @Delete
    suspend fun deleteShift(shift: ShiftEntity)

    @Query("DELETE FROM shifts WHERE id = :id")
    suspend fun deleteShiftById(id: Int)
}
