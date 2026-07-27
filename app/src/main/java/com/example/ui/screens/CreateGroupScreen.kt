package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.data.local.UserEntity
import com.example.ui.theme.VibeCyanSecondary
import com.example.ui.theme.VibeVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    users: List<UserEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: (String, String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }
    val selectedUserIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Group") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onCreateGroup(groupName, groupDesc, selectedUserIds.toList())
                            },
                            modifier = Modifier.testTag("submit_create_group_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Create", tint = VibeVioletPrimary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth().testTag("group_name_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = groupDesc,
                onValueChange = { groupDesc = it },
                label = { Text("Group Description (Optional)") },
                modifier = Modifier.fillMaxWidth().testTag("group_desc_input"),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Group Members (${selectedUserIds.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(users, key = { it.id }) { user ->
                    val isSelected = selectedUserIds.contains(user.id)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (isSelected) selectedUserIds.remove(user.id)
                                else selectedUserIds.add(user.id)
                            },
                        color = if (isSelected) VibeVioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(VibeVioletPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.name, fontWeight = FontWeight.Bold)
                                Text(text = user.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedUserIds.add(user.id)
                                    else selectedUserIds.remove(user.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = VibeVioletPrimary)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onCreateGroup(groupName, groupDesc, selectedUserIds.toList()) },
                enabled = groupName.isNotBlank() && selectedUserIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("create_group_action_btn"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibeVioletPrimary)
            ) {
                Text("Create Group (${selectedUserIds.size} Members)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
