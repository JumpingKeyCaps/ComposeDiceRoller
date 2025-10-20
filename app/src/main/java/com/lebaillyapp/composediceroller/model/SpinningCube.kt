package com.lebaillyapp.composediceroller.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)

    fun rotateX(angle: Float): Vec3 {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vec3(x, y * cos - z * sin, y * sin + z * cos)
    }

    fun rotateY(angle: Float): Vec3 {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vec3(x * cos + z * sin, y, -x * sin + z * cos)
    }

    fun cross(other: Vec3) = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun normalize(): Vec3 {
        val len = sqrt(x * x + y * y + z * z)
        return if (len > 0) Vec3(x / len, y / len, z / len) else this
    }

    fun dot(other: Vec3) = x * other.x + y * other.y + z * other.z
}

// Helper to avoid zero length vector
fun Offset.normalizeOrZero(): Offset {
    val len = hypot(this.x, this.y)
    return if (len > 0f) Offset(this.x / len, this.y / len) else Offset.Zero
}



















