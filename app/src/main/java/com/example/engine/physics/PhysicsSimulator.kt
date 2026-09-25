package com.example.engine.physics

import com.example.data.model.Actor
import com.example.data.model.Vec3
import kotlin.math.hypot
import kotlin.math.max

data class CharacterState(
    var position: Vec3 = Vec3(0f, -5f, 0f),
    var velocity: Vec3 = Vec3(),
    var yaw: Float = 0f,
    var isGrounded: Boolean = true,
    var speed: Float = 4.5f
)

class PhysicsSimulator {

    private val gravity = -14.0f

    fun updateCharacter(
        charState: CharacterState,
        moveInput: Vec3, // x = right, y = forward
        jumpRequested: Boolean,
        dt: Float,
        actors: List<Actor>
    ) {
        // Apply Jump
        if (jumpRequested && charState.isGrounded) {
            charState.velocity = Vec3(charState.velocity.x, charState.velocity.y, 6.5f)
            charState.isGrounded = false
        }

        // Apply Gravity
        if (!charState.isGrounded) {
            val vz = charState.velocity.z + gravity * dt
            charState.velocity = Vec3(charState.velocity.x, charState.velocity.y, vz)
        }

        // Move character based on input and orientation
        val rad = Math.toRadians(charState.yaw.toDouble()).toFloat()
        val cosY = kotlin.math.cos(rad)
        val sinY = kotlin.math.sin(rad)

        // Forward vector in world coordinates
        val fwd = Vec3(sinY, cosY, 0f).normalized()
        val right = Vec3(cosY, -sinY, 0f).normalized()

        val desiredVel = (fwd * moveInput.y + right * moveInput.x) * charState.speed
        val newX = charState.position.x + desiredVel.x * dt
        val newY = charState.position.y + desiredVel.y * dt
        var newZ = charState.position.z + charState.velocity.z * dt

        // Ground floor check (Z = 0)
        if (newZ <= 0f) {
            newZ = 0f
            charState.velocity = Vec3(charState.velocity.x, charState.velocity.y, 0f)
            charState.isGrounded = true
        } else {
            charState.isGrounded = false
        }

        charState.position = Vec3(newX, newY, newZ)
    }

    fun stepPhysicsActors(
        actors: List<Actor>,
        charPos: Vec3,
        dt: Float
    ): List<Actor> {
        return actors.map { actor ->
            if (!actor.physics.simulatePhysics) return@map actor

            var pos = actor.location
            var vel = actor.physics.velocity

            // Gravity
            if (pos.z > 0.5f) {
                vel = Vec3(vel.x, vel.y, vel.z + gravity * dt)
            }

            // Damping
            vel = vel * (1f - actor.physics.linearDamping * dt)

            // Player collision impulse
            val distToChar = hypot(pos.x - charPos.x, pos.y - charPos.y)
            if (distToChar < 1.2f && (pos.z - charPos.z) < 1.5f) {
                val pushDir = (pos - charPos).normalized()
                vel = vel + pushDir * 3.5f
            }

            pos = pos + vel * dt
            if (pos.z < 0.6f) {
                pos = Vec3(pos.x, pos.y, 0.6f)
                vel = Vec3(vel.x * 0.7f, vel.y * 0.7f, 0f)
            }

            actor.copy(
                location = pos,
                physics = actor.physics.copy(velocity = vel)
            )
        }
    }
}
