package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.ui.components.HospitalHeader
import com.example.ui.state.FilterCriteria
import com.example.ui.state.HospitalDashboardStats
import com.example.ui.state.Screen
import com.example.ui.state.SortField
import com.example.ui.viewmodel.HospitalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    viewModel: HospitalViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.filteredAdminRecords.collectAsState()
    val allRecords by viewModel.rawRecords.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    val filterCriteria by viewModel.filters.collectAsState()
    val sortField by viewModel.sortField.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val selectedIds by viewModel.selectedRecordIds.collectAsState()

    val detailRecord by viewModel.detailRecord.collectAsState()
    val editingRecord by viewModel.editingRecord.collectAsState()
    val recordToDelete by viewModel.recordToDelete.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()

    var showFiltersPanel by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HospitalHeader(
                currentUser = currentUser,
                onLogout = { viewModel.logout() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.NURSE_ENTRY_FORM) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("admin_fab_add_patient")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // KPI Summary Stats Banner (Section 7)
            AdminStatsRow(stats = stats)

            // Primary Administrator Action Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export to Excel Button (DOCTOR/ADMIN ONLY)
                    Button(
                        onClick = { viewModel.openExportDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("admin_export_excel_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "EXPORT EXCEL (${selectedIds.size})" else "EXPORT TO EXCEL (.XLSX)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.ADMIN_AUDIT_LOGS) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_audit_trail_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit Trail", style = MaterialTheme.typography.labelSmall)
                        }

                        IconButton(
                            onClick = { showFiltersPanel = !showFiltersPanel },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showFiltersPanel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (showFiltersPanel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Expansion
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = filterCriteria.query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search by S NO, IP NO, Name, Diagnosis, Category...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (filterCriteria.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = showFiltersPanel) {
                    AdminFilterPanel(
                        criteria = filterCriteria,
                        onSpecialty = { viewModel.setFilterSpecialty(it) },
                        onMlc = { viewModel.setFilterMlc(it) },
                        onPillar = { viewModel.setFilterPillar(it) },
                        onTransferred = { viewModel.setFilterTransferred(it) },
                        onDate = { viewModel.setFilterDate(it) },
                        onClear = { viewModel.clearFilters() }
                    )
                }

                // Table Controls: Selection indicator & Sort Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = records.isNotEmpty() && selectedIds.containsAll(records.map { it.id }),
                            onCheckedChange = { viewModel.selectAllVisibleRecords() },
                            modifier = Modifier.testTag("admin_select_all_checkbox")
                        )
                        Text(
                            text = if (selectedIds.isEmpty()) "Showing ${records.size} of ${allRecords.size} Records" else "${selectedIds.size} Selected",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Sort Chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Sort:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        listOf(SortField.SERIAL_NUMBER, SortField.ADMISSION_DATE, SortField.PATIENT_NAME).forEach { sf ->
                            val isSelected = sortField == sf
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { viewModel.setSort(sf) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sf.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main Records Table / Cards List
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (allRecords.isEmpty()) "Database is Currently Empty" else "No matching records found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (allRecords.isEmpty()) "The system starts with an empty patient database. Authorized nurses can enter emergency records." else "Try adjusting or clearing your search and filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().testTag("admin_records_list")
                ) {
                    items(records, key = { it.id }) { record ->
                        AdminRecordRowCard(
                            record = record,
                            isSelected = selectedIds.contains(record.id),
                            onToggleSelect = { viewModel.toggleRecordSelection(record.id) },
                            onView = { viewModel.setDetailRecord(record) },
                            onEdit = { viewModel.setEditingRecord(record) },
                            onDelete = { viewModel.confirmDelete(record) },
                            onDuplicate = { viewModel.duplicateRecord(record.id) }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (detailRecord != null) {
        PatientDetailDialog(
            record = detailRecord!!,
            canEdit = true,
            canDelete = true,
            canDuplicate = true,
            onEdit = {
                val r = detailRecord!!
                viewModel.setDetailRecord(null)
                viewModel.setEditingRecord(r)
            },
            onDelete = {
                val r = detailRecord!!
                viewModel.setDetailRecord(null)
                viewModel.confirmDelete(r)
            },
            onDuplicate = {
                val r = detailRecord!!
                viewModel.setDetailRecord(null)
                viewModel.duplicateRecord(r.id)
            },
            onDismiss = { viewModel.setDetailRecord(null) }
        )
    }

    if (editingRecord != null) {
        EditPatientDialog(
            record = editingRecord!!,
            onSave = { updated ->
                viewModel.updateRecord(updated) {
                    viewModel.setEditingRecord(null)
                }
            },
            onDismiss = { viewModel.setEditingRecord(null) }
        )
    }

    if (recordToDelete != null) {
        DeleteConfirmDialog(
            record = recordToDelete!!,
            onConfirmDelete = { viewModel.executeDelete() },
            onDismiss = { viewModel.dismissDelete() }
        )
    }

    if (showExportDialog) {
        ExcelExportDialog(
            totalRecordsCount = allRecords.size,
            selectedRecordsCount = selectedIds.size,
            filteredRecordsCount = records.size,
            exportedFile = exportedFile,
            onExport = { fname, onlySelected ->
                viewModel.exportToExcel(context, fname, onlySelected)
            },
            onDismiss = {
                viewModel.dismissExportDialog()
                viewModel.clearExportedFile()
            }
        )
    }
}

@Composable
fun AdminStatsRow(stats: HospitalDashboardStats) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(title = "Total Patients", value = stats.totalRecords.toString(), highlight = true)
        StatPill(title = "Today's Admitted", value = stats.todayRecords.toString())
        StatPill(title = "Surgery Cases", value = stats.surgeryCount.toString())
        StatPill(title = "Medicine Cases", value = stats.medicineCount.toString())
        StatPill(title = "MLC (Medico-Legal)", value = stats.mlcCount.toString())
        StatPill(title = "Transferred Out", value = stats.transferredCount.toString())
    }
}

@Composable
fun StatPill(title: String, value: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlight) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminFilterPanel(
    criteria: FilterCriteria,
    onSpecialty: (String?) -> Unit,
    onMlc: (String?) -> Unit,
    onPillar: (String?) -> Unit,
    onTransferred: (String?) -> Unit,
    onDate: (String?) -> Unit,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Filter Matrix", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                TextButton(onClick = onClear) {
                    Text("Clear All Filters", style = MaterialTheme.typography.labelSmall)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Specialty
                FilterChip(
                    selected = criteria.broadSpecialty == "SURGERY CASES",
                    onClick = { onSpecialty(if (criteria.broadSpecialty == "SURGERY CASES") null else "SURGERY CASES") },
                    label = { Text("Surgery Cases") }
                )
                FilterChip(
                    selected = criteria.broadSpecialty == "MEDICINE CASES",
                    onClick = { onSpecialty(if (criteria.broadSpecialty == "MEDICINE CASES") null else "MEDICINE CASES") },
                    label = { Text("Medicine Cases") }
                )

                // MLC
                FilterChip(
                    selected = criteria.medicolegal == "MLC",
                    onClick = { onMlc(if (criteria.medicolegal == "MLC") null else "MLC") },
                    label = { Text("MLC") }
                )
                FilterChip(
                    selected = criteria.medicolegal == "NMLC",
                    onClick = { onMlc(if (criteria.medicolegal == "NMLC") null else "NMLC") },
                    label = { Text("NMLC") }
                )

                // Pillar
                FilterChip(
                    selected = criteria.pillarStatus == "PILLAR",
                    onClick = { onPillar(if (criteria.pillarStatus == "PILLAR") null else "PILLAR") },
                    label = { Text("Pillar") }
                )
                FilterChip(
                    selected = criteria.pillarStatus == "NON PILLAR",
                    onClick = { onPillar(if (criteria.pillarStatus == "NON PILLAR") null else "NON PILLAR") },
                    label = { Text("Non Pillar") }
                )

                // Transferred
                FilterChip(
                    selected = criteria.transferredOut == "YES",
                    onClick = { onTransferred(if (criteria.transferredOut == "YES") null else "YES") },
                    label = { Text("Transferred") }
                )

                // Date
                FilterChip(
                    selected = criteria.dateFilter == "TODAY",
                    onClick = { onDate(if (criteria.dateFilter == "TODAY") null else "TODAY") },
                    label = { Text("Today Only") }
                )
            }
        }
    }
}

@Composable
fun AdminRecordRowCard(
    record: PatientRecordEntity,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_record_card_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Bar: Checkbox, S NO, IP NO, MLC, Speciality
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() }
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "S NO #${record.serialNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "IP: ${record.ipNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (record.medicolegalCategory == "MLC") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = record.medicolegalCategory,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (record.medicolegalCategory == "MLC") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = record.broadSpecialityCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Patient Name, Demographics, Diagnosis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Age: ${record.age} Yrs (${record.ageInterval}) • Sex: ${record.sex}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adm: ${record.admissionDateTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Rec: ${record.patientReceivedTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clinical Summary Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Diagnosis: ${record.diagnosis}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Transfer: ${record.transferredOut} (${record.transferredOutTime}) • Resp: ${record.emergencyResponseTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Text(
                        text = "Nurse: ${record.createdByNurseName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: View, Edit, Duplicate, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onView, modifier = Modifier.testTag("record_view_btn_${record.id}")) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(onClick = onEdit, modifier = Modifier.testTag("record_edit_btn_${record.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(onClick = onDuplicate, modifier = Modifier.testTag("record_dup_btn_${record.id}")) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("record_del_btn_${record.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
