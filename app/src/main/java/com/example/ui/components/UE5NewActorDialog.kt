package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MeshType
import com.example.ui.theme.*

@Composable
fun UE5NewActorDialog(
    onSelectMeshType: (MeshType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = UE_Surface),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, UE_PanelBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(20.dp))
                        Text("Place Actors in World", color = UE_TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = UE_TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    "Select an Actor or Nanite primitive to spawn in the level:",
                    color = UE_TextSecondary,
                    fontSize = 11.sp
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(MeshType.values()) { type ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UE_SurfaceVariant)
                                .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    onSelectMeshType(type)
                                    onDismiss()
                                }
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (type) {
                                        MeshType.DIRECTIONAL_LIGHT, MeshType.POINT_LIGHT -> Icons.Default.LightMode
                                        MeshType.CAMERA -> Icons.Default.Videocam
                                        MeshType.CHARACTER_PAWN -> Icons.Default.Person
                                        MeshType.NANITE_PILLAR, MeshType.TEMPLE_ARCH -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Category
                                    },
                                    contentDescription = null,
                                    tint = if (type == MeshType.NANITE_PILLAR || type == MeshType.TEMPLE_ARCH) UE_AccentCyan else UE_AccentGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = type.displayName.substringBefore("'").ifEmpty { type.name },
                                        color = UE_TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${type.baseTriangles} Tris",
                                        color = UE_TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
