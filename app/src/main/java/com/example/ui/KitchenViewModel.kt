package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KitchenViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = KitchenRepository(
        database.recipeDao(),
        database.inventoryDao(),
        database.staffDao(),
        database.shiftDao()
    )

    // Exposed States
    val recipes: StateFlow<List<RecipeEntity>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staff: StateFlow<List<StaffMemberEntity>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<ShiftEntity>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed database with highly realistic data if it is empty
        viewModelScope.launch {
            combine(
                repository.allRecipes.take(1),
                repository.allInventory.take(1),
                repository.allStaff.take(1),
                repository.allShifts.take(1)
            ) { r, i, s, sh ->
                r.isEmpty() && i.isEmpty() && s.isEmpty() && sh.isEmpty()
            }.collect { isEmpty ->
                if (isEmpty) {
                    seedDatabase()
                }
            }
        }
    }

    // Recipe operations
    fun addRecipe(title: String, category: String, servings: Int, prepTime: Int, cookTime: Int, instructions: String, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            repository.insertRecipe(
                RecipeEntity(
                    title = title,
                    category = category,
                    servings = servings,
                    prepTimeMinutes = prepTime,
                    cookTimeMinutes = cookTime,
                    instructions = instructions,
                    ingredients = ingredients
                )
            )
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    // Inventory operations
    fun addInventoryItem(name: String, currentStock: Double, minThreshold: Double, unit: String, category: String) {
        viewModelScope.launch {
            repository.insertInventoryItem(
                InventoryItemEntity(
                    name = name,
                    currentStock = currentStock,
                    minThreshold = minThreshold,
                    unit = unit,
                    category = category
                )
            )
        }
    }

    fun deleteInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun adjustStock(item: InventoryItemEntity, delta: Double) {
        viewModelScope.launch {
            val newStock = (item.currentStock + delta).coerceAtLeast(0.0)
            repository.insertInventoryItem(item.copy(currentStock = newStock))
        }
    }

    fun setStockDirectly(item: InventoryItemEntity, amount: Double) {
        viewModelScope.launch {
            repository.insertInventoryItem(item.copy(currentStock = amount.coerceAtLeast(0.0)))
        }
    }

    // Staff operations
    fun addStaffMember(name: String, role: String, contactInfo: String) {
        viewModelScope.launch {
            repository.insertStaffMember(
                StaffMemberEntity(
                    name = name,
                    role = role,
                    contactInfo = contactInfo
                )
            )
        }
    }

    fun deleteStaffMember(staffMember: StaffMemberEntity) {
        viewModelScope.launch {
            repository.deleteStaffMember(staffMember)
        }
    }

    // Shift operations
    fun addShift(staffMember: StaffMemberEntity, date: String, startTime: String, endTime: String, station: String, notes: String) {
        viewModelScope.launch {
            repository.insertShift(
                ShiftEntity(
                    staffMemberId = staffMember.id,
                    staffMemberName = staffMember.name,
                    staffMemberRole = staffMember.role,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    station = station,
                    notes = notes
                )
            )
        }
    }

    fun deleteShift(shift: ShiftEntity) {
        viewModelScope.launch {
            repository.deleteShift(shift)
        }
    }

    private suspend fun seedDatabase() {
        // 1. Seed Staff Members
        val mSam = StaffMemberEntity(name = "Marcus Samuelsson", role = "Head Chef", contactInfo = "marcus@kitchen.com")
        val cSaf = StaffMemberEntity(name = "Claire Saffitz", role = "Pastry Chef", contactInfo = "claire@kitchen.com")
        val rChoi = StaffMemberEntity(name = "Roy Choi", role = "Sous Chef", contactInfo = "roy@kitchen.com")
        val mMath = StaffMemberEntity(name = "Matty Matheson", role = "Line Cook", contactInfo = "matty@kitchen.com")
        val bLeon = StaffMemberEntity(name = "Brad Leone", role = "Prep Cook", contactInfo = "brad@kitchen.com")

        repository.insertStaffMember(mSam)
        repository.insertStaffMember(cSaf)
        repository.insertStaffMember(rChoi)
        repository.insertStaffMember(mMath)
        repository.insertStaffMember(bLeon)

        // Since we generated them with autogenerated IDs, we will retrieve them from the database to link shifts.
        // For simplicity we can link using hardcoded ids or names, but we can write them in sequence to ensure correct IDs.
        // Marcus = 1, Claire = 2, Roy = 3, Matty = 4, Brad = 5
        
        // 2. Seed Recipes
        repository.insertRecipe(
            RecipeEntity(
                title = "Signature Beef Wellington",
                category = "Mains",
                servings = 4,
                prepTimeMinutes = 45,
                cookTimeMinutes = 35,
                instructions = "1. Sear the beef tenderloin on all sides in an extremely hot pan with some oil.\n2. Wrap tenderloin tightly in parma ham lined with a thick layer of mushroom duxelles.\n3. Roll puff pastry sheet over the wrapped beef and brush with egg wash.\n4. Bake at 200°C (400°F) for 30-35 minutes until pastry is perfectly golden.\n5. Let rest for 10 minutes before carving.",
                ingredients = listOf(
                    RecipeIngredient("Beef Tenderloin", 800.0, "g"),
                    RecipeIngredient("Puff Pastry Sheets", 2.0, "pcs"),
                    RecipeIngredient("Parma Ham", 100.0, "g"),
                    RecipeIngredient("Cremini Mushrooms", 400.0, "g"),
                    RecipeIngredient("Egg", 1.0, "pcs"),
                    RecipeIngredient("Dijon Mustard", 2.0, "tbsp")
                )
            )
        )

        repository.insertRecipe(
            RecipeEntity(
                title = "Truffle Mushroom Risotto",
                category = "Mains",
                servings = 2,
                prepTimeMinutes = 15,
                cookTimeMinutes = 25,
                instructions = "1. Heat chicken broth in a saucepan and keep at a steady warm simmer.\n2. Sauté minced shallots and cremini mushrooms in butter until tender and browned.\n3. Add Arborio rice and toast it dry with onions for 2 minutes.\n4. Add dry white wine and stir continuously until completely absorbed.\n5. Add broth one ladle at a time, stirring constantly to release starches.\n6. Once rice is creamy and al dente, stir in parmesan, a drizzle of truffle oil, and salt to finish.",
                ingredients = listOf(
                    RecipeIngredient("Arborio Rice", 200.0, "g"),
                    RecipeIngredient("Cremini Mushrooms", 250.0, "g"),
                    RecipeIngredient("Shallot", 1.0, "pcs"),
                    RecipeIngredient("Dry White Wine", 100.0, "ml"),
                    RecipeIngredient("Chicken Stock", 800.0, "ml"),
                    RecipeIngredient("Parmesan Cheese", 50.0, "g"),
                    RecipeIngredient("Truffle Oil", 1.0, "tsp")
                )
            )
        )

        repository.insertRecipe(
            RecipeEntity(
                title = "Classic French Onion Soup",
                category = "Appetizers",
                servings = 4,
                prepTimeMinutes = 15,
                cookTimeMinutes = 50,
                instructions = "1. Melt butter in a heavy-bottomed pot. Add sliced yellow onions and cook on low heat for 40 minutes, stirring occasionally, until caramelized.\n2. Sprinkle flour and cook for 1 minute.\n3. Add dry sherry to deglaze, scraping the browned bits off the bottom.\n4. Pour in beef stock and simmer for 20 minutes.\n5. Ladle soup into heatproof bowls, top with toasted baguette slices and Gruyère.\n6. Broil in oven for 3-5 minutes until cheese is bubbly and toasted brown.",
                ingredients = listOf(
                    RecipeIngredient("Yellow Onions", 1000.0, "g"),
                    RecipeIngredient("Butter", 50.0, "g"),
                    RecipeIngredient("Beef Stock", 1.5, "l"),
                    RecipeIngredient("Gruyère Cheese", 150.0, "g"),
                    RecipeIngredient("Baguette", 1.0, "pcs"),
                    RecipeIngredient("Dry Sherry", 100.0, "ml")
                )
            )
        )

        repository.insertRecipe(
            RecipeEntity(
                title = "Vanilla Bean Crème Brûlée",
                category = "Desserts",
                servings = 4,
                prepTimeMinutes = 15,
                cookTimeMinutes = 40,
                instructions = "1. Heat heavy cream with scraped vanilla bean seeds until simmering.\n2. Whisk egg yolks and white sugar in a bowl until pale.\n3. Temper yolk mixture with hot cream, whisking constantly.\n4. Pour custard into ramekins and bake in water bath at 160°C (325°F) for 35-40 minutes.\n5. Chill for at least 3 hours. Before serving, sprinkle sugar on top and caramelize with blowtorch.",
                ingredients = listOf(
                    RecipeIngredient("Heavy Cream", 500.0, "ml"),
                    RecipeIngredient("Vanilla Bean", 1.0, "pcs"),
                    RecipeIngredient("Egg Yolks", 6.0, "pcs"),
                    RecipeIngredient("Granulated Sugar", 75.0, "g"),
                    RecipeIngredient("Brown Sugar (for topping)", 4.0, "tbsp")
                )
            )
        )

        // 3. Seed Inventory
        repository.insertInventoryItem(InventoryItemEntity(name = "Beef Tenderloin", currentStock = 5.2, minThreshold = 2.0, unit = "kg", category = "Meat & Poultry"))
        repository.insertInventoryItem(InventoryItemEntity(name = "Cremini Mushrooms", currentStock = 0.5, minThreshold = 2.0, unit = "kg", category = "Produce")) // LOW STOCK
        repository.insertInventoryItem(InventoryItemEntity(name = "Puff Pastry Sheets", currentStock = 12.0, minThreshold = 10.0, unit = "pcs", category = "Dry Goods"))
        repository.insertInventoryItem(InventoryItemEntity(name = "Gruyère Cheese", currentStock = 0.6, minThreshold = 1.0, unit = "kg", category = "Dairy")) // LOW STOCK
        repository.insertInventoryItem(InventoryItemEntity(name = "Arborio Rice", currentStock = 1.2, minThreshold = 3.0, unit = "kg", category = "Dry Goods")) // LOW STOCK
        repository.insertInventoryItem(InventoryItemEntity(name = "Yellow Onions", currentStock = 18.0, minThreshold = 5.0, unit = "kg", category = "Produce"))
        repository.insertInventoryItem(InventoryItemEntity(name = "Butter", currentStock = 4.5, minThreshold = 2.0, unit = "kg", category = "Dairy"))
        repository.insertInventoryItem(InventoryItemEntity(name = "Heavy Cream", currentStock = 6.0, minThreshold = 3.0, unit = "l", category = "Dairy"))
        repository.insertInventoryItem(InventoryItemEntity(name = "Truffle Oil", currentStock = 0.15, minThreshold = 0.25, unit = "l", category = "Dry Goods")) // LOW STOCK
        repository.insertInventoryItem(InventoryItemEntity(name = "Vanilla Bean", currentStock = 15.0, minThreshold = 5.0, unit = "pcs", category = "Dry Goods"))

        // 4. Seed Shifts
        // Dates around today (July 4th, 2026) and upcoming days
        repository.insertShift(ShiftEntity(staffMemberId = 1, staffMemberName = "Marcus Samuelsson", staffMemberRole = "Head Chef", date = "2026-07-04", startTime = "08:00", endTime = "16:00", station = "Expo", notes = "Morning opening, prep list verification and lunch service flow."))
        repository.insertShift(ShiftEntity(staffMemberId = 3, staffMemberName = "Roy Choi", staffMemberRole = "Sous Chef", date = "2026-07-04", startTime = "12:00", endTime = "21:00", station = "Grill", notes = "Lunch rush support, dinner shift transition."))
        repository.insertShift(ShiftEntity(staffMemberId = 4, staffMemberName = "Matty Matheson", staffMemberRole = "Line Cook", date = "2026-07-04", startTime = "15:00", endTime = "23:00", station = "Sauté", notes = "Dinner sauté lead, station breakdown and kitchen close."))
        repository.insertShift(ShiftEntity(staffMemberId = 5, staffMemberName = "Brad Leone", staffMemberRole = "Prep Cook", date = "2026-07-04", startTime = "07:00", endTime = "15:00", station = "Prep", notes = "Meat and veg prep for weekend rush, storage rotation."))

        repository.insertShift(ShiftEntity(staffMemberId = 2, staffMemberName = "Claire Saffitz", staffMemberRole = "Pastry Chef", date = "2026-07-05", startTime = "06:00", endTime = "14:00", station = "Pastry", notes = "Sunday brunch desserts prep, soufflé and custard bake."))
        repository.insertShift(ShiftEntity(staffMemberId = 3, staffMemberName = "Roy Choi", staffMemberRole = "Sous Chef", date = "2026-07-05", startTime = "11:00", endTime = "20:00", station = "Expo", notes = "Brunch expo & evening briefing."))
        repository.insertShift(ShiftEntity(staffMemberId = 4, staffMemberName = "Matty Matheson", staffMemberRole = "Line Cook", date = "2026-07-05", startTime = "14:00", endTime = "22:00", station = "Grill", notes = "Steak and fish lead."))
    }
}
