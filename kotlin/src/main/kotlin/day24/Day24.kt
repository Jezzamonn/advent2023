package day24

import shared.getInputFile
import java.io.File

data class DoublePoint(val x: Double, val y: Double)

data class Point3D(val x: Long, val y: Long, val z: Long) {
    operator fun plus(other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }

    companion object {
        /**
         * Parse a string like "19, 13, 30".
         */
        fun fromString(s: String): Point3D {
            val (x, y, z) = s.split(",").map { it.trim().toLong() }
            return Point3D(x, y, z)
        }
    }
}

data class Hailstone(val position: Point3D, val velocity: Point3D) {

    fun coordOfXYPlaneIntersection(other: Hailstone): DoublePoint? {
        val m1 = velocity.y.toDouble() / velocity.x.toDouble()
        val m2 = other.velocity.y.toDouble() / other.velocity.x.toDouble()
        val b1 = position.y.toDouble() - m1 * position.x.toDouble()
        val b2 = other.position.y.toDouble() - m2 * other.position.x.toDouble()
        if (m1 == m2) {
            // No intersection
            return null
        }
        // Handle vertical lines straight away.
        if (m1.isInfinite()) {
            return DoublePoint(position.x.toDouble(), (m2 * position.x.toDouble() + b2))
        }
        if (m2.isInfinite()) {
            return DoublePoint(other.position.x.toDouble(), (m1 * other.position.x.toDouble() + b1))
        }
        val xCoord = (b2 - b1) / (m1 - m2)
        val yCoord = m1 * xCoord + b1
        return DoublePoint(xCoord, yCoord)
    }

    fun timeForXPosition(x: Double): Double {
        // x = x0 + v * t
        // -> t = (x - x0) / v
        return (x - position.x) / velocity.x
    }

    companion object {
        /**
         * Parse a string like "19, 13, 30 @ -2,  1, -2"
         */
        fun fromString(s: String): Hailstone {
            val (position, velocity) = s.split("@").map { it.trim() }
            return Hailstone(Point3D.fromString(position), Point3D.fromString(velocity))
        }
    }
}

fun main() {
    solve(getInputFile(24), 200000000000000.0, 400000000000000.0)
}

fun solve(file: File, rangeMin: Double, rangeMax: Double) {
    val hailstones = file
        .readLines()
        .filter { it.isNotBlank() }
        .map { Hailstone.fromString(it) }

    // Loop over every pair of hailstones, count the number that intersect
    val pairs = hailstones.indices.flatMap { i ->
        (i + 1..<hailstones.size).map { j ->
            hailstones[i] to hailstones[j]
        }
    }
    val numIntersecting = pairs.count { (a, b) ->
        val p = a.coordOfXYPlaneIntersection(b) ?: return@count false

        if (p.x < rangeMin || p.x > rangeMax || p.y < rangeMin || p.y > rangeMax) {
            return@count false
        }

        val t1 = a.timeForXPosition(p.x)
        val t2 = b.timeForXPosition(p.x)
        t1 > 0 && t2 > 0
    }
    println("Part 1: $numIntersecting")
}