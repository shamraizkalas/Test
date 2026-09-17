package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAccount
import com.example.ui.CloudSyncStatus
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary

@Composable
fun UserProfileDialog(
    user: UserAccount,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    syncStatus: CloudSyncStatus = CloudSyncStatus.SYNCED,
    isSyncing: Boolean = false,
    pendingApprovalsCount: Int = 0,
    onUpdateDisplayName: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onChangeUserPassword: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onViewLinkedMember: ((Long) -> Unit)? = null,
    onChangePassword: () -> Unit = {},
    onOpenBackup: () -> Unit,
    onOpenApprovals: (() -> Unit)? = null,
    onOpenMySubmissions: (() -> Unit)? = null,
    onSyncCloud: (() -> Unit)? = null,
    onOpenCloudSettings: (() -> Unit)? = null,
    onOpenUserManagement: (() -> Unit)? = null,
    onOpenSupport: (() -> Unit)? = null
) {
    // State for Display Name editing
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(user.fullName) { mutableStateOf(user.fullName) }
    var nameUpdateLoading by remember { mutableStateOf(false) }
    var nameMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // State for Password changing
    var isChangingPassword by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var passwordChangeLoading by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "صارف پروفائل (User Profile)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Avatar with User Initial
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (user.isAdmin) GoldPrimary else EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).ifBlank { "ص" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Full Name & Role Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else EmeraldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (user.isAdmin) GoldDark else EmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (user.isAdmin) Icons.Default.Security else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (user.isAdmin) Color.White else (if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (user.isAdmin) "ایڈمنسٹریٹر" else "فیملی ممبر",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isAdmin) Color.White else (if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark)
                                    )
                                )
                            }
                        }

                        // Approval Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                user.isAdmin || user.isApproved -> Color(0xFF16A34A).copy(alpha = 0.15f)
                                user.isPending -> Color(0xFFD97706).copy(alpha = 0.15f)
                                else -> Color(0xFFDC2626).copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        user.isAdmin || user.isApproved -> "منظور شدہ (Approved)"
                                        user.isPending -> "زیرِ التواء (Pending)"
                                        else -> "مسترد (Rejected)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            user.isAdmin || user.isApproved -> Color(0xFF15803D)
                                            user.isPending -> Color(0xFFB45309)
                                            else -> Color(0xFFB91C1C)
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                // 1. Current Approval Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            user.isAdmin -> GoldPrimary.copy(alpha = 0.12f)
                            user.isApproved -> Color(0xFF16A34A).copy(alpha = 0.12f)
                            user.isPending -> Color(0xFFD97706).copy(alpha = 0.12f)
                            else -> Color(0xFFDC2626).copy(alpha = 0.12f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            user.isAdmin -> GoldPrimary.copy(alpha = 0.4f)
                            user.isApproved -> Color(0xFF16A34A).copy(alpha = 0.4f)
                            user.isPending -> Color(0xFFD97706).copy(alpha = 0.4f)
                            else -> Color(0xFFDC2626).copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_approval_status_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    user.isAdmin -> Icons.Default.Security
                                    user.isApproved -> Icons.Default.CheckCircle
                                    user.isPending -> Icons.Default.HourglassTop
                                    else -> Icons.Default.Cancel
                                },
                                contentDescription = null,
                                tint = when {
                                    user.isAdmin -> GoldDark
                                    user.isApproved -> Color(0xFF15803D)
                                    user.isPending -> Color(0xFFB45309)
                                    else -> Color(0xFFB91C1C)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    user.isAdmin -> "اکاؤنٹ کی حیثیت: چیف ایڈمنسٹریٹر"
                                    user.isApproved -> "اکاؤنٹ کی حیثیت: منظور شدہ (Approved)"
                                    user.isPending -> "اکاؤنٹ کی حیثیت: زیرِ التواء (Pending Approval)"
                                    else -> "اکاؤنٹ کی حیثیت: مسترد شدہ (Rejected)"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = when {
                                user.isAdmin -> "آپ کو تمام افراد کی منظوری، ڈیٹا بیس بیک اپ، کلاؤڈ سنک اور شجرہ میں ہر قسم کی تبدیلیوں کے مکمل اختیارات حاصل ہیں۔"
                                user.isApproved -> "آپ کا اکاؤنٹ منظور ہو چکا ہے۔ آپ خاندانی شجرہ میں نیا اندراج اور ترامیم جمع کروا سکتے ہیں۔"
                                user.isPending -> "آپ کا اکاؤنٹ ایڈمن کی منظوری کے انتظار میں ہے۔ منظوری ملتے ہی آپ نئے اندراجات کر سکیں گے۔"
                                else -> "آپ کا اکاؤنٹ فی الوقت مسترد ہے، برائے رابطہ ایڈمنسٹریٹر سے رابطہ کریں۔"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // 2. Account Information & Display Name Update Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_account_info_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ڈسپلے نام (Display Name):",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (!isEditingName) {
                                TextButton(
                                    onClick = { isEditingName = true },
                                    modifier = Modifier.testTag("edit_display_name_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تبدیل کریں", fontSize = 12.sp)
                                }
                            }
                        }

                        if (!isEditingName) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("نیا ڈسپلے نام درج کریں") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("display_name_input")
                                )

                                nameMessage?.let { (success, msg) ->
                                    Text(
                                        text = msg,
                                        color = if (success) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            isEditingName = false
                                            editedName = user.fullName
                                            nameMessage = null
                                        },
                                        enabled = !nameUpdateLoading
                                    ) {
                                        Text("منسوخ")
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (editedName.trim().length >= 2 && onUpdateDisplayName != null) {
                                                nameUpdateLoading = true
                                                nameMessage = null
                                                onUpdateDisplayName(editedName.trim()) { success, msg ->
                                                    nameUpdateLoading = false
                                                    nameMessage = Pair(success, msg)
                                                    if (success) {
                                                        isEditingName = false
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !nameUpdateLoading && editedName.trim().length >= 2,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier.testTag("save_display_name_button")
                                    ) {
                                        if (nameUpdateLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("نام محفوظ کریں", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "رابطہ / ای میل: ${user.emailOrPhone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (!user.linkedMemberName.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "شجرہ میں نام: ${user.linkedMemberName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // Shortcut to view linked member card in the family tree
                if (user.linkedMemberId != null && onViewLinkedMember != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onViewLinkedMember(user.linkedMemberId)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("شجرہ میں اپنا تفصیلی کارڈ دیکھیں", color = if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark)
                    }
                }

                // 3. Change Account Password Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_password_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اکاؤنٹ پاس ورڈ:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            TextButton(
                                onClick = {
                                    isChangingPassword = !isChangingPassword
                                    passwordMessage = null
                                },
                                modifier = Modifier.testTag("toggle_change_password_button")
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isChangingPassword) "بند کریں" else "پاس ورڈ بدلیں", fontSize = 12.sp)
                            }
                        }

                        AnimatedVisibility(visible = isChangingPassword) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("نیا پاس ورڈ (کم از کم 6 حروف)") },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("new_password_input")
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("نئے پاس ورڈ کی تصدیق کریں") },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("confirm_password_input")
                                )

                                passwordMessage?.let { (success, msg) ->
                                    Text(
                                        text = msg,
                                        color = if (success) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            if (newPassword.length < 6) {
                                                passwordMessage = Pair(false, "پاس ورڈ کم از کم 6 حروف پر مشتمل ہونا چاہیے")
                                                return@Button
                                            }
                                            if (newPassword != confirmPassword) {
                                                passwordMessage = Pair(false, "پاس ورڈز مطابقت نہیں رکھتے")
                                                return@Button
                                            }
                                            if (onChangeUserPassword != null) {
                                                passwordChangeLoading = true
                                                passwordMessage = null
                                                onChangeUserPassword(newPassword) { success, msg ->
                                                    passwordChangeLoading = false
                                                    passwordMessage = Pair(success, msg)
                                                    if (success) {
                                                        newPassword = ""
                                                        confirmPassword = ""
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !passwordChangeLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("save_password_button")
                                    ) {
                                        if (passwordChangeLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("پاس ورڈ محفوظ کریں", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Accessibility Theme Toggle Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_theme_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDarkMode) GoldDark else EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ڈارک موڈ (Dark Theme)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "بہتر رسائی و واضح پڑھائی کے لیے",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            modifier = Modifier.testTag("profile_theme_switch")
                        )
                    }
                }

                // 5. Cloud Sync Status Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_sync_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (syncStatus) {
                                    CloudSyncStatus.SYNCING -> Icons.Default.CloudSync
                                    CloudSyncStatus.SYNCED -> Icons.Default.CloudDone
                                    CloudSyncStatus.OFFLINE -> Icons.Default.CloudOff
                                    CloudSyncStatus.ERROR -> Icons.Default.CloudOff
                                },
                                contentDescription = null,
                                tint = when (syncStatus) {
                                    CloudSyncStatus.SYNCING -> GoldDark
                                    CloudSyncStatus.SYNCED -> Color(0xFF15803D)
                                    CloudSyncStatus.OFFLINE -> Color(0xFFD97706)
                                    CloudSyncStatus.ERROR -> Color(0xFFDC2626)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (syncStatus) {
                                    CloudSyncStatus.SYNCING -> "کلاؤڈ سنک اسٹیٹس: ہم آہنگ ہو رہا ہے..."
                                    CloudSyncStatus.SYNCED -> "کلاؤڈ سنک اسٹیٹس: محفوظ و ہم آہنگ (Synced)"
                                    CloudSyncStatus.OFFLINE -> "کلاؤڈ سنک اسٹیٹس: آف لائن کیشے فعال"
                                    CloudSyncStatus.ERROR -> "کلاؤڈ سنک اسٹیٹس: کنکشن کی خرابی"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        if (onSyncCloud != null) {
                            OutlinedButton(
                                onClick = { onSyncCloud() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("profile_sync_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSyncing) "آن لائن ڈیٹا ہم آہنگ ہو رہا ہے..." else "کلاؤڈ ڈیٹا ہم آہنگ کریں (Sync Now)",
                                    color = if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                // Requests & Approvals Shortcuts
                if (user.isAdmin && onOpenApprovals != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenApprovals()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("profile_approvals_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (pendingApprovalsCount > 0) {
                                    Badge(containerColor = Color(0xFFDC2626)) {
                                        Text("$pendingApprovalsCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pendingApprovalsCount > 0) "نئے اندراج کی درخواستیں منظور کریں ($pendingApprovalsCount)" else "نئے اندراج و ترامیم کی منظوری",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (onOpenMySubmissions != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenMySubmissions()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("profile_my_submissions_button")
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("میری جمع کروائی گئی درخواستیں و حیثیت", color = if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Admin Controls Shortcuts
                if (user.isAdmin) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ایڈمن اختیارات:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldDark)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onChangePassword,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ایڈمن پن کوڈ", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onOpenBackup,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ڈیٹا بیک اپ", fontSize = 12.sp)
                            }
                        }
                        if (user.isSuperAdmin && onOpenUserManagement != null) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenUserManagement()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("profile_manage_users_button")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صارفین کا انتظام و ایڈمن کی تقرری (سپر ایڈمن)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (onOpenCloudSettings != null) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenCloudSettings()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("آن لائن کلاؤڈ و فائر بیس سیٹنگز (Cloud Config)", color = if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                // Dedication & Team Banner
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "« یہ ایپلیکیشن ہماری آنے والی نسل کے لیے ایک تحفہ ہے »",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF5DD99F) else EmeraldDark,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تخلیق: شمریز ایوب کالس • معاونت ڈیٹا: محمد شبیر کالس (سابق کونسلر)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // App Support & Donation Button
                if (onOpenSupport != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenSupport()
                        },
                        border = BorderStroke(1.dp, Color(0xFFE11D48)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFF1F2).copy(alpha = if (isDarkMode) 0.15f else 1f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_support_app_button")
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💖 آپ کا تعاون — ایپ کو مزید بہتر بنانے کے لیے",
                            color = if (isDarkMode) Color(0xFFFF85A2) else Color(0xFF9F1239),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Logout Button
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اکاؤنٹ سے لاگ آؤٹ کریں", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("profile_dismiss_button")) {
                Text("بند کریں")
            }
        }
    )
}
