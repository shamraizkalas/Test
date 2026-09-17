package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.StayPrimaryLandscape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMember
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.util.FamilyTreePdfGenerator
import com.example.util.PdfExportConfig
import com.example.util.PdfExportResult
import com.example.util.PdfOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExportPdfDialog(
    allMembers: List<FamilyMember>,
    currentSelectedBranchId: Long?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rootMember = remember(allMembers) {
        allMembers.find { it.fatherId == null } ?: allMembers.firstOrNull()
    }
    val mainBranches = remember(allMembers, rootMember) {
        if (rootMember != null) {
            allMembers.filter { it.fatherId == rootMember.id }.sortedBy { it.displayOrder }
        } else emptyList()
    }

    val selectedBranch = remember(currentSelectedBranchId, mainBranches) {
        mainBranches.find { it.id == currentSelectedBranchId }
    }

    // Export Options State
    var exportScopeAll by remember { mutableStateOf(currentSelectedBranchId == null) }
    var selectedBranchIdForExport by remember {
        mutableStateOf(currentSelectedBranchId ?: mainBranches.firstOrNull()?.id)
    }
    var orientation by remember { mutableStateOf(PdfOrientation.PORTRAIT) }
    var includeCoverPage by remember { mutableStateOf(true) }

    // Generation State
    var isGenerating by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savedToDownloadsNotice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isGenerating) onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDC2626).copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "پی ڈی ایف شجرہ برآمد کریں",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Export Family Tree to PDF",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isGenerating
                ) {
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
                if (exportResult == null) {
                    // =====================================
                    // CONFIGURATION VIEW
                    // =====================================
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "خاندانی شجرہ کا باضابطہ خوبصورت پی ڈی ایف دستاویز تیار کریں، جسے آپ محفوظ، پرنٹ یا واٹس ایپ پر شیئر کر سکتے ہیں۔",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldDark,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }

                    // 1. SCOPE SELECTION (تمام شاخیں یا مخصوص شاخ)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "۱. شجرہ کا دائرہ اختیار:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )

                        // Option A: Full Family Tree
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (exportScopeAll) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (exportScopeAll) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportScopeAll = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (exportScopeAll) EmeraldPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                        .then(
                                            if (!exportScopeAll) Modifier.background(Color.LightGray, CircleShape) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (exportScopeAll) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "مکمل خاندانی شجرہ (تمام ۴ شاخیں)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (exportScopeAll) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "کل ${allMembers.size} افراد، سرورق اور تمام شاخوں کے تفصیلی خاکے",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Option B: Single Selected Branch
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (!exportScopeAll) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (!exportScopeAll) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportScopeAll = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (!exportScopeAll) EmeraldPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                        .then(
                                            if (exportScopeAll) Modifier.background(Color.LightGray, CircleShape) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!exportScopeAll) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "صرف مخصوص شاخ برآمد کریں",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (!exportScopeAll) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    val branchLabel = selectedBranch?.let { "شاخ ${it.name}" } ?: "کوئی ایک شاخ"
                                    Text(
                                        text = "منتخب شاخ: $branchLabel",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // If single branch selected, show branch chips
                        if (!exportScopeAll) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mainBranches.forEach { b ->
                                    val isSelected = selectedBranchIdForExport == b.id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedBranchIdForExport = b.id }
                                    ) {
                                        Text(
                                            text = b.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. ORIENTATION SELECTION (پورٹریٹ یا لینڈ سکیپ)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "۲. صفحہ کا انداز (Page Orientation):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Portrait
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (orientation == PdfOrientation.PORTRAIT) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (orientation == PdfOrientation.PORTRAIT) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { orientation = PdfOrientation.PORTRAIT }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.StayCurrentPortrait,
                                        contentDescription = null,
                                        tint = if (orientation == PdfOrientation.PORTRAIT) EmeraldPrimary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "عمودی رخ (A4)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (orientation == PdfOrientation.PORTRAIT) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "دستاویزی کتابچہ",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Landscape
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (orientation == PdfOrientation.LANDSCAPE) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (orientation == PdfOrientation.LANDSCAPE) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { orientation = PdfOrientation.LANDSCAPE }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.StayPrimaryLandscape,
                                        contentDescription = null,
                                        tint = if (orientation == PdfOrientation.LANDSCAPE) EmeraldPrimary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "افقی رخ (A4)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (orientation == PdfOrientation.LANDSCAPE) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "وسیع خاکہ",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. COVER PAGE TOGGLE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سرورق شامل کریں (Include Title Cover)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "خاندانی تعارف، تاریخ، بنیادی ڈھانچہ اور شماریات پر مشتمل صفحہ اول",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Switch(
                            checked = includeCoverPage,
                            onCheckedChange = { includeCoverPage = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // GENERATE BUTTON
                    Button(
                        onClick = {
                            isGenerating = true
                            errorMessage = null
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val branchId = if (exportScopeAll) null else selectedBranchIdForExport
                                    val config = PdfExportConfig(
                                        selectedBranchId = branchId,
                                        orientation = orientation,
                                        includeCoverPage = includeCoverPage
                                    )
                                    val result = FamilyTreePdfGenerator.generatePdf(
                                        context = context,
                                        allMembers = allMembers,
                                        config = config
                                    )
                                    withContext(Dispatchers.Main) {
                                        exportResult = result
                                        isGenerating = false
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        errorMessage = "پی ڈی ایف بنانے میں خرابی: ${e.message}"
                                        isGenerating = false
                                    }
                                }
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "پی ڈی ایف تیار ہو رہا ہے...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "پی ڈی ایف تیار کریں (Generate PDF)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                } else {
                    // =====================================
                    // SUCCESS & EXPORT ACTIONS VIEW
                    // =====================================
                    val res = exportResult!!
                    val fileSizeKb = (res.fileSizeBytes / 1024).toInt()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "پی ڈی ایف کامیابی سے تیار ہو گیا ہے!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "کل صفحات: ${res.pageCount} • فائل کا حجم: $fileSizeKb KB",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // File info summary
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.file.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "شجرہ نسب کالس (خاندان محمد علی)",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // ACTION 1: OPEN PDF
                    Button(
                        onClick = {
                            try {
                                val intent = FamilyTreePdfGenerator.createViewIntent(context, res.file)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "پی ڈی ایف ویور ایپ دستیاب نہیں ہے", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پی ڈی ایف کھولیں (Open PDF)", fontWeight = FontWeight.Bold)
                    }

                    // ACTION 2: SHARE VIA WHATSAPP / OTHER APPS
                    Button(
                        onClick = {
                            try {
                                val intent = FamilyTreePdfGenerator.createShareIntent(context, res.file)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "شیئر کرنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("واٹس ایپ / دیگر پر شیئر کریں (Share PDF)", fontWeight = FontWeight.Bold)
                    }

                    // ACTION 3: SAVE TO DOWNLOADS
                    OutlinedButton(
                        onClick = {
                            val savedUri = FamilyTreePdfGenerator.saveToDownloads(context, res.file)
                            if (savedUri != null) {
                                savedToDownloadsNotice = true
                                Toast.makeText(context, "فائل ڈاؤن لوڈز فولڈر میں محفوظ ہو گئی ہے!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "فائل ڈاؤن لوڈز میں محفوظ نہیں ہو سکی", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (savedToDownloadsNotice) "✅ ڈاؤن لوڈز فولڈر میں محفوظ ہے" else "ڈاؤن لوڈز فولڈر میں محفوظ کریں (Save)",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (savedToDownloadsNotice) {
                        Text(
                            text = "فائل آپ کے فون کے 'Downloads/ShajraKalas' فولڈر میں محفوظ کر دی گئی ہے۔",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF059669), fontSize = 11.sp),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // BUTTON TO CREATE ANOTHER PDF
                    OutlinedButton(
                        onClick = {
                            exportResult = null
                            savedToDownloadsNotice = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("دوبارہ ترتیبات تبدیل کر کے بنائیں", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(if (exportResult != null) "مکمل (Done)" else "بند کریں")
            }
        }
    )
}
