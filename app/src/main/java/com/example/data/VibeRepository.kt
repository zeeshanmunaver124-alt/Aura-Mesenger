package com.example.data

import com.example.data.local.*
import com.example.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class VibeRepository(private val dao: VibeDao) {

    val users: Flow<List<UserEntity>> = dao.getAllUsers()
    val activeChats: Flow<List<ChatEntity>> = dao.getActiveChats()
    val archivedChats: Flow<List<ChatEntity>> = dao.getArchivedChats()
    val stories: Flow<List<StoryEntity>> = dao.getAllStories()
    val callLogs: Flow<List<CallLogEntity>> = dao.getAllCallLogs()
    val settings: Flow<AppSettingsEntity?> = dao.getSettingsFlow()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return dao.getMessagesForChat(chatId)
    }

    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?> {
        return dao.getChatByIdFlow(chatId)
    }

    suspend fun seedInitialDataIfNeeded() {
        val existingSettings = dao.getSettings()
        if (existingSettings == null) {
            // Default App Settings
            dao.saveSettings(
                AppSettingsEntity(
                    id = 1,
                    themeMode = "DARK",
                    appLockEnabled = false,
                    pinCode = "1234",
                    autoReplyEnabled = true,
                    autoReplyText = "Hey! Thanks for messaging. I am testing Vibe Messenger right now!",
                    readReceipts = true,
                    lastSeenPrivacy = "EVERYONE"
                )
            )

            // Current User Profile
            val myUser = UserEntity(
                id = "my_user_id",
                name = "Alex Rivera",
                phoneNumber = "+1 (555) 019-2834",
                email = "alex.rivera@vibeapp.io",
                bio = "Building the future of messaging 🚀",
                statusMessage = "Available for chats",
                isOnline = true
            )

            // Initial Contacts
            val contact1 = UserEntity(
                id = "user_1",
                name = "Elena Rostova",
                phoneNumber = "+1 (555) 234-5678",
                email = "elena@vibeapp.io",
                bio = "UI/UX Designer & Mobile Enthusiast ✨",
                statusMessage = "At the coffee shop",
                isOnline = true,
                isFavorite = true
            )
            val contact2 = UserEntity(
                id = "user_2",
                name = "Marcus Vance",
                phoneNumber = "+1 (555) 345-6789",
                email = "marcus@vibeapp.io",
                bio = "Software Engineer | Music Lover 🎧",
                statusMessage = "Coding non-stop",
                isOnline = false,
                lastSeen = "Last seen 12 mins ago"
            )
            val contact3 = UserEntity(
                id = "user_3",
                name = "Sophia Chen",
                phoneNumber = "+1 (555) 456-7890",
                email = "sophia@vibeapp.io",
                bio = "Photography & Traveling 📸",
                statusMessage = "Exploring Tokyo",
                isOnline = true,
                isFavorite = true
            )
            val contact4 = UserEntity(
                id = "user_4",
                name = "David Miller",
                phoneNumber = "+1 (555) 567-8901",
                email = "david@vibeapp.io",
                bio = "Product Manager @ Vibe Tech",
                statusMessage = "In a meeting",
                isOnline = false,
                lastSeen = "Last seen today at 10:15 AM"
            )
            val vibeAiUser = UserEntity(
                id = "vibe_ai_user",
                name = "Vibe AI Assistant",
                phoneNumber = "+1 (800) GEMINI-AI",
                email = "ai@vibeapp.io",
                bio = "Powered by Gemini 3.5 Flash ✨",
                statusMessage = "Ready to assist you 24/7",
                isOnline = true,
                isFavorite = true
            )

            dao.insertUsers(listOf(myUser, contact1, contact2, contact3, contact4, vibeAiUser))

            // Initial Chats
            val now = System.currentTimeMillis()
            val chat1 = ChatEntity(
                id = "chat_1",
                participantIds = "my_user_id,user_1",
                isGroup = false,
                chatName = "Elena Rostova",
                lastMessage = "Did you check out the new dark design palette?",
                lastMessageTime = now - 1000 * 60 * 5, // 5 mins ago
                unreadCount = 2,
                isPinned = true
            )
            val chat2 = ChatEntity(
                id = "chat_2",
                participantIds = "my_user_id,user_2",
                isGroup = false,
                chatName = "Marcus Vance",
                lastMessage = "Voice Note (0:14)",
                lastMessageTime = now - 1000 * 60 * 35, // 35 mins ago
                unreadCount = 0,
                isPinned = true
            )
            val chat3 = ChatEntity(
                id = "chat_group_1",
                participantIds = "my_user_id,user_1,user_2,user_3",
                isGroup = true,
                chatName = "🚀 Vibe Product Team",
                groupDescription = "Official design & product discussion squad for Vibe Messenger",
                lastMessage = "Sophia: Shared location at Design Studio",
                lastMessageTime = now - 1000 * 60 * 120, // 2 hours ago
                unreadCount = 5,
                isPinned = false
            )
            val chat4 = ChatEntity(
                id = "chat_secret_1",
                participantIds = "my_user_id,user_3",
                isGroup = false,
                chatName = "Sophia Chen (Secret)",
                lastMessage = "🔒 End-to-end encrypted secret chat",
                lastMessageTime = now - 1000 * 60 * 240,
                unreadCount = 0,
                isPinned = false,
                isSecret = true
            )

            val chatAi = ChatEntity(
                id = "chat_vibe_ai",
                participantIds = "my_user_id,vibe_ai_user",
                isGroup = false,
                chatName = "✨ Vibe AI Assistant",
                lastMessage = "Hello Alex! Ask me anything, request a summary, or polish your draft messages!",
                lastMessageTime = now - 1000 * 30, // 30s ago
                unreadCount = 0,
                isPinned = true
            )

            dao.insertChats(listOf(chatAi, chat1, chat2, chat3, chat4))

            // Initial Messages for chat_1 (Elena)
            val msg1 = MessageEntity(
                id = "msg_1_1",
                chatId = "chat_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                text = "Hey Alex! Check out the new Compose layout.",
                timestamp = now - 1000 * 60 * 15,
                status = "READ"
            )
            val msg2 = MessageEntity(
                id = "msg_1_2",
                chatId = "chat_1",
                senderId = "my_user_id",
                senderName = "Alex Rivera",
                text = "It looks incredible! Soft shadows and violet accents match our vibe perfectly.",
                timestamp = now - 1000 * 60 * 10,
                status = "READ",
                reactions = "❤️"
            )
            val msg3 = MessageEntity(
                id = "msg_1_3",
                chatId = "chat_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                text = "Did you check out the new dark design palette?",
                timestamp = now - 1000 * 60 * 5,
                status = "DELIVERED"
            )

            // Messages for chat_2 (Marcus)
            val msg2_1 = MessageEntity(
                id = "msg_2_1",
                chatId = "chat_2",
                senderId = "user_2",
                senderName = "Marcus Vance",
                text = "Sending you the audio recording for the backend sync test.",
                timestamp = now - 1000 * 60 * 40,
                status = "READ"
            )
            val msg2_2 = MessageEntity(
                id = "msg_2_2",
                chatId = "chat_2",
                senderId = "user_2",
                senderName = "Marcus Vance",
                text = "Voice Note",
                timestamp = now - 1000 * 60 * 35,
                status = "READ",
                mediaType = "VOICE",
                voiceDuration = 14
            )

            // Messages for Group Chat
            val msgGroup1 = MessageEntity(
                id = "msg_g_1",
                chatId = "chat_group_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                text = "Sprint planning starts in 10 minutes everyone!",
                timestamp = now - 1000 * 60 * 150,
                status = "READ"
            )
            val msgGroup2 = MessageEntity(
                id = "msg_g_2",
                chatId = "chat_group_1",
                senderId = "user_3",
                senderName = "Sophia Chen",
                text = "Location: Design Studio HQ (37.7749, -122.4194)",
                timestamp = now - 1000 * 60 * 120,
                status = "READ",
                mediaType = "LOCATION"
            )

            // Seed Group Poll
            val seedPollData = PollData(
                question = "Where should we host the Q3 Product Hackathon?",
                options = listOf(
                    PollOptionItem("opt_1", "San Francisco Design Hub", listOf("user_1", "user_3")),
                    PollOptionItem("opt_2", "Tokyo Innovation Center", listOf("my_user_id")),
                    PollOptionItem("opt_3", "Remote VR Metaverse Hub", listOf("user_2"))
                ),
                allowMultiple = false,
                isClosed = false,
                creatorName = "Elena Rostova"
            )
            val msgGroupPoll = MessageEntity(
                id = "msg_g_poll_1",
                chatId = "chat_group_1",
                senderId = "user_1",
                senderName = "Elena Rostova",
                text = "📊 Poll: Where should we host the Q3 Product Hackathon?",
                timestamp = now - 1000 * 60 * 90,
                status = "READ",
                mediaType = "POLL",
                pollDataJson = seedPollData.toJson()
            )

            // Seed Group Event
            val seedEventData = EventData(
                title = "🚀 Vibe 2.0 Architecture Sync & Celebration",
                description = "Join us for the live code demo, architecture review, and team celebration drinks!",
                dateTime = "Friday, Aug 1 • 4:00 PM - 6:00 PM",
                location = "Design Studio HQ (Main Auditorium) & Google Meet",
                organizerName = "Alex Rivera",
                goingUserIds = listOf("my_user_id", "user_1", "user_3"),
                maybeUserIds = listOf("user_2"),
                declinedUserIds = emptyList()
            )
            val msgGroupEvent = MessageEntity(
                id = "msg_g_event_1",
                chatId = "chat_group_1",
                senderId = "my_user_id",
                senderName = "Alex Rivera",
                text = "📅 Event: Vibe 2.0 Architecture Sync & Celebration",
                timestamp = now - 1000 * 60 * 45,
                status = "READ",
                mediaType = "EVENT",
                eventDataJson = seedEventData.toJson()
            )

            val msgAi1 = MessageEntity(
                id = "msg_ai_1",
                chatId = "chat_vibe_ai",
                senderId = "vibe_ai_user",
                senderName = "Vibe AI Assistant",
                text = "Hello Alex! I'm your Vibe AI assistant powered by Gemini 3.5 Flash ✨. Ask me questions, request quick summaries of your chats, or ask me to polish your draft messages!",
                timestamp = now - 1000 * 30,
                status = "READ"
            )

            dao.insertMessages(listOf(msgAi1, msg1, msg2, msg3, msg2_1, msg2_2, msgGroup1, msgGroup2, msgGroupPoll, msgGroupEvent))

            // Initial Stories
            val story1 = StoryEntity(
                id = "story_1",
                userId = "user_1",
                userName = "Elena Rostova",
                userAvatar = "",
                mediaType = "TEXT",
                textContent = "Launching our new Vibe Messenger UI today! 💜✨",
                bgColorHex = "0xFF6C5CE7",
                timestamp = now - 1000 * 60 * 180,
                viewsCount = 14
            )
            val story2 = StoryEntity(
                id = "story_2",
                userId = "user_3",
                userName = "Sophia Chen",
                userAvatar = "",
                mediaType = "TEXT",
                textContent = "Late night coding session in Tokyo 🗼 Tokyo Tower looking stunning!",
                bgColorHex = "0xFF00CEC9",
                timestamp = now - 1000 * 60 * 300,
                viewsCount = 28
            )

            dao.insertStories(listOf(story1, story2))

            // Initial Call Logs
            val call1 = CallLogEntity(
                id = "call_1",
                contactId = "user_1",
                contactName = "Elena Rostova",
                contactAvatar = "",
                callType = "VIDEO",
                direction = "INCOMING",
                timestamp = now - 1000 * 60 * 60,
                durationSeconds = 145
            )
            val call2 = CallLogEntity(
                id = "call_2",
                contactId = "user_2",
                contactName = "Marcus Vance",
                contactAvatar = "",
                callType = "VOICE",
                direction = "MISSED",
                timestamp = now - 1000 * 60 * 240,
                durationSeconds = 0
            )

            dao.insertCallLogs(listOf(call1, call2))
        }
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        mediaType: String = "TEXT",
        mediaUrl: String? = null,
        voiceDuration: Int = 0,
        replyToId: String? = null,
        replyToText: String? = null
    ) {
        val messageId = "msg_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val newMessage = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "my_user_id",
            senderName = "Alex Rivera",
            text = text,
            timestamp = now,
            status = "SENT",
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            voiceDuration = voiceDuration,
            replyToId = replyToId,
            replyToText = replyToText
        )

        dao.insertMessage(newMessage)

        // Update chat's last message
        val chat = dao.getChatById(chatId)
        if (chat != null) {
            val displaySummary = when (mediaType) {
                "VOICE" -> "🎤 Voice Note (${voiceDuration}s)"
                "IMAGE" -> "📷 Image attachment"
                "LOCATION" -> "📍 Location shared"
                "DOCUMENT" -> "📄 Document file"
                else -> text
            }
            dao.updateChat(
                chat.copy(
                    lastMessage = displaySummary,
                    lastMessageTime = now
                )
            )
        }

        // Auto reply simulation if enabled
        val settings = dao.getSettings()
        if (settings?.autoReplyEnabled == true && chat?.isGroup == false) {
            CoroutineScope(Dispatchers.IO).launch {
                kotlinx.coroutines.delay(2000)
                val replyMessage = MessageEntity(
                    id = "msg_reply_${UUID.randomUUID()}",
                    chatId = chatId,
                    senderId = "user_1",
                    senderName = chat?.chatName ?: "Contact",
                    text = settings.autoReplyText,
                    timestamp = System.currentTimeMillis(),
                    status = "DELIVERED"
                )
                dao.insertMessage(replyMessage)
                dao.updateChat(
                    chat.copy(
                        lastMessage = settings.autoReplyText,
                        lastMessageTime = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun receiveAiMessage(chatId: String, senderId: String, senderName: String, text: String) {
        val messageId = "msg_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val newMessage = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = now,
            status = "READ"
        )
        dao.insertMessage(newMessage)

        val chat = dao.getChatById(chatId)
        if (chat != null) {
            dao.updateChat(
                chat.copy(
                    lastMessage = text,
                    lastMessageTime = now,
                    unreadCount = 0
                )
            )
        }
    }

    suspend fun togglePinChat(chatId: String) {
        val chat = dao.getChatById(chatId) ?: return
        dao.updateChat(chat.copy(isPinned = !chat.isPinned))
    }

    suspend fun toggleArchiveChat(chatId: String) {
        val chat = dao.getChatById(chatId) ?: return
        dao.updateChat(chat.copy(isArchived = !chat.isArchived))
    }

    suspend fun addReaction(messageId: String, reaction: String) {
        // Find message across chats or dao update
    }

    suspend fun createGroup(name: String, description: String, selectedMemberIds: List<String>) {
        val groupId = "chat_group_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val newGroup = ChatEntity(
            id = groupId,
            participantIds = (listOf("my_user_id") + selectedMemberIds).joinToString(","),
            isGroup = true,
            chatName = name,
            groupDescription = description,
            lastMessage = "Group created",
            lastMessageTime = now,
            adminIds = "my_user_id"
        )
        dao.insertChat(newGroup)
    }

    suspend fun postStory(textContent: String, bgColorHex: String = "0xFF6C5CE7") {
        val storyId = "story_${UUID.randomUUID()}"
        val newStory = StoryEntity(
            id = storyId,
            userId = "my_user_id",
            userName = "Alex Rivera",
            userAvatar = "",
            mediaType = "TEXT",
            textContent = textContent,
            bgColorHex = bgColorHex,
            timestamp = System.currentTimeMillis()
        )
        dao.insertStory(newStory)
    }

    suspend fun logCall(contactId: String, contactName: String, callType: String, direction: String) {
        val callLog = CallLogEntity(
            id = "call_${UUID.randomUUID()}",
            contactId = contactId,
            contactName = contactName,
            contactAvatar = "",
            callType = callType,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSeconds = if (direction == "MISSED") 0 else (10..300).random()
        )
        dao.insertCallLog(callLog)
    }

    suspend fun updateSettings(updated: AppSettingsEntity) {
        dao.saveSettings(updated)
    }

    suspend fun sendPoll(
        chatId: String,
        question: String,
        optionTexts: List<String>,
        allowMultiple: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val messageId = "msg_poll_${UUID.randomUUID()}"
        val options = optionTexts.mapIndexed { idx, optText ->
            PollOptionItem(id = "opt_$idx", text = optText, voterUserIds = emptyList())
        }
        val pollData = PollData(
            question = question,
            options = options,
            allowMultiple = allowMultiple,
            isClosed = false,
            creatorName = "Alex Rivera"
        )
        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "my_user_id",
            senderName = "Alex Rivera",
            text = "📊 Poll: $question",
            timestamp = now,
            status = "SENT",
            mediaType = "POLL",
            pollDataJson = pollData.toJson()
        )
        dao.insertMessage(message)

        val chat = dao.getChatById(chatId)
        if (chat != null) {
            dao.updateChat(chat.copy(lastMessage = "📊 Poll: $question", lastMessageTime = now))
        }
    }

    suspend fun voteOnPoll(messageId: String, optionId: String, userId: String = "my_user_id") {
        val msg = dao.getMessageById(messageId) ?: return
        val currentPoll = PollData.fromJson(msg.pollDataJson) ?: return
        if (currentPoll.isClosed) return

        val updatedOptions = currentPoll.options.map { opt ->
            val hasVoted = opt.voterUserIds.contains(userId)
            if (opt.id == optionId) {
                if (hasVoted) {
                    opt.copy(voterUserIds = opt.voterUserIds - userId)
                } else {
                    opt.copy(voterUserIds = opt.voterUserIds + userId)
                }
            } else {
                if (!currentPoll.allowMultiple && !hasVoted) {
                    // Remove vote from other options if single choice
                    opt.copy(voterUserIds = opt.voterUserIds - userId)
                } else {
                    opt
                }
            }
        }
        val updatedPoll = currentPoll.copy(options = updatedOptions)
        dao.updateMessage(msg.copy(pollDataJson = updatedPoll.toJson()))
    }

    suspend fun sendEvent(
        chatId: String,
        title: String,
        description: String,
        dateTime: String,
        location: String
    ) {
        val now = System.currentTimeMillis()
        val messageId = "msg_event_${UUID.randomUUID()}"
        val eventData = EventData(
            title = title,
            description = description,
            dateTime = dateTime,
            location = location,
            organizerName = "Alex Rivera",
            goingUserIds = listOf("my_user_id"),
            maybeUserIds = emptyList(),
            declinedUserIds = emptyList()
        )
        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "my_user_id",
            senderName = "Alex Rivera",
            text = "📅 Event: $title",
            timestamp = now,
            status = "SENT",
            mediaType = "EVENT",
            eventDataJson = eventData.toJson()
        )
        dao.insertMessage(message)

        val chat = dao.getChatById(chatId)
        if (chat != null) {
            dao.updateChat(chat.copy(lastMessage = "📅 Event: $title", lastMessageTime = now))
        }
    }

    suspend fun rsvpToEvent(messageId: String, rsvpType: String, userId: String = "my_user_id") {
        val msg = dao.getMessageById(messageId) ?: return
        val event = EventData.fromJson(msg.eventDataJson) ?: return

        var going = event.goingUserIds - userId
        var maybe = event.maybeUserIds - userId
        var declined = event.declinedUserIds - userId

        when (rsvpType.uppercase()) {
            "GOING" -> going = going + userId
            "MAYBE" -> maybe = maybe + userId
            "DECLINED" -> declined = declined + userId
        }

        val updatedEvent = event.copy(
            goingUserIds = going,
            maybeUserIds = maybe,
            declinedUserIds = declined
        )
        dao.updateMessage(msg.copy(eventDataJson = updatedEvent.toJson()))
    }

    fun searchMessagesInChat(chatId: String, query: String): Flow<List<MessageEntity>> {
        return dao.searchMessagesInChat(chatId, query)
    }

    fun searchAllMessages(query: String): Flow<List<MessageEntity>> {
        return dao.searchAllMessages(query)
    }

    suspend fun updateUserProfile(name: String, bio: String, phone: String, statusMsg: String) {
        val user = dao.getUserById("my_user_id") ?: return
        dao.updateUser(
            user.copy(
                name = name,
                bio = bio,
                phoneNumber = phone,
                statusMessage = statusMsg
            )
        )
    }
}
