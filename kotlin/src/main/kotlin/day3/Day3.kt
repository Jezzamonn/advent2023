package day3

import java.io.File

data class PartNumber(val xRange: IntRange, val y: Int, val number: Int, var isPart: Boolean = false) {
    fun getExtendedRange(): IntRange {
        return (xRange.first - 1)..(xRange.last + 1)
    }
}

fun main(args: Array<String>) {
    val lines = File(args[0]).readLines().filter{ it.isNotEmpty() }
    // Part 1
    val partNumbersByHeight = lines.mapIndexed { y, line ->
        Regex("\\d+").findAll(line).map {
                PartNumber(xRange = it.range, y = y, number = it.value.toInt())
            }
            .toList()
    }
    // Find part numbers next to symbols.
    lines.forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            when (c) {
                in '0'..'9' -> Unit
                '.' -> Unit
                else -> {
                    markAdjacentAsParts(partNumbersByHeight, x, y)
                }
            }
        }
    }
    val part1 = partNumbersByHeight
        .flatten()
        .filter { partNumber -> partNumber.isPart }
        .sumOf { partNumber -> partNumber.number }
    println("Part 1: $part1")

    // Part 2
    val delta = listOf(-1, 0, 1)

    val part2 = lines.mapIndexed { y, line ->
        line.mapIndexed { x, c ->
            var score = 0
            if (c == '*') {
                val numbersInAdjacentRows = delta.flatMap { dy ->
                    partNumbersByHeight.elementAtOrElse(y + dy) { emptyList() }
                }

                val adjacentNumbers = numbersInAdjacentRows.filter {
                    it.getExtendedRange().contains(x)
                }
                if (adjacentNumbers.size == 2) {
                    score = adjacentNumbers[0].number * adjacentNumbers[1].number
                }
            }
            score
        }.sum()
    }.sum()

    println("Part 2: $part2")
}

fun markAdjacentAsParts(partNumbersByHeight: List<List<PartNumber>>, x: Int, y: Int) {
    val delta = listOf(-1, 0, 1)
    for (dy in delta) {
        val partNumbersInRow = partNumbersByHeight.elementAtOrElse(y + dy) { emptyList() }
        for (partNumber in partNumbersInRow) {
            if (x in partNumber.getExtendedRange()) {
                partNumber.isPart = true
            }
        }
    }
}
