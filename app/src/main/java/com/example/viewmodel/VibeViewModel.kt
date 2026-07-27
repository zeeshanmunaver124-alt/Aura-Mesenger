package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VibeRepository
import com.example.data.ai.GeminiApiClient
import com.example.data.local.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveCallState(
    val contactId: String = "",
    val contactName: String = "",
    val contactAvatar: String = "",
    val callType: String = "VOICE", // VOICE, VIDEO
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val durationSeconds: Int = 0
)

class VibeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = VibeRepository(db.vibeDao())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Navigation & Auth Flow State
    private val _currentScreen = MutableStateFlow("main") // splash, welcome, login, otp, profile_setup, main
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow("chats") // chats, status, calls, contacts, settings
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _selectedStoryId = MutableStateFlow<String?>(null)
    val selectedStoryId: StateFlow<String?> = _selectedStoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _chatFilter = MutableStateFlow("ALL") // ALL, UNREAD, GROUPS, SECRET
    val chatFilter: StateFlow<String> = _chatFilter.asStateFlow()

    // Secret Lock State
    private val _isSecretUnlocked = MutableStateFlow(false)
    val isSecretUnlocked: StateFlow<Boolean> = _isSecretUnlocked.asStateFlow()

    // Real-Time Typing Indicator State (chatId -> typingUserName or null)
    private val _typingStates = MutableStateFlow<Map<String, String?>>(emptyMap())
    val typingStates: StateFlow<Map<String, String?>> = _typingStates.asStateFlow()

    // Gemini AI Features State
    private val _aiSmartReplies = MutableStateFlow<List<String>>(emptyList())
    val aiSmartReplies: StateFlow<List<String>> = _aiSmartReplies.asStateFlow()

    private val _aiSummary = MutableStateFlow<String?>(null)
    val aiSummary: StateFlow<String?> = _aiSummary.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Active Call State
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    // Data Flows from Repository
    val activeChats: StateFlow<List<ChatEntity>> = repository.activeChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedChats: StateFlow<List<ChatEntity>> = repository.archivedChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.stories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.callLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Messages for selected chat
    val currentMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { chatId ->
            if (chatId != null) repository.getMessagesForChat(chatId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedChat: StateFlow<ChatEntity?> = _selectedChatId
        .flatMapLatest { chatId ->
            if (chatId != null) repository.getChatByIdFlow(chatId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val globalMessageSearchResults: StateFlow<List<MessageEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.trim().length >= 2) {
                repository.searchAllMessages(query.trim())
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Actions
    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChatFilter(filter: String) {
        _chatFilter.value = filter
    }

    fun setTypingState(chatId: String, userName: String?) {
        _typingStates.value = _typingStates.value.toMutableMap().apply {
            if (userName == null) remove(chatId) else put(chatId, userName)
        }
    }

    fun triggerSimulatedTyping(
        chatId: String,
        partnerName: String,
        delayMs: Long = 1000L,
        durationMs: Long = 3200L
    ) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(delayMs)
            setTypingState(chatId, partnerName)
            kotlinx.coroutines.delay(durationMs)
            setTypingState(chatId, null)
        }
    }

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        if (chatId != null) {
            // Trigger a simulated live typing session when opening a chat session
            viewModelScope.launch {
                val chat = activeChats.value.find { it.id == chatId }
                val partnerName = when {
                    chat?.isGroup == true -> "Sophia Chen"
                    chat?.chatName != null -> chat.chatName
                    else -> "Elena Rostova"
                }
                triggerSimulatedTyping(chatId = chatId, partnerName = partnerName, delayMs = 1200L, durationMs = 3500L)
            }
        }
    }

    fun selectStory(storyId: String?) {
        _selectedStoryId.value = storyId
    }

    fun sendMessage(
        chatId: String,
        text: String,
        mediaType: String = "TEXT",
        mediaUrl: String? = null,
        voiceDuration: Int = 0,
        replyToId: String? = null,
        replyToText: String? = null
    ) {
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                voiceDuration = voiceDuration,
                replyToId = replyToId,
                replyToText = replyToText
            )

            if (chatId == "chat_vibe_ai") {
                // Live Gemini AI response for Vibe AI Assistant chat
                setTypingState("chat_vibe_ai", "Vibe AI Assistant")
                _isAiThinking.value = true
                val aiResponseText = GeminiApiClient.generateContent(
                    prompt = text,
                    systemInstruction = "You are Vibe AI Assistant, a helpful and friendly AI integrated into Vibe Messenger. Be concise, engaging, and clear."
                )
                setTypingState("chat_vibe_ai", null)
                _isAiThinking.value = false

                repository.receiveAiMessage(
                    chatId = "chat_vibe_ai",
                    senderId = "vibe_ai_user",
                    senderName = "Vibe AI Assistant",
                    text = aiResponseText
                )
            } else {
                // Trigger partner typing indicator and automated response
                val chat = activeChats.value.find { it.id == chatId }
                val partnerName = chat?.chatName ?: "Elena Rostova"
                triggerSimulatedTyping(chatId = chatId, partnerName = partnerName, delayMs = 800L, durationMs = 3000L)
            }
        }
    }

    fun generateSmartRepliesForCurrentChat() {
        val messages = currentMessages.value
        if (messages.isEmpty()) return

        viewModelScope.launch {
            _isAiThinking.value = true
            val contextText = messages.takeLast(8).joinToString("\n") { "${it.senderName}: ${it.text}" }
            val replies = GeminiApiClient.generateSmartReplies(contextText)
            _aiSmartReplies.value = replies
            _isAiThinking.value = false
        }
    }

    fun summarizeCurrentChat() {
        val messages = currentMessages.value
        if (messages.isEmpty()) return

        viewModelScope.launch {
            _isAiThinking.value = true
            val contextText = messages.takeLast(15).joinToString("\n") { "${it.senderName}: ${it.text}" }
            val summaryText = GeminiApiClient.summarizeConversation(contextText)
            _aiSummary.value = summaryText
            _isAiThinking.value = false
        }
    }

    fun clearAiSummary() {
        _aiSummary.value = null
    }

    fun polishMessageDraft(text: String, style: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            val polished = GeminiApiClient.polishMessage(text, style)
            _isAiThinking.value = false
            onResult(polished)
        }
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun toggleArchiveChat(chatId: String) {
        viewModelScope.launch {
            repository.toggleArchiveChat(chatId)
        }
    }

    fun createGroup(name: String, description: String, selectedMemberIds: List<String>) {
        viewModelScope.launch {
            repository.createGroup(name, description, selectedMemberIds)
        }
    }

    fun postStory(textContent: String, colorHex: String) {
        viewModelScope.launch {
            repository.postStory(textContent, colorHex)
        }
    }

    fun startCall(contactId: String, contactName: String, callType: String) {
        viewModelScope.launch {
            repository.logCall(contactId, contactName, callType, "OUTGOING")
            _activeCall.value = ActiveCallState(
                contactId = contactId,
                contactName = contactName,
                callType = callType,
                isVideoEnabled = (callType == "VIDEO")
            )
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun endCall() {
        _activeCall.value = null
    }

    fun unlockSecret(pin: String): Boolean {
        val currentPin = settings.value?.pinCode ?: "1234"
        if (pin == currentPin) {
            _isSecretUnlocked.value = true
            return true
        }
        return false
    }

    fun updateSettings(updatedSettings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(updatedSettings)
        }
    }

    fun sendPoll(chatId: String, question: String, optionTexts: List<String>, allowMultiple: Boolean = false) {
        viewModelScope.launch {
            repository.sendPoll(chatId, question, optionTexts, allowMultiple)
        }
    }

    fun voteOnPoll(messageId: String, optionId: String) {
        viewModelScope.launch {
            repository.voteOnPoll(messageId, optionId)
        }
    }

    fun sendEvent(chatId: String, title: String, description: String, dateTime: String, location: String) {
        viewModelScope.launch {
            repository.sendEvent(chatId, title, description, dateTime, location)
        }
    }

    fun rsvpToEvent(messageId: String, rsvpType: String) {
        viewModelScope.launch {
            repository.rsvpToEvent(messageId, rsvpType)
        }
    }

    fun updateProfile(name: String, bio: String, phone: String, statusMsg: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, bio, phone, statusMsg)
        }
    }
}
