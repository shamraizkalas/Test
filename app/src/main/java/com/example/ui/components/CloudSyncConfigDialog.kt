package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseConfigData
import com.example.data.FirebaseManager
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary

@Composable
fun CloudSyncConfigDialog(
    savedConfig: FirebaseConfigData?,
    isInitialized: Boolean,
    isAdmin: Boolean = false,
    onSaveManualConfig: (String, String, String, (Boolean, String) -> Unit) -> Unit,
    onSaveJsonConfig: (String, (Boolean, String) -> Unit) -> Unit,
    onUploadAllToCloud: ((Boolean, String) -> Unit) -> Unit,
    onDownloadAllFromCloud: ((Boolean, String) -> Unit) -> Unit,
    onOpenBackupDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Paste JSON, 1: Manual Entry

    var jsonText by remember { mutableStateOf("") }
    var projectId by remember { mutableStateOf(savedConfig?.projectId ?: "") }
    var apiKey by remember { mutableStateOf(savedConfig?.apiKey ?: "") }
    var appId by remember { mutableStateOf(savedConfig?.applicationId ?: "1:163641130981:android:shajranasab") }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showEditForm by remember { mutableStateOf(!isInitialized) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کلاؤڈ سنک و فائر بیس سیٹنگز",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 17.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بند کریں")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Banner
                if (isInitialized) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "فائر بیس کلاؤڈ آن لائن فعال ہے",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    fontSize = 14.sp
                                )
                            }
                            if (savedConfig != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "منسلک پروجیکٹ: ${savedConfig.projectId}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 12.sp)
                                )
                            }
                        }
                    }

                    // Direct Cloud Actions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "فوری آن لائن کلاؤڈ آپریشنز:",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 13.sp
                            )

                            // Upload Button
                            Button(
                                onClick = {
                                    isLoading = true
                                    statusMessage = "تمام مقامی شجرہ ڈیٹا فائر بیس کلاؤڈ پر اپلوڈ ہو رہا ہے..."
                                    isError = false
                                    onUploadAllToCloud { success, msg ->
                                        isLoading = false
                                        isError = !success
                                        statusMessage = msg
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_all_cloud_btn")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تمام ڈیٹا کلاؤڈ پر اپلوڈ کریں (Upload All)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Download Button
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    statusMessage = "کلاؤڈ سے ڈیٹا ڈاؤن لوڈ ہو رہا ہے..."
                                    isError = false
                                    onDownloadAllFromCloud { success, msg ->
                                        isLoading = false
                                        isError = !success
                                        statusMessage = msg
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_all_cloud_btn")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("کلاؤڈ سے ڈیٹا ڈاؤن لوڈ کریں (Download All)", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            TextButton(
                                onClick = { showEditForm = !showEditForm },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (showEditForm) "کنفیگریشن فارم چھپائیں" else "فائر بیس کنکشن تبدیل یا دوبارہ سیٹ کریں",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "فائر بیس آن لائن کلاؤڈ ابھی کنفیگر نہیں ہے",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "آپ کو کسی ٹیکنیکل سیٹ اپ کی ضرورت نہیں۔ آپ کا تمام ڈیٹا آپ کے موبائل میں 100% محفوظ ہے۔ اگر آپ کے پاس فائر بیس کونسول کا اکاؤنٹ ہے تو نیچے سیٹ کر سکتے ہیں، ورنہ نیچے دیا گیا 'مقامی بیک اپ' بلاجھجک استعمال کریں۔",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.5.sp)
                                )
                            }
                        }
                    }
                }

                // Configuration Form
                if (showEditForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "فائر بیس کنفیگریشن سیٹ کریں:",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )

                            TabRow(
                                selectedTabIndex = selectedTab,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("google-services.json چسپاں کریں", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("دستی اندراج (Manual)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }

                            if (selectedTab == 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "فائر بیس کونسول سے ڈاؤن لوڈ کردہ google-services.json فائل کا متن یہاں پیسٹ کریں:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.5.sp)
                                    )

                                    OutlinedTextField(
                                        value = jsonText,
                                        onValueChange = { jsonText = it },
                                        placeholder = { Text("{\"project_info\": {\"project_id\": \"...\"}}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        maxLines = 8,
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                val clip = clipboardManager.getText()
                                                if (clip != null) {
                                                    jsonText = clip.text
                                                }
                                            }) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = "پیسٹ کریں")
                                            }
                                        }
                                    )

                                    Button(
                                        onClick = {
                                            if (jsonText.isBlank()) {
                                                statusMessage = "براہ کرم google-services.json کا متن چسپاں کریں"
                                                isError = true
                                                return@Button
                                            }
                                            isLoading = true
                                            onSaveJsonConfig(jsonText) { success, msg ->
                                                isLoading = false
                                                isError = !success
                                                statusMessage = msg
                                                if (success) {
                                                    showEditForm = false
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("خودکار شناخت اور فائر بیس منسلک کریں", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = projectId,
                                        onValueChange = { projectId = it },
                                        label = { Text("فائر بیس Project ID *") },
                                        placeholder = { Text("مثلاً: shajra-nasab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = apiKey,
                                        onValueChange = { apiKey = it },
                                        label = { Text("فائر بیس Web API Key *") },
                                        placeholder = { Text("مثلاً: AIzaSy...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = appId,
                                        onValueChange = { appId = it },
                                        label = { Text("Mobile App ID (اختیاری)") },
                                        placeholder = { Text("مثلاً: 1:163641130981:android:...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (projectId.isBlank() || apiKey.isBlank()) {
                                                statusMessage = "Project ID اور API Key دونوں درج کرنا لازمی ہیں"
                                                isError = true
                                                return@Button
                                            }
                                            isLoading = true
                                            onSaveManualConfig(projectId, appId, apiKey) { success, msg ->
                                                isLoading = false
                                                isError = !success
                                                statusMessage = msg
                                                if (success) {
                                                    showEditForm = false
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("محفوظ کریں اور منسلک کریں", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading Indicator
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("عمل جاری ہے، براہ کرم انتظار کریں...", fontSize = 12.5.sp, color = EmeraldDark)
                    }
                }

                // Status Message display
                if (statusMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            color = if (isError) Color(0xFFC62828) else Color(0xFF2E7D32),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isAdmin) {
                    HorizontalDivider()

                    // Offline Local Backup Alternative Card (Admin only)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.04f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "متبادل: مکمل آف لائن بیک اپ (بغیر انٹرنیٹ)",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "اگر آپ کے پاس فائر بیس کلاؤڈ نہیں ہے تو آپ بغیر انٹرنیٹ کے بھی اپنے فون پر پورا شجرہ محفوظ (JSON Export) اور دوبارہ بحال کر سکتے ہیں۔",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.sp)
                            )
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenBackupDialog()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("مقامی ڈیٹا بیک اپ اور بحالی کھولیں", fontSize = 12.sp, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بند کریں", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
