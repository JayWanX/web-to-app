package com.webtoapp.ui.screens.community

import androidx.compose.animation.*
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.PremiumOutlinedButton
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.cloud.CommunityModuleDetail
import com.webtoapp.core.cloud.CommunityPostItem
import com.webtoapp.core.cloud.TeamWorkItem
import com.webtoapp.core.cloud.UserActivityInfo
import com.webtoapp.ui.viewmodel.CommunityViewModel
import com.webtoapp.ui.screens.community.formatTimeAgo
import com.webtoapp.ui.screens.community.formatDuration
import com.webtoapp.ui.screens.community.tagColor
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.ThemedBackgroundBox
import com.webtoapp.ui.components.UserTitleBadges
import androidx.compose.ui.graphics.Color

/**
 * 用户主页 — Jobs-style: 弹簧关注按钮 + 毛玻璃分隔 + 物理入场
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Int,
    communityViewModel: CommunityViewModel,
    onBack: () -> Unit,
    onModuleClick: (Int) -> Unit
) {
    val profile by communityViewModel.userProfile.collectAsStateWithLifecycle()
    val modules by communityViewModel.userModules.collectAsStateWithLifecycle()
    val teamWorks by communityViewModel.userTeamWorks.collectAsStateWithLifecycle()
    val userPosts by communityViewModel.userPosts.collectAsStateWithLifecycle()
    val userActivity by communityViewModel.userActivity.collectAsStateWithLifecycle()
    val loading by communityViewModel.userProfileLoading.collectAsStateWithLifecycle()
    val message by communityViewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tab state: 0 = Modules, 1 = Team Works, 2 = Posts, 3 = Activity
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(userId) { communityViewModel.loadUserProfile(userId) }
    LaunchedEffect(message) { message?.let { snackbarHostState.showSnackbar(it); communityViewModel.clearMessage() } }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    profile?.let { u ->
                        Column {
                            Text(u.displayName ?: u.username, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("${modules.size} posts", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(22.dp)) }
                }
            )
        }
    ) { padding ->
        ThemedBackgroundBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        if (loading) {
            ProfileShimmer(Modifier)
        } else {
            profile?.let { user ->
                LazyColumn(Modifier) {
                    // === Header ===
                    item {
                        StaggeredItem(index = 0) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Avatar(name = user.username, avatarUrl = user.avatarUrl, size = 68)
                                    Spacer(Modifier.weight(weight = 1f, fill = true))
                                    SpringFollowButton(
                                        isFollowing = user.isFollowing,
                                        onClick = { communityViewModel.toggleFollow(userId) }
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.displayName ?: user.username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    // Online status indicator
                                    userActivity?.let { act ->
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (act.isOnline) Color(0xFF4CAF50).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (act.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(6.dp)
                                                ) {}
                                                Text(
                                                    if (act.isOnline) Strings.communityOnline else Strings.communityOffline,
                                                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                                    color = if (act.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                                Text("@${user.username}", fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))

                                // Developer + Team badges
                                if (user.isDeveloper || user.teamBadges.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    UserTitleBadges(
                                        isDeveloper = user.isDeveloper,
                                        teamBadges = user.teamBadges
                                    )
                                }

                                // Online time stats
                                userActivity?.let { act ->
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OnlineTimeBadge(Strings.communityTodayOnline, formatDuration(act.todaySeconds), Modifier.weight(1f))
                                        OnlineTimeBadge(Strings.communityMonthOnline, formatDuration(act.monthSeconds), Modifier.weight(1f))
                                        OnlineTimeBadge(Strings.communityYearOnline, formatDuration(act.yearSeconds), Modifier.weight(1f))
                                    }
                                }

                                user.bio?.let {
                                    Spacer(Modifier.height(10.dp))
                                    Text(it, fontSize = 15.sp, lineHeight = 21.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f))
                                }
                                Spacer(Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    StatLabel(user.followingCount, "Following")
                                    StatLabel(user.followerCount, "Followers")
                                }
                            }
                        }
                        GlassDivider()
                    }

                    // === Tabs: Modules | Team Works ===
                    item {
                        StaggeredItem(index = 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Modules tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = 0 },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Modules",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    if (selectedTab == 0) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(40.dp).height(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(Modifier.height(3.dp))
                                    }
                                }
                                // Team Works tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = 1 },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "Team Works",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        if (teamWorks.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    "${teamWorks.size}",
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    if (selectedTab == 1) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(40.dp).height(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(Modifier.height(3.dp))
                                    }
                                }
                                // Posts tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = 2 },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            Strings.communityPosts,
                                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = if (selectedTab == 2) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        if (userPosts.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    "${userPosts.size}",
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    if (selectedTab == 2) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(40.dp).height(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(Modifier.height(3.dp))
                                    }
                                }
                                // Activity tab
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = 3 },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        Strings.communityActivity,
                                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 3) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    if (selectedTab == 3) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(40.dp).height(3.dp)
                                        ) {}
                                    } else {
                                        Spacer(Modifier.height(3.dp))
                                    }
                                }
                            }
                        }
                        GlassDivider()
                    }

                    // === Tab Content ===
                    if (selectedTab == 0) {
                        // ── Modules ──
                        if (modules.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("No modules yet", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(2.dp))
                                        Text("When they publish, it will show up here.", fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f))
                                    }
                                }
                            }
                        }

                        itemsIndexed(modules, key = { _, m -> m.id }) { index, module ->
                            StaggeredItem(index = index + 2) {
                                ModuleRow(module, onClick = { onModuleClick(module.id) })
                            }
                            GlassDivider()
                        }
                    } else if (selectedTab == 1) {
                        // ── Team Works ──
                        if (teamWorks.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Outlined.Groups, null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text("No team works yet", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Team contributions will show up here.", fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f))
                                    }
                                }
                            }
                        }

                        itemsIndexed(teamWorks, key = { _, w -> "tw_${w.id}" }) { index, work ->
                            StaggeredItem(index = index + 2) {
                                TeamWorkRow(work, onClick = { onModuleClick(work.id) })
                            }
                            GlassDivider()
                        }
                    } else if (selectedTab == 2) {
                        // ── Posts ──
                        if (userPosts.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Outlined.Forum, null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(Strings.communityNoPosts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        itemsIndexed(userPosts, key = { _, p -> "post_${p.id}" }) { index, post ->
                            StaggeredItem(index = index + 2) {
                                PostRow(post)
                            }
                            GlassDivider()
                        }
                    } else {
                        // ── Activity ──
                        item {
                            StaggeredItem(index = 0) {
                                userActivity?.let { act ->
                                    Column(Modifier.padding(16.dp)) {
                                        // Online status
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                                        ) {
                                            Column(Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (act.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                                        modifier = Modifier.size(10.dp)
                                                    ) {}
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        if (act.isOnline) Strings.communityOnline else Strings.communityOffline,
                                                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                                                    )
                                                }
                                                act.lastSeenAt?.let {
                                                    Spacer(Modifier.height(6.dp))
                                                    Text(
                                                        "Last seen: ${formatTimeAgo(it)}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                                Spacer(Modifier.height(16.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OnlineTimeBadge(Strings.communityTodayOnline, formatDuration(act.todaySeconds), Modifier.weight(1f))
                                                    OnlineTimeBadge(Strings.communityMonthOnline, formatDuration(act.monthSeconds), Modifier.weight(1f))
                                                    OnlineTimeBadge(Strings.communityYearOnline, formatDuration(act.yearSeconds), Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                } ?: Box(Modifier.fillMaxWidth().padding(56.dp), contentAlignment = Alignment.Center) {
                                    Text("No activity data", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        }
}

// ═══ 弹簧关注按钮 ═══

@Composable
private fun SpringFollowButton(isFollowing: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.9f else 1f,
        CommunityPhysics.MorphButton,
        label = "followScale",
        finishedListener = { pressed = false }
    )

    if (isFollowing) {
        PremiumOutlinedButton(
            onClick = { pressed = true; onClick() },
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            modifier = Modifier.scale(scale)
        ) { Text("Following", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
    } else {
        PremiumButton(
            onClick = { pressed = true; onClick() },
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            modifier = Modifier.scale(scale),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) { Text("Follow", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun StatLabel(count: Int, label: String) {
    Row {
        AnimatedCounter(count = count, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun ModuleRow(module: CommunityModuleDetail, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Extension, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(weight = 1f, fill = true)) {
            Text(module.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!module.description.isNullOrBlank()) {
                Text(module.description, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(6.dp))
            Text("${module.downloads} downloads  ·  ${module.rating}", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f))
        }
    }
}

@Composable
private fun TeamWorkRow(work: TeamWorkItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon — use different icons for app vs module
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (work.moduleType == "app")
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (work.moduleType == "app") Icons.Outlined.Apps else Icons.Outlined.Extension,
                    null, Modifier.size(20.dp),
                    tint = if (work.moduleType == "app")
                        MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(weight = 1f, fill = true)) {
            // Name
            Text(work.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            // Role badge + team name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Role badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (work.contributorRole == "lead")
                        Color(0xFFFFB300).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        if (work.contributorRole == "lead") "🔹 Lead" else "Member",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (work.contributorRole == "lead")
                            Color(0xFFFFB300)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                // Points
                if (work.contributionPoints > 0) {
                    Text(
                        "${work.contributionPoints} pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                // Team name
                work.teamName?.let {
                    Text("·", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Text(
                        it,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // Contribution description
            work.contributionDescription?.let {
                Spacer(Modifier.height(3.dp))
                Text(
                    it, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(4.dp))
            // Stats
            Text(
                "${work.downloads} downloads  ·  ${String.format("%.1f", work.rating)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

// ═══ Shimmer ═══

@Composable
private fun ProfileShimmer(modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Row {
            ShimmerBlock(68.dp, 68.dp, CircleShape)
            Spacer(Modifier.weight(weight = 1f, fill = true))
            ShimmerBlock(88.dp, 36.dp, RoundedCornerShape(20.dp))
        }
        Spacer(Modifier.height(14.dp))
        ShimmerBlock(140.dp, 18.dp)
        Spacer(Modifier.height(6.dp))
        ShimmerBlock(100.dp, 14.dp)
        Spacer(Modifier.height(14.dp))
        ShimmerBlock(280.dp, 14.dp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            repeat(2) { ShimmerBlock(80.dp, 14.dp) }
        }
        Spacer(Modifier.height(20.dp))
        GlassDivider()
        repeat(4) {
            Spacer(Modifier.height(12.dp))
            Row {
                ShimmerBlock(36.dp, 36.dp, CircleShape)
                Spacer(Modifier.width(10.dp))
                Column {
                    ShimmerBlock(160.dp, 14.dp)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBlock(240.dp, 13.dp)
                }
            }
        }
    }
}

// ═══ Online Time Badge ═══

@Composable
private fun OnlineTimeBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

// ═══ Post Row ═══

@Composable
private fun PostRow(post: CommunityPostItem) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        // Content preview
        Text(
            post.content,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
        )
        // Tags
        if (post.tags.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                post.tags.take(4).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tagColor(tag).copy(alpha = 0.12f)
                    ) {
                        Text(
                            "#$tag",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tagColor(tag)
                        )
                    }
                }
            }
        }
        // Interaction counts
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "❤ ${post.likeCount}  💬 ${post.commentCount}  🔄 ${post.shareCount}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.weight(1f))
            Text(
                formatTimeAgo(post.createdAt),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}
