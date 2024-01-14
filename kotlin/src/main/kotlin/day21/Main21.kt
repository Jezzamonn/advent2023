package day21

import shared.getInputFile
import java.io.File

data class Point(val x: Int, val y: Int) {

    val neighbors: List<Point> get() {
        return listOf(
            Point(x - 1, y),
            Point(x + 1, y),
            Point(x, y - 1),
            Point(x, y + 1),
        )
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}

data class RepeatingMap(private val map: List<String>) {

    val width = map[0].length
    val height = map.size

    val start: Point = map.mapIndexed { y, line ->
        line.mapIndexed { x, c ->
            if (c == 'S') {
                Point(x, y)
            } else {
                null
            }
        }
    }.flatten().filterNotNull().first()

    fun get(x: Int, y: Int): Char {
        // Both coords can be negative, so we need to use modulo + length
        val yLooped = Math.floorMod(y, height)
        val xLooped = Math.floorMod(x, width)
        return map[yLooped][xLooped]
    }

    fun get(p: Point): Char {
        return get(p.x, p.y)
    }

    fun boardIndexFromPoint(p: Point): Point {
        // Floored division (always rounding towards negative infinity)
        return Point(Math.floorDiv(p.x, width), Math.floorDiv(p.y, height))
    }

    fun positionInBoardFromPoint(p: Point): Point {
        // Again, p can be negative so need to do the positive modulo trick
        return Point(Math.floorMod(p.x, width), Math.floorMod(p.y, height))
    }
}

fun main() {
    val inputFile = getInputFile(21)

    val map = inputFile.readLines().filter { it.isNotEmpty() }
    // Find 'S' in the map
    val start = map.mapIndexed { y, line ->
        line.mapIndexed { x, c ->
            if (c == 'S') {
                Point(x, y)
            } else {
                null
            }
        }
    }.flatten().filterNotNull().first()

    val part1 = countPossiblePositions(map, start, 64)
    println("Part 1: $part1")

    solvePart2(inputFile)
}

val possiblePositionsCache = mutableMapOf<Point, List<Int>>()

// Assumes the map is always the same.
fun countPossiblePositions(map: List<String>, start: Point, numSteps: Int): Int {
    if (start !in possiblePositionsCache) {
        // Create the cache for this start point
        var current = setOf(start)
        val newCacheEntry = mutableListOf(current.size)

        while (true) {
            current = current.flatMap { point ->
                point.neighbors.filter { neighbor ->
                    neighbor.x >= 0 && neighbor.y >= 0 &&
                            neighbor.x < map[0].length && neighbor.y < map.size &&
                            map[neighbor.y][neighbor.x] != '#'
                }
            }.toSet()

            if (newCacheEntry.size > 2 && current.size == newCacheEntry[newCacheEntry.size - 2]) {
                // Found a loop
                break
            }

            newCacheEntry.add(current.size)
        }

        possiblePositionsCache[start] = newCacheEntry
    }

    val cached = possiblePositionsCache[start]!!
    if (numSteps > cached.size) {
        // Because the pattern loops in the last two, just need to check the modulo with 2 and return either the
        // last or the second last value.
        val numStepsIsEven = numSteps % 2 == 0
        val lastIsEven = (cached.size - 1) % 2 == 0
        val index = if (numStepsIsEven == lastIsEven) {
            cached.size - 1
        } else {
            cached.size - 2
        }
        return cached[index]
    }
    return cached[numSteps]
}

fun solvePart2(file: File) {
//    val map = file.readLines().filter { it.isNotEmpty() }.let { RepeatingMap(it) }
//
//    println("Map width: ${map.width}, height: ${map.height}")
//    println("Start: ${map.start}")
//
//    // When full, each board has either 7574 or 7612 positions depending on even or odd.
//    //
//    // Reaches full in 129 steps.
//
//    var current = setOf(map.start)
//    val visitedBoardIndices = mutableSetOf<Point>()
//    val firstTimeVisited = mutableMapOf<Point, Int>()
//    val firstPositionVisited = mutableMapOf<Point, Point>()
//    for (i in 0..<1000000) {
//        for (point in current) {
//            val boardIndex = map.boardIndexFromPoint(point)
//            if (!visitedBoardIndices.contains(boardIndex)) {
//                val positionInBoard = map.positionInBoardFromPoint(point)
//                firstTimeVisited[boardIndex] = i
//                firstPositionVisited[boardIndex] = positionInBoard
//                visitedBoardIndices.add(boardIndex)
//
//                println()
//                println()
//
//                // Print a CSV
//                for (y in -3..3) {
//                    for (x in -3..3) {
//                        val p = Point(x, y)
//                        if (visitedBoardIndices.contains(p)) {
//                            print("\"${firstPositionVisited[p]} @ ${firstTimeVisited[p]}\",")
//                        } else {
//                            print(",")
//                        }
//                    }
//                    println()
//                }
//            }
//        }
//
//        current = current.flatMap { point ->
//            point.neighbors.filter { neighbor -> map.get(neighbor) != '#' }
//        }.toSet()
//    }

    val map = file.readLines().filter { it.isNotEmpty() }

    val steps = 26501365
    // Add up how many steps there are for each board. Handling the following sections:
    // Center board
    // Straight lines going north, south, east and west
    // The triangle sections north-east, north-west, south-east and south-west
    val stepsLostLeavingCenter = 66
    val stepsLostAcrossBoard = 131

    println("Calculating center...")
    val possibleCenterPositions = countPossiblePositions(map, Point(65, 65), steps)

    // Straight lines
    println("Calculating lines...")
    val possiblePositionsOnLine = run {
        var stepsLeft = steps - stepsLostLeavingCenter
        val startPositions = listOf(
            Point(130, 65), // Going west
            Point(65, 130), // Going north
            Point(0, 65), // Going east
            Point(65, 0), // Going south
        )
        var possiblePositions = 0L
        while (stepsLeft > 0) {
            for (start in startPositions) {
                val possibleStepsOnBoard = countPossiblePositions(map, start, stepsLeft)
                possiblePositions += possibleStepsOnBoard
            }
            stepsLeft -= stepsLostAcrossBoard
        }
        possiblePositions
    }

    // Triangle sections
    println("Calculating triangle...")
    val possiblePositionsOnTriangle = run {
        var stepsLeft = steps - stepsLostLeavingCenter - stepsLostLeavingCenter
        val startPositions = listOf(
            Point(130, 130), // Going north-west
            Point(0, 130), // Going north-east
            Point(0, 0), // Going south-east
            Point(130, 0), // Going south-west
        )
        var possiblePositions = 0L
        var diagonalSize = 1
        while (stepsLeft > 0) {
            for (start in startPositions) {
                val possibleStepsOnBoard = countPossiblePositions(map, start, stepsLeft)
                possiblePositions += diagonalSize * possibleStepsOnBoard
            }
            stepsLeft -= stepsLostAcrossBoard
            diagonalSize++
        }
        possiblePositions
    }

    val totalSteps = possibleCenterPositions + possiblePositionsOnLine + possiblePositionsOnTriangle
    println("Part 2: $totalSteps")
}
