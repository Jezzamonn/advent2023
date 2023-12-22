package day18

import day14.printGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import shared.getInputFile
import java.util.function.Function
import kotlin.time.Duration.Companion.milliseconds

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    operator fun times(other: Int) = Point(x * other, y * other)
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun delta(): Point = when (this) {
        UP -> Point(0, -1)
        DOWN -> Point(0, 1)
        LEFT -> Point(-1, 0)
        RIGHT -> Point(1, 0)
    }

    // Parse from character
    companion object {
        fun fromChar(c: Char): Direction = when (c) {
            'U' -> UP
            'D' -> DOWN
            'L' -> LEFT
            'R' -> RIGHT
            else -> throw IllegalArgumentException("Invalid direction character: $c")
        }
    }
}

data class DirectionDistance(val direction: Direction, val distance: Int)

data class Segment(val start: Point, val end: Point) {

    val isVertical: Boolean
        get() = start.x == end.x

    val isHorizontal: Boolean
        get() = start.y == end.y

    fun intersects(other: Segment): Boolean {
        // If the segments are parallel, they can't intersect
        if (this.isVertical == other.isVertical) {
            return false
        }

        // Also skip segments that share a point.
        if (this.start == other.start || this.start == other.end ||
            this.end == other.start || this.end == other.end) {
            return false
        }

        // If this is vertical, swap the segments so that this is horizontal
        val (horizontal, vertical) = if (this.isVertical) {
            other to this
        } else {
            this to other
        }

        // If the vertical segment is between the horizontal segment's x values, and the horizontal segment is between
        // the vertical segment's y values, then they intersect.
        return vertical.start.x in horizontal.start.x..horizontal.end.x &&
                horizontal.start.y in vertical.start.y..vertical.end.y
    }

    companion object {
        fun sorted(start: Point, end: Point): Segment {
            return if (start.x < end.x || start.y < end.y) {
                Segment(start, end)
            } else {
                Segment(end, start)
            }
        }
    }
}

class Growable2DGrid<T> {
    val grid = mutableListOf(mutableListOf<T?>(null))
    // TODO: When I learn kotlin more, make this public read private set
    var minX = 0
    var minY = 0
    var width = 1
    var height = 1

    val maxX
        get() = minX + width - 1
    val maxY
        get() = minY + height - 1

    fun get(point: Point): T? {
        if (grid.isEmpty()) {
            return null
        }
        if (!contains(point)) {
            return null
        }
        return grid[point.y - minY][point.x - minX]
    }

    /** Add one row in the up direction. */
    private fun expandUp() {
        // Add a row full of nulls
        grid.add(0, MutableList(width) { null })
        minY--
        height++
    }

    /** Add one row in the down direction. */
    private fun expandDown() {
        // Add a row full of nulls
        grid.add(MutableList(width) { null })
        height++
    }

    /** Add one column in the left direction. */
    private fun expandLeft() {
        // Add a null to the beginning of each row
        for (row in grid) {
            row.add(0, null)
        }
        minX--
        width++
    }

    /** Add one column in the right direction. */
    private fun expandRight() {
        // Add a null to the end of each row
        for (row in grid) {
            row.add(null)
        }
        width++
    }

    /** Set the value at the given point, growing the grid in either direction if necessary */
    fun set(point: Point, value: T?) {
        // Grow the grid if necessary
        while (point.y < minY) {
            expandUp()
        }
        while (point.y > maxY) {
            expandDown()
        }
        while (point.x < minX) {
            expandLeft()
        }
        while (point.x > maxX) {
            expandRight()
        }
        grid[point.y - minY][point.x - minX] = value
    }

    fun contains(point: Point): Boolean {
        return point.x in minX..maxX && point.y in minY..maxY
    }

    val indices
        get() = (minY..maxY).flatMap { y ->
            (minX..maxX).map { x ->
                Point(x, y)
            }
        }

    val values
        get() = indices.map { get(it) }

    fun toString(elementToString: (T?) -> String): String {
        return grid.joinToString("\n") { row ->
            row.joinToString(" ", transform = elementToString)
        }
    }

}

fun main() {
    runBlocking {
//        solvePart1()
        solvePart2()
    }
}

suspend fun solvePart1() {
    val input = getInputFile(18).readLines()

    val grid = Growable2DGrid<Boolean>()
    var currentPosition = Point(0,0)
    grid.set(currentPosition, true)
    for (line in input) {
        // Parse out direction and length (ignore the color for now)
        val (dirString, lenStr) = line.split(" ")
        val dir = Direction.fromChar(dirString.single())
        val len = lenStr.toInt()

        // "Dig" the path in the direction
        for (i in 1..len) {
            currentPosition += dir.delta()
            grid.set(currentPosition, true)
//            printFrame(grid)
        }
    }

    // Now we should have the outline of the shape. We need to fill it.
    // Not sure the "best" way to do this but I think this will work:
    // 1. Expand the grid 1 more around the shape
    // 2. Do a flood fill from the outside, marking those as false
    // 3. Everything that's null is the inside, so mark that as true.
    grid.set(Point(grid.minX - 1, grid.minY - 1), null)
    grid.set(Point(grid.maxX + 1, grid.maxY + 1), null)
    // Fill from the outside
    val toVisit = mutableListOf(Point(grid.minX, grid.minY))
    while (toVisit.isNotEmpty()) {
        val visiting = toVisit.removeLast()
        if (!grid.contains(visiting)) {
            continue
        }
        if (grid.get(visiting) != null) {
            continue
        }
        grid.set(visiting, false)

        val adjacent = Direction.entries
            .map { visiting + it.delta() }
        toVisit.addAll(adjacent)

//        printFrame(grid)
    }

    val part1 = grid.values.count { it == true || it == null }
    println("Part 1: $part1")
}

suspend fun printFrame(grid: Growable2DGrid<Boolean>) {
    println("\n".repeat(20))
    println(grid.toString { b ->
        when (b) {
            true -> "\u001B[31m#\u001B[0m"
            false -> "."
            null -> "x"
        }
    })
    delay(50.milliseconds)
}

fun solvePart2() {
    val lines = getInputFile(18).readLines().filter { it.isNotEmpty() }
    val directions = lines.map { line ->
        val (_, _, hex) = line.split(" ")
        val distance = hex.substring(2..6).toInt(16)
        val direction = when (hex[7]) {
            '0' -> Direction.RIGHT
            '1' -> Direction.DOWN
            '2' -> Direction.LEFT
            '3' -> Direction.UP
            else -> throw IllegalArgumentException("Invalid direction character: ${hex[7]}")
        }
        DirectionDistance(direction, distance)
    }
    val part2 = countSizeOfShape(directions)
    println("Part 2: $part2")
}

fun countSizeOfShape(directions: List<DirectionDistance>): Long {
    val segments = mutableListOf<Segment>()
    var currentPosition = Point(0, 0)
    for (directionDistance in directions) {
        val newPosition = currentPosition + directionDistance.direction.delta() * directionDistance.distance
        val newSegment = Segment.sorted(currentPosition, newPosition)
        // Check that this doesn't intersect any of the existing segments. If it does, we'll need to use a more
        // complicated solution.
        println("Checking segment $newSegment")
        if (segments.any { it.intersects(newSegment) }) {
            throw IllegalArgumentException("This solution doesn't work for intersections")
        }
        segments.add(newSegment)
        currentPosition = newPosition
    }
    val segmentsByX = segments.flatMap { segment ->
        if (segment.isVertical) {
            listOf(segment.start.x.toLong() to segment)
        } else {
            listOf(
                segment.start.x.toLong() to segment,
                segment.end.x.toLong() to segment)
        }
    }.sortedBy { it.first }

    // The currently active ranges in the shape as we scan across columns.
    var activeRanges = listOf<IntRange>()
    var lastX = segmentsByX.first().first.toLong()
    var totalArea = 0L
    for ((x, segment) in segmentsByX) {
        println("$x: $segment")
        if (x != lastX) {
            totalArea += activeRanges.sumOf { it.last - it.first + 1 } * (x - lastX)
        }

        // Update the active ranges
        if (segment.isVertical) {
            val segmentRange = segment.start.y..segment.end.y
            // If this line is already in the active ranges, reduce the active ranges to not include this. Otherwise, add to active ranges.
            activeRanges = if (activeRanges.any { rangesOverlap(it, segmentRange) }) {
                activeRanges.flatMap { subtractFromRange(it, segmentRange) }
            } else {
                activeRanges + listOf(segmentRange)
            }
        }

        lastX = x
        // Demo answer
        // 952408144115
        // What I got:
        // 615099948035
    }
    return totalArea
}

fun rangesOverlap(range1: IntRange, range2: IntRange): Boolean {
    return range1.first <= range2.last && range2.first <= range1.last
}

fun subtractFromRange(range1: IntRange, range2: IntRange): List<IntRange> {
    if (!rangesOverlap(range1, range2)) {
        return listOf(range1)
    }
    val result = mutableListOf<IntRange>()
    if (range1.first < range2.first) {
        result.add(range1.first..range2.first - 1)
    }
    if (range1.last > range2.last) {
        result.add(range2.last + 1..range1.last)
    }
    return result
}
