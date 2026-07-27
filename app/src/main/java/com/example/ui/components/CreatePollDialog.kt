package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }
    var allowMultiple by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_poll_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Title Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = "Poll",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Create Group Poll",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Input
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask a question...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("poll_question_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Poll Options",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Options List
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(options) { index, optText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = optText,
                                onValueChange = { newText ->
                                    val updated = options.toMutableList()
                                    updated[index] = newText
                                    options = updated
                                },
                                placeholder = { Text("Option ${index + 1}") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("poll_option_input_$index"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            if (options.size > 2) {
                                IconButton(
                                    onClick = {
                                        val updated = options.toMutableList()
                                        updated.removeAt(index)
                                        options = updated
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove option")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Option Button
                if (options.size < 6) {
                    TextButton(
                        onClick = { options = options + "" },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add option")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Option")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Multiple choices switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow multiple answers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = allowMultiple,
                        onCheckedChange = { allowMultiple = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = question.isNotBlank() && options.count { it.isNotBlank() } >= 2,
                        onClick = {
                            val validOptions = options.filter { it.isNotBlank() }
                            onCreatePoll(question, validOptions, allowMultiple)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("submit_create_poll_btn")
                    ) {
                        Text("Send Poll")
                    }
                }
            }
        }
    }
}
