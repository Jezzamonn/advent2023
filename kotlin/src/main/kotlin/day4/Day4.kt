package day4

import java.io.File
import kotlin.math.pow

// Parse a line like "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53"
val linePattern = "Card\\s+(\\d+): (.*) \\| (.*)".toRegex()

fun main(args: Array<String>) {
    val lines = File(args[0]).readLines().filter { it.isNotEmpty() }
    val part1 = lines.sumOf { line ->
        val (_, numbersStr, winningNumbersStr) = linePattern.matchEntire(line)!!.destructured
        val numbers = numbersStr
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toSet()
        val winningNumbers = winningNumbersStr
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toSet()
        val numWinningNumbers = numbers.intersect(winningNumbers).size
        if (numWinningNumbers == 0) 0 else 1 shl (numWinningNumbers - 1)
    }
    println("Part 1: $part1")

    // Part 2
    val numCopies = lines.map { 1 }.toMutableList()

    for ((i, line) in lines.withIndex()) {
        val (_, numbersStr, winningNumbersStr) = linePattern.matchEntire(line)!!.destructured
        val numbers = numbersStr
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toSet()
        val winningNumbers = winningNumbersStr
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toSet()
        val numWinningNumbers = numbers.intersect(winningNumbers).size

        for (j in 1..numWinningNumbers) {
            numCopies[i+j] += numCopies[i]
        }
    }

    val part2 = numCopies.sum()
    println("Part 2: $part2")
}