package day9

import shared.getInputFile

fun main() {
    val input = getInputFile(9).readText().trim()
    val part1 = input.lines().sumOf { line ->
//        println("\nSequence: $line")
        val start = line.split(" ").map { it.toLong() }.toMutableList()
        val toExtrapolate = mutableListOf(start)
        while (toExtrapolate.last().any { it != 0L }) {
            val next = toExtrapolate.last().zipWithNext().map { it.second - it.first }.toMutableList()
            toExtrapolate.add(next)
        }
        toExtrapolate.last().add(0)
        // Now go back and extrapolate
        toExtrapolate.reversed().zipWithNext().forEach { (deltas, next) ->
            val nextValue = next.last() + deltas.last()
            next.add(nextValue)
        }
//        println(toExtrapolate.joinToString("\n"))
        toExtrapolate.first().last()
    }
    println("Part 1: $part1")

    val part2 = input.lines().sumOf { line ->
//        println("\nSequence: $line")
        val start = line.split(" ").map { it.toLong() }.reversed().toMutableList()
        val toExtrapolate = mutableListOf(start)
        while (toExtrapolate.last().any { it != 0L }) {
            val next = toExtrapolate.last().zipWithNext().map { it.second - it.first }.toMutableList()
            toExtrapolate.add(next)
        }
        toExtrapolate.last().add(0)
        // Now go back and extrapolate
        toExtrapolate.reversed().zipWithNext().forEach { (deltas, next) ->
            val nextValue = next.last() + deltas.last()
            next.add(nextValue)
        }
//        println(toExtrapolate.joinToString("\n"))
        toExtrapolate.first().last()
    }
    println("Part 2: $part2")

}