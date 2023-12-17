package day14

import shared.getInputFile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    runBlocking { solve(animate = false) }
}

enum class Direction {
    LEFT, RIGHT, UP, DOWN;

    // Getter for if this is along the x-axis.
    val isX: Boolean
        get() = this == LEFT || this == RIGHT

    // Getter for if this is along the y-axis.
    val isY: Boolean
        get() = this == UP || this == DOWN

    val isPositive: Boolean
        get() = this == RIGHT || this == DOWN
}

// For this problem, I'm going to try animating it as an excuse to try Kotlin's coroutines.
suspend fun solve(animate: Boolean = false) {
    val grid = getInputFile(14)
        .readLines()
        .filterNot { it.isEmpty() }
        // Split each line into a mutable list of characters
        .map { it.toMutableList() }
        .toMutableList()

    if (animate) {
        printFrame(null, grid)
    }

//    fall(grid, Direction.UP, animate);
//
//    // Calculate the 'load'.
//    val load = grid
//        .reversed()
//        .mapIndexed { antiY, row ->
//            row.count { it == 'O' } * (antiY + 1)
//        }
//        .sum()
//    println("Part 1: $load")

    // Part 2
    // Test the falling logic
    val fallOrder = listOf(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT)
    val encounteredStates = mutableMapOf(gridToString(grid) to 0)
    for (i in 1..10000) {
        for (direction in fallOrder) {
            fall(grid, direction, animate)
        }
        val gridString = gridToString(grid)
        val load = calculateLoad(grid)
        println("$i:\t${i%18}\t$load")

        if (gridString in encounteredStates) {
            val loopStart = encounteredStates[gridString]!!
            val loopLength = i - loopStart
            println("Loop detected: $loopStart -> $i ($loopLength)")
            break
        }
        encounteredStates[gridString] = i
    }
}

suspend fun fall(grid: MutableList<MutableList<Char>>, direction: Direction, animate: Boolean) {
    val fallAxis = when (direction) {
        Direction.UP -> grid.indices
        Direction.DOWN -> grid.indices.reversed()
        Direction.LEFT -> grid.first().indices
        Direction.RIGHT -> grid.first().indices.reversed()
    }
    val otherAxis = if (direction.isY) grid.first().indices else grid.indices

    for (o in otherAxis) {
        var fallIndex = fallAxis.first()
        for (f in fallAxis) {
            val gridVal = if (direction.isY) grid[f][o] else grid[o][f]
            when (gridVal) {
                '.' -> Unit
                '#' -> {
                    // '#' represents a blocked space, so the next space is our new fall index.
                    // If that next space is also blocked, we'll update fall index on the next loop so that's fine.
                    fallIndex = f + if (direction.isPositive) -1 else 1
                }
                'O' -> {
                    // 'O' represents a rock that will fall (upwards).
                    // Remove from this position and place at fall index.
                    if (direction.isY) {
                        grid[f][o] = '.'
                        grid[fallIndex][o] = 'O'
                    } else {
                        grid[o][f] = '.'
                        grid[o][fallIndex] = 'O'
                    }
                    // New rocks fall on top.
                    fallIndex += if (direction.isPositive) -1 else 1

                    if (animate) {
                        printFrame(direction, grid)
                    }
                }
            }
        }
    }
}

/**
 * Prints one frame to the console, by clearing the console, printing the grid, and then waiting for a frame's length.
 */
suspend fun printFrame(direction: Direction?, grid: List<List<Char>>) {

    // Clear the console.
    print("\u001b[H\u001b[2J")
//    // Workaround for IntelliJ not clearing the console properly.
//    println("\n".repeat(20))
    println("Falling $direction")
    printGrid(grid)
    delay(500.milliseconds)
}

fun printGrid(grid: List<List<Char>>) {
    println(gridToString(grid))
}

fun gridToString(grid: List<List<Char>>): String {
    return grid.joinToString("\n") { it.joinToString("") }
}

fun calculateLoad(grid: List<List<Char>>): Int {
    return grid
        .reversed()
        .mapIndexed { antiY, row ->
            row.count { it == 'O' } * (antiY + 1)
        }
        .sum()
}
