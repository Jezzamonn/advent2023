package day8

import shared.getInputFile

// Parse a line like "AAA = (BBB, CCC)"
val graphSectionRegex = """(\w+) = \((\w+), (\w+)\)""".toRegex()

data class Loop(val position: Long, val length: Long): Comparable<Loop> {
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
                loop = Loop(loopLength.toLong(), loopLength.toLong())
                break
            }
            firstVisitedAt[visited] = numSteps
        }
        loop
    }

    val part2 = loops.reduce { acc, loop ->
        println("acc: $acc, loop: $loop")
        var position = acc.position
        while (position % loop.length != loop.position % loop.length) {
//            println("position: ${position % loop.length}, target: ${loop.position % loop.length}")
            position += acc.length
        }
        Loop(position, lcm(acc.length, loop.length))
    }
    println("Part 2: ${part2.position}")
    // Too high = 12237387389802 :(
    //            10151663816849  - wahoo!!
}

fun lcm(a: Long, b: Long): Long {
    return a * b / gcd(a, b)
}

fun gcd(a: Long, b: Long): Long {
    return if (b == 0L) a else gcd(b, a % b)
}