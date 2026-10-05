package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RecipeEntity
import com.example.ui.KitchenViewModel
import com.example.ui.inventory.InventoryScreen
import com.example.ui.recipes.RecipeDetailScreen
import com.example.ui.recipes.RecipesScreen
import com.example.ui.shifts.ShiftsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KitchenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var selectedRecipe by remember { mutableStateOf<RecipeEntity?>(null) }

                val recipes by viewModel.recipes.collectAsStateWithLifecycle()
                val inventory by viewModel.inventory.collectAsStateWithLifecycle()
                val staff by viewModel.staff.collectAsStateWithLifecycle()
                val shifts by viewModel.shifts.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Only show bottom navigation when not viewing recipe details
                        if (selectedRecipe == null) {
                            NavigationBar(
                                modifier = Modifier.testTag("bottom_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Recipes") },
                                    label = { Text("Recipes") },
                                    modifier = Modifier.testTag("tab_recipes")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                                    label = { Text("Inventory") },
                                    modifier = Modifier.testTag("tab_inventory")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Shifts") },
                                    label = { Text("Shifts") },
                                    modifier = Modifier.testTag("tab_shifts")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (selectedRecipe != null) {
                            RecipeDetailScreen(
                                recipe = selectedRecipe,
                                onBack = { selectedRecipe = null }
                            )
                        } else {
                            when (selectedTab) {
                                0 -> RecipesScreen(
                                    recipes = recipes,
                                    onRecipeClick = { selectedRecipe = it },
                                    onAddRecipe = { title, category, servings, prep, cook, instructions, ingredients ->
                                        viewModel.addRecipe(title, category, servings, prep, cook, instructions, ingredients)
                                    },
                                    onDeleteRecipe = { viewModel.deleteRecipe(it) }
                                )
                                1 -> InventoryScreen(
                                    inventory = inventory,
                                    onAddItem = { name, current, min, unit, category ->
                                        viewModel.addInventoryItem(name, current, min, unit, category)
                                    },
                                    onAdjustStock = { item, delta ->
                                        viewModel.adjustStock(item, delta)
                                    },
                                    onSetStockDirectly = { item, amt ->
                                        viewModel.setStockDirectly(item, amt)
                                    },
                                    onDeleteItem = { viewModel.deleteInventoryItem(it) }
                                )
                                2 -> ShiftsScreen(
                                    shifts = shifts,
                                    staff = staff,
                                    onAddStaff = { name, role, contact ->
                                        viewModel.addStaffMember(name, role, contact)
                                    },
                                    onDeleteStaff = { viewModel.deleteStaffMember(it) },
                                    onAddShift = { staffMember, date, start, end, station, notes ->
                                        viewModel.addShift(staffMember, date, start, end, station, notes)
                                    },
                                    onDeleteShift = { viewModel.deleteShift(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
