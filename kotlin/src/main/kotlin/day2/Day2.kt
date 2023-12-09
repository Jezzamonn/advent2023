package day2

import java.io.File

// Regex to parse a line like
// Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
val lineRegex = "Game (\\d+): (.*)".toRegex()
val cubeRegex = "(\\d+) (red|green|blue)".toRegex()

fun gameHasMax(cubesCount: Map<String, Int>, maxRed: Int, maxGreen: Int, maxBlue: Int): Boolean {
    return (cubesCount["red"] ?: 0) <= maxRed &&
            (cubesCount["green"] ?: 0) <= maxGreen &&
            (cubesCount["blue"] ?: 0) <= maxBlue
}

fun main(args: Array<String>) {
    // Read input using filename from program arguments.
    val input = File(args[0]).readText()
    // Part 1.
    val part1 = input.lines()
        .filterNot { it.isEmpty() }
        .sumOf { line ->
            val (gameStr, drawsStr) = lineRegex.matchEntire(line)!!.destructured

            val allPossible = drawsStr.split("; ").map { draw ->
                draw.split(", ")
                    .map { cubeRegex.matchEntire(it)!! }
                    .associateBy({ it.groupValues[2] }, { it.groupValues[1].toInt() })
            }.all {
                gameHasMax(it, maxRed = 12, maxGreen = 13, maxBlue = 14)
            }

            if (allPossible) gameStr.toInt() else 0
        }
    println("Part 1: $part1")

    // Part 2
    val part2 = input.lines()
        .filterNot { it.isEmpty() }
        .sumOf { line ->
            val (gameIndex, drawsStr) = lineRegex.matchEntire(line)!!.destructured

            val maxCubes = cubeRegex.findAll(drawsStr)
                .groupBy { it.groupValues[2] }
                .mapValues { it.value.maxOf { cube -> cube.groupValues[1].toInt() } }
            val power = (maxCubes["red"] ?: 0) * (maxCubes["green"] ?: 0) * (maxCubes["blue"] ?: 0)
            println("Power for game $gameIndex: $power")
            power
        }
    println("Part 2: $part2")
}
