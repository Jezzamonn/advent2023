package day10

import shared.getInputFile

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    val opposite: Point
        get() = Point(-x, -y)
}

val right = Point(1, 0)
val left = Point(-1, 0)
val up = Point(0, -1)
val down = Point(0, 1)

val symbolDeltaMap = mapOf(
    '|' to listOf(up, down),
    '-' to listOf(left, right),
    'L' to listOf(right, up),
    'J' to listOf(left, up),
    '7' to listOf(left, down),
    'F' to listOf(right, down),
)

val deltas = listOf(
    Point(0, 1),
    Point(0, -1),
    Point(1, 0),
    Point(-1, 0)
)

fun main() {
    val map = getInputFile(10).readText().trim().lines().toMutableList()
    // Find 'S' in the map
    val start = map.mapIndexed { y, line ->
        line.mapIndexed { x, c ->
            if (c == 'S') Point(x, y) else null
        }
    }.flatten().filterNotNull().first()

    val visited = mutableSetOf<Point>()
    val toVisit = mutableListOf(start)
    // Handle start specially because it doesn't contain any deltas
    val startDeltas: MutableList<Point> = mutableListOf()
    for (delta in deltas) {
        // Bounds check
        if (start.y + delta.y < 0 || start.y + delta.y >= map.size) {
            continue
        }
        if (start.x + delta.x < 0 || start.x + delta.x >= map[start.y + delta.y].length) {
            continue
        }
        val symbol = map[start.y + delta.y][start.x + delta.x]
        val symbolDeltas = symbolDeltaMap[symbol] ?: continue
        if (symbolDeltas.contains(delta.opposite)) {
            toVisit.add(start + delta)
            startDeltas.add(delta)
        }
    }
    visited.add(start)
    // Replace the start with the corresponding symbol
    val startSymbol = symbolDeltaMap.entries.first { (_, deltas) ->
        deltas.containsAll(startDeltas)
    }.key

    map[start.y] = map[start.y].replaceRange(start.x, start.x + 1, startSymbol.toString())

    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeAt(0)
        if (visited.contains(current)) {
            continue
        }
        visited.add(current)
        val symbol = map[current.y][current.x]
        println("Visiting $current, symbol: $symbol")
        val symbolDeltas = symbolDeltaMap[symbol]!!
        for (delta in symbolDeltas) {
            toVisit.add(current + delta)
        }
    }

    println("Part 1: ${visited.size / 2}")

    // Part 2
    // Scan the map to find the squares inside the shape.
    val totalInShape = map.mapIndexed { y, line ->
        var inShape = false
        var lastEntryDirection: Point? = null
        var squaresInShape = 0
        for (x in line.indices) {
            // Skip parts that aren't part of the loop
            val symbol = if (!visited.contains(Point(x, y))) {
                ' '
            } else {
                map[y][x]
            }

            when (symbol) {
                ' ' -> {
                    if (inShape) {
                        squaresInShape++
                    }
                }
                '|' -> {
                    inShape = !inShape
                }
                // Up symbols
                'L', 'J' -> {
                    when (lastEntryDirection) {
                        null -> {
                            lastEntryDirection = up
                        }
                        // Returned from the same direction, doesn't change if we're in the shape or not.
                        up -> {
                            lastEntryDirection = null
                        }
                        // Continued a line, we're now in the shape.
                        down -> {
                            lastEntryDirection = null
                            inShape = !inShape
                        }
                    }
                }
                // Down symbols
                '7', 'F' -> {
                    when (lastEntryDirection) {
                        null -> {
                            lastEntryDirection = down
                        }
                        // Returned from the same direction, doesn't change if we're in the shape or not.
                        down -> {
                            lastEntryDirection = null
                        }
                        // Continued a line, we're now in the shape.
                        up -> {
                            lastEntryDirection = null
                            inShape = !inShape
                        }
                    }
                }
            }
        }
        squaresInShape
    }.sum()

    println("Part 2: $totalInShape")
}