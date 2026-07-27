package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.local.MessageEntity
import com.example.data.models.EventData
import com.example.data.models.PollData
import com.example.ui.theme.VibeCyanSecondary
import com.example.ui.theme.VibeVioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: MessageEntity,
    isMyMessage: Boolean,
    onReactionSelect: (String) -> Unit = {},
    onReplyClick: () -> Unit = {},
    onPollVote: (messageId: String, optionId: String) -> Unit = { _, _ -> },
    onEventRsvp: (messageId: String, rsvpType: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showReactionsBar by remember { mutableStateOf(false) }
    var isPlayingVoiceNote by remember { mutableStateOf(false) }

    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(message.timestamp))

    val bubbleShape = if (isMyMessage) {
        RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp)
    } else {
        RoundedCornerShape(22.dp, 22.dp, 22.dp, 4.dp)
    }

    val bubbleColor = if (isMyMessage) {
        VibeVioletPrimary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
    ) {
        // Reactions popover when long pressed or clicked
        AnimatedVisibility(visible = showReactionsBar) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("❤️", "👍", "😂", "😮", "🔥", "🙏").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onReactionSelect(emoji)
                                    showReactionsBar = false
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            shadowElevation = 2.dp,
            modifier = Modifier
                .testTag("message_bubble_${message.id}")
                .widthIn(max = 280.dp)
                .clickable { showReactionsBar = !showReactionsBar }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Sender name if in group or received
                if (!isMyMessage && message.senderName.isNotEmpty()) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = VibeCyanSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Reply Preview Block
                if (!message.replyToText.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(VibeCyanSecondary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.replyToText,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.85f),
                                maxLines = 2
                            )
                        }
                    }
                }

                // Content Type Handler
                when (message.mediaType) {
                    "VOICE" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { isPlayingVoiceNote = !isPlayingVoiceNote },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoiceNote) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play voice note",
                                    tint = textColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = { if (isPlayingVoiceNote) 0.6f else 0f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = if (isMyMessage) VibeCyanSecondary else VibeVioletPrimary,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Voice note • 0:${if (message.voiceDuration < 10) "0" else ""}${message.voiceDuration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    "LOCATION" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = if (isMyMessage) VibeCyanSecondary else VibeVioletPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    "DOCUMENT" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "Document",
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    }

                    "POLL" -> {
                        val pollData = PollData.fromJson(message.pollDataJson)
                        if (pollData != null) {
                            PollBubbleCard(
                                pollData = pollData,
                                isMyMessage = isMyMessage,
                                onOptionVote = { optId -> onPollVote(message.id, optId) }
                            )
                        } else {
                            Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
                        }
                    }

                    "EVENT" -> {
                        val eventData = EventData.fromJson(message.eventDataJson)
                        if (eventData != null) {
                            EventBubbleCard(
                                eventData = eventData,
                                isMyMessage = isMyMessage,
                                onRsvpSelect = { rsvpType -> onEventRsvp(message.id, rsvpType) }
                            )
                        } else {
                            Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
                        }
                    }

                    else -> { // TEXT / IMAGE
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer (Timestamp + Encrypted Lock + Delivery Checkmarks)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "End-to-end Encrypted",
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    if (message.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Color.Yellow,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )

                    if (isMyMessage) {
                        Icon(
                            imageVector = when (message.status) {
                                "READ" -> Icons.Default.DoneAll
                                "DELIVERED" -> Icons.Default.DoneAll
                                else -> Icons.Default.Done
                            },
                            contentDescription = message.status,
                            tint = if (message.status == "READ") VibeCyanSecondary else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Display reactions badge if present
                if (message.reactions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = message.reactions,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
