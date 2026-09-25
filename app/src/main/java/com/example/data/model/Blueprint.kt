package com.example.data.model

enum class PinType {
    EXEC,
    BOOLEAN,
    FLOAT,
    INTEGER,
    VECTOR,
    OBJECT
}

enum class NodeCategory {
    EVENT,
    FUNCTION,
    FLOW_CONTROL,
    MATH,
    GAMEPLAY
}

data class BlueprintPin(
    val id: String,
    val name: String,
    val type: PinType,
    val isOutput: Boolean,
    val defaultValue: String = ""
)

data class BlueprintNode(
    val id: String,
    val title: String,
    val category: NodeCategory,
    val x: Float,
    val y: Float,
    val inputs: List<BlueprintPin>,
    val outputs: List<BlueprintPin>,
    val subText: String = ""
)

data class BlueprintConnection(
    val fromNodeId: String,
    val fromPinId: String,
    val toNodeId: String,
    val toPinId: String
)

data class BlueprintGraph(
    val id: String = "BP_LevelScript",
    val name: String = "Level Blueprint",
    val nodes: List<BlueprintNode> = emptyList(),
    val connections: List<BlueprintConnection> = emptyList()
)
