package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Actor
import com.example.data.model.GizmoMode
import com.example.data.model.RenderMode
import com.example.engine.render3d.Camera3D
import com.example.engine.render3d.Renderer3D
import com.example.ui.theme.*

@Composable
fun UE5Viewport(
    actors: List<Actor>,
    camera: Camera3D,
    selectedActorId: String?,
    renderMode: RenderMode,
    gizmoMode: GizmoMode,
    cameraSpeed: Float,
    onSelectActor: (String?) -> Unit,
    onRenderModeChanged: (RenderMode) -> Unit,
    onGizmoModeChanged: (GizmoMode) -> Unit,
    onCameraSpeedChanged: (Float) -> Unit,
    onOrbitCamera: (Float, Float) -> Unit,
    onPanCamera: (Float, Float) -> Unit,
    onZoomCamera: (Float) -> Unit,
    onFocusSelected: () -> Unit,
    onAddActorClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenderModes by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E12))
    ) {
        // 3D Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f) {
                            onZoomCamera(1f / zoom)
                        } else if (pan != Offset.Zero) {
                            onPanCamera(pan.x, pan.y)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onOrbitCamera(-dragAmount.x, -dragAmount.y)
                    }
                }
                .pointerInput(actors, camera) {
                    detectTapGestures { offset ->
                        val picked = Renderer3D.pickActor(
                            tap = offset,
                            actors = actors,
                            camera = camera,
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat()
                        )
                        onSelectActor(picked?.id)
                    }
                }
        ) {
            Renderer3D.renderScene(
                drawScope = this,
                actors = actors,
                camera = camera,
                renderMode = renderMode,
                selectedActorId = selectedActorId,
                gizmoMode = gizmoMode,
                canvasWidth = size.width,
                canvasHeight = size.height
            )
        }

        // Overlay: Top-Left View Mode Selector (Lit / Nanite / Lumen / Wireframe)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(UE_Surface.copy(alpha = 0.88f))
                    .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                TextButton(
                    onClick = { showRenderModes = true },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "View: ${renderMode.label}",
                        color = UE_TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = UE_TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when (renderMode) {
                                RenderMode.LIT -> UE_AccentCyan
                                RenderMode.NANITE_CLUSTERS -> Color(0xFFFF5252)
                                RenderMode.LUMEN_GI -> Color(0xFF00E676)
                                else -> UE_TextMuted
                            }
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = renderMode.badge,
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            DropdownMenu(
                expanded = showRenderModes,
                onDismissRequest = { showRenderModes = false },
                modifier = Modifier.background(UE_SurfaceVariant)
            ) {
                RenderMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    mode.label,
                                    color = if (mode == renderMode) UE_AccentCyan else UE_TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (mode == renderMode) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    "[${mode.badge}]",
                                    color = UE_TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        },
                        onClick = {
                            onRenderModeChanged(mode)
                            showRenderModes = false
                        }
                    )
                }
            }
        }

        // Overlay: Top-Right Gizmo & Camera Tools
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(UE_Surface.copy(alpha = 0.88f))
                .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(4.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GizmoButton(
                icon = Icons.Default.OpenWith,
                tooltip = "Translate (W)",
                isActive = gizmoMode == GizmoMode.TRANSLATION,
                onClick = { onGizmoModeChanged(GizmoMode.TRANSLATION) }
            )
            GizmoButton(
                icon = Icons.Default.Cached,
                tooltip = "Rotate (E)",
                isActive = gizmoMode == GizmoMode.ROTATION,
                onClick = { onGizmoModeChanged(GizmoMode.ROTATION) }
            )
            GizmoButton(
                icon = Icons.Default.ZoomOutMap,
                tooltip = "Scale (R)",
                isActive = gizmoMode == GizmoMode.SCALE,
                onClick = { onGizmoModeChanged(GizmoMode.SCALE) }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 2.dp),
                color = UE_PanelBorder
            )

            // Focus on selected (F)
            IconButton(
                onClick = onFocusSelected,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    Icons.Default.FilterCenterFocus,
                    contentDescription = "Focus",
                    tint = if (selectedActorId != null) UE_AccentGold else UE_TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Overlay: Bottom-Right Camera Speed & Reset
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(UE_Surface.copy(alpha = 0.88f))
                .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Speed, contentDescription = null, tint = UE_TextSecondary, modifier = Modifier.size(14.dp))
            Text(
                text = "${cameraSpeed.toInt()}x",
                color = UE_TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            listOf(1f, 2f, 4f).forEach { spd ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (cameraSpeed == spd) UE_AccentBlue else Color.Transparent)
                        .clickable { onCameraSpeedChanged(spd) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${spd.toInt()}",
                        color = if (cameraSpeed == spd) Color.White else UE_TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Overlay: Bottom-Left Add Actor & Info Pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FloatingActionButton(
                onClick = onAddActorClicked,
                modifier = Modifier.size(36.dp),
                containerColor = UE_AccentBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Actor", modifier = Modifier.size(20.dp))
            }

            if (selectedActorId != null) {
                val sel = actors.find { it.id == selectedActorId }
                if (sel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(UE_Surface.copy(alpha = 0.9f))
                            .border(0.5.dp, UE_AccentGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${sel.name} (${sel.meshType.name})",
                            color = UE_AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GizmoButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isActive) UE_AccentBlue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = tooltip,
            tint = if (isActive) Color.White else UE_TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}
