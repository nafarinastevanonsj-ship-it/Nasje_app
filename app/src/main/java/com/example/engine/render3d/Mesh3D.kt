package com.example.engine.render3d

import com.example.data.model.MeshType
import com.example.data.model.Vec3
import kotlin.math.*

data class Vertex(
    val pos: Vec3,
    val normal: Vec3 = Vec3(0f, 0f, 1f)
)

data class Face(
    val i0: Int,
    val i1: Int,
    val i2: Int,
    val normal: Vec3,
    val clusterId: Int = 0
)

data class MeshGeometry(
    val vertices: List<Vertex>,
    val faces: List<Face>
)

object MeshFactory {
    private val cache = mutableMapOf<MeshType, MeshGeometry>()

    fun getMesh(type: MeshType): MeshGeometry {
        return cache.getOrPut(type) {
            when (type) {
                MeshType.CUBE -> createCube()
                MeshType.SPHERE -> createSphere(subdivisions = 10)
                MeshType.CYLINDER -> createCylinder(segments = 12)
                MeshType.CONE -> createCone(segments = 12)
                MeshType.TORUS -> createTorus(radialSegments = 12, tubularSegments = 8)
                MeshType.NANITE_PILLAR -> createNanitePillar()
                MeshType.SCIFI_CRATE -> createSciFiCrate()
                MeshType.TEMPLE_ARCH -> createTempleArch()
                MeshType.CHARACTER_PAWN -> createCharacterPawn()
                MeshType.DIRECTIONAL_LIGHT -> createDirectionalLightMesh()
                MeshType.POINT_LIGHT -> createPointLightMesh()
                MeshType.CAMERA -> createCameraMesh()
            }
        }
    }

    private fun createCube(size: Float = 1.0f): MeshGeometry {
        val h = size / 2f
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()

        fun addQuad(v0: Vec3, v1: Vec3, v2: Vec3, v3: Vec3, norm: Vec3, cluster: Int) {
            val base = verts.size
            verts.add(Vertex(v0, norm))
            verts.add(Vertex(v1, norm))
            verts.add(Vertex(v2, norm))
            verts.add(Vertex(v3, norm))
            faces.add(Face(base, base + 1, base + 2, norm, cluster))
            faces.add(Face(base, base + 2, base + 3, norm, cluster))
        }

        // 6 faces of the cube with distinct cluster IDs for Nanite visualizer
        addQuad(Vec3(-h, -h, h), Vec3(h, -h, h), Vec3(h, h, h), Vec3(-h, h, h), Vec3(0f, 0f, 1f), 0) // Top
        addQuad(Vec3(-h, h, -h), Vec3(h, h, -h), Vec3(h, -h, -h), Vec3(-h, -h, -h), Vec3(0f, 0f, -1f), 1) // Bottom
        addQuad(Vec3(-h, h, -h), Vec3(-h, h, h), Vec3(h, h, h), Vec3(h, h, -h), Vec3(0f, 1f, 0f), 2) // Front
        addQuad(Vec3(h, -h, -h), Vec3(h, -h, h), Vec3(-h, -h, h), Vec3(-h, -h, -h), Vec3(0f, -1f, 0f), 3) // Back
        addQuad(Vec3(h, -h, -h), Vec3(h, h, -h), Vec3(h, h, h), Vec3(h, -h, h), Vec3(1f, 0f, 0f), 4) // Right
        addQuad(Vec3(-h, -h, -h), Vec3(-h, -h, h), Vec3(-h, h, h), Vec3(-h, h, -h), Vec3(-1f, 0f, 0f), 5) // Left

        return MeshGeometry(verts, faces)
    }

    private fun createSphere(radius: Float = 0.8f, subdivisions: Int = 10): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()

        for (i in 0..subdivisions) {
            val theta = i * Math.PI.toFloat() / subdivisions
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (j in 0..subdivisions) {
                val phi = j * 2 * Math.PI.toFloat() / subdivisions
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta
                val y = sinPhi * sinTheta
                val z = cosTheta
                val norm = Vec3(x, y, z)
                verts.add(Vertex(norm * radius, norm))
            }
        }

        for (i in 0 until subdivisions) {
            val cluster = i % 6
            for (j in 0 until subdivisions) {
                val first = i * (subdivisions + 1) + j
                val second = first + subdivisions + 1

                val norm = verts[first].normal
                faces.add(Face(first, second, first + 1, norm, cluster))
                faces.add(Face(second, second + 1, first + 1, norm, cluster))
            }
        }

        return MeshGeometry(verts, faces)
    }

    private fun createCylinder(radius: Float = 0.6f, height: Float = 1.6f, segments: Int = 12): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()
        val halfH = height / 2f

        // Top center & bottom center
        val topIdx = 0
        val botIdx = 1
        verts.add(Vertex(Vec3(0f, 0f, halfH), Vec3(0f, 0f, 1f)))
        verts.add(Vertex(Vec3(0f, 0f, -halfH), Vec3(0f, 0f, -1f)))

        val ringStart = 2
        for (i in 0 until segments) {
            val angle = i * 2f * Math.PI.toFloat() / segments
            val x = cos(angle) * radius
            val y = sin(angle) * radius
            val norm = Vec3(cos(angle), sin(angle), 0f).normalized()
            verts.add(Vertex(Vec3(x, y, halfH), norm))
            verts.add(Vertex(Vec3(x, y, -halfH), norm))
        }

        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val t0 = ringStart + i * 2
            val b0 = ringStart + i * 2 + 1
            val t1 = ringStart + next * 2
            val b1 = ringStart + next * 2 + 1

            // Side quad
            val sideNorm = verts[t0].normal
            faces.add(Face(t0, b0, t1, sideNorm, i % 4))
            faces.add(Face(t1, b0, b1, sideNorm, i % 4))

            // Top cap
            faces.add(Face(topIdx, t0, t1, Vec3(0f, 0f, 1f), 4))
            // Bottom cap
            faces.add(Face(botIdx, b1, b0, Vec3(0f, 0f, -1f), 5))
        }

        return MeshGeometry(verts, faces)
    }

    private fun createCone(radius: Float = 0.8f, height: Float = 1.6f, segments: Int = 12): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()
        val tip = Vertex(Vec3(0f, 0f, height / 2f), Vec3(0f, 0f, 1f))
        val botCenter = Vertex(Vec3(0f, 0f, -height / 2f), Vec3(0f, 0f, -1f))
        verts.add(tip) // 0
        verts.add(botCenter) // 1

        for (i in 0 until segments) {
            val a = i * 2f * Math.PI.toFloat() / segments
            val x = cos(a) * radius
            val y = sin(a) * radius
            verts.add(Vertex(Vec3(x, y, -height / 2f), Vec3(cos(a), sin(a), 0.5f).normalized()))
        }

        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val v0 = 2 + i
            val v1 = 2 + next
            faces.add(Face(0, v0, v1, Vec3(0f, 0f, 1f), i % 3))
            faces.add(Face(1, v1, v0, Vec3(0f, 0f, -1f), 4))
        }

        return MeshGeometry(verts, faces)
    }

    private fun createTorus(radialSegments: Int = 12, tubularSegments: Int = 8, radius: Float = 0.9f, tube: Float = 0.25f): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()

        for (j in 0..radialSegments) {
            for (i in 0..tubularSegments) {
                val u = i.toFloat() / tubularSegments * 2f * Math.PI.toFloat()
                val v = j.toFloat() / radialSegments * 2f * Math.PI.toFloat()

                val x = (radius + tube * cos(v)) * cos(u)
                val y = (radius + tube * cos(v)) * sin(u)
                val z = tube * sin(v)

                val center = Vec3(radius * cos(u), radius * sin(u), 0f)
                val pos = Vec3(x, y, z)
                val norm = (pos - center).normalized()
                verts.add(Vertex(pos, norm))
            }
        }

        for (j in 0 until radialSegments) {
            val cluster = j % 6
            for (i in 0 until tubularSegments) {
                val a = (tubularSegments + 1) * j + i
                val b = (tubularSegments + 1) * (j + 1) + i
                val c = (tubularSegments + 1) * (j + 1) + i + 1
                val d = (tubularSegments + 1) * j + i + 1

                faces.add(Face(a, b, d, verts[a].normal, cluster))
                faces.add(Face(b, c, d, verts[b].normal, cluster))
            }
        }

        return MeshGeometry(verts, faces)
    }

    private fun createNanitePillar(): MeshGeometry {
        // High density architectural pillar with fluted ridges & pedestal
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()
        val segments = 16
        val rings = 6
        val height = 4.0f
        val baseR = 0.8f

        for (r in 0..rings) {
            val z = -height / 2f + r * (height / rings)
            val ringRadius = when (r) {
                0, rings -> baseR * 1.3f
                1, rings - 1 -> baseR * 1.15f
                else -> baseR * (1f + 0.08f * sin(r * 3f))
            }

            for (s in 0 until segments) {
                val angle = s * 2f * Math.PI.toFloat() / segments
                // Add fluting texture
                val flute = 0.05f * sin(angle * 8f)
                val rad = ringRadius + flute
                val x = cos(angle) * rad
                val y = sin(angle) * rad
                val norm = Vec3(cos(angle), sin(angle), 0.1f).normalized()
                verts.add(Vertex(Vec3(x, y, z), norm))
            }
        }

        for (r in 0 until rings) {
            for (s in 0 until segments) {
                val nextS = (s + 1) % segments
                val v0 = r * segments + s
                val v1 = r * segments + nextS
                val v2 = (r + 1) * segments + s
                val v3 = (r + 1) * segments + nextS

                val cluster = (r * 2 + s / 4) % 8
                faces.add(Face(v0, v2, v1, verts[v0].normal, cluster))
                faces.add(Face(v1, v2, v3, verts[v1].normal, cluster))
            }
        }

        return MeshGeometry(verts, faces)
    }

    private fun createSciFiCrate(): MeshGeometry {
        // Futuristic beveled supply crate
        val h = 0.7f
        val b = 0.15f // Bevel
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()

        fun addFace(p0: Vec3, p1: Vec3, p2: Vec3, norm: Vec3, cluster: Int) {
            val idx = verts.size
            verts.add(Vertex(p0, norm))
            verts.add(Vertex(p1, norm))
            verts.add(Vertex(p2, norm))
            faces.add(Face(idx, idx + 1, idx + 2, norm, cluster))
        }

        fun addQuad(p0: Vec3, p1: Vec3, p2: Vec3, p3: Vec3, norm: Vec3, cluster: Int) {
            addFace(p0, p1, p2, norm, cluster)
            addFace(p0, p2, p3, norm, cluster)
        }

        // Top, bottom, 4 sides with beveled corners
        val zTop = h; val zBot = -h
        val xL = -h; val xR = h; val yF = h; val yB = -h

        addQuad(Vec3(xL, yB, zTop), Vec3(xR, yB, zTop), Vec3(xR, yF, zTop), Vec3(xL, yF, zTop), Vec3(0f, 0f, 1f), 0)
        addQuad(Vec3(xL, yF, zBot), Vec3(xR, yF, zBot), Vec3(xR, yB, zBot), Vec3(xL, yB, zBot), Vec3(0f, 0f, -1f), 1)
        addQuad(Vec3(xL, yF, zBot), Vec3(xL, yF, zTop), Vec3(xR, yF, zTop), Vec3(xR, yF, zBot), Vec3(0f, 1f, 0f), 2)
        addQuad(Vec3(xR, yB, zBot), Vec3(xR, yB, zTop), Vec3(xL, yB, zTop), Vec3(xL, yB, zBot), Vec3(0f, -1f, 0f), 3)
        addQuad(Vec3(xR, yF, zBot), Vec3(xR, yF, zTop), Vec3(xR, yB, zTop), Vec3(xR, yB, zBot), Vec3(1f, 0f, 0f), 4)
        addQuad(Vec3(xL, yB, zBot), Vec3(xL, yB, zTop), Vec3(xL, yF, zTop), Vec3(xL, yF, zBot), Vec3(-1f, 0f, 0f), 5)

        return MeshGeometry(verts, faces)
    }

    private fun createTempleArch(): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()
        val steps = 14
        val innerR = 1.6f
        val outerR = 2.4f
        val depth = 0.8f

        for (i in 0..steps) {
            val angle = i * Math.PI.toFloat() / steps
            val inX = cos(angle) * innerR
            val inZ = sin(angle) * innerR
            val outX = cos(angle) * outerR
            val outZ = sin(angle) * outerR

            val nIn = Vec3(-cos(angle), 0f, -sin(angle))
            val nOut = Vec3(cos(angle), 0f, sin(angle))

            val baseIdx = verts.size
            // Front inner, front outer, back inner, back outer
            verts.add(Vertex(Vec3(inX, -depth / 2f, inZ), Vec3(0f, -1f, 0f)))
            verts.add(Vertex(Vec3(outX, -depth / 2f, outZ), Vec3(0f, -1f, 0f)))
            verts.add(Vertex(Vec3(inX, depth / 2f, inZ), Vec3(0f, 1f, 0f)))
            verts.add(Vertex(Vec3(outX, depth / 2f, outZ), Vec3(0f, 1f, 0f)))

            if (i > 0) {
                val prev = baseIdx - 4
                val cluster = (i % 6)
                // Front quad
                faces.add(Face(prev, baseIdx, prev + 1, Vec3(0f, -1f, 0f), cluster))
                faces.add(Face(baseIdx, baseIdx + 1, prev + 1, Vec3(0f, -1f, 0f), cluster))
                // Back quad
                faces.add(Face(prev + 2, prev + 3, baseIdx + 2, Vec3(0f, 1f, 0f), cluster))
                faces.add(Face(baseIdx + 2, prev + 3, baseIdx + 3, Vec3(0f, 1f, 0f), cluster))
                // Outer arch quad
                faces.add(Face(prev + 1, baseIdx + 1, prev + 3, nOut, (cluster + 1) % 6))
                faces.add(Face(baseIdx + 1, baseIdx + 3, prev + 3, nOut, (cluster + 1) % 6))
                // Inner arch quad
                faces.add(Face(prev, prev + 2, baseIdx, nIn, (cluster + 2) % 6))
                faces.add(Face(baseIdx, prev + 2, baseIdx + 2, nIn, (cluster + 2) % 6))
            }
        }

        return MeshGeometry(verts, faces)
    }

    private fun createCharacterPawn(): MeshGeometry {
        // UE5 Mannequin stylized capsule with head, chest, and arms
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()

        // Body capsule (Cylinder + hemisphere)
        val cy = createCylinder(radius = 0.4f, height = 1.2f, segments = 8)
        val offsetBody = Vec3(0f, 0f, 0.7f)
        for (v in cy.vertices) {
            verts.add(Vertex(v.pos + offsetBody, v.normal))
        }
        for (f in cy.faces) {
            faces.add(Face(f.i0, f.i1, f.i2, f.normal, 0))
        }

        // Head sphere
        val head = createSphere(radius = 0.28f, subdivisions = 6)
        val offsetHead = Vec3(0f, 0f, 1.55f)
        val headStart = verts.size
        for (v in head.vertices) {
            verts.add(Vertex(v.pos + offsetHead, v.normal))
        }
        for (f in head.faces) {
            faces.add(Face(f.i0 + headStart, f.i1 + headStart, f.i2 + headStart, f.normal, 1))
        }

        return MeshGeometry(verts, faces)
    }

    private fun createDirectionalLightMesh(): MeshGeometry {
        val verts = mutableListOf<Vertex>()
        val faces = mutableListOf<Face>()
        // Sun disc + arrow
        val sp = createSphere(radius = 0.35f, subdivisions = 6)
        for (v in sp.vertices) verts.add(v)
        for (f in sp.faces) faces.add(f)
        return MeshGeometry(verts, faces)
    }

    private fun createPointLightMesh(): MeshGeometry {
        return createSphere(radius = 0.3f, subdivisions = 6)
    }

    private fun createCameraMesh(): MeshGeometry {
        val cube = createCube(0.5f)
        return MeshGeometry(cube.vertices, cube.faces)
    }
}
