package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaterialGraph
import com.example.data.model.MaterialNodeType
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun UE5MaterialEditor(
    graph: MaterialGraph,
    modifier: Modifier = Modifier
) {
    var previewColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
    var roughness by remember { mutableFloatStateOf(0.25f) }
    var metallic by remember { mutableFloatStateOf(0.85f) }
    var emissiveGlow by remember { mutableFloatStateOf(0.5f) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121418))
    ) {
        // Left Column: Live 3D Material Preview Sphere & Controls
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(UE_Surface)
                .border(0.5.dp, UE_PanelBorder)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "MATERIAL PREVIEW",
                color = UE_TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            // Dynamic PBR Sphere Preview
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0C0F))
                    .border(1.dp, UE_PanelBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // PBR Sphere Shading with Radial Gradient
                    val highlightOffset = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
                    val baseR = previewColor.red
                    val baseG = previewColor.green
                    val baseB = previewColor.blue

                    val highlightColor = Color(
                        red = (baseR + (1f - baseR) * (1f - roughness) * (metallic * 0.5f + 0.5f)).coerceIn(0f, 1f),
                        green = (baseG + (1f - baseG) * (1f - roughness) * (metallic * 0.5f + 0.5f)).coerceIn(0f, 1f),
                        blue = (baseB + (1f - baseB) * (1f - roughness) * (metallic * 0.5f + 0.5f)).coerceIn(0f, 1f)
                    )

                    val shadowColor = Color(
                        red = baseR * 0.15f,
                        green = baseG * 0.15f,
                        blue = baseB * 0.15f
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(highlightColor, previewColor, shadowColor),
                            center = highlightOffset,
                            radius = radius * 1.3f
                        ),
                        radius = radius,
                        center = center
                    )

                    // Emissive outer ring glow
                    if (emissiveGlow > 0.1f) {
                        drawCircle(
                            color = previewColor.copy(alpha = emissiveGlow * 0.35f),
                            radius = radius + 4f,
                            center = center,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            Text(graph.name, color = UE_AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // PBR Parameter Sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_SurfaceVariant)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Color Picker Swatches
                Text("Base Color", color = UE_TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val colors = listOf(Color(0xFF00E5FF), Color(0xFFFFB300), Color(0xFFFF007F), Color(0xFFC2B29A), Color(0xFF3DDC84))
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(c)
                                .border(1.dp, if (previewColor == c) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { previewColor = c }
                        )
                    }
                }

                // Roughness
                Text("Roughness: ${String.format("%.2f", roughness)}", color = UE_TextSecondary, fontSize = 10.sp)
                Slider(
                    value = roughness,
                    onValueChange = { roughness = it },
                    colors = SliderDefaults.colors(thumbColor = UE_AccentCyan, activeTrackColor = UE_AccentBlue)
                )

                // Metallic
                Text("Metallic: ${String.format("%.2f", metallic)}", color = UE_TextSecondary, fontSize = 10.sp)
                Slider(
                    value = metallic,
                    onValueChange = { metallic = it },
                    colors = SliderDefaults.colors(thumbColor = UE_AccentGold, activeTrackColor = UE_AccentGold)
                )

                // Emissive Glow
                Text("Emissive Glow: ${String.format("%.2f", emissiveGlow)}", color = UE_TextSecondary, fontSize = 10.sp)
                Slider(
                    value = emissiveGlow,
                    onValueChange = { emissiveGlow = it },
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252))
                )
            }
        }

        // Right Area: Visual Shader Graph
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF14171D))
        ) {
            // Wires Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val start = Offset(180.dp.toPx(), 90.dp.toPx())
                val end = Offset(320.dp.toPx(), 90.dp.toPx())
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    val dx = (end.x - start.x) * 0.5f
                    cubicTo(start.x + dx, start.y, end.x - dx, end.y, end.x, end.y)
                }
                drawPath(path, Color(0xFFFFB300), style = Stroke(width = 2.5f))

                val start2 = Offset(180.dp.toPx(), 200.dp.toPx())
                val end2 = Offset(320.dp.toPx(), 130.dp.toPx())
                val path2 = Path().apply {
                    moveTo(start2.x, start2.y)
                    val dx = (end2.x - start2.x) * 0.5f
                    cubicTo(start2.x + dx, start2.y, end2.x - dx, end2.y, end2.x, end2.y)
                }
                drawPath(path2, Color(0xFF43A047), style = Stroke(width = 2.5f))
            }

            // Material Result Master Node
            Box(
                modifier = Modifier
                    .offset(x = 320.dp, y = 50.dp)
                    .width(180.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(UE_SurfaceVariant)
                    .border(1.dp, UE_AccentGold, RoundedCornerShape(6.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF785516))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Material Result", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Base Color", "Metallic", "Specular", "Roughness", "Emissive Color", "Normal", "Ambient Occlusion").forEach { slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(UE_AccentCyan))
                                Text(slot, color = UE_TextPrimary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Vector Parameter Node
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 50.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(UE_SurfaceVariant)
                    .border(1.dp, UE_PanelBorder, RoundedCornerShape(6.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00695C))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("Vector Parameter", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(previewColor))
                        Text("RGB ->", color = UE_AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Scalar Parameter Node (Roughness)
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 160.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(UE_SurfaceVariant)
                    .border(1.dp, UE_PanelBorder, RoundedCornerShape(6.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("Scalar: Roughness", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(String.format("%.2f", roughness), color = UE_TextPrimary, fontSize = 10.sp)
                        Text("Value ->", color = Color(0xFF43A047), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
