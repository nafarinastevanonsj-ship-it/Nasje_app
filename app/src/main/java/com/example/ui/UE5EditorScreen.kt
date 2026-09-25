package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EditorTab
import com.example.ui.components.*
import com.example.ui.theme.UE_Background
import com.example.ui.viewmodel.UE5EditorViewModel

@Composable
fun UE5EditorScreen(
    viewModel: UE5EditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewActorDialog by remember { mutableStateOf(false) }

    val selectedActor = remember(uiState.currentLevel.actors, uiState.selectedActorId) {
        uiState.currentLevel.actors.find { it.id == uiState.selectedActorId }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UE_Background,
        snackbarHost = {
            if (uiState.notificationMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF00E5FF)
                ) {
                    Text(uiState.notificationMessage!!)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // If in Play in Editor (PIE) mode, render fullscreen gameplay mode
            if (uiState.isPlayingInEditor) {
                UE5PlayInEditor(
                    actors = uiState.currentLevel.actors,
                    camera = uiState.camera,
                    characterState = uiState.characterState,
                    fps = uiState.engineStats.fps,
                    onMoveInput = { mx, my, jump ->
                        viewModel.updatePieInput(mx, my, jump)
                    },
                    onRotateCamera = { dy ->
                        viewModel.rotatePieCamera(dy)
                    },
                    onStopClicked = {
                        viewModel.stopPlayInEditor()
                    }
                )
            } else {
                // Top Unreal Engine 5 Header Bar
                UE5TopMenuBar(
                    projectName = uiState.currentLevel.name,
                    activeTab = uiState.activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) },
                    isPlaying = uiState.isPlayingInEditor,
                    onPlayClicked = { viewModel.startPlayInEditor() },
                    onStopClicked = { viewModel.stopPlayInEditor() },
                    stats = uiState.engineStats,
                    onSaveClicked = { viewModel.saveCurrentLevel() },
                    onSwitchPreset = { viewModel.switchPresetLevel(it) },
                    onToggleOutliner = { viewModel.toggleOutliner() },
                    onToggleDetails = { viewModel.toggleDetails() },
                    onToggleBrowser = { viewModel.toggleContentBrowser() },
                    onToggleLog = { viewModel.toggleOutputLog() }
                )

                // Main Workspace Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (uiState.activeTab) {
                        EditorTab.VIEWPORT -> {
                            UE5Viewport(
                                actors = uiState.currentLevel.actors,
                                camera = uiState.camera,
                                selectedActorId = uiState.selectedActorId,
                                renderMode = uiState.activeRenderMode,
                                gizmoMode = uiState.activeGizmoMode,
                                cameraSpeed = uiState.cameraSpeed,
                                onSelectActor = { viewModel.selectActor(it) },
                                onRenderModeChanged = { viewModel.setRenderMode(it) },
                                onGizmoModeChanged = { viewModel.setGizmoMode(it) },
                                onCameraSpeedChanged = { viewModel.setCameraSpeed(it) },
                                onOrbitCamera = { dx, dy -> viewModel.orbitCamera(dx, dy) },
                                onPanCamera = { dx, dy -> viewModel.panCamera(dx, dy) },
                                onZoomCamera = { f -> viewModel.zoomCamera(f) },
                                onFocusSelected = { viewModel.focusOnSelectedActor() },
                                onAddActorClicked = { showNewActorDialog = true }
                            )

                            // Collapsible Outliner on Left
                            androidx.compose.animation.AnimatedVisibility(
                                visible = uiState.isOutlinerOpen,
                                enter = slideInHorizontally { -it } + fadeIn(),
                                exit = slideOutHorizontally { -it } + fadeOut(),
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                UE5Outliner(
                                    actors = uiState.currentLevel.actors,
                                    selectedActorId = uiState.selectedActorId,
                                    onSelectActor = { viewModel.selectActor(it) },
                                    onToggleVisibility = { viewModel.toggleActorVisibility(it) },
                                    onAddActor = { showNewActorDialog = true },
                                    onDuplicateActor = { viewModel.duplicateSelectedActor() },
                                    onDeleteActor = { viewModel.deleteSelectedActor() },
                                    onClose = { viewModel.toggleOutliner() }
                                )
                            }

                            // Collapsible Details on Right
                            androidx.compose.animation.AnimatedVisibility(
                                visible = uiState.isDetailsOpen,
                                enter = slideInHorizontally { it } + fadeIn(),
                                exit = slideOutHorizontally { it } + fadeOut(),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                UE5DetailsPanel(
                                    actor = selectedActor,
                                    onLocationChanged = { viewModel.setActorLocation(it) },
                                    onRotationChanged = { viewModel.setActorRotation(it) },
                                    onScaleChanged = { viewModel.setActorScale(it) },
                                    onMaterialChanged = { viewModel.setActorMaterial(it) },
                                    onTogglePhysics = {
                                        if (selectedActor != null) viewModel.toggleActorPhysics(selectedActor.id)
                                    },
                                    onToggleNanite = {
                                        if (selectedActor != null) viewModel.toggleActorNanite(selectedActor.id)
                                    },
                                    onClose = { viewModel.toggleDetails() }
                                )
                            }
                        }

                        EditorTab.BLUEPRINT -> {
                            UE5BlueprintEditor(
                                graph = uiState.blueprintGraph,
                                isExecuting = uiState.isExecutingBlueprint,
                                activePulseNodeId = uiState.activePulseNodeId,
                                onExecuteBlueprint = { viewModel.simulateBlueprintExecution() },
                                onAddNode = { title, cat -> viewModel.addBlueprintNode(title, cat) }
                            )
                        }

                        EditorTab.MATERIAL -> {
                            UE5MaterialEditor(
                                graph = uiState.materialGraph
                            )
                        }

                        else -> {
                            UE5Viewport(
                                actors = uiState.currentLevel.actors,
                                camera = uiState.camera,
                                selectedActorId = uiState.selectedActorId,
                                renderMode = uiState.activeRenderMode,
                                gizmoMode = uiState.activeGizmoMode,
                                cameraSpeed = uiState.cameraSpeed,
                                onSelectActor = { viewModel.selectActor(it) },
                                onRenderModeChanged = { viewModel.setRenderMode(it) },
                                onGizmoModeChanged = { viewModel.setGizmoMode(it) },
                                onCameraSpeedChanged = { viewModel.setCameraSpeed(it) },
                                onOrbitCamera = { dx, dy -> viewModel.orbitCamera(dx, dy) },
                                onPanCamera = { dx, dy -> viewModel.panCamera(dx, dy) },
                                onZoomCamera = { f -> viewModel.zoomCamera(f) },
                                onFocusSelected = { viewModel.focusOnSelectedActor() },
                                onAddActorClicked = { showNewActorDialog = true }
                            )
                        }
                    }

                    // Content Browser Drawer (Bottom)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.isContentBrowserOpen,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        UE5ContentBrowser(
                            onSpawnMesh = { meshType ->
                                viewModel.addActor(meshType)
                            },
                            onOpenBlueprint = {
                                viewModel.setActiveTab(EditorTab.BLUEPRINT)
                                viewModel.toggleContentBrowser()
                            },
                            onOpenMaterial = {
                                viewModel.setActiveTab(EditorTab.MATERIAL)
                                viewModel.toggleContentBrowser()
                            },
                            onClose = { viewModel.toggleContentBrowser() }
                        )
                    }

                    // Output Log & Console Drawer (Bottom)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.isOutputLogOpen,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        UE5OutputLog(
                            logs = uiState.consoleLogs,
                            onSendCommand = { cmd -> viewModel.runConsoleCommand(cmd) },
                            onClose = { viewModel.toggleOutputLog() }
                        )
                    }
                }
            }
        }

        // Place Actor Dialog
        if (showNewActorDialog) {
            UE5NewActorDialog(
                onSelectMeshType = { meshType ->
                    viewModel.addActor(meshType)
                },
                onDismiss = { showNewActorDialog = false }
            )
        }
    }
}
