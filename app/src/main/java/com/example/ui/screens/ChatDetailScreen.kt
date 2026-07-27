package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.CreateEventDialog
import com.example.ui.components.CreatePollDialog
import com.example.ui.components.E2EEInfoDialog
import com.example.ui.components.MessageBubble
import com.example.ui.components.TypingIndicatorBubble
import com.example.ui.components.TypingIndicatorDots
import com.example.ui.theme.VibeCyanSecondary
import com.example.ui.theme.VibeEmeraldOnline
import com.example.ui.theme.VibeVioletPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatEntity?,
    messages: List<MessageEntity>,
    onBackClick: () -> Unit,
    onSendMessage: (String, String, String?, Int, String?, String?) -> Unit,
    onSendPoll: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit = { _, _, _ -> },
    onSendEvent: (title: String, description: String, dateTime: String, location: String) -> Unit = { _, _, _, _ -> },
    onPollVote: (messageId: String, optionId: String) -> Unit = { _, _ -> },
    onEventRsvp: (messageId: String, rsvpType: String) -> Unit = { _, _ -> },
    onStartCall: (String, String, String) -> Unit,
    onPinToggle: () -> Unit,
    typingUserName: String? = null,
    onUserTyping: (String) -> Unit = {},
    aiSummary: String? = null,
    aiSmartReplies: List<String> = emptyList(),
    isAiThinking: Boolean = false,
    onGenerateSmartReplies: () -> Unit = {},
    onSummarizeChat: () -> Unit = {},
    onClearAiSummary: () -> Unit = {},
    onPolishDraft: (text: String, style: String, onResult: (String) -> Unit) -> Unit = { _, _, _ -> }
) {
    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCreatePollDialog by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showE2EEInfoDialog by remember { mutableStateOf(false) }
    var showPolishMenu by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableIntStateOf(0) }

    var isSearchActive by remember { mutableStateOf(false) }
    var inChatSearchQuery by remember { mutableStateOf("") }

    val filteredMessages = remember(messages, inChatSearchQuery, isSearchActive) {
        if (isSearchActive && inChatSearchQuery.trim().isNotBlank()) {
            val query = inChatSearchQuery.trim().lowercase()
            messages.filter { msg ->
                msg.text.lowercase().contains(query) ||
                msg.senderName.lowercase().contains(query) ||
                (msg.pollDataJson?.lowercase()?.contains(query) == true) ||
                (msg.eventDataJson?.lowercase()?.contains(query) == true)
            }
        } else {
            messages
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to bottom when new messages arrive or typing indicator appears
    LaunchedEffect(filteredMessages.size, typingUserName) {
        if (!isSearchActive) {
            val totalItems = filteredMessages.size + (if (typingUserName != null) 1 else 0)
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    LaunchedEffect(chat?.id) {
        if (chat != null && chat.id != "chat_vibe_ai") {
            onGenerateSmartReplies()
        }
    }

    // Voice recording timer simulation
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingTimer = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingTimer++
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = inChatSearchQuery,
                            onValueChange = { inChatSearchQuery = it },
                            placeholder = { Text("Search message history...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("in_chat_search_input"),
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = {
                                if (inChatSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { inChatSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibeVioletPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                inChatSearchQuery = ""
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Exit search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showE2EEInfoDialog = true }
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(if (chat?.isSecret == true) VibeVioletPrimary.copy(alpha = 0.2f) else VibeVioletPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chat?.chatName?.take(1) ?: "C",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (chat?.isGroup == false && chat.isSecret != true) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(11.dp)
                                            .background(VibeEmeraldOnline, CircleShape)
                                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = chat?.chatName ?: "Chat",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                if (typingUserName != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$typingUserName is typing ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VibeVioletPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        TypingIndicatorDots(dotSize = 3.5f, dotColor = VibeVioletPrimary)
                                    }
                                } else {
                                    Text(
                                        text = if (chat?.isSecret == true) "End-to-End Encrypted 🔒" else if (chat?.isGroup == true) "Group • ${chat.participantIds.split(',').size} members" else "Online now",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VibeCyanSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("chat_back_btn")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { onSummarizeChat() },
                            modifier = Modifier.testTag("chat_ai_summarize_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Summarize", tint = VibeVioletPrimary)
                        }
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("chat_search_btn")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search messages", tint = VibeVioletPrimary)
                        }
                        IconButton(
                            onClick = { onStartCall(chat?.id ?: "", chat?.chatName ?: "", "VOICE") },
                            modifier = Modifier.testTag("chat_voice_call_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = VibeVioletPrimary)
                        }
                        IconButton(
                            onClick = { onStartCall(chat?.id ?: "", chat?.chatName ?: "", "VIDEO") },
                            modifier = Modifier.testTag("chat_video_call_btn")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = VibeVioletPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Chat Wallpaper
            Image(
                painter = painterResource(id = R.drawable.img_chat_wallpaper),
                contentDescription = "Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Search result count bar
                if (isSearchActive && inChatSearchQuery.trim().isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Found ${filteredMessages.size} ${if (filteredMessages.size == 1) "result" else "results"} for \"$inChatSearchQuery\"",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(
                                onClick = { inChatSearchQuery = "" },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Message List
                if (filteredMessages.isEmpty() && isSearchActive && inChatSearchQuery.trim().isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No results",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No messages found matching \"$inChatSearchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { message ->
                            val isMyMessage = (message.senderId == "my_user_id")
                            MessageBubble(
                                message = message,
                                isMyMessage = isMyMessage,
                                onReplyClick = { replyingToMessage = message },
                                onPollVote = onPollVote,
                                onEventRsvp = onEventRsvp
                            )
                        }

                        if (typingUserName != null && !isSearchActive) {
                            item(key = "typing_indicator_item") {
                                TypingIndicatorBubble(typingUserName = typingUserName)
                            }
                        }
                    }
                }

                // Reply Draft Banner
                if (replyingToMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = "Replying", tint = VibeVioletPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${replyingToMessage?.senderName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VibeVioletPrimary
                                )
                                Text(
                                    text = replyingToMessage?.text ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { replyingToMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply")
                            }
                        }
                    }
                }

                // Voice Recording Live Banner
                AnimatedVisibility(visible = isRecordingVoice) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Recording Voice Note... 0:${if (recordingTimer < 10) "0" else ""}$recordingTimer",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = {
                                    isRecordingVoice = false
                                    onSendMessage("VOICE", "Voice Note (${recordingTimer}s)", null, recordingTimer, null, null)
                                }
                            ) {
                                Text("Send Voice", color = VibeVioletPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // AI Smart Reply Chips Row
                if (aiSmartReplies.isNotEmpty() && chat?.id != "chat_vibe_ai" && !isSearchActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ Quick AI Reply: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = VibeVioletPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                aiSmartReplies.take(3).forEach { replyText ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { inputText = replyText },
                                        label = { Text(replyText, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                            IconButton(onClick = { onGenerateSmartReplies() }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Regenerate AI Replies",
                                    tint = VibeVioletPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Input Bar Container
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAttachmentSheet = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Attach", tint = VibeVioletPrimary)
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibeVioletPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            maxLines = 4,
                            trailingIcon = {
                                if (inputText.isNotBlank()) {
                                    Box {
                                        IconButton(onClick = { showPolishMenu = true }) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = "Polish with AI",
                                                tint = VibeVioletPrimary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showPolishMenu,
                                            onDismissRequest = { showPolishMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("✨ Friendly Tone") },
                                                onClick = {
                                                    showPolishMenu = false
                                                    onPolishDraft(inputText, "friendly and casual") { polished ->
                                                        inputText = polished
                                                    }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("💼 Professional Tone") },
                                                onClick = {
                                                    showPolishMenu = false
                                                    onPolishDraft(inputText, "formal and professional") { polished ->
                                                        inputText = polished
                                                    }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("⚡ Concise & Short") },
                                                onClick = {
                                                    showPolishMenu = false
                                                    onPolishDraft(inputText, "concise and direct") { polished ->
                                                        inputText = polished
                                                    }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("🔥 Enthusiastic Vibe") },
                                                onClick = {
                                                    showPolishMenu = false
                                                    onPolishDraft(inputText, "enthusiastic and energetic") { polished ->
                                                        inputText = polished
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val textToSend = inputText
                                    inputText = ""
                                    val replyId = replyingToMessage?.id
                                    val replyText = replyingToMessage?.text
                                    replyingToMessage = null
                                    onSendMessage("TEXT", textToSend, null, 0, replyId, replyText)
                                },
                                modifier = Modifier
                                    .testTag("chat_send_btn")
                                    .background(VibeVioletPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        } else {
                            IconButton(
                                onClick = { isRecordingVoice = !isRecordingVoice },
                                modifier = Modifier
                                    .testTag("chat_voice_record_btn")
                                    .background(if (isRecordingVoice) Color.Red else VibeVioletPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Record", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Attachment Modal Bottom Sheet
            if (showAttachmentSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAttachmentSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(text = "Share Attachment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachmentIconButton("Poll", Icons.Default.Poll) {
                                showAttachmentSheet = false
                                showCreatePollDialog = true
                            }
                            AttachmentIconButton("Event", Icons.Default.Event) {
                                showAttachmentSheet = false
                                showCreateEventDialog = true
                            }
                            AttachmentIconButton("Image", Icons.Default.Image) {
                                showAttachmentSheet = false
                                onSendMessage("IMAGE", "Photo attachment", null, 0, null, null)
                            }
                            AttachmentIconButton("Location", Icons.Default.LocationOn) {
                                showAttachmentSheet = false
                                onSendMessage("LOCATION", "Location: Design Studio HQ (37.7749, -122.4194)", null, 0, null, null)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachmentIconButton("Document", Icons.Default.InsertDriveFile) {
                                showAttachmentSheet = false
                                onSendMessage("DOCUMENT", "Vibe_Design_Specs.pdf (1.2 MB)", null, 0, null, null)
                            }
                            AttachmentIconButton("Security", Icons.Default.Lock) {
                                showAttachmentSheet = false
                                showE2EEInfoDialog = true
                            }
                            AttachmentIconButton("Schedule", Icons.Default.Schedule) {
                                showAttachmentSheet = false
                                onSendMessage("TEXT", "⏰ Scheduled Reminder: Team Sync at 3:00 PM", null, 0, null, null)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Dialogs
            if (showCreatePollDialog) {
                CreatePollDialog(
                    onDismiss = { showCreatePollDialog = false },
                    onCreatePoll = { question, options, allowMultiple ->
                        onSendPoll(question, options, allowMultiple)
                    }
                )
            }

            if (showCreateEventDialog) {
                CreateEventDialog(
                    onDismiss = { showCreateEventDialog = false },
                    onCreateEvent = { title, desc, dateTime, location ->
                        onSendEvent(title, desc, dateTime, location)
                    }
                )
            }

            if (showE2EEInfoDialog) {
                E2EEInfoDialog(
                    contactName = chat?.chatName ?: "Chat",
                    contactId = chat?.id ?: "chat_1",
                    onDismiss = { showE2EEInfoDialog = false }
                )
            }

            if (aiSummary != null) {
                AlertDialog(
                    onDismissRequest = { onClearAiSummary() },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VibeVioletPrimary) },
                    title = { Text("✨ AI Chat Summary", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                text = "Gemini 3.5 Flash summarized the recent messages in this conversation:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { onClearAiSummary() },
                            colors = ButtonDefaults.buttonColors(containerColor = VibeVioletPrimary)
                        ) {
                            Text("Got it")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AttachmentIconButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(54.dp)
                .background(VibeVioletPrimary.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = VibeVioletPrimary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
