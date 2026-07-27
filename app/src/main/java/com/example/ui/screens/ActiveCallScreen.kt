package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ActiveCallState
import com.example.ui.theme.VibeCyanSecondary
import com.example.ui.theme.VibeVioletPrimary
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    var callTimerSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callTimerSeconds++
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulseWave")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_call_screen")
            .background(Color(0xFF0F0E17))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = callState.contactName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${callState.callType} CALL • ${callTimerSeconds / 60}:${if (callTimerSeconds % 60 < 10) "0" else ""}${callTimerSeconds % 60}",
                    style = MaterialTheme.typography.titleMedium,
                    color = VibeCyanSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Central Animated Avatar Frame
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Wave aura
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .background(VibeVioletPrimary.copy(alpha = 0.25f), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(VibeVioletPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callState.contactName.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Controls Grid
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMuteToggle,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (callState.isMuted) Color.White else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (callState.isMuted) Color.Black else Color.White
                        )
                    }

                    IconButton(
                        onClick = onSpeakerToggle,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (callState.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (callState.isSpeakerOn) Color.Black else Color.White
                        )
                    }

                    if (callState.callType == "VIDEO") {
                        IconButton(
                            onClick = onVideoToggle,
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (!callState.isVideoEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (callState.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Camera",
                                tint = if (!callState.isVideoEnabled) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // End Call FAB
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("end_call_btn")
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
