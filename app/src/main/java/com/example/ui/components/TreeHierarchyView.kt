package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMember
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary

@Composable
fun TreeHierarchyView(
    allMembers: List<FamilyMember>,
    selectedBranchId: Long?,
    onBranchSelected: (Long?) -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    isAdmin: Boolean,
    onEditMember: (FamilyMember) -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onAddChild: (Long) -> Unit,
    onExportPdf: (() -> Unit)? = null,
    scrollToMemberId: Long? = null,
    modifier: Modifier = Modifier
) {
    // Map of parentId -> children list
    val childrenMap = remember(allMembers) {
        allMembers.groupBy { it.fatherId }
    }

    // Root member: Muhammad Ali (id = 1)
    val rootMember = remember(allMembers) {
        allMembers.find { it.fatherId == null } ?: allMembers.firstOrNull()
    }

    // 4 Main branches: children of root
    val mainBranches = remember(allMembers, rootMember) {
        if (rootMember != null) {
            allMembers.filter { it.fatherId == rootMember.id }
        } else emptyList()
    }

    // Expanded state map for member IDs
    // By default expand root and the 4 branches
    val expandedMap = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            // Initially expand root
            this[1L] = true
            // Expand main branches by default
            this[10L] = true
            this[20L] = true
            this[30L] = true
            this[40L] = true
        }
    }

    val listState = rememberLazyListState()

    // Auto-expand ancestors and scroll when scrollToMemberId changes
    LaunchedEffect(scrollToMemberId) {
        if (scrollToMemberId != null) {
            var curr = allMembers.find { it.id == scrollToMemberId }
            while (curr != null) {
                val parentId = curr.fatherId
                if (parentId != null) {
                    expandedMap[parentId] = true
                    curr = allMembers.find { it.id == parentId }
                } else {
                    break
                }
            }
            expandedMap[scrollToMemberId] = true

            // Determine which branch it belongs to and scroll
            var branchAncestor = allMembers.find { it.id == scrollToMemberId }
            while (branchAncestor != null && branchAncestor.fatherId != rootMember?.id && branchAncestor.fatherId != null) {
                branchAncestor = allMembers.find { it.id == branchAncestor?.fatherId }
            }
            if (branchAncestor != null) {
                val branchIndex = mainBranches.indexOfFirst { it.id == branchAncestor?.id }
                if (branchIndex >= 0) {
                    listState.animateScrollToItem(branchIndex + 2)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Branch Selector Chips
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شاخ منتخب کریں:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (onExportPdf != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onExportPdf() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "پی ڈی ایف شجرہ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedBranchId == null,
                            onClick = { onBranchSelected(null) },
                            label = { Text("مکمل شجرہ", fontWeight = if (selectedBranchId == null) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(mainBranches) { branch ->
                        val isSelected = selectedBranchId == branch.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onBranchSelected(if (isSelected) null else branch.id) },
                            label = { Text("شاخ ${branch.name}", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 2. Root Ancestor Card (Muhammad Ali) if all branches or root is relevant
        if (rootMember != null && selectedBranchId == null) {
            item {
                RootAncestorHeader(
                    rootMember = rootMember,
                    childrenCount = childrenMap[rootMember.id]?.size ?: 0,
                    onClick = { onMemberClick(rootMember) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 3. Main Branches or Selected Branch
        val branchesToDisplay = if (selectedBranchId != null) {
            mainBranches.filter { it.id == selectedBranchId }
        } else {
            mainBranches
        }

        items(branchesToDisplay, key = { it.id }) { branch ->
            BranchItemNode(
                member = branch,
                childrenMap = childrenMap,
                expandedMap = expandedMap,
                level = 1,
                onMemberClick = onMemberClick,
                isAdmin = isAdmin,
                onEditMember = onEditMember,
                onDeleteMember = onDeleteMember,
                onAddChild = onAddChild,
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                        fadeOutSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                        placementSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    )
                    .padding(horizontal = 16.dp)
            )
        }

        // Bottom space for scrolling
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun RootAncestorHeader(
    rootMember: FamilyMember,
    childrenCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "بانیٔ شجرہ نسب",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rootMember.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.5.sp
                )
            )

            Text(
                text = "اولاد: $childrenCount شاخیں (کرم الہی، کرم دین، حاکم علی، محمد ھاشم)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.5.sp
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun BranchItemNode(
    member: FamilyMember,
    childrenMap: Map<Long?, List<FamilyMember>>,
    expandedMap: MutableMap<Long, Boolean>,
    level: Int,
    onMemberClick: (FamilyMember) -> Unit,
    isAdmin: Boolean,
    onEditMember: (FamilyMember) -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onAddChild: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val children = childrenMap[member.id] ?: emptyList()
    val hasChildren = children.isNotEmpty()
    val isExpanded = expandedMap[member.id] ?: false

    // Indentation based on generational level
    val indent = if (level > 1) 12.dp else 0.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indent)
    ) {
        // Node Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // If has children, show toggle expand icon
            if (hasChildren) {
                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { expandedMap[member.id] = !isExpanded },
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بند کریں" else "کھولیں",
                            tint = if (isExpanded) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                MemberCard(
                    member = member,
                    childrenCount = children.size,
                    isAdmin = isAdmin,
                    onClick = { onMemberClick(member) },
                    onEdit = if (isAdmin) ({ onEditMember(member) }) else null,
                    onDelete = if (isAdmin && member.id != 1L) ({ onDeleteMember(member) }) else null,
                    onAddChild = if (isAdmin) ({ onAddChild(member.id) }) else null
                )
            }
        }

        // Animated Children sub-tree
        AnimatedVisibility(
            visible = isExpanded && hasChildren,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Subtle vertical branch connector line
                children.forEach { child ->
                    BranchItemNode(
                        member = child,
                        childrenMap = childrenMap,
                        expandedMap = expandedMap,
                        level = level + 1,
                        onMemberClick = onMemberClick,
                        isAdmin = isAdmin,
                        onEditMember = onEditMember,
                        onDeleteMember = onDeleteMember,
                        onAddChild = onAddChild
                    )
                }
            }
        }
    }
}
