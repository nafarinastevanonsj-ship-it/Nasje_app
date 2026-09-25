package com.example.data.model

enum class RenderMode(val label: String, val badge: String) {
    LIT("Lit", "LUMEN ON"),
    UNLIT("Unlit", "NO LIGHT"),
    WIREFRAME("Wireframe", "POLYS"),
    NANITE_CLUSTERS("Nanite Clusters", "NANITE"),
    LUMEN_GI("Lumen GI Radiance", "LUMEN"),
    COLLISION("Collision", "CHAOS")
}

enum class GizmoMode {
    TRANSLATION,
    ROTATION,
    SCALE
}

enum class TransformCoordSpace {
    WORLD,
    LOCAL
}

enum class EditorTab {
    VIEWPORT,
    BLUEPRINT,
    MATERIAL,
    SEQUENCER,
    PROJECT_SETTINGS
}

data class EngineStats(
    val fps: Float = 60.0f,
    val frameTimeMs: Float = 16.6f,
    val drawCalls: Int = 142,
    val naniteTriangles: Int = 48200,
    val naniteClusters: Int = 376,
    val lumenRayCount: Int = 24500,
    val memoryUsageMb: Int = 780
)

data class ProjectLevel(
    val id: String = "L_AncientRuins_01",
    val name: String = "Ancient Ruins & Sci-Fi Valley",
    val description: String = "UE5 Demonstration scene showcasing Nanite Virtualized Geometry and Lumen Global Illumination",
    val actors: List<Actor> = emptyList(),
    val activeRenderMode: RenderMode = RenderMode.LIT,
    val isLumenActive: Boolean = true,
    val isNaniteActive: Boolean = true
)
