package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMember
import com.example.data.Gender
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark

@Composable
fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ایڈمن پاس ورڈ درج کریں", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ایپ کو اپڈیٹ کرنے کے لیے ایڈمن پن کوڈ داخل کریں:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        hasError = false
                    },
                    label = { Text("ایڈمن پاس ورڈ") },
                    placeholder = { Text("پاس ورڈ درج کریں") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (onVerify(pinText)) {
                            onDismiss()
                        } else {
                            hasError = true
                        }
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (hasError) {
                    Text(
                        text = "غلط پاس ورڈ! براہ کرم درست پاس ورڈ درج کریں",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (onVerify(pinText)) {
                        onDismiss()
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("تصدیق کریں")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberDialog(
    initialMember: FamilyMember? = null,
    initialParentId: Long? = null,
    allMembers: List<FamilyMember>,
    isAdmin: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        fatherId: Long?,
        gender: Gender,
        spouse: String?,
        location: String?,
        isDeceased: Boolean,
        deathNote: String?,
        notes: String?,
        phone: String?,
        occupation: String?,
        birthYear: String?,
        deathYear: String?
    ) -> Unit
) {
    val isEditing = initialMember != null
    var name by remember { mutableStateOf(initialMember?.name ?: "") }
    var selectedFatherId by remember {
        mutableStateOf(initialMember?.fatherId ?: initialParentId)
    }
    var gender by remember { mutableStateOf(initialMember?.gender ?: Gender.MALE) }
    var spouse by remember { mutableStateOf(initialMember?.spouse ?: "") }
    var location by remember { mutableStateOf(initialMember?.location ?: "") }
    var isDeceased by remember { mutableStateOf(initialMember?.isDeceased ?: false) }
    var deathNote by remember { mutableStateOf(initialMember?.deathNote ?: "") }
    var notes by remember { mutableStateOf(initialMember?.notes ?: "") }
    var phone by remember { mutableStateOf(initialMember?.phone ?: "") }
    var occupation by remember { mutableStateOf(initialMember?.occupation ?: "") }
    var birthYear by remember { mutableStateOf(initialMember?.birthYear ?: "") }
    var deathYear by remember { mutableStateOf(initialMember?.deathYear ?: "") }

    var fatherDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // List of candidates for father: only males, excluding self and own descendants if editing
    val fatherCandidates = remember(allMembers, initialMember) {
        allMembers.filter { it.isMale && (initialMember == null || it.id != initialMember.id) }
    }
    val selectedFather = allMembers.find { it.id == selectedFatherId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when {
                        isEditing -> "معلومات تبدیل کریں: ${initialMember?.name}"
                        isAdmin -> "نیا فرد شامل کریں"
                        else -> "نئے فرد / اولاد کے اندراج کی درخواست"
                    },
                    fontWeight = FontWeight.Bold
                )
                if (!isAdmin && !isEditing) {
                    Text(
                        text = "یہ اندراج ایڈمن کی منظوری کے بعد شجرہ میں شامل ہوگا",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD97706),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isAdmin && !isEditing) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نوٹ: آپ کے درج کردہ تمام کوائف پہلے ایڈمن کو تصدیق و منظوری کے لیے جائیں گے۔",
                                color = Color(0xFF92400E),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("نام *") },
                    placeholder = { Text("مثلاً: شمریز ایوب") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text("نام درج کرنا ضروری ہے", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                // Gender Selection (مرد / خاتون)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("جنس:", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(60.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { gender = Gender.MALE }
                    ) {
                        RadioButton(selected = gender == Gender.MALE, onClick = { gender = Gender.MALE })
                        Text("بیٹا / مرد")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { gender = Gender.FEMALE }
                    ) {
                        RadioButton(selected = gender == Gender.FEMALE, onClick = { gender = Gender.FEMALE })
                        Text("بیٹی / خاتون")
                    }
                }

                // Father Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = fatherDropdownExpanded,
                    onExpandedChange = { fatherDropdownExpanded = !fatherDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFather?.let { "${it.name} (نسل ${it.generation})" } ?: "کوئی والد منتخب نہیں (بانی)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("والد محترم") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fatherDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fatherDropdownExpanded,
                        onDismissRequest = { fatherDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("کوئی والد نہیں (سب سے اوپر بانی)") },
                            onClick = {
                                selectedFatherId = null
                                fatherDropdownExpanded = false
                            }
                        )
                        fatherCandidates.forEach { candidate ->
                            DropdownMenuItem(
                                text = { Text("${candidate.name} (ولد ${candidate.fatherName ?: "-"}) - نسل ${candidate.generation}") },
                                onClick = {
                                    selectedFatherId = candidate.id
                                    fatherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Spouse Input
                OutlinedTextField(
                    value = spouse,
                    onValueChange = { spouse = it },
                    label = { Text("زوجہ / شوہر کی تفصیل") },
                    placeholder = { Text("مثلاً: زوجہ محمد حسین سرگودھا") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Location / City
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("شہر / گاؤں / علاقہ") },
                    placeholder = { Text("مثلاً: سرگودھا، پھالیہ، سدوال") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone / WhatsApp
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("فون نمبر / واٹس ایپ (اختیاری)") },
                    placeholder = { Text("مثلاً: 03001234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Occupation
                OutlinedTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    label = { Text("پیشہ / ملازمت / کاروبار (اختیاری)") },
                    placeholder = { Text("مثلاً: زراعت، استاد، تاجر، انجینئر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Birth & Death Year Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = birthYear,
                        onValueChange = { birthYear = it },
                        label = { Text("سن پیدائش") },
                        placeholder = { Text("مثلاً: 1985") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deathYear,
                        onValueChange = { deathYear = it },
                        label = { Text("سن وفات") },
                        placeholder = { Text("مثلاً: 2020") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Deceased Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDeceased = !isDeceased },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isDeceased, onCheckedChange = { isDeceased = it })
                    Text("وفات پا چکے ہیں (مرحوم)")
                }

                if (isDeceased) {
                    OutlinedTextField(
                        value = deathNote,
                        onValueChange = { deathNote = it },
                        label = { Text("وفات نوٹ (اختیاری)") },
                        placeholder = { Text("مثلاً: کم عمر وفات / عمر 2.5 سال") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Additional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("اضافی معلومات یا نوٹ") },
                    placeholder = { Text("کوئی خاص یادداشت، رابطہ وغیرہ") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(
                            name,
                            selectedFatherId,
                            gender,
                            spouse,
                            location,
                            isDeceased,
                            deathNote,
                            notes,
                            phone,
                            occupation,
                            birthYear,
                            deathYear
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(
                    when {
                        isEditing -> "تبدیلی محفوظ کریں"
                        isAdmin -> "محفوظ کریں (شجرہ میں شامل کریں)"
                        else -> "محفوظ کریں (درخواست جمع کرائیں)"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    member: FamilyMember,
    hasChildren: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        },
        title = {
            Text("کیا آپ حذف کرنا چاہتے ہیں؟", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("کیا آپ واقعی '${member.name}' کو شجرہ نسب سے خارج کرنا چاہتے ہیں؟")
                if (hasChildren) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تنبہیہ: اس فرد کی اولاد بھی شجرہ میں موجود ہے۔ حذف کرنے سے ان کا شجرہ زنجیر متاثر ہو سکتا ہے۔",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("ہاں، حذف کریں")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("نہیں")
            }
        }
    )
}

@Composable
fun ResetDatabaseConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
        },
        title = {
            Text("اصل ریکارڈ بحال کریں؟", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("کیا آپ تمام ترامیم ختم کر کے شجرہ کو دستاویز کی اصل حالت میں دوبارہ لوڈ کرنا چاہتے ہیں؟")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Text("ہاں، اصل ریکارڈ بحال کریں")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onChangePin: (oldPin: String, newPin: String) -> Boolean
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ایڈمن پاس ورڈ تبدیل کریں", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { oldPin = it },
                    label = { Text("پرانا پاس ورڈ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("نیا پاس ورڈ (کم از کم 4 حروف یا ہندسے)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("نئے پاس ورڈ کی تصدیق کریں") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin != confirmPin) {
                        errorMessage = "نیا پاس ورڈ اور تصدیق آپس میں نہیں ملتے"
                    } else if (newPin.length < 4) {
                        errorMessage = "نیا پاس ورڈ کم از کم 4 حروف پر مشتمل ہونا چاہیے"
                    } else {
                        val success = onChangePin(oldPin, newPin)
                        if (!success) {
                            errorMessage = "پرانا پاس ورڈ غلط ہے"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("محفوظ کریں")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

@Composable
fun AboutDedicationDialog(
    onDismiss: () -> Unit,
    onOpenSupport: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("پیغام، ٹیم و خاندانی تحفہ", fontWeight = FontWeight.Bold, color = EmeraldDark)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gift Box
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "« یہ ایپلیکیشن ہماری آنے والی نسل کے لیے ایک انمول تحفہ ہے »",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اس کا مقصد خاندانی رشتوں، تاریخ اور بزرگوں کے ناموں کو ہمیشہ کے لیے محفوظ رکھنا ہے۔",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }

                Text(
                    text = "یہ ٹیم ہے جنہوں نے یہ ایپلیکیشن بنائی ہے:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                )

                // Shamraiz Ayub Kalas Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("شمریز ایوب کالس", fontWeight = FontWeight.Bold, color = EmeraldDark, fontSize = 14.sp)
                            Text("تخلیق و ڈیولپمنٹ (یہ ایپلیکیشن شمریز ایوب کالس کی جانب سے بنائی گئی ہے)", fontSize = 11.5.sp, color = Color.Gray)
                        }
                    }
                }

                // Muhammad Shabbir Kalas Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Dataset, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("محمد شبیر کالس (سابق کونسلر)", fontWeight = FontWeight.Bold, color = EmeraldDark, fontSize = 14.sp)
                            Text("معاونت برائے شجرہ ڈیٹا و تصدیق (اس کے لیے ڈیٹا کا تعاون محمد شبیر کالس نے کیا)", fontSize = 11.5.sp, color = Color.Gray)
                        }
                    }
                }

                // Support & Contribution Banner
                if (onOpenSupport != null) {
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onOpenSupport()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ایپ کو مزید بہتر بنانے کے لیے تعاون",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9F1239),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "JazzCash / EasyPaisa کے ذریعے ہماری حوصلہ افزائی فرمائیں (یہاں کلک کریں)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBE123C)
                                )
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
                Text("ٹھیک ہے")
            }
        }
    )
}

@Composable
fun AppSupportContributionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val phoneNumber = "03007558489"
    val formattedPhone = "03007558489"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "آپ کا تعاون — ایپ کی ترقی کے لیے",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Banner
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💖 محترم صارفین! 💖",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBE123C),
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ہماری ایپ \"شجرہ نسب\" کو مزید بہترین، تیز اور خوبصورت بنانے کے لیے آپ کے تعاون کی ضرورت ہے۔",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF881337),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Message Description Card
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "اگر آپ چاہتے ہیں کہ یہ ایپ مزید نئے فیچرز، بہتر کارکردگی اور آسان استعمال کے ساتھ ترقی کرے، تو آپ ہماری حوصلہ افزائی فرما سکتے ہیں۔\n\nآپ کا ہر تعاون اس ایپ کی ترقی میں اہم کردار ادا کرے گا۔",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF374151),
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify
                            )
                        )
                    }
                }

                // Payment Details Card
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تعاون بھیجنے کی تفصیلات:",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 13.5.sp
                            )
                        }

                        // Method Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color(0xFFDC2626),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "JazzCash",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF16A34A),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "EasyPaisa",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Phone Number Box with Copy Action
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "موبائل نمبر:",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = formattedPhone,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = EmeraldDark
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(phoneNumber))
                                        Toast.makeText(context, "نمبر کاپی ہو گیا: $formattedPhone", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کاپی کریں",
                                        tint = EmeraldPrimary
                                    )
                                }
                            }
                        }

                        // Action Buttons: Copy & Call/Dial
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(phoneNumber))
                                    Toast.makeText(context, "نمبر کاپی ہو گیا: $formattedPhone", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نمبر کاپی کریں", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "ڈائلر کھولنے میں مسئلہ پیش آیا", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ڈائلر کھولیں", fontSize = 12.sp, color = EmeraldDark)
                            }
                        }
                    }
                }

                // Dua & Gratitude Footer
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🤲 جزاک اللہ خیر! 🤲",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "آپ کی دعائیں اور تعاون ہمارے لیے سب سے بڑا سرمایہ ہیں۔",
                            color = Color(0xFF78350F),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ٹھیک ہے")
            }
        }
    )
}

