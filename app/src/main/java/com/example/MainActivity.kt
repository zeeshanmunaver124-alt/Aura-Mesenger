package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.VibeTheme
import com.example.viewmodel.VibeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VibeViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            val isDarkTheme = when (settings?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            VibeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VibeAppHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VibeAppHost(viewModel: VibeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()
    val selectedStoryId by viewModel.selectedStoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.chatFilter.collectAsStateWithLifecycle()

    val activeChats by viewModel.activeChats.collectAsStateWithLifecycle()
    val archivedChats by viewModel.archivedChats.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val isSecretUnlocked by viewModel.isSecretUnlocked.collectAsStateWithLifecycle()
    val globalMessageSearchResults by viewModel.globalMessageSearchResults.collectAsStateWithLifecycle()
    val typingStates by viewModel.typingStates.collectAsStateWithLifecycle()
    val aiSmartReplies by viewModel.aiSmartReplies.collectAsStateWithLifecycle()
    val aiSummary by viewModel.aiSummary.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    var showSecretLockModal by remember { mutableStateOf(false) }
    var pendingSecretChatId by remember { mutableStateOf<String?>(null) }

    // Active Call Interceptor
    if (activeCall != null) {
        ActiveCallScreen(
            callState = activeCall!!,
            onMuteToggle = { viewModel.toggleMute() },
            onSpeakerToggle = { viewModel.toggleSpeaker() },
            onVideoToggle = { viewModel.toggleVideo() },
            onEndCall = { viewModel.endCall() }
        )
        return
    }

    // Secret Lock Dialog
    if (showSecretLockModal) {
        SecretLockDialog(
            onDismiss = {
                showSecretLockModal = false
                pendingSecretChatId = null
            },
            onPinSubmitted = { pin ->
                val success = viewModel.unlockSecret(pin)
                if (success) {
                    showSecretLockModal = false
                    if (pendingSecretChatId != null) {
                        viewModel.selectChat(pendingSecretChatId)
                        viewModel.setScreen("chat_detail")
                        pendingSecretChatId = null
                    }
                }
                success
            }
        )
    }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            "splash" -> {
                SplashScreen(
                    onSplashFinished = { viewModel.setScreen("welcome") }
                )
            }

            "welcome" -> {
                WelcomeScreen(
                    onContinueClick = { viewModel.setScreen("login") }
                )
            }

            "login" -> {
                LoginScreen(
                    onOtpVerified = { viewModel.setScreen("profile_setup") }
                )
            }

            "profile_setup" -> {
                ProfileSetupScreen(
                    onSetupComplete = { name, bio, status ->
                        viewModel.updateProfile(name, bio, "+1 (555) 019-2834", status)
                        viewModel.setScreen("main")
                    }
                )
            }

            "chat_detail" -> {
                ChatDetailScreen(
                    chat = selectedChat,
                    messages = currentMessages,
                    onBackClick = {
                        viewModel.selectChat(null)
                        viewModel.setScreen("main")
                    },
                    onSendMessage = { mediaType, text, url, duration, replyId, replyText ->
                        if (selectedChatId != null) {
                            viewModel.sendMessage(
                                chatId = selectedChatId!!,
                                text = text,
                                mediaType = mediaType,
                                mediaUrl = url,
                                voiceDuration = duration,
                                replyToId = replyId,
                                replyToText = replyText
                            )
                        }
                    },
                    onSendPoll = { question, options, allowMultiple ->
                        if (selectedChatId != null) {
                            viewModel.sendPoll(selectedChatId!!, question, options, allowMultiple)
                        }
                    },
                    onSendEvent = { title, desc, dateTime, location ->
                        if (selectedChatId != null) {
                            viewModel.sendEvent(selectedChatId!!, title, desc, dateTime, location)
                        }
                    },
                    onPollVote = { msgId, optId ->
                        viewModel.voteOnPoll(msgId, optId)
                    },
                    onEventRsvp = { msgId, rsvpType ->
                        viewModel.rsvpToEvent(msgId, rsvpType)
                    },
                    onStartCall = { _, name, callType ->
                        viewModel.startCall("user_1", name, callType)
                    },
                    onPinToggle = {
                        if (selectedChatId != null) viewModel.togglePinChat(selectedChatId!!)
                    },
                    typingUserName = selectedChatId?.let { typingStates[it] },
                    onUserTyping = { chatId ->
                        val chat = activeChats.find { it.id == chatId }
                        if (chat != null) {
                            viewModel.triggerSimulatedTyping(chatId, chat.chatName, delayMs = 1200L, durationMs = 3000L)
                        }
                    },
                    aiSummary = aiSummary,
                    aiSmartReplies = aiSmartReplies,
                    isAiThinking = isAiThinking,
                    onGenerateSmartReplies = { viewModel.generateSmartRepliesForCurrentChat() },
                    onSummarizeChat = { viewModel.summarizeCurrentChat() },
                    onClearAiSummary = { viewModel.clearAiSummary() },
                    onPolishDraft = { text, style, onResult ->
                        viewModel.polishMessageDraft(text, style, onResult)
                    }
                )
            }

            "story_viewer" -> {
                val selectedStory = stories.find { it.id == selectedStoryId }
                StoryViewerScreen(
                    story = selectedStory,
                    onCloseClick = {
                        viewModel.selectStory(null)
                        viewModel.setScreen("main")
                    },
                    onReplyStory = { replyText ->
                        val existingChat = activeChats.firstOrNull { !it.isGroup }
                        if (existingChat != null) {
                            viewModel.sendMessage(
                                chatId = existingChat.id,
                                text = "Replied to story: $replyText"
                            )
                        }
                    }
                )
            }

            "create_group" -> {
                CreateGroupScreen(
                    users = users,
                    onBackClick = { viewModel.setScreen("main") },
                    onCreateGroup = { name, desc, memberIds ->
                        viewModel.createGroup(name, desc, memberIds)
                        viewModel.setScreen("main")
                    }
                )
            }

            "archived" -> {
                ArchivedChatsScreen(
                    archivedChats = archivedChats,
                    onBackClick = { viewModel.setScreen("main") },
                    onChatSelect = { chatId ->
                        viewModel.selectChat(chatId)
                        viewModel.setScreen("chat_detail")
                    }
                )
            }

            "edit_profile" -> {
                val myUser = users.find { it.id == "my_user_id" }
                EditProfileScreen(
                    user = myUser,
                    onBackClick = { viewModel.setScreen("main") },
                    onSaveProfile = { name, bio, phone, statusMsg ->
                        viewModel.updateProfile(name, bio, phone, statusMsg)
                    }
                )
            }

            else -> { // "main" screen
                HomeScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> viewModel.setSelectedTab(tab) },
                    chats = activeChats,
                    stories = stories,
                    callLogs = callLogs,
                    users = users,
                    settings = settings,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    activeFilter = activeFilter,
                    onFilterChange = { viewModel.setChatFilter(it) },
                    messageSearchResults = globalMessageSearchResults,
                    typingStates = typingStates,
                    onChatSelect = { chatId ->
                        val chat = activeChats.find { it.id == chatId }
                        if (chat?.isSecret == true && !isSecretUnlocked) {
                            pendingSecretChatId = chatId
                            showSecretLockModal = true
                        } else {
                            viewModel.selectChat(chatId)
                            viewModel.setScreen("chat_detail")
                        }
                    },
                    onStorySelect = { storyId ->
                        viewModel.selectStory(storyId)
                        viewModel.setScreen("story_viewer")
                    },
                    onStartCall = { contactId, name, callType ->
                        viewModel.startCall(contactId, name, callType)
                    },
                    onCreateGroupClick = { viewModel.setScreen("create_group") },
                    onNewChatClick = {
                        val firstContactChat = activeChats.firstOrNull()
                        if (firstContactChat != null) {
                            viewModel.selectChat(firstContactChat.id)
                            viewModel.setScreen("chat_detail")
                        }
                    },
                    onSecretChatClick = {
                        val secretChat = activeChats.find { it.isSecret }
                        if (secretChat != null) {
                            if (!isSecretUnlocked) {
                                pendingSecretChatId = secretChat.id
                                showSecretLockModal = true
                            } else {
                                viewModel.selectChat(secretChat.id)
                                viewModel.setScreen("chat_detail")
                            }
                        }
                    },
                    onArchivedClick = { viewModel.setScreen("archived") },
                    onEditProfileClick = { viewModel.setScreen("edit_profile") },
                    onPostStoryClick = {
                        viewModel.postStory("Having an amazing day testing Vibe Messenger! ✨💜", "0xFF6C5CE7")
                    },
                    onUpdateSettings = { updatedSettings ->
                        viewModel.updateSettings(updatedSettings)
                    }
                )
            }
        }
    }
}
