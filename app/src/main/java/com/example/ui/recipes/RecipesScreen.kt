package com.example.ui.recipes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.RecipeEntity
import com.example.data.RecipeIngredient

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipesScreen(
    recipes: List<RecipeEntity>,
    onRecipeClick: (RecipeEntity) -> Unit,
    onAddRecipe: (String, String, Int, Int, Int, String, List<RecipeIngredient>) -> Unit,
    onDeleteRecipe: (RecipeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    // Categories list based on data
    val categories = remember(recipes) {
        listOf("All") + recipes.map { it.category }.distinct().sorted()
    }

    val filteredRecipes = remember(recipes, searchQuery, selectedCategory) {
        recipes.filter { recipe ->
            val matchesSearch = recipe.title.contains(searchQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.name.contains(searchQuery, ignoreCase = true) }
            val matchesCategory = selectedCategory == "All" || recipe.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddDialogOpen = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Recipe") },
                text = { Text("New Recipe") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_recipe_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Culinary Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_kitchen_hero_1783178571752),
                        contentDescription = "Kitchen workspace",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    // Gradient overlay to ground the aesthetic
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                    // Workspace Active Badge
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "EXECUTIVE CHEF STATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recipe Book",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage and scale your culinary secrets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes or ingredients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("recipe_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Recipe List
            if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "All") "No matching recipes" else "No recipes yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "All") "Try adjusting your filters or search query." else "Get started by adding your first signature kitchen recipe!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) },
                            onDelete = { onDeleteRecipe(recipe) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddRecipeDialog(
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { title, category, servings, prepTime, cookTime, instructions, ingredients ->
                onAddRecipe(title, category, servings, prepTime, cookTime, instructions, ingredients)
                isAddDialogOpen = false
            }
        )
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recipe_card_${recipe.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Recipe",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Servings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Prep & Cook Time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} mins",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SoupKitchen,
                        contentDescription = "Ingredients count",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.ingredients.size} ingredients",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "Servings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Yields ${recipe.servings}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, String, List<RecipeIngredient>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mains") }
    var servings by remember { mutableIntStateOf(4) }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    // List of ingredients for the builder
    val ingredientsList = remember { mutableStateListOf<RecipeIngredient>() }

    // Temp variables for adding a single ingredient to the list
    var ingName by remember { mutableStateOf("") }
    var ingAmount by remember { mutableStateOf("") }
    var ingUnit by remember { mutableStateOf("g") }

    val categories = listOf("Mains", "Appetizers", "Desserts", "Sauces", "Breads", "Sides")
    val units = listOf("g", "kg", "ml", "l", "pcs", "tbsp", "tsp", "cans", "pinch")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Kitchen Recipe", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Recipe Name (e.g. Lobster Bisque)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_recipe_title_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) prepTime = it },
                            label = { Text("Prep (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cookTime,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) cookTime = it },
                            label = { Text("Cook (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Default Yield (Servings): $servings",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (servings > 1) servings-- }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Servings")
                            }
                            Text("$servings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { servings++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Servings")
                            }
                        }
                    }
                }

                // Ingredient List Builder
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Ingredients (${ingredientsList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (ingredientsList.isNotEmpty()) {
                    items(ingredientsList.toList()) { ing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${ing.amount} ${ing.unit} of ${ing.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { ingredientsList.remove(ing) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove ingredient", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Add Ingredient Row Inputs
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = ingName,
                                    onValueChange = { ingName = it },
                                    placeholder = { Text("Ingredient name") },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = ingAmount,
                                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { c -> c.isDigit() }) ingAmount = it },
                                    placeholder = { Text("Qty") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.7f),
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Unit Selection Dropdown / Selector scroll
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(units) { u ->
                                        val isSel = u == ingUnit
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                .clickable { ingUnit = u }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(u, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val amt = ingAmount.toDoubleOrNull() ?: 0.0
                                        if (ingName.isNotBlank() && amt > 0.0) {
                                            ingredientsList.add(RecipeIngredient(ingName, amt, ingUnit))
                                            ingName = ""
                                            ingAmount = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions / Steps (one per line)") },
                        placeholder = { Text("1. Sear the beef...\n2. Heat the broth...") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        maxLines = 10
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pTime = prepTime.toIntOrNull() ?: 0
                    val cTime = cookTime.toIntOrNull() ?: 0
                    if (title.isNotBlank() && ingredientsList.isNotEmpty() && instructions.isNotBlank()) {
                        onConfirm(title, category, servings, pTime, cTime, instructions, ingredientsList.toList())
                    }
                },
                enabled = title.isNotBlank() && ingredientsList.isNotEmpty() && instructions.isNotBlank(),
                modifier = Modifier.testTag("recipe_dialog_submit")
            ) {
                Text("Save Recipe")
            }
        },
        dismissButton = {
            TextButton(onClick = dismissButton@{ onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
