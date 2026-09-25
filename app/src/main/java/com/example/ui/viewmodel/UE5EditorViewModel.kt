package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.UE5Database
import com.example.data.model.*
import com.example.data.repository.LevelRepository
import com.example.engine.physics.CharacterState
import com.example.engine.physics.PhysicsSimulator
import com.example.engine.render3d.Camera3D
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class UE5EditorUiState(
    val currentLevel: ProjectLevel = ProjectLevel(),
    val savedLevelList: List<com.example.data.db.LevelEntity> = emptyList(),
    val selectedActorId: String? = "arch_01",
    val activeTab: EditorTab = EditorTab.VIEWPORT,
    val activeRenderMode: RenderMode = RenderMode.LIT,
    val activeGizmoMode: GizmoMode = GizmoMode.TRANSLATION,
    val isPlayingInEditor: Boolean = false,
    val isSimulating: Boolean = false,
    val camera: Camera3D = Camera3D(),
    val cameraSpeed: Float = 1.0f,
    val engineStats: EngineStats = EngineStats(),
    val consoleLogs: List<String> = listOf(
        "LogInit: Unreal Engine 5.4.3 Mobile Editor Initialized.",
        "LogRenderer: MobileHDR=1, VulkanES3.2, RHI=Vulkan.",
        "LogNanite: Nanite Virtualized Geometry pipeline initialized. 376 clusters active.",
        "LogLumen: Lumen Surface Cache and Global Illumination active.",
        "LogWorld: Level 'Ancient Ruins & Sci-Fi Valley' loaded successfully."
    ),
    val blueprintGraph: BlueprintGraph = createDefaultBlueprintGraph(),
    val materialGraph: MaterialGraph = createDefaultMaterialGraph(),
    val isContentBrowserOpen: Boolean = false,
    val isOutlinerOpen: Boolean = true,
    val isDetailsOpen: Boolean = true,
    val isOutputLogOpen: Boolean = false,
    val characterState: CharacterState = CharacterState(),
    val pieScore: Int = 0,
    val notificationMessage: String? = null,
    val isExecutingBlueprint: Boolean = false,
    val activePulseNodeId: String? = null
)

class UE5EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UE5Database.getInstance(application)
    private val repository = LevelRepository(db.levelDao())
    private val physicsSimulator = PhysicsSimulator()

    private val _uiState = MutableStateFlow(UE5EditorUiState())
    val uiState: StateFlow<UE5EditorUiState> = _uiState.asStateFlow()

    private var pieLoopJob: Job? = null
    private var statsTickerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initDefaultLevelsIfNeeded()
            repository.getAllLevels().collect { levels ->
                _uiState.update { it.copy(savedLevelList = levels) }
            }
        }

        // Load initial level
        val defaultLevel = repository.createDefaultAncientRuinsLevel()
        _uiState.update {
            it.copy(
                currentLevel = defaultLevel,
                selectedActorId = defaultLevel.actors.firstOrNull()?.id
            )
        }

        startStatsTicker()
    }

    private fun startStatsTicker() {
        statsTickerJob?.cancel()
        statsTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(500)
                val actors = _uiState.value.currentLevel.actors
                val totalTris = actors.sumOf { it.estimatedTriangles }
                val totalClusters = actors.sumOf { it.clusterCount }
                val drawCalls = actors.size * 2 + 18
                val jitterFps = 59.8f + (kotlin.random.Random.nextFloat() * 0.4f)
                val frameTime = 1000f / jitterFps

                _uiState.update { state ->
                    state.copy(
                        engineStats = state.engineStats.copy(
                            fps = jitterFps,
                            frameTimeMs = frameTime,
                            naniteTriangles = totalTris,
                            naniteClusters = totalClusters,
                            drawCalls = drawCalls
                        )
                    )
                }
            }
        }
    }

    fun selectActor(actorId: String?) {
        _uiState.update { it.copy(selectedActorId = actorId) }
    }

    fun setRenderMode(mode: RenderMode) {
        _uiState.update {
            it.copy(
                activeRenderMode = mode,
                consoleLogs = it.consoleLogs + "Cmd: viewmode ${mode.name.lowercase()}"
            )
        }
    }

    fun setGizmoMode(mode: GizmoMode) {
        _uiState.update { it.copy(activeGizmoMode = mode) }
    }

    fun setActiveTab(tab: EditorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleOutliner() {
        _uiState.update { it.copy(isOutlinerOpen = !it.isOutlinerOpen) }
    }

    fun toggleDetails() {
        _uiState.update { it.copy(isDetailsOpen = !it.isDetailsOpen) }
    }

    fun toggleContentBrowser() {
        _uiState.update { it.copy(isContentBrowserOpen = !it.isContentBrowserOpen) }
    }

    fun toggleOutputLog() {
        _uiState.update { it.copy(isOutputLogOpen = !it.isOutputLogOpen) }
    }

    fun setCameraSpeed(speed: Float) {
        _uiState.update { it.copy(cameraSpeed = speed) }
    }

    fun orbitCamera(deltaX: Float, deltaY: Float) {
        val cam = _uiState.value.camera
        val speed = 0.35f * _uiState.value.cameraSpeed
        val newYaw = cam.yaw + deltaX * speed
        val newPitch = (cam.pitch + deltaY * speed).coerceIn(-85f, 85f)
        _uiState.update { it.copy(camera = cam.copy(yaw = newYaw, pitch = newPitch)) }
    }

    fun panCamera(deltaX: Float, deltaY: Float) {
        val cam = _uiState.value.camera
        val factor = 0.02f * cam.distance * _uiState.value.cameraSpeed
        val rad = Math.toRadians(cam.yaw.toDouble()).toFloat()
        val forward = Vec3(kotlin.math.sin(rad), -kotlin.math.cos(rad), 0f)
        val right = Vec3(kotlin.math.cos(rad), kotlin.math.sin(rad), 0f)

        val newTarget = cam.target + (right * (-deltaX * factor)) + (Vec3(0f, 0f, 1f) * (deltaY * factor))
        _uiState.update { it.copy(camera = cam.copy(target = newTarget)) }
    }

    fun zoomCamera(factor: Float) {
        val cam = _uiState.value.camera
        val newDist = (cam.distance * factor).coerceIn(2.5f, 60f)
        _uiState.update { it.copy(camera = cam.copy(distance = newDist)) }
    }

    fun focusOnSelectedActor() {
        val selected = _uiState.value.currentLevel.actors.find { it.id == _uiState.value.selectedActorId } ?: return
        val cam = _uiState.value.camera
        _uiState.update {
            it.copy(
                camera = cam.copy(target = selected.location, distance = 6f),
                consoleLogs = it.consoleLogs + "LogViewport: Focus camera on Actor '${selected.name}'"
            )
        }
    }

    fun updateActorLocation(delta: Vec3) {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == id) it.copy(location = it.location + delta) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun setActorLocation(newLoc: Vec3) {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == id) it.copy(location = newLoc) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun setActorRotation(newRot: Vec3) {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == id) it.copy(rotation = newRot) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun setActorScale(newScale: Vec3) {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == id) it.copy(scale = newScale) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun setActorMaterial(material: MaterialData) {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == id) it.copy(material = material) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun toggleActorVisibility(actorId: String) {
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == actorId) it.copy(isVisible = !it.isVisible) else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun toggleActorPhysics(actorId: String) {
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == actorId) {
                    val p = it.physics
                    it.copy(physics = p.copy(simulatePhysics = !p.simulatePhysics))
                } else it
            }
            state.copy(currentLevel = state.currentLevel.copy(actors = updated))
        }
    }

    fun toggleActorNanite(actorId: String) {
        _uiState.update { state ->
            val updated = state.currentLevel.actors.map {
                if (it.id == actorId) it.copy(isNaniteEnabled = !it.isNaniteEnabled) else it
            }
            state.copy(
                currentLevel = state.currentLevel.copy(actors = updated),
                consoleLogs = state.consoleLogs + "LogNanite: Toggled Nanite for actor '$actorId'"
            )
        }
    }

    fun addActor(meshType: MeshType, name: String? = null) {
        val actorName = name ?: "${meshType.name.lowercase().replaceFirstChar { it.uppercase() }}_${UUID.randomUUID().toString().take(4)}"
        val newActor = Actor(
            id = "actor_${UUID.randomUUID().toString().take(6)}",
            name = actorName,
            meshType = meshType,
            location = _uiState.value.camera.target + Vec3(0f, 0f, 1f),
            material = when (meshType) {
                MeshType.NANITE_PILLAR -> MaterialData("M_Nanite_Pillar", 0xFF9E9E9E, roughness = 0.4f)
                MeshType.SCIFI_CRATE -> MaterialData("M_SciFi_Crate", 0xFFCD7F32, metallic = 0.8f, roughness = 0.3f)
                MeshType.SPHERE -> MaterialData("M_Energy_Glow", 0xFF00E5FF, emissiveHex = 0xFF00E5FF, emissiveStrength = 3f)
                else -> MaterialData("M_Basic_White", 0xFFD0D7DE, roughness = 0.5f)
            },
            folder = "UserActors"
        )

        _uiState.update { state ->
            state.copy(
                currentLevel = state.currentLevel.copy(actors = state.currentLevel.actors + newActor),
                selectedActorId = newActor.id,
                consoleLogs = state.consoleLogs + "LogWorld: Spawned new Actor '${newActor.name}' (${meshType.name})"
            )
        }
    }

    fun deleteSelectedActor() {
        val id = _uiState.value.selectedActorId ?: return
        _uiState.update { state ->
            val updated = state.currentLevel.actors.filter { it.id != id }
            state.copy(
                currentLevel = state.currentLevel.copy(actors = updated),
                selectedActorId = updated.firstOrNull()?.id,
                consoleLogs = state.consoleLogs + "LogWorld: Destroyed Actor '$id'"
            )
        }
    }

    fun duplicateSelectedActor() {
        val selected = _uiState.value.currentLevel.actors.find { it.id == _uiState.value.selectedActorId } ?: return
        val dup = selected.copy(
            id = "actor_${UUID.randomUUID().toString().take(6)}",
            name = "${selected.name}_Copy",
            location = selected.location + Vec3(1.5f, 0f, 0f)
        )
        _uiState.update { state ->
            state.copy(
                currentLevel = state.currentLevel.copy(actors = state.currentLevel.actors + dup),
                selectedActorId = dup.id,
                consoleLogs = state.consoleLogs + "LogWorld: Duplicated Actor '${dup.name}'"
            )
        }
    }

    // Play In Editor (PIE)
    fun startPlayInEditor() {
        val charActor = _uiState.value.currentLevel.actors.find { it.meshType == MeshType.CHARACTER_PAWN }
        val spawnPos = charActor?.location ?: Vec3(0f, -5f, 0f)

        _uiState.update {
            it.copy(
                isPlayingInEditor = true,
                characterState = CharacterState(position = spawnPos),
                consoleLogs = it.consoleLogs + "LogPlayLevel: Starting Play in Editor (PIE) on Mobile ES3.2..."
            )
        }

        pieLoopJob?.cancel()
        pieLoopJob = viewModelScope.launch {
            val dt = 0.033f // ~30-60 Hz loop
            while (_uiState.value.isPlayingInEditor && isActive) {
                delay(33)
                stepPlayInEditor(dt)
            }
        }
    }

    fun stopPlayInEditor() {
        pieLoopJob?.cancel()
        _uiState.update {
            it.copy(
                isPlayingInEditor = false,
                consoleLogs = it.consoleLogs + "LogPlayLevel: Stopped Play in Editor."
            )
        }
    }

    private fun stepPlayInEditor(dt: Float) {
        val state = _uiState.value
        val charState = state.characterState

        // Update physics bodies around player
        val updatedActors = physicsSimulator.stepPhysicsActors(
            actors = state.currentLevel.actors,
            charPos = charState.position,
            dt = dt
        )

        // Update camera to follow character in third person
        val cam = state.camera.copy(
            target = charState.position + Vec3(0f, 0f, 1.2f),
            distance = 6.0f,
            yaw = charState.yaw
        )

        _uiState.update {
            it.copy(
                characterState = charState,
                camera = cam,
                currentLevel = it.currentLevel.copy(actors = updatedActors)
            )
        }
    }

    fun updatePieInput(moveX: Float, moveY: Float, jump: Boolean) {
        val charState = _uiState.value.characterState
        physicsSimulator.updateCharacter(
            charState = charState,
            moveInput = Vec3(moveX, moveY, 0f),
            jumpRequested = jump,
            dt = 0.033f,
            actors = _uiState.value.currentLevel.actors
        )
        _uiState.update { it.copy(characterState = charState) }
    }

    fun rotatePieCamera(deltaYaw: Float) {
        val charState = _uiState.value.characterState
        charState.yaw += deltaYaw * 0.4f
        _uiState.update { it.copy(characterState = charState) }
    }

    // Blueprint Node execution simulation
    fun simulateBlueprintExecution() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExecutingBlueprint = true) }
            val nodes = _uiState.value.blueprintGraph.nodes
            for (node in nodes) {
                _uiState.update {
                    it.copy(
                        activePulseNodeId = node.id,
                        consoleLogs = it.consoleLogs + "LogBlueprintUserMessages: [${node.title}] executed successfully."
                    )
                }
                delay(600)
            }
            _uiState.update {
                it.copy(
                    isExecutingBlueprint = false,
                    activePulseNodeId = null,
                    notificationMessage = "Blueprint graph executed without errors!"
                )
            }
            delay(2000)
            _uiState.update { it.copy(notificationMessage = null) }
        }
    }

    fun addBlueprintNode(title: String, category: NodeCategory) {
        val newNode = BlueprintNode(
            id = "node_${UUID.randomUUID().toString().take(5)}",
            title = title,
            category = category,
            x = 200f + kotlin.random.Random.nextInt(100),
            y = 200f + kotlin.random.Random.nextInt(100),
            inputs = listOf(BlueprintPin("pin_in_${UUID.randomUUID().toString().take(4)}", "Exec", PinType.EXEC, false)),
            outputs = listOf(BlueprintPin("pin_out_${UUID.randomUUID().toString().take(4)}", "Then", PinType.EXEC, true))
        )
        _uiState.update {
            it.copy(
                blueprintGraph = it.blueprintGraph.copy(nodes = it.blueprintGraph.nodes + newNode),
                consoleLogs = it.consoleLogs + "LogBlueprint: Added node '$title'"
            )
        }
    }

    fun runConsoleCommand(cmd: String) {
        val trimmed = cmd.trim()
        val response = when (trimmed.lowercase()) {
            "stat fps" -> "Displaying engine frame rate overlay"
            "stat nanite" -> "Nanite Statistics: 376 clusters, 48,200 triangles, 100% LOD efficiency"
            "stat lumen" -> "Lumen Statistics: 24,500 active surface cache rays, 16.6ms budget"
            "r.nanite 1" -> "Nanite Virtualized Geometry: ENABLED"
            "r.nanite 0" -> "Nanite Virtualized Geometry: DISABLED (Fallback LOD)"
            "r.lumen.diffuseindirect 1" -> "Lumen Diffuse Indirect Lighting: ENABLED"
            "viewmode lit" -> {
                setRenderMode(RenderMode.LIT)
                "ViewMode set to Lit"
            }
            "viewmode nanite" -> {
                setRenderMode(RenderMode.NANITE_CLUSTERS)
                "ViewMode set to Nanite Clusters"
            }
            "viewmode lumen" -> {
                setRenderMode(RenderMode.LUMEN_GI)
                "ViewMode set to Lumen GI"
            }
            "viewmode wireframe" -> {
                setRenderMode(RenderMode.WIREFRAME)
                "ViewMode set to Wireframe"
            }
            "build geometry" -> "Geometry rebuild complete (0 errors, 0 warnings)"
            "help" -> "Commands: stat fps, stat nanite, stat lumen, viewmode lit/nanite/lumen/wireframe, build geometry"
            else -> "Command '$trimmed' executed in Editor context."
        }

        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + "Cmd: $trimmed" + "LogConsole: $response"
            )
        }
    }

    fun saveCurrentLevel() {
        viewModelScope.launch {
            val level = _uiState.value.currentLevel
            repository.saveLevel(level)
            _uiState.update {
                it.copy(
                    notificationMessage = "Level '${level.name}' saved to local storage!",
                    consoleLogs = it.consoleLogs + "LogWorld: Saved level '${level.id}'"
                )
            }
            delay(2000)
            _uiState.update { it.copy(notificationMessage = null) }
        }
    }

    fun loadLevelById(levelId: String) {
        viewModelScope.launch {
            val level = repository.loadLevel(levelId)
            if (level != null) {
                _uiState.update {
                    it.copy(
                        currentLevel = level,
                        selectedActorId = level.actors.firstOrNull()?.id,
                        consoleLogs = it.consoleLogs + "LogWorld: Opened level '${level.name}'"
                    )
                }
            }
        }
    }

    fun switchPresetLevel(preset: String) {
        val level = if (preset == "Ancient") {
            repository.createDefaultAncientRuinsLevel()
        } else {
            repository.createDefaultCyberpunkLevel()
        }
        _uiState.update {
            it.copy(
                currentLevel = level,
                selectedActorId = level.actors.firstOrNull()?.id,
                consoleLogs = it.consoleLogs + "LogWorld: Switched to '${level.name}'"
            )
        }
    }
}

private fun createDefaultBlueprintGraph(): BlueprintGraph {
    val node1 = BlueprintNode(
        id = "node_beginplay",
        title = "Event BeginPlay",
        category = NodeCategory.EVENT,
        x = 50f,
        y = 120f,
        inputs = emptyList(),
        outputs = listOf(
            BlueprintPin("p1", "", PinType.EXEC, true)
        ),
        subText = "Entry point on level start"
    )

    val node2 = BlueprintNode(
        id = "node_set_location",
        title = "Set Actor Location",
        category = NodeCategory.FUNCTION,
        x = 320f,
        y = 100f,
        inputs = listOf(
            BlueprintPin("p2", "", PinType.EXEC, false),
            BlueprintPin("p3", "Target", PinType.OBJECT, false),
            BlueprintPin("p4", "New Location", PinType.VECTOR, false, "0, 0, 5")
        ),
        outputs = listOf(
            BlueprintPin("p5", "", PinType.EXEC, true),
            BlueprintPin("p6", "Return Value", PinType.BOOLEAN, true)
        )
    )

    val node3 = BlueprintNode(
        id = "node_print",
        title = "Print String",
        category = NodeCategory.FUNCTION,
        x = 640f,
        y = 120f,
        inputs = listOf(
            BlueprintPin("p7", "", PinType.EXEC, false),
            BlueprintPin("p8", "In String", PinType.OBJECT, false, "Nanite & Lumen Online!"),
            BlueprintPin("p9", "Print to Screen", PinType.BOOLEAN, false, "true")
        ),
        outputs = listOf(
            BlueprintPin("p10", "", PinType.EXEC, true)
        )
    )

    val conn1 = BlueprintConnection("node_beginplay", "p1", "node_set_location", "p2")
    val conn2 = BlueprintConnection("node_set_location", "p5", "node_print", "p7")

    return BlueprintGraph(
        id = "BP_PlayerController",
        name = "BP_PlayerController (EventGraph)",
        nodes = listOf(node1, node2, node3),
        connections = listOf(conn1, conn2)
    )
}

private fun createDefaultMaterialGraph(): MaterialGraph {
    val resultNode = MaterialNode(
        id = "mat_result",
        title = "M_NaniteCyberpunk (PBR Result)",
        type = MaterialNodeType.RESULT,
        x = 520f,
        y = 120f,
        inputs = listOf("Base Color", "Metallic", "Specular", "Roughness", "Emissive Color", "Normal", "Ambient Occlusion")
    )

    val colorNode = MaterialNode(
        id = "mat_basecolor",
        title = "VectorParameter (BaseColor)",
        type = MaterialNodeType.VECTOR3,
        x = 80f,
        y = 80f,
        colorHex = 0xFF00E5FF,
        outputs = listOf("RGB", "R", "G", "B")
    )

    val roughNode = MaterialNode(
        id = "mat_roughness",
        title = "Scalar (Roughness)",
        type = MaterialNodeType.SCALAR,
        x = 100f,
        y = 280f,
        scalarValue = 0.25f,
        outputs = listOf("Value")
    )

    val metallicNode = MaterialNode(
        id = "mat_metallic",
        title = "Scalar (Metallic)",
        type = MaterialNodeType.SCALAR,
        x = 100f,
        y = 420f,
        scalarValue = 0.85f,
        outputs = listOf("Value")
    )

    val connections = listOf(
        MaterialConnection("mat_basecolor", "RGB", "mat_result", "Base Color"),
        MaterialConnection("mat_roughness", "Value", "mat_result", "Roughness"),
        MaterialConnection("mat_metallic", "Value", "mat_result", "Metallic")
    )

    return MaterialGraph(
        id = "M_NaniteCyberpunk",
        name = "M_NaniteCyberpunk (Shader Graph)",
        nodes = listOf(resultNode, colorNode, roughNode, metallicNode),
        connections = connections
    )
}
