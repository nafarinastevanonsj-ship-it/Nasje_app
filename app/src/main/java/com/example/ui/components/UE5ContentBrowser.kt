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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeshType
import com.example.ui.theme.*

data class AssetItem(
    val name: String,
    val type: String,
    val meshType: MeshType? = null,
    val color: Color
)

@Composable
fun UE5ContentBrowser(
    onSpawnMesh: (MeshType) -> Unit,
    onOpenBlueprint: () -> Unit,
    onOpenMaterial: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFolder by remember { mutableStateOf("All") }

    val allAssets = listOf(
        AssetItem("SM_CyberPillar_Nanite", "StaticMesh (Nanite)", MeshType.NANITE_PILLAR, UE_AccentCyan),
        AssetItem("SM_AncientArch_Nanite", "StaticMesh (Nanite)", MeshType.TEMPLE_ARCH, UE_AccentCyan),
        AssetItem("SM_SciFiCrate_Physics", "StaticMesh", MeshType.SCIFI_CRATE, Color(0xFFFFB300)),
        AssetItem("SM_EnergySphere", "StaticMesh", MeshType.SPHERE, Color(0xFF00E5FF)),
        AssetItem("BP_PlayerController", "Blueprint Class", null, Color(0xFF1E88E5)),
        AssetItem("BP_DoorTrigger", "Blueprint Class", null, Color(0xFF1E88E5)),
        AssetItem("M_NaniteCyberpunk", "Material (PBR)", null, Color(0xFFFF007F)),
        AssetItem("M_Ancient_Sandstone", "Material (PBR)", null, Color(0xFFC2B29A)),
        AssetItem("NS_LumenDustParticles", "Niagara System", null, Color(0xFF00E676)),
        AssetItem("L_AncientRuins_01", "World / Level", null, Color(0xFF7C4DFF))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(UE_Surface)
            .border(0.5.dp, UE_PanelBorder)
    ) {
        // Content Browser Header
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
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Text("Content Browser", color = UE_TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("/All/Content/Game", color = UE_TextMuted, fontSize = 11.sp)
            }

            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close Browser", tint = UE_TextSecondary, modifier = Modifier.size(16.dp))
            }
        }

        // Folder Tabs & Asset Grid
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Left folder tree
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0E1013))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("All", "Meshes", "Blueprints", "Materials", "Maps").forEach { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (selectedFolder == folder) UE_AccentBlue.copy(alpha = 0.5f) else Color.Transparent)
                            .clickable { selectedFolder = folder }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Text(folder, color = UE_TextPrimary, fontSize = 11.sp)
                    }
                }
            }

            // Right asset tiles
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(allAssets) { asset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UE_SurfaceVariant)
                            .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                if (asset.meshType != null) {
                                    onSpawnMesh(asset.meshType)
                                } else if (asset.type.contains("Blueprint")) {
                                    onOpenBlueprint()
                                } else if (asset.type.contains("Material")) {
                                    onOpenMaterial()
                                }
                            }
                            .padding(6.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(asset.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        asset.type.contains("Mesh") -> Icons.Default.Category
                                        asset.type.contains("Blueprint") -> Icons.Default.AccountTree
                                        asset.type.contains("Material") -> Icons.Default.Palette
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = asset.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(asset.name, color = UE_TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(asset.type, color = UE_TextMuted, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
