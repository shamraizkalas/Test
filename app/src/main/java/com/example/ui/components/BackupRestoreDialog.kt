package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun BackupRestoreDialog(
    totalMembers: Int,
    onExportJson: () -> String,
    onExportReadableTree: () -> String,
    onImportJson: (String, (Boolean, String) -> Unit) -> Unit,
    onOpenExportPdf: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import, 2: Text Tree
    var importJsonText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ڈیٹا بیک اپ اور بحالی (Backup & Restore)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 18.sp
                    )
                )
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
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; statusMessage = null },
                        text = { Text("بیک اپ (Export)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; statusMessage = null },
                        text = { Text("بحالی (Import)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; statusMessage = null },
                        text = { Text("متنی شجرہ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedTab) {
                    // TAB 0: EXPORT
                    0 -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "کل محفوظ افراد: $totalMembers",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "تمام خاندانی شجرہ کا ڈیٹا JSON فارمیٹ میں محفوظ یا دوسرے فون میں منتقل کرنے کے لیے ایکسپورٹ کریں۔",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val json = onExportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                copiedNotice = true
                                statusMessage = "پورا ڈیٹا کلپ بورڈ پر کاپی ہو گیا ہے!"
                                isError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON ڈیٹا کاپی کریں (Copy JSON)")
                        }

                        OutlinedButton(
                            onClick = {
                                val json = onExportJson()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "شجرہ بیک اپ ڈیٹا شیئر کریں")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بیک اپ فائل واٹس ایپ / ای میل کریں")
                        }

                        if (onOpenExportPdf != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFDC2626).copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clickable { onOpenExportPdf() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "پی ڈی ایف تصویری شجرہ (PDF Document)",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "خاندانی شجرہ کا باضابطہ تصویری و پرنٹ دستاویز برآمد کریں",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: IMPORT
                    1 -> {
                        Text(
                            text = "اگر آپ کے پاس پہلے سے ایکسپورٹ شدہ بیک اپ JSON ڈیٹا موجود ہے تو نیچے چسپاں (Paste) کریں اور 'بحال کریں' پر کلک کریں:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            label = { Text("بیک اپ JSON کوڈ یہاں چسپاں کریں") },
                            placeholder = { Text("{\n  \"shajra_name\": ...\n}") },
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )

                        Button(
                            onClick = {
                                if (importJsonText.isBlank()) {
                                    statusMessage = "برائے مہربانی پہلے بیک اپ کوڈ درج کریں"
                                    isError = true
                                    return@Button
                                }
                                onImportJson(importJsonText) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) {
                                        importJsonText = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بیک اپ سے ڈیٹا بحال کریں (Restore)")
                        }
                    }

                    // TAB 2: TEXT TREE
                    2 -> {
                        Text(
                            text = "پورا خاندانی شجرہ ایک خوبصورت ٹیکسٹ خاکہ کی شکل میں جسے آپ پڑھ سکتے ہیں یا واٹس ایپ پر خاندان کے ساتھ شیئر کر سکتے ہیں:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val textTree = onExportReadableTree()
                                clipboardManager.setText(AnnotatedString(textTree))
                                statusMessage = "متنی شجرہ کلپ بورڈ پر کاپی ہو گیا!"
                                isError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("متنی شجرہ کاپی کریں (Copy)")
                        }

                        OutlinedButton(
                            onClick = {
                                val textTree = onExportReadableTree()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, textTree)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "متنی شجرہ شیئر کریں")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("متنی شجرہ واٹس ایپ پر شیئر کریں")
                        }
                    }
                }

                if (statusMessage != null) {
                    HorizontalDivider()
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFD1FAE5)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF065F46),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("بند کریں")
            }
        }
    )
}
