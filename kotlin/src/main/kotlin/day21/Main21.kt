package day21

import shared.getInputFile
import java.io.File

data class Point(val x: Int, val y: Int)

fun main() {
    val inputFile = getInputFile(21)
    val part1 = solve(inputFile, 64)
    println("Part 1: $part1")
}

fun solve(file: File, numSteps: Int): Int {
    val map = file.readLines().filter { it.isNotEmpty() }
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

    var current = setOf(start)
    for (i in 0..<numSteps) {
        current = current.flatMap { point ->
            val neighbors = listOf(
                Point(point.x - 1, point.y),
                Point(point.x + 1, point.y),
                Point(point.x, point.y - 1),
                Point(point.x, point.y + 1),
            )
            neighbors.filter { neighbor ->
                neighbor.x >= 0 && neighbor.y >= 0 &&
                        neighbor.x < map[0].length && neighbor.y < map.size &&
                map[neighbor.y][neighbor.x] != '#'
            }
        }.toSet()
    }
    return current.size
}