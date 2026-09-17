package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AboutDedicationDialog
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.AnnouncementsDialog
import com.example.ui.components.AppSupportContributionDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.ChangePinDialog
import com.example.ui.components.CloudSyncConfigDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.ExportPdfDialog
import com.example.ui.components.FamilyStatsSheet
import com.example.ui.components.MemberCard
import com.example.ui.components.MemberDetailSheet
import com.example.ui.components.MySubmissionsDialog
import com.example.ui.components.PendingApprovalsDialog
import com.example.ui.components.PinVerificationDialog
import com.example.ui.components.RelationshipCalculatorDialog
import com.example.ui.components.ResetDatabaseConfirmDialog
import com.example.ui.components.SearchFilterSheet
import com.example.ui.components.TreeHierarchyView
import com.example.ui.components.UserManagementDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ParchmentBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShajraMainScreen(
    viewModel: FamilyTreeViewModel,
    modifier: Modifier = Modifier
) {
    val allMembers by viewModel.allMembers.collectAsState()
    val filteredMembers by viewModel.filteredMembers.collectAsState()
    val membersMap by viewModel.membersMap.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val genderFilter by viewModel.genderFilter.collectAsState()
    val generationFilter by viewModel.generationFilter.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedMemberForDetail by viewModel.selectedMemberForDetail.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val showAddMemberDialog by viewModel.showAddMemberDialog.collectAsState()
    val addMemberInitialParentId by viewModel.addMemberInitialParentId.collectAsState()
    val memberToEdit by viewModel.memberToEdit.collectAsState()
    val memberToDelete by viewModel.memberToDelete.collectAsState()
    val showResetConfirmDialog by viewModel.showResetConfirmDialog.collectAsState()
    val showChangePinDialog by viewModel.showChangePinDialog.collectAsState()
    val showRelationshipDialog by viewModel.showRelationshipDialog.collectAsState()
    val relationshipPersonA by viewModel.personAForRelation.collectAsState()
    val relationshipPersonB by viewModel.personBForRelation.collectAsState()
    val relationshipResult by viewModel.relationshipResult.collectAsState()
    val showBackupRestoreDialog by viewModel.showBackupRestoreDialog.collectAsState()
    val showExportPdfDialog by viewModel.showExportPdfDialog.collectAsState()
    val showCloudConfigDialog by viewModel.showCloudConfigDialog.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val showUserProfileDialog by viewModel.showUserProfileDialog.collectAsState()
    val authInitialTab by viewModel.authInitialTab.collectAsState()
    val allPendingRequests by viewModel.allPendingRequests.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val mySubmittedRequests by viewModel.mySubmittedRequests.collectAsState()
    val showApprovalsDialog by viewModel.showApprovalsDialog.collectAsState()
    val showMySubmissionsDialog by viewModel.showMySubmissionsDialog.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val showAnnouncementsDialog by viewModel.showAnnouncementsDialog.collectAsState()
    val showUserManagementDialog by viewModel.showUserManagementDialog.collectAsState()
    val targetUserApprovalQuery by viewModel.targetUserApprovalQuery.collectAsState()
    val scrollToMemberId by viewModel.scrollToMemberId.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val directoryListState = rememberLazyListState()

    // Auto-scroll in directory mode when new member is added
    LaunchedEffect(scrollToMemberId, filteredMembers) {
        if (scrollToMemberId != null) {
            val index = filteredMembers.indexOfFirst { it.id == scrollToMemberId }
            if (index >= 0) {
                directoryListState.animateScrollToItem(index + 1)
            }
        }
    }

    var showStatsSheet by remember { mutableStateOf(false) }
    var showDedicationDialog by remember { mutableStateOf(false) }
    var showAppSupportDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSearchFilterSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Force RTL layout direction for authentic Urdu typography and reading flow
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize().testTag("shajra_main_screen"),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else EmeraldPrimary
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.6f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountTree,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Column {
                                Text(
                                    text = "شجرۂ نسب",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.5.sp,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "اولاد محمد علی کالس",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        // Visual Cloud Sync Status Indicator (3 Distinct Clear States)
                        val isCurrentlySyncing = isSyncing || syncStatus == CloudSyncStatus.SYNCING
                        val isCloudSynced = !isCurrentlySyncing && syncStatus == CloudSyncStatus.SYNCED

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when {
                                isCurrentlySyncing -> GoldPrimary.copy(alpha = 0.28f)
                                isCloudSynced -> Color(0xFF15803D).copy(alpha = 0.35f)
                                else -> Color(0xFFB91C1C).copy(alpha = 0.25f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCurrentlySyncing -> GoldLight
                                    isCloudSynced -> Color(0xFF4ADE80)
                                    else -> Color(0xFFFCA5A5)
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.syncWithCloud() }
                                .testTag("sync_status_indicator")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCurrentlySyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        color = GoldLight,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "سنکنگ...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                } else if (isCloudSynced) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "سنک مکمل",
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "ہم آہنگ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "آف لائن",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "آف لائن",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFEE2E2),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Theme Toggle Button (Light / Dark mode)
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "لائٹ موڈ" else "ڈارک موڈ",
                                tint = if (isDarkMode) GoldLight else Color.White
                            )
                        }

                        // Announcements / Notice Board Button
                        IconButton(
                            onClick = { viewModel.openAnnouncementsDialog() },
                            modifier = Modifier.testTag("announcements_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (allAnnouncements.isNotEmpty()) {
                                        Badge(containerColor = GoldPrimary) {
                                            Text("${allAnnouncements.size}", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "خاندانی نوٹس بورڈ و اعلانات",
                                    tint = if (allAnnouncements.isNotEmpty()) GoldLight else Color.White
                                )
                            }
                        }

                        // Pending Approvals Button (Admin only)
                        if (isAdminLoggedIn) {
                            IconButton(
                                onClick = { viewModel.openApprovalsDialog() },
                                modifier = Modifier.testTag("approvals_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (pendingCount > 0) {
                                            Badge(containerColor = Color(0xFFDC2626)) {
                                                Text("$pendingCount", color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "درخواستوں کی منظوری",
                                        tint = if (pendingCount > 0) GoldLight else Color.White
                                    )
                                }
                            }
                        }

                        // User Authentication (Login/Sign Up) or Profile Pill
                        if (currentUser == null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GoldPrimary,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.openAuthDialog(0) }
                                    .testTag("auth_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "لاگ ان و سائن اپ",
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "لاگ ان",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isAdminLoggedIn) GoldPrimary else Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(0.5.dp, if (isAdminLoggedIn) GoldDark else Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.openUserProfileDialog() }
                                    .testTag("user_profile_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "پروفائل",
                                        tint = if (isAdminLoggedIn) EmeraldDark else Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "صارف",
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdminLoggedIn) EmeraldDark else Color.White,
                                            fontSize = 12.sp
                                        )
                                    )
                                    if (isAdminLoggedIn) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldDark,
                                            modifier = Modifier.size(15.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "A",
                                                    fontSize = 8.5.sp,
                                                    color = GoldLight,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Toggle Search & Filter button on Top Bar
                        val hasActiveSearchFilter = searchQuery.isNotBlank() || genderFilter != FilterGender.ALL || generationFilter != null
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier.testTag("toggle_search_top_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (hasActiveSearchFilter) {
                                        Badge(
                                            containerColor = GoldLight,
                                            contentColor = EmeraldDark
                                        ) {
                                            Text("•", fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "تلاش و فلٹرز",
                                    tint = if (isSearchExpanded || hasActiveSearchFilter) GoldLight else Color.White
                                )
                            }
                        }

                        // Stats button on Top Bar
                        IconButton(
                            onClick = { showStatsSheet = true },
                            modifier = Modifier.testTag("stats_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "خاندانی شماریات",
                                tint = Color.White
                            )
                        }

                        // Main Menu Button (Hamburger 3-lines ☰) - Large, prominent and clearly visible
                        Box {
                            Surface(
                                onClick = { showOverflowMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(1.2.dp, GoldLight.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("overflow_menu_button")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "مین مینو (اختیارات)",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("پی ڈی ایف شجرہ برآمد کریں (Export to PDF)", fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.openExportPdfDialog()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("خاندانی نوٹس بورڈ و اعلانات", fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = EmeraldPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.openAnnouncementsDialog()
                                    }
                                )
                                if (currentUser?.isSuperAdmin == true) {
                                    DropdownMenuItem(
                                        text = { Text("صارفین کا انتظام و ایڈمن کی تقرری", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldDark) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openUserManagementDialog()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("باہمی رشتہ تلاش کار", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = EmeraldPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.openRelationshipDialog()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("خاندانی شماریات و گراف", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, tint = EmeraldPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showStatsSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("آن لائن کلاؤڈ سنک", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldPrimary) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.syncWithCloud()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isDarkMode) "لائٹ تھیم (Light Mode)" else "ڈارک تھیم (Dark Mode)",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = if (isDarkMode) GoldDark else EmeraldPrimary
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.toggleDarkMode()
                                    }
                                )
                                if (isAdminLoggedIn) {
                                    DropdownMenuItem(
                                        text = { Text("کلاؤڈ سنک ترتیبات (Firebase)", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldPrimary) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openCloudConfigDialog()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("بیک اپ و ڈیٹا فائل (JSON)", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, tint = EmeraldPrimary) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openBackupRestoreDialog()
                                        }
                                    )
                                }
                                if (currentUser != null) {
                                    DropdownMenuItem(
                                        text = { Text("میری بھیجی گئی درخواستیں", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openMySubmissionsDialog()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("💖 آپ کا تعاون (ایپ کی ترقی کے لیے)", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showAppSupportDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("پیغام، ٹیم و خاندانی تحفہ", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldDark) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showDedicationDialog = true
                                    }
                                )
                                if (isAdminLoggedIn) {
                                    DropdownMenuItem(
                                        text = { Text("ایڈمن پن کوڈ تبدیل کریں", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.openChangePinDialog()
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddMemberDialog(null) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = {
                        Text(
                            text = if (isAdminLoggedIn) "نیا فرد شامل کریں" else "اولاد / نیا فرد شامل کریں",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = if (isAdminLoggedIn) EmeraldPrimary else GoldDark,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_member_fab")
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Admin Status Banner if logged in
                AnimatedVisibility(visible = isAdminLoggedIn) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GoldLight,
                        border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ایڈمن موڈ فعال: خوش آمدید ${currentUser?.fullName ?: "ایڈمن"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            if (pendingCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.openApprovalsDialog() }
                                        .padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = "$pendingCount نئی درخواستیں",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Guest / Login Invitation Banner if not logged in
                AnimatedVisibility(visible = currentUser == null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "خاندان کے فرد ہیں؟ لاگ ان یا اکاؤنٹ بنائیں",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.openAuthDialog(0) }
                            ) {
                                Text(
                                    text = "لاگ ان",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Modern Segmented View Switcher & Single Search Action Option
                val hasActiveFilters = searchQuery.isNotBlank() || genderFilter != FilterGender.ALL || generationFilter != null
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tree Tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (viewMode == ViewMode.TREE) EmeraldPrimary else Color.Transparent,
                            shadowElevation = if (viewMode == ViewMode.TREE) 1.5.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setViewMode(ViewMode.TREE) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountTree, 
                                    contentDescription = null, 
                                    tint = if (viewMode == ViewMode.TREE) Color.White else EmeraldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "خاندانی درخت",
                                    fontWeight = if (viewMode == ViewMode.TREE) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (viewMode == ViewMode.TREE) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Directory Tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (viewMode == ViewMode.DIRECTORY) EmeraldPrimary else Color.Transparent,
                            shadowElevation = if (viewMode == ViewMode.DIRECTORY) 1.5.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setViewMode(ViewMode.DIRECTORY) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.FormatListBulleted, 
                                    contentDescription = null, 
                                    tint = if (viewMode == ViewMode.DIRECTORY) Color.White else EmeraldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "فہرست (${filteredMembers.size})",
                                    fontWeight = if (viewMode == ViewMode.DIRECTORY) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (viewMode == ViewMode.DIRECTORY) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                    }
                }

                // Live Search Bar (Direct, reactive, and without generation selection strip)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(
                        1.dp,
                        if (searchQuery.isNotBlank()) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = {
                                Text(
                                    "نام یا ولدیت لکھ کر تلاش کریں... (لائیو سرچ)",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "تلاش",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "صاف کریں",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_field")
                        )

                        // Quick Gender Filter Chip
                        FilterChip(
                            selected = genderFilter != FilterGender.ALL,
                            onClick = {
                                val next = when (genderFilter) {
                                    FilterGender.ALL -> FilterGender.MALES
                                    FilterGender.MALES -> FilterGender.FEMALES
                                    FilterGender.FEMALES -> FilterGender.ALL
                                }
                                viewModel.setGenderFilter(next)
                            },
                            label = {
                                Text(
                                    when (genderFilter) {
                                        FilterGender.MALES -> "صرف مرد"
                                        FilterGender.FEMALES -> "صرف خواتین"
                                        FilterGender.ALL -> "فلٹر"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Clear and prominent Live Search Results Banner
                if (searchQuery.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (filteredMembers.isNotEmpty()) EmeraldContainer.copy(alpha = 0.7f) else Color(0xFFFEE2E2),
                        border = BorderStroke(
                            1.dp,
                            if (filteredMembers.isNotEmpty()) EmeraldPrimary.copy(alpha = 0.4f) else Color(0xFFF87171)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (filteredMembers.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (filteredMembers.isNotEmpty()) EmeraldDark else Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (filteredMembers.isNotEmpty())
                                        "نتائج: ${filteredMembers.size} افراد مل گئے (\"$searchQuery\")"
                                    else
                                        "کوئی فرد نہیں ملا، نام یا ولدیت کی املا چیک کریں",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (filteredMembers.isNotEmpty()) EmeraldDark else Color(0xFF991B1B)
                                )
                            }
                            TextButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "تمام دکھائیں",
                                    fontSize = 11.sp,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Main Content Body: Tree View OR Directory List View
                Box(modifier = Modifier.weight(1f)) {
                    val isTreeView = viewMode == ViewMode.TREE && searchQuery.isBlank()
                    AnimatedContent(
                        targetState = isTreeView,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220), initialOffsetY = { 24 })) togetherWith
                                (fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180), targetOffsetY = { -24 }))
                        },
                        label = "MainViewContentTransition"
                    ) { showTree ->
                        if (showTree) {
                            TreeHierarchyView(
                                allMembers = allMembers,
                                selectedBranchId = selectedBranchId,
                                onBranchSelected = { viewModel.setBranchFilter(it) },
                                onMemberClick = { viewModel.selectMemberForDetail(it) },
                                isAdmin = isAdminLoggedIn,
                                onEditMember = { viewModel.openEditMemberDialog(it) },
                                onDeleteMember = { viewModel.openDeleteMemberDialog(it) },
                                onAddChild = { parentId -> viewModel.openAddMemberDialog(parentId) },
                                onExportPdf = { viewModel.openExportPdfDialog() },
                                scrollToMemberId = scrollToMemberId
                            )
                        } else {
                            // Directory / Search Results List with smooth transitions
                            AnimatedContent(
                                targetState = filteredMembers.isEmpty(),
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200), initialOffsetY = { 20 })) togetherWith
                                        (fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180), targetOffsetY = { -20 }))
                                },
                                label = "DirectoryListTransition"
                            ) { isEmpty ->
                                if (isEmpty) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = GoldDark,
                                                    modifier = Modifier.size(44.dp)
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "اس تلاش کا کوئی فرد نہیں ملا",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "براہ کرم نام یا ولدیت کے ہجے درست کریں یا فلٹرز کو صاف کریں",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = directoryListState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        item(key = "results_count_header") {
                                            Text(
                                                text = "تلاش کے نتائج: ${filteredMembers.size} افراد",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                modifier = Modifier
                                                    .animateItem(
                                                        fadeInSpec = spring(
                                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                                            stiffness = Spring.StiffnessMediumLow
                                                        ),
                                                        fadeOutSpec = spring(
                                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                                            stiffness = Spring.StiffnessMedium
                                                        ),
                                                        placementSpec = spring(
                                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                                            stiffness = Spring.StiffnessMediumLow
                                                        )
                                                    )
                                                    .padding(top = 8.dp)
                                            )
                                        }

                                        items(filteredMembers, key = { it.id }) { member ->
                                            val childrenCount = viewModel.getChildrenOf(member.id).size
                                            MemberCard(
                                                member = member,
                                                childrenCount = childrenCount,
                                                isAdmin = isAdminLoggedIn,
                                                onClick = { viewModel.selectMemberForDetail(member) },
                                                onEdit = if (isAdminLoggedIn) ({ viewModel.openEditMemberDialog(member) }) else null,
                                                onDelete = if (isAdminLoggedIn && member.id != 1L) ({ viewModel.openDeleteMemberDialog(member) }) else null,
                                                onAddChild = if (isAdminLoggedIn) ({ viewModel.openAddMemberDialog(member.id) }) else null,
                                                modifier = Modifier.animateItem(
                                                    fadeInSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    fadeOutSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    ),
                                                    placementSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            )
                                        }

                                        item(key = "list_bottom_spacer") {
                                            Spacer(modifier = Modifier.height(72.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialogs & Sheets

            // 1. Member Detail Sheet
            selectedMemberForDetail?.let { member ->
                val lineage = viewModel.getLineage(member)
                val father = member.fatherId?.let { membersMap[it] }
                val siblings = viewModel.getSiblingsOf(member)
                val children = viewModel.getChildrenOf(member.id)

                MemberDetailSheet(
                    member = member,
                    lineage = lineage,
                    father = father,
                    siblings = siblings,
                    children = children,
                    isAdmin = isAdminLoggedIn,
                    onDismiss = { viewModel.selectMemberForDetail(null) },
                    onSelectMember = { newMember -> viewModel.selectMemberForDetail(newMember) },
                    onEdit = { viewModel.openEditMemberDialog(member) },
                    onDelete = { viewModel.openDeleteMemberDialog(member) },
                    onAddChild = { parentId -> viewModel.openAddMemberDialog(parentId) },
                    onCalculateRelationship = { targetMember ->
                        viewModel.selectMemberForDetail(null)
                        viewModel.openRelationshipDialog(initialA = targetMember)
                    },
                    onSharePdf = {
                        viewModel.selectMemberForDetail(null)
                        viewModel.openExportPdfDialog()
                    },
                    onCreateLineagePdf = {
                        if (currentUser == null && !isAdminLoggedIn) {
                            viewModel.selectMemberForDetail(null)
                            viewModel.openAuthDialog(0)
                            viewModel.showSnackbar("شجرہ نسب PDF برآمد کرنے کے لیے پہلے لاگ ان کریں")
                        } else {
                            try {
                                val lineageChain = lineage.reversed()
                                val result = com.example.util.FamilyTreePdfGenerator.generateSpecificLineagePdf(
                                    context = context,
                                    member = member,
                                    lineageChain = lineageChain,
                                    rootAncestorName = lineageChain.firstOrNull()?.name ?: "محمد علی"
                                )
                                val intent = com.example.util.FamilyTreePdfGenerator.createShareIntent(context, result.file)
                                context.startActivity(android.content.Intent.createChooser(intent, "شجرہ نسب PDF شیئر کریں"))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "PDF فائل تیار نہ ہو سکی: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // 2. PIN Verification Dialog
            if (showPinDialog) {
                PinVerificationDialog(
                    onDismiss = { viewModel.dismissPinDialog() },
                    onVerify = { pin -> viewModel.verifyPin(pin) }
                )
            }

            // 3. Add Member Dialog
            if (showAddMemberDialog) {
                AddEditMemberDialog(
                    initialMember = null,
                    initialParentId = addMemberInitialParentId,
                    allMembers = allMembers,
                    isAdmin = isAdminLoggedIn,
                    onDismiss = { viewModel.dismissAddMemberDialog() },
                    onSave = { name, fatherId, gender, spouse, location, isDeceased, deathNote, notes, phone, occupation, birthYear, deathYear ->
                        viewModel.saveNewMember(
                            name = name,
                            fatherId = fatherId,
                            gender = gender,
                            spouse = spouse,
                            location = location,
                            isDeceased = isDeceased,
                            deathNote = deathNote,
                            notes = notes,
                            phone = phone,
                            occupation = occupation,
                            birthYear = birthYear,
                            deathYear = deathYear
                        )
                    }
                )
            }

            // 4. Edit Member Dialog
            memberToEdit?.let { member ->
                AddEditMemberDialog(
                    initialMember = member,
                    initialParentId = member.fatherId,
                    allMembers = allMembers,
                    isAdmin = isAdminLoggedIn,
                    onDismiss = { viewModel.dismissEditMemberDialog() },
                    onSave = { name, fatherId, gender, spouse, location, isDeceased, deathNote, notes, phone, occupation, birthYear, deathYear ->
                        viewModel.updateMember(
                            id = member.id,
                            name = name,
                            fatherId = fatherId,
                            gender = gender,
                            spouse = spouse,
                            location = location,
                            isDeceased = isDeceased,
                            deathNote = deathNote,
                            notes = notes,
                            phone = phone,
                            occupation = occupation,
                            birthYear = birthYear,
                            deathYear = deathYear
                        )
                    }
                )
            }

            // 5. Delete Member Confirmation Dialog
            memberToDelete?.let { member ->
                val hasChildren = viewModel.getChildrenOf(member.id).isNotEmpty()
                DeleteConfirmDialog(
                    member = member,
                    hasChildren = hasChildren,
                    onDismiss = { viewModel.dismissDeleteMemberDialog() },
                    onConfirm = { viewModel.confirmDeleteMember(member) }
                )
            }

            // 6. Reset Database Confirmation Dialog
            if (showResetConfirmDialog) {
                ResetDatabaseConfirmDialog(
                    onDismiss = { viewModel.dismissResetDialog() },
                    onConfirm = { viewModel.confirmResetToDefault() }
                )
            }

            // 7. Change PIN Dialog
            if (showChangePinDialog) {
                ChangePinDialog(
                    onDismiss = { viewModel.dismissChangePinDialog() },
                    onChangePin = { oldPin, newPin -> viewModel.changePin(oldPin, newPin) }
                )
            }

            // 8. Statistics Sheet
            if (showStatsSheet && statistics != null) {
                FamilyStatsSheet(
                    stats = statistics!!,
                    isAdmin = isAdminLoggedIn,
                    onDismiss = { showStatsSheet = false },
                    onResetData = {
                        showStatsSheet = false
                        viewModel.openResetDialog()
                    },
                    onChangePin = {
                        showStatsSheet = false
                        viewModel.openChangePinDialog()
                    },
                    onBackupRestore = {
                        showStatsSheet = false
                        viewModel.openBackupRestoreDialog()
                    }
                )
            }

            // 9. Relationship Calculator Dialog
            if (showRelationshipDialog) {
                RelationshipCalculatorDialog(
                    allMembers = allMembers,
                    personA = relationshipPersonA,
                    personB = relationshipPersonB,
                    result = relationshipResult,
                    onSelectPersonA = { viewModel.setPersonAForRelation(it) },
                    onSelectPersonB = { viewModel.setPersonBForRelation(it) },
                    onDismiss = { viewModel.dismissRelationshipDialog() }
                )
            }

            // 10. Backup & Restore Dialog (Admin only)
            if (showBackupRestoreDialog && isAdminLoggedIn) {
                BackupRestoreDialog(
                    totalMembers = allMembers.size,
                    onExportJson = { viewModel.exportJson() },
                    onExportReadableTree = { viewModel.exportReadableTree() },
                    onImportJson = { jsonStr, onResult ->
                        viewModel.importBackup(jsonStr, onResult)
                    },
                    onOpenExportPdf = {
                        viewModel.dismissBackupRestoreDialog()
                        viewModel.openExportPdfDialog()
                    },
                    onDismiss = { viewModel.dismissBackupRestoreDialog() }
                )
            }

            // 10b. Export to PDF Dialog
            if (showExportPdfDialog) {
                ExportPdfDialog(
                    allMembers = allMembers,
                    currentSelectedBranchId = selectedBranchId,
                    onDismiss = { viewModel.dismissExportPdfDialog() }
                )
            }

            // 10c. Search & Filter Bottom Sheet
            if (showSearchFilterSheet) {
                SearchFilterSheet(
                    currentGenderFilter = genderFilter,
                    currentGenerationFilter = generationFilter,
                    totalMembersCount = allMembers.size,
                    onGenderFilterChange = { viewModel.setGenderFilter(it) },
                    onGenerationFilterChange = { viewModel.setGenerationFilter(it) },
                    onResetFilters = {
                        viewModel.setGenderFilter(FilterGender.ALL)
                        viewModel.setGenerationFilter(null)
                    },
                    onDismiss = { showSearchFilterSheet = false }
                )
            }

            // 11. Authentication (Login / Sign Up) Dialog
            if (showAuthDialog) {
                AuthDialog(
                    allMembers = allMembers,
                    initialTab = authInitialTab,
                    onDismiss = { viewModel.dismissAuthDialog() },
                    onLogin = { id, pass, onResult ->
                        viewModel.loginUser(id, pass, onResult)
                    },
                    onRegister = { name, id, pass, linkedId, adminPin, onResult ->
                        viewModel.registerUser(name, id, pass, linkedId, adminPin, onResult)
                    },
                    onForgotPassword = { email, onResult ->
                        viewModel.sendPasswordResetEmail(email, onResult)
                    },
                    onRequestOtpReset = { identifier, onResult ->
                        viewModel.requestPasswordReset(identifier, onResult)
                    },
                    onResetPasswordWithOtp = { identifier, otp, newPass, onResult ->
                        viewModel.verifyOtpAndResetPassword(identifier, otp, newPass, onResult)
                    }
                )
            }

            // 12. User Profile Dialog
            if (showUserProfileDialog && currentUser != null) {
                UserProfileDialog(
                    user = currentUser!!,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    syncStatus = syncStatus,
                    isSyncing = isSyncing,
                    pendingApprovalsCount = pendingCount,
                    onUpdateDisplayName = { newName, onResult ->
                        viewModel.updateUserDisplayName(newName, onResult)
                    },
                    onChangeUserPassword = { newPass, onResult ->
                        viewModel.changeUserPassword(newPass, onResult)
                    },
                    onDismiss = { viewModel.dismissUserProfileDialog() },
                    onLogout = { viewModel.logoutUser() },
                    onViewLinkedMember = { memberId ->
                        val member = membersMap[memberId]
                        if (member != null) {
                            viewModel.selectMemberForDetail(member)
                        }
                    },
                    onChangePassword = {
                        viewModel.dismissUserProfileDialog()
                        viewModel.openChangePinDialog()
                    },
                    onOpenBackup = {
                        viewModel.dismissUserProfileDialog()
                        viewModel.openBackupRestoreDialog()
                    },
                    onOpenApprovals = {
                        viewModel.openApprovalsDialog()
                    },
                    onOpenMySubmissions = {
                        viewModel.openMySubmissionsDialog()
                    },
                    onOpenUserManagement = {
                        viewModel.dismissUserProfileDialog()
                        viewModel.openUserManagementDialog()
                    },
                    onSyncCloud = {
                        viewModel.syncWithCloud()
                    },
                    onOpenCloudSettings = if (isAdminLoggedIn) {
                        { viewModel.openCloudConfigDialog() }
                    } else null,
                    onOpenSupport = {
                        showAppSupportDialog = true
                    }
                )
            }

            // 13. Admin Pending Approvals Dialog
            if (showApprovalsDialog) {
                PendingApprovalsDialog(
                    allRequests = allPendingRequests,
                    onDismiss = { viewModel.dismissApprovalsDialog() },
                    onApprove = { req -> viewModel.approveRequest(req) },
                    onReject = { req, reason -> viewModel.rejectRequest(req, reason) },
                    onDelete = { req -> viewModel.deletePendingRequest(req) },
                    onRefresh = { viewModel.refreshPendingRequestsFromCloud() },
                    isSyncing = isSyncing
                )
            }

            // 14. User Submissions & Status Dialog
            if (showMySubmissionsDialog) {
                MySubmissionsDialog(
                    myRequests = mySubmittedRequests,
                    onDismiss = { viewModel.dismissMySubmissionsDialog() },
                    onAddNew = { viewModel.openAddMemberDialog(null) }
                )
            }

            // 15. Message, Team & Dedication Dialog (پیغام، ٹیم و خاندانی تحفہ)
            if (showDedicationDialog) {
                AboutDedicationDialog(
                    onDismiss = { showDedicationDialog = false },
                    onOpenSupport = { showAppSupportDialog = true }
                )
            }

            // 15b. App Support & Contribution Dialog (آپ کا تعاون)
            if (showAppSupportDialog) {
                AppSupportContributionDialog(
                    onDismiss = { showAppSupportDialog = false }
                )
            }

            // 16. Firebase Cloud Sync & Configuration Dialog (Admin only)
            if (showCloudConfigDialog && isAdminLoggedIn) {
                CloudSyncConfigDialog(
                    savedConfig = viewModel.getCloudConfig(),
                    isInitialized = viewModel.isCloudConfigured(),
                    isAdmin = isAdminLoggedIn,
                    onSaveManualConfig = { proj, app, key, callback ->
                        viewModel.saveFirebaseConfigManual(proj, app, key, callback)
                    },
                    onSaveJsonConfig = { json, callback ->
                        viewModel.saveFirebaseConfigFromJson(json, callback)
                    },
                    onUploadAllToCloud = { callback ->
                        viewModel.uploadAllMembersToCloudDirect(callback)
                    },
                    onDownloadAllFromCloud = { callback ->
                        viewModel.downloadAllMembersFromCloudDirect(callback)
                    },
                    onOpenBackupDialog = {
                        viewModel.openBackupRestoreDialog()
                    },
                    onDismiss = { viewModel.dismissCloudConfigDialog() }
                )
            }

            // 17. Family Announcements & Notice Board Dialog
            if (showAnnouncementsDialog) {
                AnnouncementsDialog(
                    announcements = allAnnouncements,
                    currentUser = currentUser,
                    isAdmin = isAdminLoggedIn || (currentUser?.isAdmin == true),
                    onDismiss = { viewModel.dismissAnnouncementsDialog() },
                    onSaveAnnouncement = { title, content, isPinned, id ->
                        viewModel.saveAnnouncement(title, content, isPinned, id)
                    },
                    onDeleteAnnouncement = { announcement ->
                        viewModel.deleteAnnouncement(announcement)
                    },
                    onTogglePin = { announcement ->
                        viewModel.togglePinAnnouncement(announcement)
                    }
                )
            }

            // 18. Super Admin / Admin User Management Dialog
            if (showUserManagementDialog) {
                UserManagementDialog(
                    users = allUsers,
                    currentUser = currentUser,
                    initialSearchQuery = targetUserApprovalQuery ?: "",
                    onDismiss = { viewModel.dismissUserManagementDialog() },
                    onToggleAdminRole = { user -> viewModel.toggleUserAdminRole(user) },
                    onApproveUser = { user -> viewModel.approveUser(user) },
                    onRejectUser = { user -> viewModel.rejectUser(user) }
                )
            }
        }
    }
}

@Composable
private fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.20f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

