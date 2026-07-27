package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val bio: String = "Hey there! I am using Vibe.",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val lastSeen: String = "Online now",
    val statusMessage: String = "Available",
    val isFavorite: Boolean = false,
    val isBlocked: Boolean = false
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val participantIds: String, // comma separated e.g. "my_user_id,contact_1"
    val isGroup: Boolean = false,
    val chatName: String = "",
    val chatAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isSecret: Boolean = false,
    val wallpaperUri: String? = null,
    val groupDescription: String? = null,
    val adminIds: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "READ", // SENT, DELIVERED, READ
    val mediaType: String = "TEXT", // TEXT, IMAGE, VOICE, LOCATION, DOCUMENT, VIDEO
    val mediaUrl: String? = null,
    val voiceDuration: Int = 0, // in seconds
    val replyToId: String? = null,
    val replyToText: String? = null,
    val isStarred: Boolean = false,
    val isScheduled: Boolean = false,
    val scheduledTime: Long? = null,
    val reactions: String = "", // e.g. "❤️,👍"
    val pollDataJson: String? = null,
    val eventDataJson: String? = null,
    val isEncrypted: Boolean = true
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaType: String = "TEXT", // TEXT, IMAGE
    val textContent: String? = null,
    val mediaUrl: String? = null,
    val bgColorHex: String = "0xFF6C5CE7",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L, // 24 hours
    val viewsCount: Int = 0
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val contactAvatar: String,
    val callType: String = "VOICE", // VOICE, VIDEO
    val direction: String = "INCOMING", // INCOMING, OUTGOING, MISSED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM", // SYSTEM, DARK, LIGHT
    val appLockEnabled: Boolean = false,
    val pinCode: String = "1234",
    val autoReplyEnabled: Boolean = false,
    val autoReplyText: String = "Hey! I am currently busy. Sent via Vibe Auto-Reply.",
    val readReceipts: Boolean = true,
    val lastSeenPrivacy: String = "EVERYONE", // EVERYONE, CONTACTS, NOBODY
    val onlinePrivacy: String = "EVERYONE",
    val language: String = "English"
)
