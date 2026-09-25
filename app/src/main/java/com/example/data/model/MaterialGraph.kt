package com.example.data.model

enum class MaterialNodeType {
    RESULT,
    VECTOR3,
    SCALAR,
    TEXTURE_SAMPLE,
    MULTIPLY,
    ADD,
    FRESNEL,
    PANNER,
    SINE
}

data class MaterialNode(
    val id: String,
    val title: String,
    val type: MaterialNodeType,
    val x: Float,
    val y: Float,
    val scalarValue: Float = 1.0f,
    val colorHex: Long = 0xFFFFFFFF,
    val textureName: String = "T_Grid_PBR",
    val inputs: List<String> = emptyList(),
    val outputs: List<String> = emptyList()
)

data class MaterialConnection(
    val fromNodeId: String,
    val fromOutput: String,
    val toNodeId: String,
    val toInput: String
)

data class MaterialGraph(
    val id: String = "M_MainMaterial",
    val name: String = "M_NaniteCyberpunk",
    val nodes: List<MaterialNode> = emptyList(),
    val connections: List<MaterialConnection> = emptyList()
)
