package com.example.ui.shifts

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Info
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
import com.example.data.ShiftEntity
import com.example.data.StaffMemberEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(
    shifts: List<ShiftEntity>,
    staff: List<StaffMemberEntity>,
    onAddStaff: (String, String, String) -> Unit,
    onDeleteStaff: (StaffMemberEntity) -> Unit,
    onAddShift: (StaffMemberEntity, String, String, String, String, String) -> Unit,
    onDeleteShift: (ShiftEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Shift Calendar, 1 = Kitchen Roster
    
    // Default system date in July 2026
    val todayDateString = "2026-07-04"
    var selectedDate by remember { mutableStateOf(todayDateString) }
    
    var isAddStaffDialogOpen by remember { mutableStateOf(false) }
    var isAddShiftDialogOpen by remember { mutableStateOf(false) }

    // Generates a neat list of dates around today for the slider (7 days ahead, 2 days behind)
    val dateList = remember {
        val dates = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.parse(todayDateString)
        for (i in -2..6) {
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        dates
    }

    val dailyShifts = remember(shifts, selectedDate) {
        shifts.filter { it.date == selectedDate }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        isAddShiftDialogOpen = true
                    } else {
                        isAddStaffDialogOpen = true
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedTab == 0) "Assign Shift" else "Add Staff") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("shifts_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kitchen Coordination",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Schedule kitchen stations and staff shifts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Sub tabs for shifting focus between "Daily Calendar" and "Kitchen Staff Registry"
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Daily Schedule", fontWeight = FontWeight.SemiBold)
                    } }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kitchen Roster", fontWeight = FontWeight.SemiBold)
                    } }
                )
            }

            if (selectedTab == 0) {
                // DAILY SCHEDULE VIEW
                // 1. Date Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateList) { dateStr ->
                        val date = LocalDate.parse(dateStr)
                        val isSelected = dateStr == selectedDate
                        val dayOfWeek = date.dayOfWeek.name.take(3).lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        val dayOfMonth = date.dayOfMonth.toString()
                        val month = date.month.name.take(3).lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                        Card(
                            modifier = Modifier
                                .width(65.dp)
                                .clickable { selectedDate = dateStr }
                                .testTag("date_card_$dateStr"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayOfWeek,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayOfMonth,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = month,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // 2. Scheduled shifts for selected day
                if (dailyShifts.isEmpty()) {
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
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No shifts scheduled for this day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Assign registered kitchen staff to stations (Grill, Sauté, Pastry, Expo, etc.) for this shift.",
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dailyShifts, key = { it.id }) { shift ->
                            ShiftItemCard(
                                shift = shift,
                                onDelete = { onDeleteShift(shift) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }            } else {
                // KITCHEN ROSTER VIEW
                if (staff.isEmpty()) {
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
                                imageVector = Icons.Default.PeopleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Roster is empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Register your kitchen crew (Chefs, Cooks, Prep and Stewarding) to schedule their shifts.",
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
                        items(staff, key = { it.id }) { member ->
                            StaffRosterRow(
                                member = member,
                                onDelete = { onDeleteStaff(member) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (isAddStaffDialogOpen) {
        AddStaffDialog(
            onDismiss = { isAddStaffDialogOpen = false },
            onConfirm = { name, role, contact ->
                onAddStaff(name, role, contact)
                isAddStaffDialogOpen = false
            }
        )
    }

    if (isAddShiftDialogOpen) {
        if (staff.isEmpty()) {
            AlertDialog(
                onDismissRequest = { isAddShiftDialogOpen = false },
                title = { Text("No Staff Registered") },
                text = { Text("You must register at least one kitchen staff member in the 'Kitchen Roster' tab before scheduling a shift.") },
                confirmButton = {
                    Button(onClick = { 
                        isAddShiftDialogOpen = false
                        selectedTab = 1 // Switch to roster tab
                    }) {
                        Text("Go to Roster")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddShiftDialogOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            AddShiftDialog(
                staffList = staff,
                defaultDate = selectedDate,
                onDismiss = { isAddShiftDialogOpen = false },
                onConfirm = { selectedStaff, date, start, end, station, notes ->
                    onAddShift(selectedStaff, date, start, end, station, notes)
                    isAddShiftDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun ShiftItemCard(
    shift: ShiftEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stationColor = when (shift.station.lowercase()) {
        "expo" -> Color(0xFFE07A5F)      // Terracotta
        "grill" -> Color(0xFFD62828)     // Fire Red
        "sauté" -> Color(0xFFF77F00)     // Flame Orange
        "pastry" -> Color(0xFFE0A96D)    // Honey Copper
        "prep" -> Color(0xFF81B29A)      // Sage Green
        else -> Color(0xFF3D405B)        // Steel Blue
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shift_card_${shift.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Color Pillar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(stationColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shift.staffMemberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Station badge
                    Box(
                        modifier = Modifier
                            .background(stationColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = shift.station.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = stationColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${shift.startTime} - ${shift.endTime} • ${shift.staffMemberRole}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (shift.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notes: ${shift.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Cancel Shift",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StaffRosterRow(
    member: StaffMemberEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("staff_row_${member.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = member.role.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text(
                        text = member.contactInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove from roster",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Line Cook") }
    var contact by remember { mutableStateOf("") }

    val roles = listOf("Head Chef", "Sous Chef", "Pastry Chef", "Line Cook", "Prep Cook", "Dishwasher", "Steward")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Kitchen Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Crew Member Name (e.g. Roy Choi)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_staff_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Email / Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Text("Kitchen Role", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(roles) { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, role, contact)
                    }
                },
                enabled = name.isNotBlank() && contact.isNotBlank(),
                modifier = Modifier.testTag("staff_dialog_submit")
            ) {
                Text("Register Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftDialog(
    staffList: List<StaffMemberEntity>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (StaffMemberEntity, String, String, String, String, String) -> Unit
) {
    var selectedStaff by remember { mutableStateOf(staffList.first()) }
    var date by remember { mutableStateOf(defaultDate) }
    var startTime by remember { mutableStateOf("15:00") }
    var endTime by remember { mutableStateOf("23:00") }
    var station by remember { mutableStateOf("Sauté") }
    var notes by remember { mutableStateOf("") }
    var showStaffDropdown by remember { mutableStateOf(false) }

    val stations = listOf("Sauté", "Grill", "Pastry", "Prep", "Expo", "Dishwashing")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Shift Assignment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Staff Dropdown Picker
                Column {
                    Text("Assign To", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showStaffDropdown = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${selectedStaff.name} (${selectedStaff.role})", fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = showStaffDropdown,
                        onDismissRequest = { showStaffDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        staffList.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.role})") },
                                onClick = {
                                    selectedStaff = s
                                    showStaffDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Shift Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Station list selection
                Column {
                    Text("Working Kitchen Station", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(stations) { s ->
                            FilterChip(
                                selected = station == s,
                                onClick = { station = s },
                                label = { Text(s) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Specific Duties / Prep Notes") },
                    placeholder = { Text("Lead evening sauté service.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedStaff, date, startTime, endTime, station, notes)
                },
                enabled = date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && station.isNotBlank(),
                modifier = Modifier.testTag("shift_dialog_submit")
            ) {
                Text("Schedule Assignment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
