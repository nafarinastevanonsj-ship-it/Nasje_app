package com.example.engine.render3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.Actor
import com.example.data.model.GizmoMode
import com.example.data.model.MeshType
import com.example.data.model.RenderMode
import com.example.data.model.Vec3
import kotlin.math.*

data class RenderPolygon(
    val p0: Offset,
    val p1: Offset,
    val p2: Offset,
    val depth: Float,
    val color: Color,
    val wireColor: Color? = null,
    val isGizmo: Boolean = false,
    val isSelectedActor: Boolean = false
)

object Renderer3D {

    private val naniteClusterColors = listOf(
        Color(0xFFFF5252),
        Color(0xFFFF7A00),
        Color(0xFFFFD600),
        Color(0xFF00E676),
        Color(0xFF00B0FF),
        Color(0xFF7C4DFF),
        Color(0xFFFF4081),
        Color(0xFF69F0AE)
    )

    fun renderScene(
        drawScope: DrawScope,
        actors: List<Actor>,
        camera: Camera3D,
        renderMode: RenderMode,
        selectedActorId: String?,
        gizmoMode: GizmoMode,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        val aspect = canvasWidth / canvasHeight.coerceAtLeast(1f)
        val proj = Mat4.perspective(camera.fov, aspect, 0.1f, 100f)
        val view = camera.getViewMatrix()
        val viewProj = proj * view

        // 1. Draw 3D Ground Grid
        drawGroundGrid(drawScope, viewProj, canvasWidth, canvasHeight, renderMode)

        // 2. Collect and project all scene polygons
        val polyList = mutableListOf<RenderPolygon>()
        val sunDir = Vec3(0.5f, -0.6f, 0.8f).normalized()
        val eyePos = camera.getEyePosition()

        for (actor in actors) {
            if (!actor.isVisible) continue

            val isSelected = actor.id == selectedActorId
            val mesh = MeshFactory.getMesh(actor.meshType)

            // Model Matrix: Translation * Rotation * Scale
            val rotMat = Mat4.euler(actor.rotation.x, actor.rotation.y, actor.rotation.z)
            val scaleMat = Mat4.scale(actor.scale.x, actor.scale.y, actor.scale.z)
            val transMat = Mat4.translation(actor.location.x, actor.location.y, actor.location.z)
            val model = transMat * (rotMat * scaleMat)

            // Transform vertices to world space and screen space
            val worldVerts = ArrayList<Vec3>(mesh.vertices.size)
            val screenVerts = ArrayList<Offset?>(mesh.vertices.size)
            val clipZ = ArrayList<Float>(mesh.vertices.size)

            for (v in mesh.vertices) {
                val (wPos, _) = model.transform(v.pos, 1f)
                worldVerts.add(wPos)

                val (cPos, wClip) = viewProj.transform(wPos, 1f)
                if (wClip > 0.05f) {
                    val ndcX = cPos.x / wClip
                    val ndcY = cPos.y / wClip
                    val screenX = (ndcX * 0.5f + 0.5f) * canvasWidth
                    val screenY = (-ndcY * 0.5f + 0.5f) * canvasHeight
                    screenVerts.add(Offset(screenX, screenY))
                    clipZ.add(wClip)
                } else {
                    screenVerts.add(null)
                    clipZ.add(wClip)
                }
            }

            // Project faces
            for (face in mesh.faces) {
                val s0 = screenVerts[face.i0] ?: continue
                val s1 = screenVerts[face.i1] ?: continue
                val s2 = screenVerts[face.i2] ?: continue

                // 2D screen backface culling
                val cross2D = (s1.x - s0.x) * (s2.y - s0.y) - (s1.y - s0.y) * (s2.x - s0.x)
                if (cross2D <= 0f && renderMode != RenderMode.WIREFRAME && !isSelected) {
                    continue
                }

                val avgDepth = (clipZ[face.i0] + clipZ[face.i1] + clipZ[face.i2]) / 3f
                if (avgDepth <= 0.1f) continue

                // Compute world normal
                val w0 = worldVerts[face.i0]
                val w1 = worldVerts[face.i1]
                val w2 = worldVerts[face.i2]
                val faceNorm = (w1 - w0).cross(w2 - w0).normalized()

                val polyColor = computeShadingColor(
                    renderMode = renderMode,
                    actor = actor,
                    normal = faceNorm,
                    sunDir = sunDir,
                    viewDir = (eyePos - w0).normalized(),
                    clusterId = face.clusterId
                )

                val wire = if (renderMode == RenderMode.WIREFRAME) {
                    if (isSelected) Color(0xFFFF9800) else Color(0xFF00E5FF)
                } else if (isSelected) {
                    Color(0xFFFFB300)
                } else null

                polyList.add(
                    RenderPolygon(
                        p0 = s0,
                        p1 = s1,
                        p2 = s2,
                        depth = avgDepth,
                        color = polyColor,
                        wireColor = wire,
                        isSelectedActor = isSelected
                    )
                )
            }
        }

        // 3. Sort polygons back-to-front (Painter's algorithm)
        polyList.sortByDescending { it.depth }

        // 4. Rasterize Polygons on Canvas
        val path = Path()
        for (poly in polyList) {
            path.reset()
            path.moveTo(poly.p0.x, poly.p0.y)
            path.lineTo(poly.p1.x, poly.p1.y)
            path.lineTo(poly.p2.x, poly.p2.y)
            path.close()

            if (renderMode != RenderMode.WIREFRAME) {
                drawScope.drawPath(path, poly.color, style = Fill)
            }

            if (poly.wireColor != null) {
                drawScope.drawPath(path, poly.wireColor, style = Stroke(width = if (poly.isSelectedActor) 1.5f else 0.8f))
            }
        }

        // 5. Draw 3D Gizmo for selected Actor
        val selectedActor = actors.find { it.id == selectedActorId }
        if (selectedActor != null) {
            drawTransformGizmo(
                drawScope = drawScope,
                actor = selectedActor,
                viewProj = viewProj,
                gizmoMode = gizmoMode,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        }
    }

    private fun computeShadingColor(
        renderMode: RenderMode,
        actor: Actor,
        normal: Vec3,
        sunDir: Vec3,
        viewDir: Vec3,
        clusterId: Int
    ): Color {
        return when (renderMode) {
            RenderMode.UNLIT -> Color(actor.material.baseColorHex)
            RenderMode.WIREFRAME -> Color(0x22111111)
            RenderMode.NANITE_CLUSTERS -> {
                // False-color cluster visualization like UE5 Nanite
                naniteClusterColors[abs(clusterId) % naniteClusterColors.size]
            }
            RenderMode.LUMEN_GI -> {
                // Lumen Radiance / Indirect Bounce Heatmap
                val bounce = (normal.dot(Vec3(0f, 0f, 1f)) * 0.5f + 0.5f)
                val irradiance = (0.3f + 0.7f * bounce).coerceIn(0f, 1f)
                Color(
                    red = (1f - irradiance) * 0.2f,
                    green = irradiance * 0.85f,
                    blue = irradiance,
                    alpha = 1.0f
                )
            }
            RenderMode.COLLISION -> {
                Color(0xCC00E676)
            }
            RenderMode.LIT -> {
                val base = Color(actor.material.baseColorHex)
                // Directional diffuse
                val nDotL = max(0f, normal.dot(sunDir))

                // Lumen indirect bounce approximation (ground reflection bounce)
                val lumenBounce = (normal.dot(Vec3(0f, 0f, 1f)) * 0.3f + 0.3f).coerceIn(0f, 0.6f)
                val ambient = 0.22f + lumenBounce

                // Specular reflection
                val halfVec = (sunDir + viewDir).normalized()
                val nDotH = max(0f, normal.dot(halfVec))
                val specular = (nDotH.pow(16f * (1.1f - actor.material.roughness))) * (actor.material.metallic * 0.7f + 0.1f)

                // Emissive component
                val emissive = Color(actor.material.emissiveHex)
                val emissiveFactor = actor.material.emissiveStrength

                val r = (base.red * (ambient + nDotL * 0.75f) + specular + emissive.red * emissiveFactor * 0.4f).coerceIn(0f, 1f)
                val g = (base.green * (ambient + nDotL * 0.75f) + specular + emissive.green * emissiveFactor * 0.4f).coerceIn(0f, 1f)
                val b = (base.blue * (ambient + nDotL * 0.75f) + specular + emissive.blue * emissiveFactor * 0.4f).coerceIn(0f, 1f)
                Color(r, g, b, 1.0f)
            }
        }
    }

    private fun drawGroundGrid(
        drawScope: DrawScope,
        viewProj: Mat4,
        canvasWidth: Float,
        canvasHeight: Float,
        renderMode: RenderMode
    ) {
        val gridExtent = 12f
        val step = 2f

        fun toScreen(v: Vec3): Offset? {
            val (cPos, w) = viewProj.transform(v, 1f)
            if (w <= 0.05f) return null
            val sx = (cPos.x / w * 0.5f + 0.5f) * canvasWidth
            val sy = (-cPos.y / w * 0.5f + 0.5f) * canvasHeight
            return Offset(sx, sy)
        }

        val gridLineColor = if (renderMode == RenderMode.LUMEN_GI) Color(0x3300E5FF) else Color(0x2A334050)

        // Draw sub-grid
        var x = -gridExtent
        while (x <= gridExtent) {
            val p0 = toScreen(Vec3(x, -gridExtent, 0f))
            val p1 = toScreen(Vec3(x, gridExtent, 0f))
            if (p0 != null && p1 != null) {
                drawScope.drawLine(gridLineColor, p0, p1, strokeWidth = 1f)
            }
            x += step
        }

        var y = -gridExtent
        while (y <= gridExtent) {
            val p0 = toScreen(Vec3(-gridExtent, y, 0f))
            val p1 = toScreen(Vec3(gridExtent, y, 0f))
            if (p0 != null && p1 != null) {
                drawScope.drawLine(gridLineColor, p0, p1, strokeWidth = 1f)
            }
            y += step
        }

        // Draw UE5 Principal Coordinate Axes (X Red, Y Green)
        val center = toScreen(Vec3(0f, 0f, 0f))
        val axisX = toScreen(Vec3(gridExtent, 0f, 0f))
        val axisY = toScreen(Vec3(0f, gridExtent, 0f))

        if (center != null && axisX != null) {
            drawScope.drawLine(Color(0xFFFF3333), center, axisX, strokeWidth = 2.5f)
        }
        if (center != null && axisY != null) {
            drawScope.drawLine(Color(0xFF33DD33), center, axisY, strokeWidth = 2.5f)
        }
    }

    private fun drawTransformGizmo(
        drawScope: DrawScope,
        actor: Actor,
        viewProj: Mat4,
        gizmoMode: GizmoMode,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        val origin = actor.location

        fun toScreen(v: Vec3): Offset? {
            val (cPos, w) = viewProj.transform(v, 1f)
            if (w <= 0.05f) return null
            val sx = (cPos.x / w * 0.5f + 0.5f) * canvasWidth
            val sy = (-cPos.y / w * 0.5f + 0.5f) * canvasHeight
            return Offset(sx, sy)
        }

        val sOrigin = toScreen(origin) ?: return
        val gizmoLength = 1.8f

        when (gizmoMode) {
            GizmoMode.TRANSLATION -> {
                // X Axis (Red)
                val sX = toScreen(origin + Vec3(gizmoLength, 0f, 0f))
                if (sX != null) {
                    drawScope.drawLine(Color(0xFFFF2A2A), sOrigin, sX, strokeWidth = 4f)
                    drawScope.drawCircle(Color(0xFFFF2A2A), radius = 6f, center = sX)
                }

                // Y Axis (Green)
                val sY = toScreen(origin + Vec3(0f, gizmoLength, 0f))
                if (sY != null) {
                    drawScope.drawLine(Color(0xFF00E676), sOrigin, sY, strokeWidth = 4f)
                    drawScope.drawCircle(Color(0xFF00E676), radius = 6f, center = sY)
                }

                // Z Axis (Blue)
                val sZ = toScreen(origin + Vec3(0f, 0f, gizmoLength))
                if (sZ != null) {
                    drawScope.drawLine(Color(0xFF2979FF), sOrigin, sZ, strokeWidth = 4f)
                    drawScope.drawCircle(Color(0xFF2979FF), radius = 6f, center = sZ)
                }

                // Center pivot white dot
                drawScope.drawCircle(Color.White, radius = 5f, center = sOrigin)
            }
            GizmoMode.ROTATION -> {
                // Draw rotation rings
                drawScope.drawCircle(Color(0x88FF2A2A), radius = 45f, center = sOrigin, style = Stroke(width = 3f))
                drawScope.drawCircle(Color(0x8800E676), radius = 35f, center = sOrigin, style = Stroke(width = 3f))
                drawScope.drawCircle(Color(0x882979FF), radius = 25f, center = sOrigin, style = Stroke(width = 3f))
            }
            GizmoMode.SCALE -> {
                val sX = toScreen(origin + Vec3(gizmoLength, 0f, 0f))
                if (sX != null) {
                    drawScope.drawLine(Color(0xFFFF5252), sOrigin, sX, strokeWidth = 3.5f)
                    drawScope.drawRect(Color(0xFFFF5252), topLeft = Offset(sX.x - 5f, sX.y - 5f), size = androidx.compose.ui.geometry.Size(10f, 10f))
                }
                val sY = toScreen(origin + Vec3(0f, gizmoLength, 0f))
                if (sY != null) {
                    drawScope.drawLine(Color(0xFF69F0AE), sOrigin, sY, strokeWidth = 3.5f)
                    drawScope.drawRect(Color(0xFF69F0AE), topLeft = Offset(sY.x - 5f, sY.y - 5f), size = androidx.compose.ui.geometry.Size(10f, 10f))
                }
                val sZ = toScreen(origin + Vec3(0f, 0f, gizmoLength))
                if (sZ != null) {
                    drawScope.drawLine(Color(0xFF448AFF), sOrigin, sZ, strokeWidth = 3.5f)
                    drawScope.drawRect(Color(0xFF448AFF), topLeft = Offset(sZ.x - 5f, sZ.y - 5f), size = androidx.compose.ui.geometry.Size(10f, 10f))
                }
            }
        }
    }

    fun pickActor(
        tap: Offset,
        actors: List<Actor>,
        camera: Camera3D,
        canvasWidth: Float,
        canvasHeight: Float
    ): Actor? {
        val aspect = canvasWidth / canvasHeight.coerceAtLeast(1f)
        val proj = Mat4.perspective(camera.fov, aspect, 0.1f, 100f)
        val view = camera.getViewMatrix()
        val viewProj = proj * view

        var bestActor: Actor? = null
        var minDistance = 80f // tap threshold in pixels

        for (actor in actors) {
            if (!actor.isVisible) continue
            val (cPos, w) = viewProj.transform(actor.location, 1f)
            if (w <= 0.05f) continue

            val sx = (cPos.x / w * 0.5f + 0.5f) * canvasWidth
            val sy = (-cPos.y / w * 0.5f + 0.5f) * canvasHeight
            val dist = hypot(tap.x - sx, tap.y - sy)
            if (dist < minDistance) {
                minDistance = dist
                bestActor = actor
            }
        }
        return bestActor
    }
}
