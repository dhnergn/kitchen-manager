package com.example.data

import kotlinx.coroutines.flow.Flow

class KitchenRepository(
    private val recipeDao: RecipeDao,
    private val inventoryDao: InventoryDao,
    private val staffDao: StaffDao,
    private val shiftDao: ShiftDao
) {
    // Recipes
    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    suspend fun getRecipeById(id: Int): RecipeEntity? = recipeDao.getRecipeById(id)
    suspend fun insertRecipe(recipe: RecipeEntity) = recipeDao.insertRecipe(recipe)
    suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)
    suspend fun deleteRecipeById(id: Int) = recipeDao.deleteRecipeById(id)

    // Inventory
    val allInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventory()

    suspend fun insertInventoryItem(item: InventoryItemEntity) = inventoryDao.insertInventoryItem(item)
    suspend fun deleteInventoryItem(item: InventoryItemEntity) = inventoryDao.deleteInventoryItem(item)
    suspend fun updateStock(id: Int, quantity: Double) = inventoryDao.updateStock(id, quantity)

    // Staff
    val allStaff: Flow<List<StaffMemberEntity>> = staffDao.getAllStaff()

    suspend fun insertStaffMember(staff: StaffMemberEntity) = staffDao.insertStaffMember(staff)
    suspend fun deleteStaffMember(staff: StaffMemberEntity) = staffDao.deleteStaffMember(staff)

    // Shifts
    val allShifts: Flow<List<ShiftEntity>> = shiftDao.getAllShifts()

    fun getShiftsByDate(date: String): Flow<List<ShiftEntity>> = shiftDao.getShiftsByDate(date)
    suspend fun insertShift(shift: ShiftEntity) = shiftDao.insertShift(shift)
    suspend fun deleteShift(shift: ShiftEntity) = shiftDao.deleteShift(shift)
    suspend fun deleteShiftById(id: Int) = shiftDao.deleteShiftById(id)
}
