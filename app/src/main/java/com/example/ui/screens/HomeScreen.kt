package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.ChatCardItem
import com.example.ui.components.StoryHighlightsRow
import com.example.ui.components.VibeBottomBar
import com.example.ui.theme.VibeCyanSecondary
import com.example.ui.theme.VibeVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    chats: List<ChatEntity>,
    stories: List<StoryEntity>,
    callLogs: List<CallLogEntity>,
    users: List<UserEntity>,
    settings: AppSettingsEntity?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    messageSearchResults: List<MessageEntity> = emptyList(),
    typingStates: Map<String, String?> = emptyMap(),
    onChatSelect: (String) -> Unit,
    onStorySelect: (String) -> Unit,
    onStartCall: (String, String, String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSecretChatClick: () -> Unit,
    onArchivedClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPostStoryClick: () -> Unit,
    onUpdateSettings: (AppSettingsEntity) -> Unit
) {
    var showQuickActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VIBE",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = VibeVioletPrimary,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(VibeCyanSecondary, CircleShape)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNewChatClick,
                        modifier = Modifier.testTag("new_chat_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(
                        onClick = { onTabSelected("settings") },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            val totalUnread = chats.sumOf { it.unreadCount }
            VibeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                unreadMessagesCount = totalUnread
            )
        },
        floatingActionButton = {
            if (selectedTab == "chats") {
                FloatingActionButton(
                    onClick = { showQuickActionSheet = true },
                    containerColor = VibeVioletPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_quick_actions")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Actions")
                }
            } else if (selectedTab == "status") {
                FloatingActionButton(
                    onClick = onPostStoryClick,
                    containerColor = VibeVioletPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_post_status")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Post Status")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "chats" -> {
                    ChatsTabContent(
                        chats = chats,
                        stories = stories,
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        activeFilter = activeFilter,
                        onFilterChange = onFilterChange,
                        messageSearchResults = messageSearchResults,
                        typingStates = typingStates,
                        onChatSelect = onChatSelect,
                        onStorySelect = onStorySelect,
                        onMyStatusClick = onPostStoryClick,
                        onArchivedClick = onArchivedClick
                    )
                }
                "status" -> {
                    StatusTabContent(
                        stories = stories,
                        onStorySelect = onStorySelect,
                        onMyStatusClick = onPostStoryClick
                    )
                }
                "calls" -> {
                    CallsTabContent(
                        callLogs = callLogs,
                        users = users,
                        onStartCall = onStartCall
                    )
                }
                "contacts" -> {
                    ContactsTabContent(
                        users = users,
                        onContactClick = { user -> onStartCall(user.id, user.name, "VOICE") },
                        onChatClick = { user ->
                            val existingChat = chats.find { it.participantIds.contains(user.id) }
                            if (existingChat != null) {
                                onChatSelect(existingChat.id)
                            } else {
                                onNewChatClick()
                            }
                        }
                    )
                }
                "settings" -> {
                    SettingsTabContent(
                        settings = settings,
                        users = users,
                        onEditProfileClick = onEditProfileClick,
                        onArchivedClick = onArchivedClick,
                        onUpdateSettings = onUpdateSettings
                    )
                }
            }

            // Quick Actions Modal Bottom Sheet
            if (showQuickActionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showQuickActionSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Start Something New",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        QuickActionRowItem("New Direct Chat", "Message a friend directly", Icons.Default.Chat) {
                            showQuickActionSheet = false
                            onNewChatClick()
                        }
                        QuickActionRowItem("New Group", "Chat with multiple people", Icons.Default.Group) {
                            showQuickActionSheet = false
                            onCreateGroupClick()
                        }
                        QuickActionRowItem("Secret Encrypted Chat", "Self-destructing private chat", Icons.Default.Lock) {
                            showQuickActionSheet = false
                            onSecretChatClick()
                        }
                        QuickActionRowItem("Broadcast Message", "Send message to all contacts", Icons.Default.Campaign) {
                            showQuickActionSheet = false
                            onNewChatClick()
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatsTabContent(
    chats: List<ChatEntity>,
    stories: List<StoryEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    messageSearchResults: List<MessageEntity> = emptyList(),
    typingStates: Map<String, String?> = emptyMap(),
    onChatSelect: (String) -> Unit,
    onStorySelect: (String) -> Unit,
    onMyStatusClick: () -> Unit,
    onArchivedClick: () -> Unit
) {
    val filteredChats = chats.filter { chat ->
        val matchesQuery = chat.chatName.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (activeFilter) {
            "UNREAD" -> chat.unreadCount > 0
            "GROUPS" -> chat.isGroup
            "SECRET" -> chat.isSecret
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search messages, groups...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("chats_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibeVioletPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )
        }

        // Story highlights horizontal row
        item {
            StoryHighlightsRow(
                stories = stories,
                onMyStatusClick = onMyStatusClick,
                onStoryClick = { story -> onStorySelect(story.id) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Filter chips (All, Unread, Groups, Secret)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All", "UNREAD" to "Unread", "GROUPS" to "Groups", "SECRET" to "Secret 🔒").forEach { (key, label) ->
                    val isSelected = (activeFilter == key)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Archived Chats bar trigger
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onArchivedClick() },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archived",
                        tint = VibeVioletPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Archived Chats",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Chat list
        if (filteredChats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "No chats",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No chats found matching \"$searchQuery\"" else "No chats in this folder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredChats, key = { it.id }) { chat ->
                ChatCardItem(
                    chat = chat,
                    onClick = { onChatSelect(chat.id) },
                    typingText = typingStates[chat.id]
                )
            }
        }

        // Message Search Results Section
        if (searchQuery.isNotBlank() && messageSearchResults.isNotEmpty()) {
            item {
                Text(
                    text = "Matching Messages (${messageSearchResults.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = VibeVioletPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(messageSearchResults, key = { "msg_search_${it.id}" }) { message ->
                val parentChat = chats.find { it.id == message.chatId }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onChatSelect(message.chatId) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VibeVioletPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "Message result",
                                tint = VibeVioletPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = parentChat?.chatName ?: message.senderName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${message.senderName}: ${message.text}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTabContent(
    stories: List<StoryEntity>,
    onStorySelect: (String) -> Unit,
    onMyStatusClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onMyStatusClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(VibeVioletPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Status", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "My Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Tap to add status story", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Recent Updates", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(stories, key = { it.id }) { story ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onStorySelect(story.id) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(VibeCyanSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = story.userName.take(1), fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = story.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = story.textContent ?: "Story update", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "2h ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CallsTabContent(
    callLogs: List<CallLogEntity>,
    users: List<UserEntity>,
    onStartCall: (String, String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Recent Call History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(callLogs, key = { it.id }) { call ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onStartCall(call.contactId, call.contactName, call.callType) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(VibeVioletPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = call.callType,
                            tint = VibeVioletPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = call.contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (call.direction) {
                                    "MISSED" -> Icons.Default.CallMissed
                                    "OUTGOING" -> Icons.Default.CallMade
                                    else -> Icons.Default.CallReceived
                                },
                                contentDescription = call.direction,
                                tint = if (call.direction == "MISSED") Color.Red else VibeCyanSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${call.direction} • ${call.durationSeconds}s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { onStartCall(call.contactId, call.contactName, call.callType) }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = VibeVioletPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsTabContent(
    users: List<UserEntity>,
    onContactClick: (UserEntity) -> Unit,
    onChatClick: (UserEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Contacts (${users.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(users, key = { it.id }) { user ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onChatClick(user) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(VibeVioletPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = user.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    IconButton(onClick = { onContactClick(user) }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = VibeVioletPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    settings: AppSettingsEntity?,
    users: List<UserEntity>,
    onEditProfileClick: () -> Unit,
    onArchivedClick: () -> Unit,
    onUpdateSettings: (AppSettingsEntity) -> Unit
) {
    val myProfile = users.find { it.id == "my_user_id" }
    var currentSettings by remember(settings) { mutableStateOf(settings ?: AppSettingsEntity()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onEditProfileClick() },
                color = VibeVioletPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(VibeVioletPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = myProfile?.name ?: "Alex Rivera", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = myProfile?.phoneNumber ?: "+1 (555) 019-2834", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = myProfile?.bio ?: "Building the future of messaging", style = MaterialTheme.typography.bodySmall, color = VibeCyanSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Edit Profile", tint = VibeVioletPrimary)
                }
            }
        }

        item {
            Text(text = "Preferences & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            SettingSwitchRow("Auto-Reply Assistant", "Send instant automatic replies when away", currentSettings.autoReplyEnabled) { enabled ->
                currentSettings = currentSettings.copy(autoReplyEnabled = enabled)
                onUpdateSettings(currentSettings)
            }
        }

        item {
            SettingSwitchRow("App Lock (PIN Security)", "Require 4-digit PIN code to open secret chats", currentSettings.appLockEnabled) { enabled ->
                currentSettings = currentSettings.copy(appLockEnabled = enabled)
                onUpdateSettings(currentSettings)
            }
        }

        item {
            SettingSwitchRow("Read Receipts", "Show blue double checkmarks when messages are read", currentSettings.readReceipts) { enabled ->
                currentSettings = currentSettings.copy(readReceipts = enabled)
                onUpdateSettings(currentSettings)
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onArchivedClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Archive, contentDescription = "Archived", tint = VibeVioletPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Archived Chats Vault", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open")
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VibeVioletPrimary)
            )
        }
    }
}

@Composable
fun QuickActionRowItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(VibeVioletPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = VibeVioletPrimary)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
