package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.model.PatientRecordEntity
import com.example.util.ExcelExporter
import java.io.File

@Composable
fun ExcelExportDialog(
    totalRecordsCount: Int,
    selectedRecordsCount: Int,
    filteredRecordsCount: Int,
    onExport: (filename: String, exportOnlySelected: Boolean) -> Unit,
    exportedFile: File?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var filename by remember { mutableStateOf(ExcelExporter.getDefaultFilename()) }
    var exportScope by remember {
        mutableStateOf(if (selectedRecordsCount > 0) "SELECTED" else "ALL")
    }

    // Android SAF document creator contract
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null && exportedFile != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    exportedFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "Excel file saved successfully to your device!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().testTag("excel_export_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Export Records to Excel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (exportedFile != null) {
                    // Export Success State: Provide Save to Downloads & Share
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Excel File Generated Successfully!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "File: ${exportedFile.name} (${exportedFile.length() / 1024 + 1} KB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        saveFileLauncher.launch(exportedFile.name)
                                    },
                                    modifier = Modifier.weight(1f).testTag("save_to_downloads_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save to Device", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = {
                                        shareExcelFile(context, exportedFile)
                                    },
                                    modifier = Modifier.weight(1f).testTag("share_excel_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share / Open", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "The generated Excel (.xlsx) file preserves the exact 15-column hospital structure and is compatible with Microsoft Excel and Google Sheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filename configuration
                    OutlinedTextField(
                        value = filename,
                        onValueChange = { filename = it },
                        label = { Text("Export Filename (.xlsx)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("export_filename_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Records Scope to Export:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scope options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = exportScope == "ALL",
                            onClick = { exportScope = "ALL" }
                        )
                        Text(
                            text = "All Available Records ($totalRecordsCount patients)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (selectedRecordsCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = exportScope == "SELECTED",
                                onClick = { exportScope = "SELECTED" }
                            )
                            Text(
                                text = "Selected Records Only ($selectedRecordsCount patients)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (filteredRecordsCount != totalRecordsCount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = exportScope == "FILTERED",
                                onClick = { exportScope = "FILTERED" }
                            )
                            Text(
                                text = "Current Filtered Results ($filteredRecordsCount patients)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (exportedFile == null) {
                Button(
                    onClick = {
                        val isOnlySelected = exportScope == "SELECTED"
                        onExport(filename.trim(), isOnlySelected)
                    },
                    modifier = Modifier.testTag("generate_excel_submit_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GENERATE .XLSX")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (exportedFile == null) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun shareExcelFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Hospital Patient Emergency Records Export")
            putExtra(Intent.EXTRA_TEXT, "Exported Hospital Patient Records (${file.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share or Open Excel Sheet"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open share dialog: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
