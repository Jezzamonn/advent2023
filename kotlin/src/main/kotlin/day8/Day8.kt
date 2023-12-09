package day8

import shared.getInputFile

// Parse a line like "AAA = (BBB, CCC)"
val graphSectionRegex = """(\w+) = \((\w+), (\w+)\)""".toRegex()

fun main() {
    val sections = getInputFile(8).readText().trim().split("\n\n")
    val instructions = sections[0]
    val graph = sections[1].lines().map { line ->
        val (node, left, right) = graphSectionRegex.matchEntire(line)!!.destructured
        node to Pair(left, right)
    }.toMap()

    var current = "AAA"
    var numSteps = 0
    while (current != "ZZZ") {
        val (left, right) = graph[current]!!
        current = if (instructions[numSteps % instructions.length] == 'L') left else right
        numSteps++
    }
    println("Part 1: $numSteps")
}