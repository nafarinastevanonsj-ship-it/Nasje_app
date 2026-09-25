package com.example.data.repository

import com.example.data.db.LevelDao
import com.example.data.db.LevelEntity
import com.example.data.model.Actor
import com.example.data.model.BlueprintGraph
import com.example.data.model.MaterialGraph
import com.example.data.model.MeshType
import com.example.data.model.MobilityType
import com.example.data.model.MaterialData
import com.example.data.model.PhysicsData
import com.example.data.model.ProjectLevel
import com.example.data.model.Vec3
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class LevelRepository(private val levelDao: LevelDao) {

    fun getAllLevels(): Flow<List<LevelEntity>> = levelDao.getAllLevels()

    suspend fun loadLevel(id: String): ProjectLevel? {
        val entity = levelDao.getLevelById(id) ?: return null
        return parseProjectLevel(entity)
    }

    suspend fun saveLevel(level: ProjectLevel, blueprint: BlueprintGraph? = null, material: MaterialGraph? = null) {
        val actorsJson = serializeActors(level.actors)
        val entity = LevelEntity(
            id = level.id,
            name = level.name,
            description = level.description,
            lastModified = System.currentTimeMillis(),
            actorsJson = actorsJson,
            blueprintJson = blueprint?.id ?: "",
            materialJson = material?.id ?: ""
        )
        levelDao.insertLevel(entity)
    }

    suspend fun deleteLevel(id: String) {
        val entity = levelDao.getLevelById(id)
        if (entity != null) {
            levelDao.deleteLevel(entity)
        }
    }

    suspend fun initDefaultLevelsIfNeeded() {
        if (levelDao.getLevelCount() == 0) {
            val defaultLevel = createDefaultAncientRuinsLevel()
            saveLevel(defaultLevel)

            val cityLevel = createDefaultCyberpunkLevel()
            saveLevel(cityLevel)
        }
    }

    fun createDefaultAncientRuinsLevel(): ProjectLevel {
        val actors = listOf(
            Actor(
                id = "sun_01",
                name = "DirectionalLight_Sun",
                meshType = MeshType.DIRECTIONAL_LIGHT,
                location = Vec3(0f, 0f, 15f),
                rotation = Vec3(-45f, 35f, 0f),
                material = MaterialData("M_Sun_Yellow", 0xFFFFE890, emissiveStrength = 5f),
                folder = "Lights"
            ),
            Actor(
                id = "arch_01",
                name = "AncientTempleArch_Nanite",
                meshType = MeshType.TEMPLE_ARCH,
                location = Vec3(0f, 0f, 0f),
                scale = Vec3(2.5f, 2.5f, 2.5f),
                material = MaterialData("M_Ancient_Sandstone", 0xFFC2B29A, roughness = 0.85f, metallic = 0.05f),
                folder = "Architecture"
            ),
            Actor(
                id = "pillar_01",
                name = "Pillar_Left_Nanite",
                meshType = MeshType.NANITE_PILLAR,
                location = Vec3(-4f, -2f, 0f),
                scale = Vec3(1f, 1f, 1.8f),
                material = MaterialData("M_Ancient_Granite", 0xFF8A847C, roughness = 0.7f),
                folder = "Architecture"
            ),
            Actor(
                id = "pillar_02",
                name = "Pillar_Right_Nanite",
                meshType = MeshType.NANITE_PILLAR,
                location = Vec3(4f, -2f, 0f),
                scale = Vec3(1f, 1f, 1.8f),
                material = MaterialData("M_Ancient_Granite", 0xFF8A847C, roughness = 0.7f),
                folder = "Architecture"
            ),
            Actor(
                id = "energy_core",
                name = "LumenEnergyCore_Sphere",
                meshType = MeshType.SPHERE,
                location = Vec3(0f, 0f, 3.5f),
                scale = Vec3(1.2f, 1.2f, 1.2f),
                material = MaterialData("M_Lumen_CyanGlow", 0xFF00E5FF, emissiveHex = 0xFF00E5FF, emissiveStrength = 3.5f),
                folder = "FX"
            ),
            Actor(
                id = "crate_01",
                name = "PhysicsCrate_A",
                meshType = MeshType.SCIFI_CRATE,
                location = Vec3(-2f, 3f, 0.8f),
                scale = Vec3(1.2f, 1.2f, 1.2f),
                material = MaterialData("M_SciFi_Bronze", 0xFFCD7F32, metallic = 0.8f, roughness = 0.3f),
                physics = PhysicsData(simulatePhysics = true, massKg = 35f),
                folder = "Props"
            ),
            Actor(
                id = "mannequin_01",
                name = "BP_ThirdPersonCharacter",
                meshType = MeshType.CHARACTER_PAWN,
                location = Vec3(0f, -5f, 0f),
                material = MaterialData("M_UE5_Mannequin", 0xFFDDDDDD, roughness = 0.4f, metallic = 0.1f),
                folder = "Characters"
            ),
            Actor(
                id = "camera_main",
                name = "CineCameraActor_Hero",
                meshType = MeshType.CAMERA,
                location = Vec3(0f, -8f, 4f),
                rotation = Vec3(-18f, 0f, 0f),
                folder = "Cinematics"
            )
        )

        return ProjectLevel(
            id = "L_AncientRuins_01",
            name = "Ancient Ruins & Valley (Nanite & Lumen)",
            description = "High-poly ancient architecture illuminated with Lumen global ray bounces",
            actors = actors
        )
    }

    fun createDefaultCyberpunkLevel(): ProjectLevel {
        val actors = listOf(
            Actor(
                id = "sun_cyber",
                name = "SkyAtmosphere_Midnight",
                meshType = MeshType.DIRECTIONAL_LIGHT,
                location = Vec3(0f, 0f, 20f),
                rotation = Vec3(-60f, 45f, 0f),
                material = MaterialData("M_Moonlight", 0xFF667799, emissiveStrength = 2f),
                folder = "Lighting"
            ),
            Actor(
                id = "cyber_tower_1",
                name = "CyberTower_North",
                meshType = MeshType.NANITE_PILLAR,
                location = Vec3(-6f, 6f, 0f),
                scale = Vec3(2f, 2f, 4f),
                material = MaterialData("M_Dark_Chrome", 0xFF1C222E, metallic = 0.9f, roughness = 0.15f),
                folder = "City"
            ),
            Actor(
                id = "cyber_tower_2",
                name = "CyberTower_East",
                meshType = MeshType.NANITE_PILLAR,
                location = Vec3(6f, 6f, 0f),
                scale = Vec3(2f, 2f, 3.5f),
                material = MaterialData("M_Dark_Chrome", 0xFF1C222E, metallic = 0.9f, roughness = 0.15f),
                folder = "City"
            ),
            Actor(
                id = "neon_ring",
                name = "Hologram_PortalRing",
                meshType = MeshType.TORUS,
                location = Vec3(0f, 4f, 3f),
                rotation = Vec3(90f, 0f, 0f),
                scale = Vec3(2f, 2f, 2f),
                material = MaterialData("M_Neon_Magenta", 0xFFFF007F, emissiveHex = 0xFFFF007F, emissiveStrength = 4.0f),
                folder = "FX"
            ),
            Actor(
                id = "crate_cyber_1",
                name = "CargoContainer_Heavy",
                meshType = MeshType.SCIFI_CRATE,
                location = Vec3(-1f, 1f, 0.7f),
                scale = Vec3(1.5f, 1.5f, 1.5f),
                material = MaterialData("M_Alloy_Gold", 0xFFFFB300, metallic = 0.85f, roughness = 0.2f),
                physics = PhysicsData(simulatePhysics = true, massKg = 75f),
                folder = "Props"
            ),
            Actor(
                id = "hero_pawn",
                name = "BP_CyberRunner",
                meshType = MeshType.CHARACTER_PAWN,
                location = Vec3(0f, -4f, 0f),
                material = MaterialData("M_CyberSuit", 0xFF00E5FF, metallic = 0.6f, roughness = 0.2f),
                folder = "Characters"
            )
        )

        return ProjectLevel(
            id = "L_Cyberpunk_02",
            name = "Cyberpunk Neo-Metropolis",
            description = "Neon city level featuring reflective metallic materials and emissive Lumen surfaces",
            actors = actors
        )
    }

    private fun serializeActors(actors: List<Actor>): String {
        val array = JSONArray()
        for (actor in actors) {
            val obj = JSONObject()
            obj.put("id", actor.id)
            obj.put("name", actor.name)
            obj.put("meshType", actor.meshType.name)
            obj.put("posX", actor.location.x.toDouble())
            obj.put("posY", actor.location.y.toDouble())
            obj.put("posZ", actor.location.z.toDouble())
            obj.put("rotP", actor.rotation.x.toDouble())
            obj.put("rotY", actor.rotation.y.toDouble())
            obj.put("rotR", actor.rotation.z.toDouble())
            obj.put("scaleX", actor.scale.x.toDouble())
            obj.put("scaleY", actor.scale.y.toDouble())
            obj.put("scaleZ", actor.scale.z.toDouble())
            obj.put("mobility", actor.mobility.name)
            obj.put("matName", actor.material.name)
            obj.put("matColor", actor.material.baseColorHex)
            obj.put("matMetallic", actor.material.metallic.toDouble())
            obj.put("matRoughness", actor.material.roughness.toDouble())
            obj.put("matEmissive", actor.material.emissiveHex)
            obj.put("matEmissiveStrength", actor.material.emissiveStrength.toDouble())
            obj.put("physSim", actor.physics.simulatePhysics)
            obj.put("physMass", actor.physics.massKg.toDouble())
            obj.put("isNanite", actor.isNaniteEnabled)
            obj.put("isLumen", actor.isLumenEnabled)
            obj.put("folder", actor.folder)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseProjectLevel(entity: LevelEntity): ProjectLevel {
        val actors = mutableListOf<Actor>()
        try {
            val array = JSONArray(entity.actorsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val meshType = try {
                    MeshType.valueOf(obj.optString("meshType", "CUBE"))
                } catch (e: Exception) {
                    MeshType.CUBE
                }
                val mobility = try {
                    MobilityType.valueOf(obj.optString("mobility", "MOVABLE"))
                } catch (e: Exception) {
                    MobilityType.MOVABLE
                }

                actors.add(
                    Actor(
                        id = obj.optString("id", "actor_$i"),
                        name = obj.optString("name", "Actor_$i"),
                        meshType = meshType,
                        location = Vec3(
                            obj.optDouble("posX", 0.0).toFloat(),
                            obj.optDouble("posY", 0.0).toFloat(),
                            obj.optDouble("posZ", 0.0).toFloat()
                        ),
                        rotation = Vec3(
                            obj.optDouble("rotP", 0.0).toFloat(),
                            obj.optDouble("rotY", 0.0).toFloat(),
                            obj.optDouble("rotR", 0.0).toFloat()
                        ),
                        scale = Vec3(
                            obj.optDouble("scaleX", 1.0).toFloat(),
                            obj.optDouble("scaleY", 1.0).toFloat(),
                            obj.optDouble("scaleZ", 1.0).toFloat()
                        ),
                        mobility = mobility,
                        material = MaterialData(
                            name = obj.optString("matName", "M_Default"),
                            baseColorHex = obj.optLong("matColor", 0xFF888888),
                            metallic = obj.optDouble("matMetallic", 0.1).toFloat(),
                            roughness = obj.optDouble("matRoughness", 0.5).toFloat(),
                            emissiveHex = obj.optLong("matEmissive", 0xFF000000),
                            emissiveStrength = obj.optDouble("matEmissiveStrength", 0.0).toFloat()
                        ),
                        physics = PhysicsData(
                            simulatePhysics = obj.optBoolean("physSim", false),
                            massKg = obj.optDouble("physMass", 50.0).toFloat()
                        ),
                        isNaniteEnabled = obj.optBoolean("isNanite", true),
                        isLumenEnabled = obj.optBoolean("isLumen", true),
                        folder = obj.optString("folder", "Default")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback if json parsing error
        }

        return ProjectLevel(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            actors = actors
        )
    }
}
