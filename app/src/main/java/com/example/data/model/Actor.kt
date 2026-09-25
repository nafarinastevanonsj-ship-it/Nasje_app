package com.example.data.model

data class Vec3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vec3 = if (scalar != 0f) Vec3(x / scalar, y / scalar, z / scalar) else Vec3()

    fun length(): Float = kotlin.math.sqrt(x * x + y * y + z * z)
    fun normalized(): Vec3 {
        val l = length()
        return if (l > 0.0001f) this / l else Vec3(0f, 0f, 0f)
    }

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}

enum class MeshType(val displayName: String, val baseTriangles: Int) {
    CUBE("StaticMesh'Cube'", 12),
    SPHERE("StaticMesh'Sphere'", 240),
    CYLINDER("StaticMesh'Cylinder'", 96),
    CONE("StaticMesh'Cone'", 48),
    TORUS("StaticMesh'Torus'", 192),
    NANITE_PILLAR("Nanite'CyberPillar'", 14400),
    SCIFI_CRATE("StaticMesh'SciFiCrate'", 480),
    TEMPLE_ARCH("Nanite'AncientArch'", 28800),
    CHARACTER_PAWN("SkeletalMesh'UE5_Mannequin'", 8600),
    DIRECTIONAL_LIGHT("Light'SunDirectional'", 16),
    POINT_LIGHT("Light'OmniPoint'", 32),
    CAMERA("Camera'CineCameraActor'", 24)
}

enum class MobilityType {
    STATIC,
    STATIONARY,
    MOVABLE
}

data class MaterialData(
    val name: String = "M_Default_Grey",
    val baseColorHex: Long = 0xFF888888,
    val metallic: Float = 0.1f,
    val roughness: Float = 0.5f,
    val emissiveHex: Long = 0xFF000000,
    val emissiveStrength: Float = 0.0f
)

data class PhysicsData(
    val simulatePhysics: Boolean = false,
    val massKg: Float = 50f,
    val linearDamping: Float = 0.05f,
    val velocity: Vec3 = Vec3(),
    val angularVelocity: Vec3 = Vec3()
)

data class Actor(
    val id: String,
    val name: String,
    val meshType: MeshType = MeshType.CUBE,
    val location: Vec3 = Vec3(),
    val rotation: Vec3 = Vec3(), // Pitch, Yaw, Roll in degrees
    val scale: Vec3 = Vec3(1f, 1f, 1f),
    val mobility: MobilityType = MobilityType.MOVABLE,
    val material: MaterialData = MaterialData(),
    val physics: PhysicsData = PhysicsData(),
    val isNaniteEnabled: Boolean = true,
    val isLumenEnabled: Boolean = true,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val folder: String = "Default"
) {
    val estimatedTriangles: Int
        get() = if (isNaniteEnabled && (meshType == MeshType.NANITE_PILLAR || meshType == MeshType.TEMPLE_ARCH)) {
            meshType.baseTriangles
        } else {
            meshType.baseTriangles
        }

    val clusterCount: Int
        get() = (estimatedTriangles / 128).coerceAtLeast(1)
}
