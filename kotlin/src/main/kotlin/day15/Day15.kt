package day15

import shared.getInputFile

data class Entry(val label: String, var value: Int) {
    override fun toString() = "[$label $value]"
}

fun main() {
    val input = getInputFile(15).readText().trim()
    val steps = input.split(",")

    steps
        .sumOf { hash(it) }
        .let { println("Part 1: $it") }

    // Part 2
    // Our custom hashmap
    val boxes = mutableListOf<MutableList<Entry>>()
    for (i in 0..255) {
        boxes.add(mutableListOf())
    }
    for (step in steps) {
        println("Step: $step")
        if (step.endsWith("-")) {
            val label = step.dropLast(1)
            val hash = hash(label)
            val box = boxes[hash]
            // Remove entry with the same label, if it exists
            box.removeIf { it.label == label }
        }
        else if (step.contains("=")){
            val (label, value) = step.split("=")
            val hash = hash(label)
            val box = boxes[hash]
            // Check if there's already a value with the same label
            val existing = box.find { it.label == label }
            if (existing != null) {
                existing.value = value.toInt()
            }
            else {
                box.add(Entry(label, value.toInt()))
            }
        }
        else {
            println("Invalid step: $step")
        }
        println(boxesToString(boxes))
        println()
    }
    // Calculate the value for part 2
    boxes.withIndex().sumOf { (boxIndex, box) ->
        box.withIndex().sumOf { (slotIndex, entry) ->
            (boxIndex + 1) * (slotIndex + 1) * entry.value
        }
    }.let { println("Part 2: $it") }
}

fun hash(s: String): Int {
    var value = 0
    for (c in s) {
        value += c.code
        value *= 17
        value %= 256
    }
    return value
}

fun boxesToString(boxes: List<List<Entry>>) = boxes
    .withIndex()
    .filter { (_, box) -> box.isNotEmpty() }
    .joinToString("\n") { (i, box) -> "$i: ${box.joinToString(" ")}"
}