package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EditorTab
import com.example.data.model.EngineStats
import com.example.ui.theme.*

@Composable
fun UE5TopMenuBar(
    projectName: String,
    activeTab: EditorTab,
    onTabSelected: (EditorTab) -> Unit,
    isPlaying: Boolean,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit,
    stats: EngineStats,
    onSaveClicked: () -> Unit,
    onSwitchPreset: (String) -> Unit,
    onToggleOutliner: () -> Unit,
    onToggleDetails: () -> Unit,
    onToggleBrowser: () -> Unit,
    onToggleLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFileMenu by remember { mutableStateOf(false) }
    var showPresetsMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(UE_Surface)
            .border(width = 0.5.dp, color = UE_PanelBorder)
    ) {
        // Row 1: Logo, Project Name, File/Edit/Window Menus, Play Buttons, Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: UE5 Logo & Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0070E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U5",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Column {
                    Text(
                        text = "UNREAL ENGINE 5.4",
                        color = UE_TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = projectName,
                        color = UE_AccentCyan,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Center: Play / Stop Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(UE_SurfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                if (!isPlaying) {
                    IconButton(
                        onClick = onPlayClicked,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play in Editor",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "PIE",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    IconButton(
                        onClick = onStopClicked,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "STOP",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: FPS and Engine Stats Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1115))
                    .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", stats.fps)} FPS",
                    color = if (stats.fps > 55) Color(0xFF00E676) else Color(0xFFFFB300),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${stats.naniteTriangles / 1000}k Tris",
                    color = UE_AccentCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Row 2: Menu items & Main Tabs (Viewport, Blueprint, Material, Content, Outliner)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Quick File & Presets Menu
            Box {
                Button(
                    onClick = { showFileMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UE_SurfaceVariant,
                        contentColor = UE_TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File", fontSize = 11.sp)
                }

                DropdownMenu(
                    expanded = showFileMenu,
                    onDismissRequest = { showFileMenu = false },
                    modifier = Modifier.background(UE_SurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Save Level to Storage", color = UE_TextPrimary, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = UE_AccentCyan) },
                        onClick = {
                            showFileMenu = false
                            onSaveClicked()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Load: Ancient Ruins (Nanite)", color = UE_TextPrimary, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = null, tint = UE_AccentGold) },
                        onClick = {
                            showFileMenu = false
                            onSwitchPreset("Ancient")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Load: Cyberpunk City (Lumen)", color = UE_TextPrimary, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = UE_AccentBlue) },
                        onClick = {
                            showFileMenu = false
                            onSwitchPreset("Cyber")
                        }
                    )
                }
            }

            // Tab Buttons
            EditorTabButton(
                title = "Level Viewport",
                icon = Icons.Default.ViewInAr,
                isSelected = activeTab == EditorTab.VIEWPORT,
                onClick = { onTabSelected(EditorTab.VIEWPORT) }
            )

            EditorTabButton(
                title = "Blueprint (Graph)",
                icon = Icons.Default.AccountTree,
                isSelected = activeTab == EditorTab.BLUEPRINT,
                onClick = { onTabSelected(EditorTab.BLUEPRINT) }
            )

            EditorTabButton(
                title = "Material (Shader)",
                icon = Icons.Default.Palette,
                isSelected = activeTab == EditorTab.MATERIAL,
                onClick = { onTabSelected(EditorTab.MATERIAL) }
            )

            // Panel Toggles
            IconButton(
                onClick = onToggleOutliner,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.FormatListBulleted,
                    contentDescription = "Outliner",
                    tint = UE_TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onToggleDetails,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Details",
                    tint = UE_TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onToggleBrowser,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Content Browser",
                    tint = UE_TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onToggleLog,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = "Output Log",
                    tint = UE_TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) UE_AccentBlue else UE_SurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else UE_TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.White else UE_TextPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
