package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.RelationshipResult
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RelationshipCalculatorDialog(
    allMembers: List<FamilyMember>,
    personA: FamilyMember?,
    personB: FamilyMember?,
    result: RelationshipResult?,
    onSelectPersonA: (FamilyMember?) -> Unit,
    onSelectPersonB: (FamilyMember?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchA by remember { mutableStateOf("") }
    var searchB by remember { mutableStateOf("") }
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    val filteredListA = remember(allMembers, searchA) {
        if (searchA.isBlank()) allMembers.take(40)
        else allMembers.filter { it.name.contains(searchA.trim(), ignoreCase = true) || it.fatherName?.contains(searchA.trim(), ignoreCase = true) == true }
    }

    val filteredListB = remember(allMembers, searchB) {
        if (searchB.isBlank()) allMembers.take(40)
        else allMembers.filter { it.name.contains(searchB.trim(), ignoreCase = true) || it.fatherName?.contains(searchB.trim(), ignoreCase = true) == true }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "باہمی رشتہ تلاش کار (کیلکولیٹر)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 20.sp
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
                Text(
                    text = "خاندان کے کہی بھی دو افراد منتخب کریں تاکہ ان کے درمیان رشتہ اور مشترکہ بزرگ معلوم ہو سکیں:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector for Person A
                Column {
                    Text(
                        text = "پہلا فرد (Person 1):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedA,
                        onExpandedChange = { expandedA = !expandedA },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = personA?.let { "${it.name} (ولد ${it.fatherName ?: "-"})" } ?: "فرد منتخب کریں",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedA,
                            onDismissRequest = { expandedA = false },
                            modifier = Modifier.height(280.dp)
                        ) {
                            OutlinedTextField(
                                value = searchA,
                                onValueChange = { searchA = it },
                                placeholder = { Text("نام یا ولدیت سے تلاش کریں...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                            filteredListA.forEach { member ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${member.name} (ولد ${member.fatherName ?: "بانی"}) - نسل ${member.generation}")
                                    },
                                    onClick = {
                                        onSelectPersonA(member)
                                        expandedA = false
                                        searchA = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Swap Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            val tempA = personA
                            val tempB = personB
                            onSelectPersonA(tempB)
                            onSelectPersonB(tempA)
                        },
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "الٹ پلٹ کریں")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الٹ پلٹ کریں", fontSize = 12.sp)
                    }
                }

                // Selector for Person B
                Column {
                    Text(
                        text = "دوسرا فرد (Person 2):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedB,
                        onExpandedChange = { expandedB = !expandedB },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = personB?.let { "${it.name} (ولد ${it.fatherName ?: "-"})" } ?: "فرد منتخب کریں",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedB,
                            onDismissRequest = { expandedB = false },
                            modifier = Modifier.height(280.dp)
                        ) {
                            OutlinedTextField(
                                value = searchB,
                                onValueChange = { searchB = it },
                                placeholder = { Text("نام یا ولدیت سے تلاش کریں...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                            filteredListB.forEach { member ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${member.name} (ولد ${member.fatherName ?: "بانی"}) - نسل ${member.generation}")
                                    },
                                    onClick = {
                                        onSelectPersonB(member)
                                        expandedB = false
                                        searchB = ""
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Result Box
                if (result != null && personA != null && personB != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.5f))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Relationship title pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary
                                ) {
                                    Text(
                                        text = "رشتہ: ${result.relationAtoB}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (result.commonAncestor != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GoldPrimary
                                    ) {
                                        Text(
                                            text = "مشترکہ جد: ${result.commonAncestor.name}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Detail Explanation
                            Text(
                                text = result.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )

                            // Reverse Perspective
                            if (result.personA.id != result.personB.id) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "دوسری طرف سے: ${result.personB.name} کے لیے ${result.personA.name} ہیں: '${result.relationAtoB}'",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = "اور ${result.personA.name} کے لیے ${result.personB.name} ہیں: '${result.relationBtoA}'",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }

                            // Ancestral Path Trace
                            if (result.commonAncestor != null && result.personA.id != result.personB.id) {
                                Text(
                                    text = "شجرہ میں نسبی راستہ:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val pathAStr = result.pathA.reversed().joinToString(" ➔ ") { it.name }
                                    val pathBStr = result.pathB.reversed().joinToString(" ➔ ") { it.name }
                                    Text("نسب ${result.personA.name}: $pathAStr", fontSize = 12.sp)
                                    Text("نسب ${result.personB.name}: $pathBStr", fontSize = 12.sp)
                                }
                            }

                            // Share relationship result
                            Button(
                                onClick = {
                                    val shareText = buildString {
                                        append("📜 خاندانی شجرہ نسب - باہمی رشتہ رزلٹ\n")
                                        append("────────────────────\n")
                                        append("فرد ۱: ${result.personA.name} (ولد ${result.personA.fatherName ?: "-"})\n")
                                        append("فرد ۲: ${result.personB.name} (ولد ${result.personB.fatherName ?: "-"})\n")
                                        append("رشتہ: ${result.relationAtoB}\n")
                                        if (result.commonAncestor != null) {
                                            append("مشترکہ جد امجد: ${result.commonAncestor.name}\n")
                                        }
                                        append("تفصیل: ${result.description}\n")
                                        append("────────────────────\n")
                                        append("شجرہ نسب اولاد محمد علی")
                                    }
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "رشتہ رزلٹ واٹس ایپ پر شیئر کریں")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("یہ رشتہ واٹس ایپ پر شیئر کریں")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("مکمل")
            }
        }
    )
}
