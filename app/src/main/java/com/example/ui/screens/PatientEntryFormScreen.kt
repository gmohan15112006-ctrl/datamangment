package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientRecordEntity
import com.example.data.model.UserEntity
import com.example.ui.state.Screen
import com.example.ui.viewmodel.HospitalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PatientEntryFormScreen(
    viewModel: HospitalViewModel,
    currentUser: UserEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Form Fields matching Hospital Excel structure
    var ipNumber by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("M") }

    val now = remember { Date() }
    val defaultAdmissionDate = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(now) }
    val defaultReceivedTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }

    var admissionDateTime by remember { mutableStateOf(defaultAdmissionDate) }
    var patientReceivedTime by remember { mutableStateOf(defaultReceivedTime) }

    var broadSpeciality by remember { mutableStateOf("SURGERY CASES") }
    var diagnosis by remember { mutableStateOf("") }
    var ageInterval by remember { mutableStateOf("12-60") }
    var pillarStatus by remember { mutableStateOf("PILLAR") }
    var medicolegalCategory by remember { mutableStateOf("NMLC") }
    var transferredOut by remember { mutableStateOf("NO") }
    var transferredOutTime by remember { mutableStateOf("N/A") }
    var emergencyResponseTime by remember { mutableStateOf("Immediate") }

    // Validation & Confirmation
    var formValidationErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Helper options
    val broadSpecialities = listOf("SURGERY CASES", "MEDICINE CASES")
    val commonDiagnoses = listOf("RTA", "POISONING", "ASF", "CHEST PAIN", "HYPOGLYCEMIA", "ASSAULT", "FEVER", "ABDOMINAL PAIN")
    val ageIntervals = listOf("12-60", ">60", "<12", "Other")
    val pillarOptions = listOf("PILLAR", "NON PILLAR")
    val mlcOptions = listOf("NMLC", "MLC")
    val transferDestinations = listOf("NO", "ICU", "WARD 1", "WARD 2", "WARD 4", "HIGHER CENTER", "OT")
    val responseTimes = listOf("Immediate", "< 5 mins", "5-10 mins", "15 mins", "30 mins")

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        if (ipNumber.isBlank()) errors.add("IP Number is required.")
        if (patientName.isBlank()) errors.add("Patient Name cannot be empty.")
        val age = ageString.toIntOrNull()
        if (age == null || age < 0 || age > 130) errors.add("Age must be a valid number between 0 and 130.")
        if (admissionDateTime.isBlank()) errors.add("Admission Date & Time is required.")
        if (patientReceivedTime.isBlank()) errors.add("Patient Received Time is required.")
        if (diagnosis.isBlank()) errors.add("Diagnosis is required.")

        formValidationErrors = errors
        return errors.isEmpty()
    }

    fun openDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                TimePickerDialog(
                    context,
                    { _, h, m ->
                        val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d", year, month + 1, day, h, m)
                        onDateSelected(formatted)
                    },
                    hour,
                    minute,
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun openTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, m ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                onTimeSelected(formatted)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Patient Entry Form",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Hospital Emergency Excel Data Structure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Info & S NO Auto Notice
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "FIELD 1: S NO (Serial Number)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Automatically generated by hospital database upon submission. Manual input is locked.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Validation error banner
            if (formValidationErrors.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Please resolve the following before submitting:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        formValidationErrors.forEach { err ->
                            Text(
                                text = "• $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // SECTION 1: Patient Core Identifiers
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Patient Identifiers",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. IP NO
                    OutlinedTextField(
                        value = ipNumber,
                        onValueChange = { ipNumber = it },
                        label = { Text("2. IP NO (Inpatient Number) *") },
                        placeholder = { Text("e.g. 9985 or 10452") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_ip_number_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. NAME
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("3. NAME (Patient Full Name) *") },
                        placeholder = { Text("e.g. John Doe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_patient_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 4. AGE
                        OutlinedTextField(
                            value = ageString,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() } && it.length <= 3) {
                                    ageString = it
                                    // Auto update age interval suggestion
                                    val a = it.toIntOrNull()
                                    if (a != null) {
                                        ageInterval = if (a < 12) "<12" else if (a in 12..60) "12-60" else ">60"
                                    }
                                }
                            },
                            label = { Text("4. AGE *") },
                            placeholder = { Text("e.g. 35") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("form_age_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // 5. SEX
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "5. SEX *",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                listOf("M", "F", "Other").forEach { s ->
                                    FilterChip(
                                        selected = sex == s,
                                        onClick = { sex = s },
                                        label = { Text(s, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.testTag("sex_chip_$s")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 10. AGE INTERVAL
                    Text(
                        text = "10. AGE INTERVAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        ageIntervals.forEach { interval ->
                            FilterChip(
                                selected = ageInterval == interval,
                                onClick = { ageInterval = interval },
                                label = { Text(interval, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 2: Admission & Timings
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Admission & Arrival Timings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. ADMISSION DATE & TIME
                    OutlinedTextField(
                        value = admissionDateTime,
                        onValueChange = { admissionDateTime = it },
                        label = { Text("6. ADMISSION DATE & TIME *") },
                        trailingIcon = {
                            IconButton(onClick = { openDatePicker { admissionDateTime = it } }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date time")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_admission_datetime_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. PATIENT RECEIVED TIME
                    OutlinedTextField(
                        value = patientReceivedTime,
                        onValueChange = { patientReceivedTime = it },
                        label = { Text("7. PATIENT RECEIVED TIME *") },
                        trailingIcon = {
                            IconButton(onClick = { openTimePicker { patientReceivedTime = it } }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Pick received time")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_received_time_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 15. EMERGENCY RESPONSE TIME
                    Text(
                        text = "15. EMERGENCY RESPONSE TIME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        responseTimes.forEach { rt ->
                            FilterChip(
                                selected = emergencyResponseTime == rt,
                                onClick = { emergencyResponseTime = rt },
                                label = { Text(rt, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 3: Clinical Speciality & Diagnosis
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Clinical Classification & Diagnosis",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 8. BROAD SPECIALITY CATEGORY
                    Text(
                        text = "8. BROAD SPECIALITY CATEGORY *",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        broadSpecialities.forEach { bsc ->
                            FilterChip(
                                selected = broadSpeciality == bsc,
                                onClick = { broadSpeciality = bsc },
                                label = { Text(bsc, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) },
                                modifier = Modifier.testTag("speciality_chip_$bsc")
                            )
                        }
                    }

                    // 9. DIAGNOSIS
                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text("9. DIAGNOSIS *") },
                        placeholder = { Text("e.g. RTA, CHEST PAIN, POISONING...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_diagnosis_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quick Clinical Diagnosis Options:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        commonDiagnoses.forEach { d ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (diagnosis.equals(d, ignoreCase = true)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { diagnosis = d }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = d,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (diagnosis.equals(d, ignoreCase = true)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 4: Hospital Policy Categories & Transfer Out
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Hospital Policy & Transfer Tracking",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 11. TAEI/PILLAR/TAEI NON PILLAR
                    Text(
                        text = "11. TAEI/PILLAR/TAEI NON PILLAR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        pillarOptions.forEach { opt ->
                            FilterChip(
                                selected = pillarStatus == opt,
                                onClick = { pillarStatus = opt },
                                label = { Text(opt, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    // 12. MEDICOLEGAL CATEGORY
                    Text(
                        text = "12. MEDICOLEGAL CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        mlcOptions.forEach { mlc ->
                            FilterChip(
                                selected = medicolegalCategory == mlc,
                                onClick = { medicolegalCategory = mlc },
                                label = { Text(mlc, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                                colors = if (mlc == "MLC") FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                ) else FilterChipDefaults.filterChipColors()
                            )
                        }
                    }

                    // 13. TRANSFERRED OUT
                    OutlinedTextField(
                        value = transferredOut,
                        onValueChange = { transferredOut = it },
                        label = { Text("13. TRANSFERRED OUT (Destination)") },
                        placeholder = { Text("NO, ICU, WARD, HIGHER CENTER...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_transferred_out_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        transferDestinations.forEach { dest ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (transferredOut.equals(dest, ignoreCase = true)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        transferredOut = dest
                                        if (dest != "NO") {
                                            val t = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                            transferredOutTime = t
                                        } else {
                                            transferredOutTime = "N/A"
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = dest,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (transferredOut.equals(dest, ignoreCase = true)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 14. TRANSFERRED OUT TIME
                    OutlinedTextField(
                        value = transferredOutTime,
                        onValueChange = { transferredOutTime = it },
                        label = { Text("14. TRANSFERRED OUT TIME") },
                        trailingIcon = {
                            IconButton(onClick = { openTimePicker { transferredOutTime = it } }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Pick transfer time")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (validateForm()) {
                            showConfirmDialog = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(2f).height(52.dp).testTag("form_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT PATIENT RECORD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Mandatory Confirmation Dialog before final submission as per Section 6
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Verify Patient Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please verify the patient information before submitting to the hospital database:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• IP NO: $ipNumber", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("• Patient: $patientName (Age: $ageString, $sex)", style = MaterialTheme.typography.bodySmall)
                            Text("• Admission: $admissionDateTime", style = MaterialTheme.typography.bodySmall)
                            Text("• Speciality: $broadSpeciality", style = MaterialTheme.typography.bodySmall)
                            Text("• Diagnosis: $diagnosis", style = MaterialTheme.typography.bodySmall)
                            Text("• Category: $medicolegalCategory • $pillarStatus", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val newRecord = PatientRecordEntity(
                            serialNumber = 0, // Auto-computed in repository
                            ipNumber = ipNumber.trim(),
                            name = patientName.trim(),
                            age = ageString.toIntOrNull() ?: 0,
                            sex = sex,
                            admissionDateTime = admissionDateTime.trim(),
                            patientReceivedTime = patientReceivedTime.trim(),
                            broadSpecialityCategory = broadSpeciality,
                            diagnosis = diagnosis.trim(),
                            ageInterval = ageInterval,
                            pillarStatus = pillarStatus,
                            medicolegalCategory = medicolegalCategory,
                            transferredOut = transferredOut.trim(),
                            transferredOutTime = transferredOutTime.trim(),
                            emergencyResponseTime = emergencyResponseTime.trim(),
                            createdByNurseId = currentUser?.employeeId ?: "NURSE",
                            createdByNurseName = currentUser?.fullName ?: "Nurse",
                            createdAt = System.currentTimeMillis()
                        )
                        viewModel.saveNewRecord(newRecord) {
                            // On success: clear fields and return to Nurse Dashboard
                            ipNumber = ""
                            patientName = ""
                            ageString = ""
                            diagnosis = ""
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    Text("Confirm & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Review Form")
                }
            }
        )
    }
}
