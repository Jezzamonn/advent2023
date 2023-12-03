import java.io.File

val digits = mapOf(
    "0" to 0,
    "1" to 1,
    "2" to 2,
    "3" to 3,
    "4" to 4,
    "5" to 5,
    "6" to 6,
    "7" to 7,
    "8" to 8,
    "9" to 9,
    "zero" to 0,
    "one" to 1,
    "two" to 2,
    "three" to 3,
    "four" to 4,
    "five" to 5,
    "six" to 6,
    "seven" to 7,
    "eight" to 8,
    "nine" to 9
)

fun firstDigit(line: String): String {
    return digits
        .mapValues { line.indexOf(it.key) }
        .filterValues { it >= 0 }
        .minBy { it.value }
        .key
}

fun lastDigit(line: String): String {
    return digits
        .mapValues { line.lastIndexOf(it.key) }
        .filterValues { it >= 0 }
        .maxBy { it.value }
        .key
}

fun main(args: Array<String>) {
    // Read input using filename from program arguments.
    val input = File(args[0]).readText()
    // Part 1.
    val part1 = input.lines()
        .filterNot { it.isEmpty() }
        .map { line ->
            val digits = line.filter { c -> c.isDigit() }
            (digits.first().toString() + digits.last()).toInt()
        }
        .sum()

    println("Part 1: $part1")

    // Part 2
    val part2 = input.lines()
        .filterNot { it.isEmpty() }
        .sumOf { line ->
            val firstDigit = firstDigit(line)
            val lastDigit = lastDigit(line)
            10 * digits[firstDigit]!! + digits[lastDigit]!!
        }

    println("Part 2: $part2")
}

