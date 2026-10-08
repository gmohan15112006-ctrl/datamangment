package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PatientRecordEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPatientDialog(
    record: PatientRecordEntity,
    onSave: (PatientRecordEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var ipNumber by remember { mutableStateOf(record.ipNumber) }
    var patientName by remember { mutableStateOf(record.name) }
    var ageString by remember { mutableStateOf(record.age.toString()) }
    var sex by remember { mutableStateOf(record.sex) }
    var admissionDateTime by remember { mutableStateOf(record.admissionDateTime) }
    var patientReceivedTime by remember { mutableStateOf(record.patientReceivedTime) }
    var broadSpeciality by remember { mutableStateOf(record.broadSpecialityCategory) }
    var diagnosis by remember { mutableStateOf(record.diagnosis) }
    var ageInterval by remember { mutableStateOf(record.ageInterval) }
    var pillarStatus by remember { mutableStateOf(record.pillarStatus) }
    var medicolegalCategory by remember { mutableStateOf(record.medicolegalCategory) }
    var transferredOut by remember { mutableStateOf(record.transferredOut) }
    var transferredOutTime by remember { mutableStateOf(record.transferredOutTime) }
    var emergencyResponseTime by remember { mutableStateOf(record.emergencyResponseTime) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val specialities = listOf("SURGERY CASES", "MEDICINE CASES")
    val mlcOptions = listOf("NMLC", "MLC")
    val pillarOptions = listOf("PILLAR", "NON PILLAR")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().testTag("edit_patient_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Patient Record (S NO #${record.serialNumber})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = validationError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = ipNumber,
                    onValueChange = { ipNumber = it },
                    label = { Text("IP NO") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_ip_number_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Patient Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_patient_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageString,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) ageString = it
                        },
                        label = { Text("Age *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Sex", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("M", "F", "Other").forEach { s ->
                                FilterChip(
                                    selected = sex == s,
                                    onClick = { sex = s },
                                    label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = admissionDateTime,
                    onValueChange = { admissionDateTime = it },
                    label = { Text("Admission Date & Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = patientReceivedTime,
                    onValueChange = { patientReceivedTime = it },
                    label = { Text("Patient Received Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Broad Speciality Category", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    specialities.forEach { sp ->
                        FilterChip(
                            selected = broadSpeciality == sp,
                            onClick = { broadSpeciality = sp },
                            label = { Text(sp, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosis *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Medico-Legal", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            mlcOptions.forEach { m ->
                                FilterChip(
                                    selected = medicolegalCategory == m,
                                    onClick = { medicolegalCategory = m },
                                    label = { Text(m, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Pillar Status", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            pillarOptions.forEach { p ->
                                FilterChip(
                                    selected = pillarStatus == p,
                                    onClick = { pillarStatus = p },
                                    label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = transferredOut,
                    onValueChange = { transferredOut = it },
                    label = { Text("Transferred Out (Destination)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = transferredOutTime,
                    onValueChange = { transferredOutTime = it },
                    label = { Text("Transferred Out Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emergencyResponseTime,
                    onValueChange = { emergencyResponseTime = it },
                    label = { Text("Emergency Response Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageString.toIntOrNull()
                    if (patientName.isBlank()) {
                        validationError = "Patient Name cannot be empty."
                        return@Button
                    }
                    if (ipNumber.isBlank()) {
                        validationError = "IP Number cannot be empty."
                        return@Button
                    }
                    if (age == null || age < 0 || age > 130) {
                        validationError = "Age must be valid."
                        return@Button
                    }
                    if (diagnosis.isBlank()) {
                        validationError = "Diagnosis cannot be empty."
                        return@Button
                    }

                    val updated = record.copy(
                        ipNumber = ipNumber.trim(),
                        name = patientName.trim(),
                        age = age,
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
                        emergencyResponseTime = emergencyResponseTime.trim()
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_edit_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
