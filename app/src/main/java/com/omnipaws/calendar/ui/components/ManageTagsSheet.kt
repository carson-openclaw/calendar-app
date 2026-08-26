package com.omnipaws.calendar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.data.EventTag
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.InkLight
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.Paper
import com.omnipaws.calendar.ui.theme.PaperSurface

private val PresetColors = listOf(
    Color(0xFF9CAF9A),
    Color(0xFFB8A9C9),
    Color(0xFFC4A882),
    Color(0xFF8AABB8),
    Color(0xFFD4A5A5),
    Color(0xFFA5C4D4),
    Color(0xFFC9B8A8),
    Color(0xFFB8C9A8),
    Color(0xFFD4B8A5),
    Color(0xFFA8B8C9),
    Color(0xFFC9A8B8),
    Color(0xFFA8C9B8),
)

@Composable
fun ManageTagsSheet(
    onDismiss: () -> Unit
) {
    var editingTag by remember { mutableStateOf<EventTag?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<EventTag?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(20.dp),
        color = PaperSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Tags",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Ink
                )
                Row {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add tag",
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(EventRepository.tags, key = { it.id }) { tag ->
                    TagRow(
                        tag = tag,
                        onEdit = { editingTag = tag },
                        onDelete = { showDeleteConfirm = tag }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        TagEditDialog(
            title = "New Tag",
            initialName = "",
            initialColor = PresetColors.first().toArgb().toLong(),
            onConfirm = { name, color ->
                EventRepository.addTag(name, color)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingTag?.let { tag ->
        TagEditDialog(
            title = "Edit Tag",
            initialName = tag.name,
            initialColor = tag.color,
            onConfirm = { name, color ->
                EventRepository.updateTag(tag.id, name, color)
                editingTag = null
            },
            onDismiss = { editingTag = null }
        )
    }

    showDeleteConfirm?.let { tag ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = PaperSurface,
            titleContentColor = Ink,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Delete tag?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "\"${tag.name}\" will be removed. Events using it will be reassigned to another tag.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    EventRepository.deleteTag(tag.id)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = Color(0xFFD4A5A5))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Muted)
                }
            }
        )
    }
}

@Composable
private fun TagRow(
    tag: EventTag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Paper.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(tag.color.toInt()))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = tag.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = InkLight,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit tag",
                tint = Muted,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete tag",
                tint = Muted.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TagEditDialog(
    title: String,
    initialName: String,
    initialColor: Long,
    onConfirm: (name: String, color: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperSurface,
        titleContentColor = Ink,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tag name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Outline,
                        focusedContainerColor = PaperSurface,
                        unfocusedContainerColor = PaperSurface,
                        cursorColor = Accent,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = Muted
                    )
                )

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0 until 4) {
                                val idx = row * 4 + col
                                val presetColor = PresetColors[idx]
                                val isSelected = selectedColor == presetColor.toArgb().toLong()
                                val animatedBorder by animateColorAsState(
                                    targetValue = if (isSelected) Accent else Color.Transparent,
                                    label = "swatchBorder"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = animatedBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = presetColor.toArgb().toLong() }
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor.toInt()))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canSave) onConfirm(name.trim(), selectedColor) },
                enabled = canSave
            ) {
                Text("Save", color = if (canSave) Accent else Muted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Muted)
            }
        }
    )
}
