package com.example.ottracker.ui.tabs

import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ottracker.data.AppSettings
import com.example.ottracker.theme.DangerRed
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.components.NeoButton
import com.example.ottracker.ui.components.NeoCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SettingsTab(
    settings: AppSettings,
    recordCount: Int = 0,
    onSaveSettings: (AppSettings) -> Unit,
    onClearAllData: () -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String, Boolean) -> Result<Int>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var baseRateText by remember(settings.baseHourlyRate) { mutableStateOf(settings.baseHourlyRate.toString()) }
    var startHour by remember(settings.defaultStartHour) { mutableIntStateOf(settings.defaultStartHour) }
    var startMinute by remember(settings.defaultStartMinute) { mutableIntStateOf(settings.defaultStartMinute) }
    var showClearDialog by remember { mutableStateOf(false) }
    var confirmInputText by remember { mutableStateOf("") }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    val startCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
    }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    if (showClearDialog) {
        val isConfirmed = confirmInputText.trim().equals("DELETE", ignoreCase = true)
        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
                confirmInputText = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Clear All Records?",
                    fontWeight = FontWeight.Black,
                    color = onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "This will permanently erase all your logged overtime history and earnings. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "To confirm, please type DELETE below:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmInputText,
                        onValueChange = { confirmInputText = it },
                        placeholder = { Text("Type DELETE", color = onSurfaceVariant) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = onSurface,
                            unfocusedBorderColor = outline,
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            cursorColor = onSurface
                        )
                    )
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                        confirmInputText = ""
                    },
                    enabled = isConfirmed,
                    containerColor = DangerRed,
                    contentColor = Color.White,
                    borderColor = Color.Black,
                    shadowColor = Color.Black,
                    shadowOffset = 2.5.dp
                ) {
                    Text("Delete All Records", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        confirmInputText = ""
                    }
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        color = onSurface
                    )
                }
            }
        )
    }

    if (pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Restore Overtime Records",
                    fontWeight = FontWeight.Black,
                    color = onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Backup file detected. How would you like to restore your overtime records?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Merge: Keeps your existing entries and adds/updates records from the backup file.\n• Replace All: Erases current data and replaces everything with the backup file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoButton(
                        onClick = {
                            val json = pendingImportJson ?: return@NeoButton
                            pendingImportJson = null
                            val res = onImportBackup(json, false)
                            res.onSuccess { count ->
                                Toast.makeText(context, "Successfully merged $count records & settings!", Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Restore failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        containerColor = GoldAccent,
                        contentColor = Color.Black,
                        borderColor = Color.Black,
                        shadowColor = Color.Black,
                        shadowOffset = 2.5.dp
                    ) {
                        Text("Merge", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    NeoButton(
                        onClick = {
                            val json = pendingImportJson ?: return@NeoButton
                            pendingImportJson = null
                            val res = onImportBackup(json, true)
                            res.onSuccess { count ->
                                Toast.makeText(context, "Successfully replaced with $count records!", Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Restore failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        containerColor = DangerRed,
                        contentColor = Color.White,
                        borderColor = Color.Black,
                        shadowColor = Color.Black,
                        shadowOffset = 2.5.dp
                    ) {
                        Text("Replace All", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold, color = onSurface)
                }
            }
        )
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val json = onExportBackup()
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Backup file saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.use { stream ->
                    stream.bufferedReader().readText()
                }
                if (content.isNullOrBlank()) {
                    Toast.makeText(context, "Selected backup file is empty", Toast.LENGTH_SHORT).show()
                    return@let
                }
                if (recordCount > 0) {
                    pendingImportJson = content
                } else {
                    val res = onImportBackup(content, false)
                    res.onSuccess { count ->
                        Toast.makeText(context, "Successfully restored $count records & settings!", Toast.LENGTH_LONG).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Restore failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = onSurface,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Warehouse OT Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Text(
                        text = "Configure your overtime rate and shift schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }
        }

        // 1. Wage & Shift Settings
        item {
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Overtime Rate & Shift",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )

                    OutlinedTextField(
                        value = baseRateText,
                        onValueChange = {
                            baseRateText = it
                            val rate = it.toDoubleOrNull()
                            if (rate != null && rate > 0) {
                                onSaveSettings(settings.copy(baseHourlyRate = rate))
                            }
                        },
                        label = { Text("Overtime Hourly Rate (${settings.currencySymbol}/hr)", color = onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = onSurface) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = onSurface,
                            unfocusedBorderColor = outline,
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            focusedLabelColor = onSurface,
                            cursorColor = onSurface
                        )
                    )

                    // Default OT Start Time (Regular shift ends)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, outline, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h
                                        startMinute = m
                                        onSaveSettings(
                                            settings.copy(
                                                defaultStartHour = h,
                                                defaultStartMinute = m
                                            )
                                        )
                                    },
                                    startHour,
                                    startMinute,
                                    false
                                ).show()
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = onSurface)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Regular Shift End / OT Start Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant
                                )
                                Text(
                                    timeFormat.format(startCal.time),
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface
                                )
                            }
                        }
                        Text(
                            "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Cutoff Schedule Explanation Card
        item {
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payroll Cutoff Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("1st – 15th of the month", modifier = Modifier.weight(1f, fill = false), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paid on the 20th", style = MaterialTheme.typography.bodyMedium, color = onSurface, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("16th – End of month", modifier = Modifier.weight(1f, fill = false), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paid on the 5th (next mo.)", style = MaterialTheme.typography.bodyMedium, color = onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Privacy & Offline Local Storage
        item {
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                        Text(
                            text = "All hours and calculations are stored safely on your phone. No internet or login required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Backup & Restore Data
        item {
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup & Restore Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    Text(
                        text = "Save your overtime records to a file or share them to Google Drive so you never lose your data, even if you reinstall the app or change phones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved in app:",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariant
                        )
                        Text(
                            text = "$recordCount record${if (recordCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeoButton(
                            onClick = {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                createDocumentLauncher.launch("ot_backup_$timeStamp.json")
                            },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = onSurface,
                            borderColor = onSurface,
                            shadowColor = onSurface,
                            shadowOffset = 2.5.dp
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = onSurface, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save File", fontWeight = FontWeight.Bold, color = onSurface, style = MaterialTheme.typography.labelMedium)
                        }

                        NeoButton(
                            onClick = {
                                try {
                                    val json = onExportBackup()
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "OT Tracker Backup")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share or Save Backup"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error sharing: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = onSurface,
                            borderColor = onSurface,
                            shadowColor = onSurface,
                            shadowOffset = 2.5.dp
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = onSurface, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Drive", fontWeight = FontWeight.Bold, color = onSurface, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    NeoButton(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = GoldAccent,
                        contentColor = Color.Black,
                        borderColor = Color.Black,
                        shadowColor = Color.Black,
                        shadowOffset = 2.5.dp
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from Backup File", fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }
            }
        }

        // 5. Reset & Clear Data
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DangerRed,
                shadowColor = DangerRed
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Delete your recorded overtime history if needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    NeoButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = DangerRed,
                        contentColor = Color.White,
                        borderColor = Color.Black,
                        shadowColor = Color.Black,
                        shadowOffset = 3.dp
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Overtime Records", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
