package day24

import shared.getInputFile
import java.io.File
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.log
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

    fun dotProduct(other: Point3D): Double {
        return x * other.x + y * other.y + z * other.z
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
        val m1 = velocity.y / velocity.x
        val m2 = other.velocity.y / other.velocity.x
        val b1 = position.y - m1 * position.x
        val b2 = other.position.y - m2 * other.position.x
        if (m1 == m2) {
            // No intersection
            return null
        }
        // Handle vertical lines straight away.
        if (m1.isInfinite()) {
            return DoublePoint(position.x, (m2 * position.x + b2))
        }
        if (m2.isInfinite()) {
            return DoublePoint(other.position.x, (m1 * other.position.x + b1))
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
        // Not normalized.
        val n = velocity.crossProduct(other.velocity)
        val top = n.dotProduct(other.position - position)
        return top * top / n.sqMagnitude
    }
}

fun main() {
    val hailstones = getInputFile(24)
        .readLines()
        .filter { it.isNotBlank() }
        .map { Hailstone.fromString(it) }

    countIntersecting(hailstones, 200000000000000.0, 400000000000000.0)

    // Part 2: Find the line that intersects all other lines.
//    findIntersecting(hailstones)
    checkResult(hailstones, hailstones[0], hailstones[1], 542714985863.0, 469657037828.0)
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

    // Use simulated annealing to find t1 and t2 such that the distance to hailstone3 is minimized.
    // Start with a large step size, then decrease it over time.
    var overallBestDistance = Double.MAX_VALUE
    var lastPrintedBestNumDigits = Int.MAX_VALUE
    var overallBestHailstone: Hailstone? = null
    var bestT1 = 0.0
    var bestT2 = 0.0
    val decay = 0.995

    var t1StepSize = 10000000000.0
    var t1 = 0.0
    while (t1StepSize > 0.4) {
        for (deltaT1 in listOf(-1.0, 1.0)) {
            val newT1 = (t1 + deltaT1 * t1StepSize).roundToLong().toDouble()

            val (t2, distance) = getBestT2ForT1(newT1, hailstones, h1, h2)

            if (distance < overallBestDistance) {
                overallBestDistance = distance
                t1 = newT1
                bestT1 = t1
                bestT2 = t2

                val numDigits = log(overallBestDistance, 10.0).toInt()
                if (numDigits < lastPrintedBestNumDigits) {
                    lastPrintedBestNumDigits = numDigits
                    println("New best distance: $overallBestDistance")
                    println("t1: $t1, t2: $t2")
                }
            }
        }
        t1StepSize *= decay
    }

    println()
    checkResult(hailstones, h1, h2, bestT1, bestT2)
//    println()
}

data class T2SearchResult(val t2: Double, val distance: Double)

fun getBestT2ForT1(t1: Double, hailstones: List<Hailstone>, h1: Hailstone, h2: Hailstone): T2SearchResult {
    val decay = 0.995
    // For each t1 value, search for the best t2
    var t2StepSize = 10000000000.0
    var bestDistance = Double.MAX_VALUE

    var t2 = 0.0
    while (t2StepSize > 0.4) {
        for (deltaT2 in listOf(-1.0, 1.0)) {
            val newT2 = (t2 + deltaT2 * t2StepSize).roundToLong().toDouble()
            val p1 = h1.position + h1.velocity * t1
            val p2 = h2.position + h2.velocity * newT2
            val hailstone = Hailstone(p1, p2 - p1)
            val sqDistToAll = hailstones.sumOf { it.sqDistanceTo(hailstone) }
            if (sqDistToAll < bestDistance) {
                bestDistance = sqDistToAll
                t2 = newT2
            }
        }
        t2StepSize *= decay
    }

    return T2SearchResult(t2, bestDistance)
}

fun checkResult(hailstones: List<Hailstone>, h1: Hailstone, h2: Hailstone, t1: Double, t2: Double) {
    val p1 = h1.position + h1.velocity * t1
    val p2 = h2.position + h2.velocity * t2
    val hailstone = Hailstone(p1, p2 - p1)

    val sqDistToAll = hailstones.sumOf { it.sqDistanceTo(hailstone) }
    println("sqDistToAll: $sqDistToAll")

//    for (h in hailstones) {
//        println("Distance to $h: ${hailstone.sqDistanceTo(h)}")
//    }
    println("t1: $t1, t2: $t2")
    println("P1: ${h1.position + h1.velocity * t1}")
    println("P2: ${h2.position + h2.velocity * t2}")
//    println("Hailstone: $hailstone")
//    println("at t=0: ${hailstone.position + hailstone.velocity * 0.0}")
//    println("at t=1: ${hailstone.position + hailstone.velocity * 1.0}")

    val tDiff = t2 - t1
    // Adjust velocity so that the magnitude is equal to tDiff
    val newVelocity = hailstone.velocity * (1 / tDiff)

    // The starting point is p1 - v * t1
    val newStartingPoint = hailstone.position - newVelocity * t1
    val finalHailstone = Hailstone(newStartingPoint, newVelocity)
    println("Final hailstone start position as long: X = ${newStartingPoint.x.toLong()}, Y = ${newStartingPoint.y.toLong()}, Z = ${newStartingPoint.z.toLong()}")
    println("Added = ${(newStartingPoint.x + newStartingPoint.y + newStartingPoint.z).toLong()}")
    // Wahoooo!!!
//    println("at t1: ${finalHailstone.position + finalHailstone.velocity * t1}")
//    println("at t2: ${finalHailstone.position + finalHailstone.velocity * t2}")

    // Final check: does this hailstone intersect all the others at the right time?
//    for ((i, h) in hailstones.withIndex()) {
//        val p = finalHailstone.coordOfXYPlaneIntersection(h) ?: return
//        val t1 = finalHailstone.timeForXPosition(p.x)
//        val t2 = h.timeForXPosition(p.x)
//        println("i: $i: t1: $t1, t2: $t2")
//    }
}