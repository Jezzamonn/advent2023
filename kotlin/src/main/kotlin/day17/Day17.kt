package day17

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import shared.getInputFile
import kotlin.time.Duration.Companion.milliseconds

const val minStepsInDirection = 4
const val maxStepsInDirection = 10


data class MazePosition(
    val x: Int,
    val y: Int,
    val numNorthSteps: Int = 0,
    val numEastSteps: Int = 0,
    val numSouthSteps: Int = 0,
    val numWestSteps: Int = 0) {
    fun neighbors(): List<MazePosition> {
        val north = MazePosition(x, y - 1, numNorthSteps = numNorthSteps + 1)
        val east = MazePosition(x + 1, y, numEastSteps = numEastSteps + 1)
        val south = MazePosition(x, y + 1, numSouthSteps = numSouthSteps + 1)
        val west = MazePosition(x - 1, y, numWestSteps = numWestSteps + 1)

        if (numNorthSteps in 1..<minStepsInDirection) {
            return listOf(north)
        }
        if (numEastSteps in 1..<minStepsInDirection) {
            return listOf(east)
        }
        if (numSouthSteps in 1..<minStepsInDirection) {
            return listOf(south)
        }
        if (numWestSteps in 1..<minStepsInDirection) {
            return listOf(west)
        }

        val neighbors = mutableListOf<MazePosition>()
        if (numNorthSteps < maxStepsInDirection && numSouthSteps == 0) {
            neighbors.add(north)
        }
        if (numEastSteps < maxStepsInDirection && numWestSteps == 0) {
            neighbors.add(east)
        }
        if (numSouthSteps < maxStepsInDirection && numNorthSteps == 0) {
            neighbors.add(south)
        }
        if (numWestSteps < maxStepsInDirection && numEastSteps == 0) {
            neighbors.add(west)
        }
        return neighbors
    }

    /** A character based on the direction thing was taken (using numNorthSteps, numEastSteps, etc) */
    val getDirectionCharacter: Char = when {
        numNorthSteps > 0 -> '^'
        numEastSteps > 0 -> '>'
        numSouthSteps > 0 -> 'v'
        numWestSteps > 0 -> '<'
        else -> 'S'
    }
}

data class SearchState(val position: MazePosition, val cost: Int, val parent: SearchState? = null)

fun main() {
    runBlocking {
        solve()
    }
}

suspend fun solve() {
    val map = getInputFile(17)
        .readText()
        .trim()
        .split("\n")
        .map { line ->
            line.map { c -> c.digitToInt() }
        }

    // BFS through the map to find the shortest path.
    val start = SearchState(MazePosition(0, 0), 0)

    val endX = map.first().size - 1
    val endY = map.size - 1

    val visited = mutableSetOf<MazePosition>()
    val toVisit = mutableListOf(start)
    var end: SearchState? = null
    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeFirst()
        if (current.position.x == endX && current.position.y == endY) {
            println("Part 1: ${current.cost}")
            end = current
            break
        }
        if (visited.contains(current.position)) {
            continue
        }
        visited.add(current.position)
        val neighbors = current.position.neighbors()
            .filter { neighborPosition ->
                neighborPosition.y in 0..endY && neighborPosition.x in 0..endX
            }
            .map { neighborPosition ->
                val extraCost = map[neighborPosition.y][neighborPosition.x]
                SearchState(neighborPosition, current.cost + extraCost, parent=current)
            }
        toVisit.addAll(neighbors)
        toVisit.sortBy { it.cost  }

//        printFrame(map, visited, toVisit)
    }

    printPathOnMap(map, end!!)

}

fun printPathOnMap(map: List<List<Int>>, end: SearchState) {
    val endX = map.first().size - 1
    val endY = map.size - 1

    val path = mutableListOf<SearchState>()
    var current = end
    while (current.parent != null) {
        path.add(current)
        current = current.parent!!
    }
    path.add(current)

    for (y in 0..endY) {
        for (x in 0..endX) {
            // Check if the x and y are in the visited set
            val matchingPath = path.find { it.position.x == x && it.position.y == y }
            if (matchingPath != null) {
                // Print an arrow depending on the direction. In red.
                print("\u001B[31m${matchingPath.position.getDirectionCharacter}\u001B[0m")
            }
            else {
                // Print the map
                print(map[y][x])
            }
        }
        println()
    }
    println()
}

suspend fun printFrame(map: List<List<Int>>, visited: Set<MazePosition>, toVisit: List<SearchState>) {
    // Print some new lines to clear the screen
    println("\n".repeat(20))
    printBFS(map, visited, toVisit)
    // Wait for a little
    delay(10.milliseconds)
}

fun printBFS(map: List<List<Int>>, visited: Set<MazePosition>, toVisit: List<SearchState>) {
    val endX = map.first().size - 1
    val endY = map.size - 1

    for (y in 0..endY) {
        for (x in 0..endX) {
            val position = MazePosition(x, y)
            // Check if the x and y are in the visited set
            if (visited.any { it.x == x && it.y == y }) {
                // Print X in red
                print("\u001B[31mX\u001B[0m")
            }
            else if (toVisit.any { it.position.x == x && it.position.y == y }) {
                // Print O in green
                print("\u001B[32mO\u001B[0m")
            }
            else {
                // Print the map
                print(map[y][x])
            }
        }
        println()
    }
    println()
}