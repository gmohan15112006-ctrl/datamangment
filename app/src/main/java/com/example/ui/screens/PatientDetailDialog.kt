package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientDetailDialog(
    record: PatientRecordEntity,
    canEdit: Boolean,
    canDelete: Boolean,
    canDuplicate: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onDismiss: () -> Unit
) {
    val createdDateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.createdAt))
    val modifiedDateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.lastModifiedAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("patient_detail_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "S NO #${record.serialNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Patient Record Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
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
                // Hospital 15 Columns Group
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CLINICAL EXCEL RECORD FIELDS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "1. S NO", value = record.serialNumber.toString())
                        DetailRow(label = "2. IP NO", value = record.ipNumber)
                        DetailRow(label = "3. NAME", value = record.name)
                        DetailRow(label = "4. AGE", value = "${record.age} Years")
                        DetailRow(label = "5. SEX", value = record.sex)
                        DetailRow(label = "6. ADMISSION DATE & TIME", value = record.admissionDateTime)
                        DetailRow(label = "7. PATIENT RECEIVED TIME", value = record.patientReceivedTime)
                        DetailRow(label = "8. BROAD SPECIALITY CATEGORY", value = record.broadSpecialityCategory)
                        DetailRow(label = "9. DIAGNOSIS", value = record.diagnosis)
                        DetailRow(label = "10. AGE INTERVAL", value = record.ageInterval)
                        DetailRow(label = "11. TAEI/PILLAR/TAEI NON PILLAR", value = record.pillarStatus)
                        DetailRow(label = "12. MEDICOLEGAL CATEGORY", value = record.medicolegalCategory)
                        DetailRow(label = "13. TRANSFERRED OUT", value = record.transferredOut)
                        DetailRow(label = "14. TRANSFERRED OUT TIME", value = record.transferredOutTime)
                        DetailRow(label = "15. EMERGENCY RESPONSE TIME", value = record.emergencyResponseTime)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Internal Audit & Tracking Fields Group
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INTERNAL AUDIT & SYSTEM METADATA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "Record Database ID", value = "#${record.id}")
                        DetailRow(label = "Entered By Nurse", value = "${record.createdByNurseName} (${record.createdByNurseId})")
                        DetailRow(label = "Created Date & Time", value = createdDateFormatted)
                        DetailRow(label = "Last Modified By", value = if (record.lastModifiedBy.isNotBlank()) record.lastModifiedBy else "Original Entry")
                        DetailRow(label = "Last Modified Date/Time", value = modifiedDateFormatted)
                        if (record.isDuplicated) {
                            DetailRow(label = "Duplication Status", value = "Duplicated from Record #${record.originalRecordId}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canDuplicate) {
                    OutlinedButton(
                        onClick = onDuplicate,
                        modifier = Modifier.testTag("detail_dialog_duplicate_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate")
                    }
                }
                if (canEdit) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.testTag("detail_dialog_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Record")
                    }
                }
            }
        },
        dismissButton = {
            if (canDelete) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("detail_dialog_delete_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}
