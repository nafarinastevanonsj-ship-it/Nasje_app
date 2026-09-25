package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Actor
import com.example.data.model.GizmoMode
import com.example.data.model.RenderMode
import com.example.engine.physics.CharacterState
import com.example.engine.render3d.Camera3D
import com.example.engine.render3d.Renderer3D
import com.example.ui.theme.*
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun UE5PlayInEditor(
    actors: List<Actor>,
    camera: Camera3D,
    characterState: CharacterState,
    fps: Float,
    onMoveInput: (moveX: Float, moveY: Float, jump: Boolean) -> Unit,
    onRotateCamera: (deltaYaw: Float) -> Unit,
    onStopClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var isJumping by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090C))
    ) {
        // 3D Gameplay Rendering Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            Renderer3D.renderScene(
                drawScope = this,
                actors = actors,
                camera = camera,
                renderMode = RenderMode.LIT,
                selectedActorId = null,
                gizmoMode = GizmoMode.TRANSLATION,
                canvasWidth = size.width,
                canvasHeight = size.height
            )
        }

        // Camera Look / Swipe Area (Right Screen Half)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onRotateCamera(dragAmount.x * 0.4f)
                    }
                }
        )

        // Top Game HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status & Stats Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC101216))
                    .border(1.dp, UE_PanelBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
                Text(
                    text = "PIE ACTIVE · ES3.2 / VULKAN",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", fps)} FPS",
                    color = Color(0xFF00E676),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pos: (${String.format("%.1f", characterState.position.x)}, ${String.format("%.1f", characterState.position.y)}, ${String.format("%.1f", characterState.position.z)})",
                    color = UE_TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Stop PIE Button
            Button(
                onClick = onStopClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop PIE", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop PIE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom Controls: Touch Joystick (Left) & Jump Button (Right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Virtual Joystick
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .border(2.dp, Color(0x6600E5FF), CircleShape)
                    .align(Alignment.BottomStart)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                joystickOffset = Offset.Zero
                                onMoveInput(0f, 0f, false)
                            },
                            onDragCancel = {
                                joystickOffset = Offset.Zero
                                onMoveInput(0f, 0f, false)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val maxR = 45f
                                val candidate = joystickOffset + dragAmount
                                val dist = hypot(candidate.x, candidate.y)
                                joystickOffset = if (dist > maxR) {
                                    Offset(candidate.x / dist * maxR, candidate.y / dist * maxR)
                                } else candidate

                                // Normalize to -1f..1f (y is inverted on screen vs world)
                                val normX = joystickOffset.x / maxR
                                val normY = -joystickOffset.y / maxR
                                onMoveInput(normX, normY, isJumping)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Joystick Thumb
                Box(
                    modifier = Modifier
                        .offset { IntOffset(joystickOffset.x.roundToInt(), joystickOffset.y.roundToInt()) }
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(UE_AccentCyan.copy(alpha = 0.85f))
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            // Jump Button (Right)
            FloatingActionButton(
                onClick = {
                    onMoveInput(joystickOffset.x / 45f, -joystickOffset.y / 45f, true)
                },
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd),
                containerColor = UE_AccentBlue,
                contentColor = Color.White
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Jump", modifier = Modifier.size(24.dp))
                    Text("JUMP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
