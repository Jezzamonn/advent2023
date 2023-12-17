package day13

import shared.getInputFile
import kotlin.math.min

fun main() {
    val input = getInputFile(13).readText().trim()

    val images = input.split("\n\n").map { it.split("\n") }
    images.sumOf { rows ->
        val height = rows.size
        val width = rows.first().length

        println("-- Checking image, w = $width, h = $height")
        val reflectionRow = findReflectionInRows(rows)
        println("row = $reflectionRow")

        val cols = rows.first().indices.map { c ->
            rows.map { row -> row[c] }.joinToString()
        }
        val reflectionCol = findReflectionInRows(cols)
        println("col = $reflectionCol")
        100 * reflectionRow + reflectionCol
    }.let { println("Part 1: $it") }


    images.sumOf { rows ->
        val height = rows.size
        val width = rows.first().length

        println("-- Checking image, w = $width, h = $height")
        val reflectionRow = findReflectionLineAccountingForSmudge(rows)
        println("row = $reflectionRow")

        val cols = rows.first().indices.map { c ->
            rows.map { row -> row[c] }.joinToString("")
        }
        val reflectionCol = findReflectionLineAccountingForSmudge(cols)
        println("col = $reflectionCol")
        100 * reflectionRow + reflectionCol
    }.let { println("Part 2: $it") }
}

fun findReflectionInRows(rows: List<String>): Int {
    val height = rows.size
    val reflectionLines = (1..<height).filter { potentialReflectionLine ->
        val rowsToCheck = min(potentialReflectionLine, height - potentialReflectionLine)
        (0..<rowsToCheck).all { delta ->
            val r1 = potentialReflectionLine - 1 - delta
            val r2 = potentialReflectionLine + delta

            rows[r1] == rows[r2]
        }
    }
    return reflectionLines.singleOrNull() ?: 0
}

fun findReflectionLineAccountingForSmudge(rows: List<String>): Int {
    val height = rows.size
    val width = rows.first().length
    val reflectionLines = (1..<height).filter { potentialReflectionLine ->
        val rowsToCheck = min(potentialReflectionLine, height - potentialReflectionLine)

        val numRowMatches = (0..<rowsToCheck).map { delta ->
            val r1 = potentialReflectionLine - 1 - delta
            val r2 = potentialReflectionLine + delta

            numMatching(rows[r1], rows[r2])
        }.groupingBy { it }.eachCount()

        numRowMatches[width - 1] == 1 && numRowMatches.getOrDefault(width, 0) == rowsToCheck - 1
    }
    return reflectionLines.singleOrNull() ?: 0
}

fun numMatching(s1: String, s2: String): Int {
    return s1.zip(s2).count { (c1, c2) -> c1 == c2 }
}