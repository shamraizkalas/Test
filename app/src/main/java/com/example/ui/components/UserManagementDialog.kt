package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserAccount
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementDialog(
    users: List<UserAccount>,
    currentUser: UserAccount?,
    initialSearchQuery: String = "",
    onDismiss: () -> Unit,
    onToggleAdminRole: (UserAccount) -> Unit,
    onApproveUser: (UserAccount) -> Unit = {},
    onRejectUser: (UserAccount) -> Unit = {}
) {
    var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery) }
    var selectedFilterIndex by remember(initialSearchQuery) {
        mutableIntStateOf(if (initialSearchQuery.isNotBlank()) 1 else 0)
    } // 0: All, 1: Pending, 2: Admins
    var userToConfirmRoleChange by remember { mutableStateOf<UserAccount?>(null) }
    var userToConfirmAction by remember { mutableStateOf<Pair<UserAccount, String>?>(null) } // action: "approve" or "reject"

    val pendingUsers = remember(users) { users.filter { it.isPending } }
    val adminUsers = remember(users) { users.filter { it.isAdmin } }

    val filteredUsers = remember(users, searchQuery, selectedFilterIndex) {
        val baseList = when (selectedFilterIndex) {
            1 -> pendingUsers
            2 -> adminUsers
            else -> users
        }
        if (searchQuery.isBlank()) baseList
        else baseList.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.emailOrPhone.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("user_management_dialog_surface"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_user_management_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "واپس",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = GoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "صارفین کا انتظام و ایڈمن کی تقرری",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "صرف سپر ایڈمن کسی بھی صارف کو ایڈمنسٹریٹر بنا سکتا ہے",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Notification Focus Banner (if opened from notification)
                if (initialSearchQuery.isNotBlank() && searchQuery == initialSearchQuery) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نوٹیفکیشن: '$initialSearchQuery' کی منظوری کے لیے فلٹر فعال ہے",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilterIndex = 0
                                }
                            ) {
                                Text("تمام دیکھیں", fontSize = 11.sp, color = Color(0xFFB45309))
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("صارف تلاش کریں (نام یا فون/ای میل)") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "تلاش صاف کریں", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("user_search_input")
                )

                // Filter Tabs (All, Pending, Admins)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterIndex == 0,
                        onClick = { selectedFilterIndex = 0 },
                        label = { Text("سبھی (${users.size})") }
                    )
                    FilterChip(
                        selected = selectedFilterIndex == 1,
                        onClick = { selectedFilterIndex = 1 },
                        label = {
                            Text(
                                text = "منظوری کے منتظر (${pendingUsers.size})",
                                color = if (pendingUsers.isNotEmpty()) Color(0xFFD97706) else Color.Unspecified,
                                fontWeight = if (pendingUsers.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    FilterChip(
                        selected = selectedFilterIndex == 2,
                        onClick = { selectedFilterIndex = 2 },
                        label = { Text("ایڈمنز (${adminUsers.size})") }
                    )
                }

                // User List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        val isSelf = user.id == currentUser?.id
                        val isTargetSuperAdmin = user.isSuperAdmin

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (user.isPending) {
                                    Color(0xFFFEF3C7) // Light warm amber for pending
                                } else if (user.isAdmin) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (user.isPending) 1.5.dp else 0.5.dp,
                                    color = if (user.isPending) Color(0xFFF59E0B) else if (user.isAdmin) GoldDark else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("user_row_${user.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (user.isPending) Color(0xFFF59E0B)
                                                else if (isTargetSuperAdmin) GoldDark
                                                else if (user.isAdmin) EmeraldPrimary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTargetSuperAdmin) Icons.Default.Security
                                                else if (user.isAdmin) Icons.Default.AdminPanelSettings
                                                else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.isAdmin || isTargetSuperAdmin || user.isPending) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = user.emailOrPhone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(
                                                color = when {
                                                    user.isPending -> Color(0xFFF59E0B)
                                                    user.status == "rejected" -> Color(0xFFDC2626)
                                                    isTargetSuperAdmin -> GoldDark
                                                    user.isAdmin -> EmeraldPrimary
                                                    else -> Color.Gray.copy(alpha = 0.2f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = when {
                                                        user.isPending -> "⚠️ منظوری کا منتظر"
                                                        user.status == "rejected" -> "❌ مسترد شدہ"
                                                        isTargetSuperAdmin -> "سپر ایڈمن (مالک)"
                                                        user.isAdmin -> "ایڈمنسٹریٹر"
                                                        else -> "منظور شدہ ممبر"
                                                    },
                                                    color = if (user.isAdmin || isTargetSuperAdmin || user.isPending || user.status == "rejected") Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Actions Section
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (user.isPending) {
                                        // Approve and Reject buttons
                                        Button(
                                            onClick = { onApproveUser(user) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("approve_user_button_${user.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("منظور", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedButton(
                                            onClick = { onRejectUser(user) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("reject_user_button_${user.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("مسترد", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (user.status == "rejected") {
                                        Button(
                                            onClick = { onApproveUser(user) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("reapprove_user_button_${user.id}")
                                        ) {
                                            Text("دوبارہ منظور کریں", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else if (!isTargetSuperAdmin && !isSelf) {
                                        // Super Admin Toggle Admin Role
                                        Button(
                                            onClick = {
                                                userToConfirmRoleChange = user
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (user.isAdmin) Color(0xFFDC2626) else EmeraldPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("toggle_admin_button_${user.id}")
                                        ) {
                                            Text(
                                                text = if (user.isAdmin) "ایڈمن ہٹائیں" else "ایڈمن بنائیں",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Confirmation Dialog for promoting/demoting user
    if (userToConfirmRoleChange != null) {
        val target = userToConfirmRoleChange!!
        val isDemoting = target.isAdmin
        AlertDialog(
            onDismissRequest = { userToConfirmRoleChange = null },
            title = {
                Text(
                    text = if (isDemoting) "ایڈمن کے اختیارات واپس لینا" else "ایڈمنسٹریٹر مقرر کرنا",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isDemoting) {
                        "کیا آپ واقعی '${target.fullName}' سے ایڈمن کے اختیارات واپس لے کر انہیں عام ممبر بنانا چاہتے ہیں؟"
                    } else {
                        "کیا آپ واقعی '${target.fullName}' کو ایڈمنسٹریٹر بنانا چاہتے ہیں؟ وہ نیا فرد شامل کرنے، ترامیم کرنے اور اعلانات جاری کرنے کے مجاز ہوں گے۔"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleAdminRole(target)
                        userToConfirmRoleChange = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDemoting) Color(0xFFDC2626) else EmeraldPrimary
                    )
                ) {
                    Text(if (isDemoting) "ہاں، ایڈمن ہٹائیں" else "ہاں، ایڈمن بنائیں", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToConfirmRoleChange = null }) {
                    Text("منسوخ")
                }
            }
        )
    }
}
