package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun UE5BlueprintEditor(
    graph: BlueprintGraph,
    isExecuting: Boolean,
    activePulseNodeId: String?,
    onExecuteBlueprint: () -> Unit,
    onAddNode: (String, NodeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var nodePositions by remember(graph) {
        mutableStateOf(graph.nodes.associate { it.id to Offset(it.x, it.y) }.toMutableMap())
    }
    var showAddNodeMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14171C))
    ) {
        // Blueprint Dot Grid & Connecting Wires Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Draw UE5 Blueprint Dot Grid
            val dotSpacing = 24.dp.toPx()
            val dotColor = Color(0x334E5D6C)
            val w = size.width
            val h = size.height

            var x = 0f
            while (x < w) {
                var y = 0f
                while (y < h) {
                    drawCircle(dotColor, radius = 1.2f, center = Offset(x, y))
                    y += dotSpacing
                }
                x += dotSpacing
            }

            // 2. Draw Curved Execution Wires
            for (conn in graph.connections) {
                val fromPos = nodePositions[conn.fromNodeId] ?: Offset(100f, 100f)
                val toPos = nodePositions[conn.toNodeId] ?: Offset(300f, 100f)

                val start = Offset(fromPos.x + 190.dp.toPx(), fromPos.y + 40.dp.toPx())
                val end = Offset(toPos.x, toPos.y + 40.dp.toPx())

                val path = Path().apply {
                    moveTo(start.x, start.y)
                    val dx = (end.x - start.x) * 0.5f
                    cubicTo(
                        start.x + dx, start.y,
                        end.x - dx, end.y,
                        end.x, end.y
                    )
                }

                val isHot = (conn.fromNodeId == activePulseNodeId || conn.toNodeId == activePulseNodeId)
                drawPath(
                    path = path,
                    color = if (isHot) Color(0xFF00E5FF) else Color(0xFFECEFF1),
                    style = Stroke(width = if (isHot) 3.5f else 2.2f)
                )
            }
        }

        // Draggable Blueprint Nodes
        Box(modifier = Modifier.fillMaxSize()) {
            for (node in graph.nodes) {
                val pos = nodePositions[node.id] ?: Offset(node.x, node.y)
                val isPulsing = node.id == activePulseNodeId

                Box(
                    modifier = Modifier
                        .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                        .width(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UE_SurfaceVariant)
                        .border(
                            width = if (isPulsing) 2.dp else 1.dp,
                            color = if (isPulsing) Color(0xFF00E5FF) else UE_PanelBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .pointerInput(node.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val current = nodePositions[node.id] ?: Offset.Zero
                                nodePositions = nodePositions.toMutableMap().apply {
                                    put(node.id, current + dragAmount)
                                }
                            }
                        }
                ) {
                    Column {
                        // Node Header
                        val headerColor = when (node.category) {
                            NodeCategory.EVENT -> Color(0xFFB71C1C)
                            NodeCategory.FUNCTION -> Color(0xFF1565C0)
                            NodeCategory.FLOW_CONTROL -> Color(0xFF455A64)
                            NodeCategory.MATH -> Color(0xFF2E7D32)
                            NodeCategory.GAMEPLAY -> Color(0xFF6A1B9A)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerColor)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (node.category) {
                                        NodeCategory.EVENT -> Icons.Default.FlashOn
                                        NodeCategory.FUNCTION -> Icons.Default.Functions
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = node.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Node Pins Body
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (node.subText.isNotEmpty()) {
                                Text(
                                    text = node.subText,
                                    color = UE_TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Inputs & Outputs rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left (Inputs)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (pin in node.inputs) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(getPinColor(pin.type))
                                            )
                                            Text(pin.name.ifEmpty { "Exec" }, color = UE_TextPrimary, fontSize = 9.sp)
                                        }
                                    }
                                }

                                // Right (Outputs)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                                    for (pin in node.outputs) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(pin.name.ifEmpty { "Exec" }, color = UE_TextPrimary, fontSize = 9.sp)
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(getPinColor(pin.type))
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

        // Top Action Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(UE_Surface.copy(alpha = 0.9f))
                .border(0.5.dp, UE_PanelBorder, RoundedCornerShape(4.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onExecuteBlueprint,
                enabled = !isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070E0)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isExecuting) "Simulating..." else "Step Execute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showAddNodeMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = UE_SurfaceVariant),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Add Node", color = UE_TextPrimary, fontSize = 11.sp)
            }

            DropdownMenu(
                expanded = showAddNodeMenu,
                onDismissRequest = { showAddNodeMenu = false },
                modifier = Modifier.background(UE_SurfaceVariant)
            ) {
                DropdownMenuItem(
                    text = { Text("Event: OnActorHit", color = UE_TextPrimary, fontSize = 11.sp) },
                    onClick = {
                        onAddNode("OnActorHit", NodeCategory.EVENT)
                        showAddNodeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Action: Add Impulse (Physics)", color = UE_TextPrimary, fontSize = 11.sp) },
                    onClick = {
                        onAddNode("Add Impulse", NodeCategory.FUNCTION)
                        showAddNodeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Flow: Branch (If / Else)", color = UE_TextPrimary, fontSize = 11.sp) },
                    onClick = {
                        onAddNode("Branch", NodeCategory.FLOW_CONTROL)
                        showAddNodeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Math: Vector + Vector", color = UE_TextPrimary, fontSize = 11.sp) },
                    onClick = {
                        onAddNode("Vector + Vector", NodeCategory.MATH)
                        showAddNodeMenu = false
                    }
                )
            }
        }
    }
}

private fun getPinColor(type: PinType): Color {
    return when (type) {
        PinType.EXEC -> UE_BlueprintExec
        PinType.BOOLEAN -> UE_BlueprintBool
        PinType.FLOAT -> UE_BlueprintFloat
        PinType.INTEGER -> UE_BlueprintInt
        PinType.VECTOR -> UE_BlueprintVector
        PinType.OBJECT -> UE_BlueprintObject
    }
}
