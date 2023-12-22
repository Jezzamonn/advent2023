package day16

import shared.getInputFile

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun delta(): Point = when (this) {
        UP -> Point(0, -1)
        DOWN -> Point(0, 1)
        LEFT -> Point(-1, 0)
        RIGHT -> Point(1, 0)
    }
}

data class Beam(val position: Point, val direction: Direction)

fun main() {
    val map = getInputFile(16).readText().trim().lines()

    println("Part 1: ${getNumVisitedPositions(map, Beam(Point(0, 0), Direction.RIGHT))}")

    // Part 2: Find the starting point that results in the most visited positions
    val startingPoints =
        map.indices.map {
            // Left edge
            Beam(Point(0, it), Direction.RIGHT)
        } + map.indices.map {
            // Right edge
            Beam(Point(map.first().length - 1, it), Direction.LEFT)
        } + map.first().indices.map {
            // Top edge
            Beam(Point(it, 0), Direction.DOWN)
        } + map.first().indices.map {
            // Bottom edge
            Beam(Point(it, map.size - 1), Direction.UP)
        }

    val bestVisited = startingPoints.maxOf { getNumVisitedPositions(map, it) }
    println("Part 2: $bestVisited")

}

fun getNumVisitedPositions(map: List<String>, start: Beam): Int {
    val inProgressBeams = mutableListOf(start)
    val visited = mutableSetOf<Beam>()

    while (inProgressBeams.isNotEmpty()) {
        val current = inProgressBeams.removeFirst()

        if (current.position.x !in map.first().indices || current.position.y !in map.indices) {
            continue
        }

        if (visited.contains(current)) {
            continue
        }
        visited.add(current)

        val char = map[current.position.y][current.position.x]
        when (char) {
            '\\' -> {
                val newDirection = when (current.direction) {
                    Direction.RIGHT -> Direction.DOWN
                    Direction.DOWN -> Direction.RIGHT
                    Direction.UP -> Direction.LEFT
                    Direction.LEFT -> Direction.UP
                }
                inProgressBeams.add(Beam(current.position + newDirection.delta(), newDirection))
            }
            '/' -> {
                val newDirection = when (current.direction) {
                    Direction.RIGHT -> Direction.UP
                    Direction.UP -> Direction.RIGHT
                    Direction.DOWN -> Direction.LEFT
                    Direction.LEFT -> Direction.DOWN
                }
                inProgressBeams.add(Beam(current.position + newDirection.delta(), newDirection))
            }
            '-' -> {
                if (current.direction == Direction.RIGHT || current.direction == Direction.LEFT) {
                    inProgressBeams.add(Beam(current.position + current.direction.delta(), current.direction))
                }
                else {
                    // Split the beam into two going left and right
                    inProgressBeams.add(Beam(current.position + Direction.LEFT.delta(), Direction.LEFT))
                    inProgressBeams.add(Beam(current.position + Direction.RIGHT.delta(), Direction.RIGHT))
                }
            }
            '|' -> {
                if (current.direction == Direction.UP || current.direction == Direction.DOWN) {
                    inProgressBeams.add(Beam(current.position + current.direction.delta(), current.direction))
                }
                else {
                    // Split the beam into two going up and down
                    inProgressBeams.add(Beam(current.position + Direction.UP.delta(), Direction.UP))
                    inProgressBeams.add(Beam(current.position + Direction.DOWN.delta(), Direction.DOWN))
                }
            }
            '.' -> {
                inProgressBeams.add(Beam(current.position + current.direction.delta(), current.direction))
            }
        }
    }

    return visited.map { it.position }.toSet().size
}