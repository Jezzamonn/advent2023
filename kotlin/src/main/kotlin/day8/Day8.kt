package day8

import shared.getInputFile
import java.lang.IllegalStateException
import java.util.PriorityQueue

// Parse a line like "AAA = (BBB, CCC)"
val graphSectionRegex = """(\w+) = \((\w+), (\w+)\)""".toRegex()

data class Loop(val position: Long, val length: Int): Comparable<Loop> {
    override fun compareTo(other: Loop): Int {
        return position.compareTo(other.position)
    }
}
data class Visited(val position: String, val instructionCount: Int)

fun main() {
    val sections = getInputFile(8).readText().trim().split("\n\n")
    val instructions = sections[0]
    val graph = sections[1].lines().map { line ->
        val (node, left, right) = graphSectionRegex.matchEntire(line)!!.destructured
        node to Pair(left, right)
    }.toMap()

    if (graph.containsKey("AAA")) {
        var current = "AAA"
        var numSteps = 0
        while (current != "ZZZ") {
            val (left, right) = graph[current]!!
            current = if (instructions[numSteps % instructions.length] == 'L') left else right
            numSteps++
        }
        println("Part 1: $numSteps")
    }

    // Part 2: Multiple positions.
    println("Part 2:")
    val startPositions = graph.keys.filter { it.endsWith("A") }

    val loops = startPositions.map { startPosition ->
        var current = startPosition
        var numSteps = 0
        var instructionIndex = 0
        val firstVisitedAt = mutableMapOf(Visited(current, instructionIndex) to 0)
        val loop: Loop
        while (true) {
            val (left, right) = graph[current]!!
            current = if (instructions[numSteps % instructions.length] == 'L') left else right
            numSteps++
            instructionIndex = (instructionIndex + 1) % instructions.length
            val visited = Visited(current, instructionIndex)

            if (current.endsWith("Z")) {
                println("at $current after $numSteps steps")
            }

            if (firstVisitedAt.containsKey(visited)) {
                val loopStart = firstVisitedAt[visited]!!
                val loopLength = numSteps - loopStart
                println("Loop found at $current after $numSteps steps, loop start: $loopStart, loop length: $loopLength")
                loop = Loop(loopStart.toLong(), loopLength)
                break
            }
            firstVisitedAt[visited] = numSteps
        }
        loop
    }

    val pq = PriorityQueue<Loop>()
    pq.addAll(loops)
    while (!pq.all { it.position == pq.peek().position }) {
        val smallest = pq.poll()
//        println("Smallest: $smallest")
        val next = smallest.copy(position = (smallest.position + smallest.length))
        pq.add(next)
    }
    println("Part 2: ${pq.peek().position}")
}