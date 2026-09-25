package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun UE5OutputLog(
    logs: List<String>,
    onSendCommand: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cmdText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF0D0F12))
            .border(0.5.dp, UE_PanelBorder)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UE_SurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = UE_AccentCyan, modifier = Modifier.size(16.dp))
                Text("Output Log & Console (Cmd)", color = UE_TextPrimary, fontSize = 11.sp)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = UE_TextSecondary, modifier = Modifier.size(14.dp))
            }
        }

        // Log Items
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(logs) { log ->
                val color = when {
                    log.startsWith("Cmd:") -> Color(0xFF00E5FF)
                    log.contains("Nanite", ignoreCase = true) -> Color(0xFFFF80AB)
                    log.contains("Lumen", ignoreCase = true) -> Color(0xFFFFD54F)
                    log.contains("Error", ignoreCase = true) -> Color(0xFFFF5252)
                    log.contains("Play", ignoreCase = true) -> Color(0xFF69F0AE)
                    else -> UE_TextSecondary
                }
                Text(
                    text = log,
                    color = color,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Cmd Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UE_Surface)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Cmd >", color = UE_AccentCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            OutlinedTextField(
                value = cmdText,
                onValueChange = { cmdText = it },
                placeholder = { Text("e.g. stat nanite, stat fps, viewmode lumen...", fontSize = 10.sp, color = UE_TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(4.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = UE_Background,
                    unfocusedContainerColor = UE_Background,
                    focusedBorderColor = UE_AccentCyan,
                    unfocusedBorderColor = UE_PanelBorder,
                    focusedTextColor = UE_TextPrimary,
                    unfocusedTextColor = UE_TextPrimary
                )
            )
            IconButton(
                onClick = {
                    if (cmdText.isNotBlank()) {
                        onSendCommand(cmdText)
                        cmdText = ""
                    }
                },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Execute", tint = UE_AccentCyan, modifier = Modifier.size(16.dp))
            }
        }
    }
}
