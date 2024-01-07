package day24

import shared.getInputFile
import java.io.File
import java.lang.StringBuilder
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class DoublePoint(val x: Double, val y: Double)

data class Point3D(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Point3D): Point3D {
        return Point3D(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(scalar: Double): Point3D {
        return Point3D(x * scalar, y * scalar, z * scalar)
    }

    fun crossProduct(other: Point3D): Point3D {
        return Point3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun sqDistanceTo(other: Point3D): Double {
        return (other - this).sqMagnitude
    }

    val sqMagnitude: Double
        get() = x * x + y * y + z * z

    val magnitude: Double
        get() = sqrt(sqMagnitude)

    val normalized: Point3D
        get() {
            val mag = magnitude
            return Point3D(x / mag, y / mag, z / mag)
        }

    companion object {
        /**
         * Parse a string like "19, 13, 30".
         */
        fun fromString(s: String): Point3D {
            val (x, y, z) = s.split(",").map { it.trim().toDouble() }
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

    /**
     * Treat this as a line, return the closest distance to the other line.
     *
     * Using the approach from https://en.wikipedia.org/wiki/Skew_lines#Distance
     * (also https://math.stackexchange.com/a/2217845/352335)
     */
    fun sqDistanceTo(other: Hailstone): Double {
        val n = velocity.crossProduct(other.velocity).normalized
        return (other.position - position).crossProduct(n).sqMagnitude
    }
}

fun main() {
    val hailstones = getInputFile(24)
        .readLines()
        .filter { it.isNotBlank() }
        .map { Hailstone.fromString(it) }

    countIntersecting(hailstones, 200000000000000.0, 400000000000000.0)

    // Part 2: Find the line that intersects all other lines.
    findIntersecting(hailstones)
}

fun countIntersecting(hailstones: List<Hailstone>, rangeMin: Double, rangeMax: Double) {
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

fun outputDistanceCSV(hailstones: List<Hailstone>) {
    // Pick three lines (shouldn't matter which I think?).
    // Try points on two of the lines and see how close it is to the third line.
    val h1 = hailstones[0]
    val h2 = hailstones[1]
    val h3 = hailstones[2]

    val tMax = 1000000000000
    val tRange = 0..tMax step (tMax / 100)

    val csvBuilder = StringBuilder()

    for (t1 in tRange) {
        for (t2 in tRange) {
            val p1 = h1.position + h1.velocity * t1.toDouble()
            val p2 = h2.position + h2.velocity * t2.toDouble()

            val hailstone = Hailstone(p1, p2 - p1)
            val sqDistance = hailstone.sqDistanceTo(h3)
            csvBuilder.append("$t1,$t2,$sqDistance\n")
        }
    }

    File("/tmp/day24.csv").writeText(csvBuilder.toString())
}

fun findIntersecting(hailstones: List<Hailstone>) {
    // Pick three lines (shouldn't matter which I think?).
    // Try points on two of the lines and see how close it is to the third line.
    val h1 = hailstones[0]
    val h2 = hailstones[1]
    val h3 = hailstones[2]

    var t1 = 0L
    var t2 = 0L
    // Use simulated annealing to find t1 and t2 such that the distance to hailstone3 is minimized.
    // Start with a large step size, then decrease it over time.
    var t1StepSize = 1000000000000000000L
    var t2StepSize = 1000000000000000000L
    var bestDistance = Double.MAX_VALUE
    var bestHailstone: Hailstone? = null
    while (t1StepSize > 0 && t2StepSize > 0) {
        var betterInIDirection = false
        var betterInJDirection = false
        for (i in listOf(-1, 1)) {
            for (j in listOf(-1, 1)) {
                val newT1 = t1 + i * t1StepSize
                val newT2 = t2 + j * t2StepSize
                val p1 = h1.position + h1.velocity * newT1.toDouble()
                val p2 = h2.position + h2.velocity * newT2.toDouble()

                val hailstone = Hailstone(p1, p2 - p1)
                val sqDistToAll = hailstones.sumOf { it.sqDistanceTo(hailstone) }
                if (sqDistToAll < bestDistance) {
                    bestDistance = sqDistToAll
                    bestHailstone = hailstone
                    t1 = newT1
                    t2 = newT2
                    betterInIDirection = i != 0
                    betterInJDirection = j != 0
                    println("New best distance: $bestDistance")
                    println("t1: $t1, t2: $t2")
                }
            }
        }
        t1StepSize = if (betterInIDirection) t1StepSize else (t1StepSize * 0.9).toLong()
        t2StepSize = if (betterInJDirection) t2StepSize else (t2StepSize * 0.9).toLong()
    }

    // One method has this:
    // New best distance: 7.42296908820297E30
    // t1: 5.318486864780333E11, t2: -9.446000783545353E20

    // Method 2:
    // New best distance: 7.422969088583463E30
    // t1: 5.318486913097331E11, t2: -3.362421874999979E20
    // ? Some local minimum?

    println("Found best: $bestHailstone")
    // Now to adjust the starting point / velocity so that the hailstone hits the others at the right time.
    // We can do this with just two lines, and t1 and t2, and the points on those lines

    if (bestHailstone == null) {
        println("No best hailstone found")
        return
    }

//    // Check all the other hailstones too?
//    for ((index, hailstone) in hailstones.withIndex()) {
//        val sqDist = bestHailstone.sqDistanceTo(hailstone)
//        println("Distance to hailstone $index: $sqDist")
//    }

    println("t1: $t1, t2: $t2")
    println("P1: ${h1.position + h1.velocity * t1.toDouble()}")
    println("P2: ${h2.position + h2.velocity * t2.toDouble()}")

    val tDiff = t2 - t1
    // Adjust velocity so that the magnitude is equal to tDiff
    val newVelocity = bestHailstone.velocity.normalized * tDiff.toDouble()

    // The starting point is p1 - v * t1
    val newStartingPoint = bestHailstone.position - newVelocity * t1.toDouble()
    val hailstone = Hailstone(newStartingPoint, newVelocity)
    println("Final hailstone: $hailstone")
    println("at t1: ${hailstone.position + hailstone.velocity * t1.toDouble()}")
    println("at t2: ${hailstone.position + hailstone.velocity * t2.toDouble()}")
}