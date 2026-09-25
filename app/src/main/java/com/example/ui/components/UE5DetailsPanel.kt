package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Actor
import com.example.data.model.MaterialData
import com.example.data.model.MobilityType
import com.example.data.model.Vec3
import com.example.ui.theme.*

@Composable
fun UE5DetailsPanel(
    actor: Actor?,
    onLocationChanged: (Vec3) -> Unit,
    onRotationChanged: (Vec3) -> Unit,
    onScaleChanged: (Vec3) -> Unit,
    onMaterialChanged: (MaterialData) -> Unit,
    onTogglePhysics: () -> Unit,
    onToggleNanite: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (actor == null) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(UE_Surface)
                .border(0.5.dp, UE_PanelBorder)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = UE_TextMuted, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select an Actor in Viewport or Outliner to inspect properties.", color = UE_TextMuted, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(UE_Surface)
            .border(0.5.dp, UE_PanelBorder)
    ) {
        // Header
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
                Icon(Icons.Default.Tune, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(16.dp))
                Text("Details", color = UE_TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close Details", tint = UE_TextSecondary, modifier = Modifier.size(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Actor Identification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_Background)
                    .padding(8.dp)
            ) {
                Column {
                    Text(actor.name, color = UE_AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Type: ${actor.meshType.name} · Mobility: ${actor.mobility.name}", color = UE_TextMuted, fontSize = 10.sp)
                }
            }

            // Transform Section
            SectionHeader(title = "TRANSFORM")

            TransformVectorRow(
                label = "Location",
                vec = actor.location,
                unit = "m",
                onAxisChange = { axis, delta ->
                    when (axis) {
                        0 -> onLocationChanged(actor.location.copy(x = actor.location.x + delta))
                        1 -> onLocationChanged(actor.location.copy(y = actor.location.y + delta))
                        2 -> onLocationChanged(actor.location.copy(z = actor.location.z + delta))
                    }
                }
            )

            TransformVectorRow(
                label = "Rotation",
                vec = actor.rotation,
                unit = "°",
                onAxisChange = { axis, delta ->
                    when (axis) {
                        0 -> onRotationChanged(actor.rotation.copy(x = (actor.rotation.x + delta * 10f) % 360f))
                        1 -> onRotationChanged(actor.rotation.copy(y = (actor.rotation.y + delta * 10f) % 360f))
                        2 -> onRotationChanged(actor.rotation.copy(z = (actor.rotation.z + delta * 10f) % 360f))
                    }
                }
            )

            TransformVectorRow(
                label = "Scale",
                vec = actor.scale,
                unit = "x",
                onAxisChange = { axis, delta ->
                    when (axis) {
                        0 -> onScaleChanged(actor.scale.copy(x = (actor.scale.x + delta * 0.2f).coerceAtLeast(0.1f)))
                        1 -> onScaleChanged(actor.scale.copy(y = (actor.scale.y + delta * 0.2f).coerceAtLeast(0.1f)))
                        2 -> onScaleChanged(actor.scale.copy(z = (actor.scale.z + delta * 0.2f).coerceAtLeast(0.1f)))
                    }
                }
            )

            // Nanite & Lumen Section
            SectionHeader(title = "NANITE & LUMEN SETTINGS")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_SurfaceVariant)
                    .clickable { onToggleNanite() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Enable Nanite Virtualization", color = UE_TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${actor.estimatedTriangles} Tris · ${actor.clusterCount} Clusters", color = UE_AccentCyan, fontSize = 10.sp)
                }
                Switch(
                    checked = actor.isNaniteEnabled,
                    onCheckedChange = { onToggleNanite() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = UE_AccentCyan,
                        checkedTrackColor = UE_AccentBlue
                    )
                )
            }

            // Material Settings
            SectionHeader(title = "MATERIAL (PBR)")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_SurfaceVariant)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Slot 0: ${actor.material.name}", color = UE_TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Quick Color Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val colors = listOf(
                        0xFFC2B29A, 0xFF8A847C, 0xFF00E5FF, 0xFFFF007F, 0xFFFFB300, 0xFF3DDC84
                    )
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(hex))
                                .border(
                                    1.dp,
                                    if (actor.material.baseColorHex == hex) Color.White else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    onMaterialChanged(actor.material.copy(baseColorHex = hex))
                                }
                        )
                    }
                }

                // Roughness Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Roughness: ${String.format("%.2f", actor.material.roughness)}", color = UE_TextSecondary, fontSize = 10.sp, modifier = Modifier.width(80.dp))
                    Slider(
                        value = actor.material.roughness,
                        onValueChange = { onMaterialChanged(actor.material.copy(roughness = it)) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = UE_AccentCyan, activeTrackColor = UE_AccentBlue)
                    )
                }

                // Metallic Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Metallic: ${String.format("%.2f", actor.material.metallic)}", color = UE_TextSecondary, fontSize = 10.sp, modifier = Modifier.width(80.dp))
                    Slider(
                        value = actor.material.metallic,
                        onValueChange = { onMaterialChanged(actor.material.copy(metallic = it)) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = UE_AccentGold, activeTrackColor = UE_AccentGold)
                    )
                }

                // Emissive Glow Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Emissive: ${String.format("%.1f", actor.material.emissiveStrength)}", color = UE_TextSecondary, fontSize = 10.sp, modifier = Modifier.width(80.dp))
                    Slider(
                        value = actor.material.emissiveStrength,
                        valueRange = 0f..5f,
                        onValueChange = { onMaterialChanged(actor.material.copy(emissiveStrength = it, emissiveHex = actor.material.baseColorHex)) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252))
                    )
                }
            }

            // Physics Section
            SectionHeader(title = "PHYSICS & CHAOS COLLISION")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_SurfaceVariant)
                    .clickable { onTogglePhysics() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Simulate Physics", color = UE_TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Mass: ${actor.physics.massKg} kg", color = UE_TextMuted, fontSize = 10.sp)
                }
                Switch(
                    checked = actor.physics.simulatePhysics,
                    onCheckedChange = { onTogglePhysics() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00897B)
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = UE_TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun TransformVectorRow(
    label: String,
    vec: Vec3,
    unit: String,
    onAxisChange: (axis: Int, delta: Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(UE_SurfaceVariant)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = UE_TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AxisScrubber(axisLabel = "X", value = vec.x, color = UE_AxisRed, unit = unit, onDelta = { onAxisChange(0, it) }, modifier = Modifier.weight(1f))
            AxisScrubber(axisLabel = "Y", value = vec.y, color = UE_AxisGreen, unit = unit, onDelta = { onAxisChange(1, it) }, modifier = Modifier.weight(1f))
            AxisScrubber(axisLabel = "Z", value = vec.z, color = UE_AxisBlue, unit = unit, onDelta = { onAxisChange(2, it) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AxisScrubber(
    axisLabel: String,
    value: Float,
    color: Color,
    unit: String,
    onDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(UE_Background)
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(axisLabel, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            text = String.format("%.1f", value),
            color = UE_TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Row {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelta(-0.5f) },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = UE_TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelta(0.5f) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = UE_TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
