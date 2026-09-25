package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Actor
import com.example.data.model.MeshType
import com.example.ui.theme.*

@Composable
fun UE5Outliner(
    actors: List<Actor>,
    selectedActorId: String?,
    onSelectActor: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onAddActor: () -> Unit,
    onDuplicateActor: () -> Unit,
    onDeleteActor: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredActors = remember(actors, searchQuery) {
        if (searchQuery.isBlank()) actors
        else actors.filter { it.name.contains(searchQuery, ignoreCase = true) || it.meshType.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(UE_Surface)
            .border(0.5.dp, UE_PanelBorder)
    ) {
        // Outliner Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UE_SurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = "Outliner",
                    color = UE_TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(${actors.size})",
                    color = UE_TextMuted,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onAddActor, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add Actor", tint = UE_AccentCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Outliner", tint = UE_TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Actors...", fontSize = 11.sp, color = UE_TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = UE_Background,
                    unfocusedContainerColor = UE_Background,
                    focusedBorderColor = UE_AccentCyan,
                    unfocusedBorderColor = UE_PanelBorder,
                    focusedTextColor = UE_TextPrimary,
                    unfocusedTextColor = UE_TextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = UE_TextSecondary, modifier = Modifier.size(14.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = UE_TextSecondary, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            )
        }

        // Actor List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            items(filteredActors, key = { it.id }) { actor ->
                val isSelected = actor.id == selectedActorId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) UE_AccentBlue.copy(alpha = 0.4f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) UE_AccentCyan else Color.Transparent,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .clickable { onSelectActor(actor.id) }
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon based on type
                        Icon(
                            imageVector = when (actor.meshType) {
                                MeshType.DIRECTIONAL_LIGHT, MeshType.POINT_LIGHT -> Icons.Default.LightMode
                                MeshType.CAMERA -> Icons.Default.Videocam
                                MeshType.CHARACTER_PAWN -> Icons.Default.Person
                                MeshType.NANITE_PILLAR, MeshType.TEMPLE_ARCH -> Icons.Default.AccountBalance
                                else -> Icons.Default.Category
                            },
                            contentDescription = null,
                            tint = if (actor.isNaniteEnabled) UE_AccentCyan else UE_TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )

                        Column {
                            Text(
                                text = actor.name,
                                color = if (isSelected) Color.White else UE_TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Text(
                                text = "${actor.meshType.name} · ${actor.folder}",
                                color = UE_TextMuted,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Visibility Eye Icon
                    IconButton(
                        onClick = { onToggleVisibility(actor.id) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (actor.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (actor.isVisible) UE_TextSecondary else UE_TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Action Toolbar at Bottom
        if (selectedActorId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UE_SurfaceVariant)
                    .border(0.5.dp, UE_PanelBorder)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onDuplicateActor,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate", color = UE_TextPrimary, fontSize = 10.sp)
                }

                TextButton(
                    onClick = onDeleteActor,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFFF5252), fontSize = 10.sp)
                }
            }
        }
    }
}
