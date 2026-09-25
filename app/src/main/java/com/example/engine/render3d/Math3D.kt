package com.example.engine.render3d

import com.example.data.model.Vec3
import kotlin.math.*

class Mat4 {
    val m = FloatArray(16)

    init {
        // Identity by default
        m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f
    }

    operator fun times(other: Mat4): Mat4 {
        val result = Mat4()
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this.m[row * 4 + k] * other.m[k * 4 + col]
                }
                result.m[row * 4 + col] = sum
            }
        }
        return result
    }

    fun transform(v: Vec3, wIn: Float = 1f): Pair<Vec3, Float> {
        val x = m[0] * v.x + m[1] * v.y + m[2] * v.z + m[3] * wIn
        val y = m[4] * v.x + m[5] * v.y + m[6] * v.z + m[7] * wIn
        val z = m[8] * v.x + m[9] * v.y + m[10] * v.z + m[11] * wIn
        val w = m[12] * v.x + m[13] * v.y + m[14] * v.z + m[15] * wIn
        return Pair(Vec3(x, y, z), w)
    }

    companion object {
        fun identity(): Mat4 = Mat4()

        fun translation(x: Float, y: Float, z: Float): Mat4 {
            val res = Mat4()
            res.m[3] = x
            res.m[7] = y
            res.m[11] = z
            return res
        }

        fun scale(sx: Float, sy: Float, sz: Float): Mat4 {
            val res = Mat4()
            res.m[0] = sx
            res.m[5] = sy
            res.m[10] = sz
            return res
        }

        fun rotationX(degrees: Float): Mat4 {
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            val res = Mat4()
            res.m[5] = c; res.m[6] = -s
            res.m[9] = s; res.m[10] = c
            return res
        }

        fun rotationY(degrees: Float): Mat4 {
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            val res = Mat4()
            res.m[0] = c; res.m[2] = s
            res.m[8] = -s; res.m[10] = c
            return res
        }

        fun rotationZ(degrees: Float): Mat4 {
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            val res = Mat4()
            res.m[0] = c; res.m[1] = -s
            res.m[4] = s; res.m[5] = c
            return res
        }

        fun euler(pitchDeg: Float, yawDeg: Float, rollDeg: Float): Mat4 {
            return rotationZ(rollDeg) * (rotationY(pitchDeg) * rotationX(yawDeg))
        }

        fun lookAt(eye: Vec3, target: Vec3, up: Vec3): Mat4 {
            val f = (target - eye).normalized()
            val s = f.cross(up.normalized()).normalized()
            val u = s.cross(f)

            val res = Mat4()
            res.m[0] = s.x; res.m[1] = s.y; res.m[2] = s.z; res.m[3] = -s.dot(eye)
            res.m[4] = u.x; res.m[5] = u.y; res.m[6] = u.z; res.m[7] = -u.dot(eye)
            res.m[8] = -f.x; res.m[9] = -f.y; res.m[10] = -f.z; res.m[11] = f.dot(eye)
            res.m[15] = 1f
            return res
        }

        fun perspective(fovYDeg: Float, aspect: Float, near: Float, far: Float): Mat4 {
            val res = Mat4()
            for (i in 0..15) res.m[i] = 0f
            val rad = Math.toRadians(fovYDeg.toDouble()).toFloat()
            val tanHalfFov = tan(rad / 2f)

            res.m[0] = 1f / (aspect * tanHalfFov)
            res.m[5] = 1f / tanHalfFov
            res.m[10] = -(far + near) / (far - near)
            res.m[11] = -(2f * far * near) / (far - near)
            res.m[14] = -1f
            return res
        }
    }
}

data class Camera3D(
    var target: Vec3 = Vec3(0f, 0f, 1f),
    var distance: Float = 14f,
    var pitch: Float = 25f,
    var yaw: Float = -45f,
    var fov: Float = 60f
) {
    fun getEyePosition(): Vec3 {
        val pitchRad = Math.toRadians(pitch.toDouble()).toFloat()
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()

        val x = target.x + distance * cos(pitchRad) * sin(yawRad)
        val y = target.y - distance * cos(pitchRad) * cos(yawRad)
        val z = target.z + distance * sin(pitchRad)
        return Vec3(x, y, z)
    }

    fun getViewMatrix(): Mat4 {
        val eye = getEyePosition()
        return Mat4.lookAt(eye, target, Vec3(0f, 0f, 1f))
    }
}
