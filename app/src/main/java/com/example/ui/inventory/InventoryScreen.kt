package com.example.ui.inventory

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Warning
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
import com.example.data.InventoryItemEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    inventory: List<InventoryItemEntity>,
    onAddItem: (String, Double, Double, String, String) -> Unit,
    onAdjustStock: (InventoryItemEntity, Double) -> Unit,
    onSetStockDirectly: (InventoryItemEntity, Double) -> Unit,
    onDeleteItem: (InventoryItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isAddItemDialogOpen by remember { mutableStateOf(false) }
    var itemToEditDirectly by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val categories = remember(inventory) {
        listOf("All") + inventory.map { it.category }.distinct().sorted()
    }

    val lowStockItems = remember(inventory) {
        inventory.filter { it.isLowStock }
    }

    val filteredInventory = remember(inventory, searchQuery, selectedCategory) {
        inventory.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddItemDialogOpen = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Inventory Item") },
                text = { Text("Add Ingredient") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_inventory_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kitchen Inventory",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Track and adjust active stock levels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Low Stock Alert Banner (Only shows when there are items low in stock)
            AnimatedVisibility(
                visible = lowStockItems.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOW STOCK ALERTS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${lowStockItems.size} ingredient(s) are critically low and require immediate reordering.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search ingredients...") },
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
                    .testTag("inventory_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Category Tabs
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

            // Inventory List
            if (filteredInventory.isEmpty()) {
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
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "All") "No matching ingredients" else "Inventory is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategory != "All") "Try adjusting your search filters." else "Add your kitchen's food stock to start tracking levels and reorder limits.",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInventory, key = { it.id }) { item ->
                        InventoryRow(
                            item = item,
                            onAdjust = { delta -> onAdjustStock(item, delta) },
                            onEditQuantity = { itemToEditDirectly = item },
                            onDelete = { onDeleteItem(item) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (isAddItemDialogOpen) {
        AddInventoryItemDialog(
            onDismiss = { isAddItemDialogOpen = false },
            onConfirm = { name, current, min, unit, category ->
                onAddItem(name, current, min, unit, category)
                isAddItemDialogOpen = false
            }
        )
    }

    if (itemToEditDirectly != null) {
        val editingItem = itemToEditDirectly!!
        DirectQuantityEditDialog(
            item = editingItem,
            onDismiss = { itemToEditDirectly = null },
            onConfirm = { amt ->
                onSetStockDirectly(editingItem, amt)
                itemToEditDirectly = null
            }
        )
    }
}

@Composable
fun InventoryRow(
    item: InventoryItemEntity,
    onAdjust: (Double) -> Unit,
    onEditQuantity: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (item.stockStatus) {
        "Out of Stock" -> MaterialTheme.colorScheme.error
        "Low Stock" -> Color(0xFFE28413) // Amber
        else -> Color(0xFF4CAF50) // Emerald Green
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_row_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditQuantity() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.stockStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category: ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Min Reorder Limit: ${item.minThreshold} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick adjustment controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val step = if (item.unit == "kg" || item.unit == "l") 0.5 else 1.0
                
                IconButton(
                    onClick = { onAdjust(-step) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.size(36.dp).testTag("inventory_minus_${item.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Reduce Stock", modifier = Modifier.size(16.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(min = 60.dp)
                        .clickable { onEditQuantity() }
                ) {
                    val formattedStock = if (item.currentStock % 1.0 == 0.0) {
                        item.currentStock.toInt().toString()
                    } else {
                        String.format(Locale.getDefault(), "%.1f", item.currentStock)
                    }
                    Text(
                        text = "$formattedStock ${item.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = if (item.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { onAdjust(step) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.size(36.dp).testTag("inventory_plus_${item.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Stock", modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currentStr by remember { mutableStateOf("") }
    var minStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var category by remember { mutableStateOf("Produce") }

    val categories = listOf("Produce", "Meat & Poultry", "Seafood", "Dairy", "Dry Goods", "Bakery", "Beverages", "Spices")
    val units = listOf("kg", "g", "l", "ml", "pcs", "cans", "boxes", "bags")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Kitchen Supply", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient Name (e.g. Ribeye Steak)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_inventory_name_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { c -> c.isDigit() }) currentStr = it },
                        label = { Text("Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = minStr,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { c -> c.isDigit() }) minStr = it },
                        label = { Text("Min Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Column {
                    Text("Unit Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(units) { u ->
                            FilterChip(
                                selected = unit == u,
                                onClick = { unit = u },
                                label = { Text(u) }
                            )
                        }
                    }
                }

                Column {
                    Text("Inventory Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val current = currentStr.toDoubleOrNull() ?: 0.0
                    val min = minStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(name, current, min, unit, category)
                    }
                },
                enabled = name.isNotBlank() && currentStr.isNotEmpty() && minStr.isNotEmpty(),
                modifier = Modifier.testTag("inventory_dialog_submit")
            ) {
                Text("Add Ingredient")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DirectQuantityEditDialog(
    item: InventoryItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantityStr by remember { mutableStateOf(item.currentStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update ${item.name} Stock Level", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter direct active quantity in stock (${item.unit}):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { c -> c.isDigit() }) quantityStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("direct_stock_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull()
                    if (qty != null && qty >= 0) {
                        onConfirm(qty)
                    }
                },
                enabled = quantityStr.toDoubleOrNull() != null,
                modifier = Modifier.testTag("direct_stock_submit")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
