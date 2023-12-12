package day12

import shared.getInputFile

data class ArrangementSearchInput(val hashes: String, val numHashes: List<Int>, val hashesStart: Int)

fun main() {
    val input = getInputFile(12).readText().trim()

    input.lines().sumOf { line ->
        val (hashes, numHashesStr) = line.split(" ")
        println()
        println(line)
        val numHashes = numHashesStr.split(",").map { it.toInt() }
        val numArrangements = numPossibleArrangements(hashes, numHashes, cache = mutableMapOf())
        println("Arrangements = $numArrangements")
        numArrangements
    }.let { println("Part 1: $it") }

    // Part 2
    input.lines().sumOf { line ->
        val (hashes, numHashesStr) = line.split(" ")
        val hashesRepeated = (0..<5).joinToString("?") { hashes }
        println()
        println(line)
        val numHashes = numHashesStr.split(",").map { it.toInt() }
        // Also repeat the number of hashes
        val numHashesRepeated = (0..<5).flatMap { numHashes }
        println("$hashesRepeated $numHashesRepeated")

        val numArrangements = numPossibleArrangements(hashesRepeated, numHashesRepeated, cache = mutableMapOf())
        println("Arrangements = $numArrangements")
        numArrangements
    }.let { println("Part 2: $it") }
    // 4093521072395 too low??? oh no
    // 28606137449920 :) better
}

/**
 * We have a string like ".??..??...?##." and a number of hashes in a row,
 * and we need to determine how many ways can we arrange the hashes.
 */
fun numPossibleArrangements(
    hashes: String,
    numHashes: List<Int>,
    hashesStart: Int = 0,
    cache: MutableMap<ArrangementSearchInput, Long>
): Long {
    // Probably faster to handle the base cases without the cache.
    // Base cases: no hashes left to place
    if (numHashes.isEmpty()) {
        // Are there any hashes still left tho?
        return if (hashes.drop(hashesStart).any { it == '#' }) 0L else 1L
    }

    // No string left but more hashes to place
    if (hashesStart >= hashes.length) return 0L

    // Check the cache
    val input = ArrangementSearchInput(hashes, numHashes, hashesStart)
    if (input in cache) return cache[input]!!

    // Try placing the first set of hashes at each possible position.
    val currentGroup = numHashes.first()
    val remainingGroups = numHashes.drop(1)
    var numWays = 0L
    for (i in hashesStart..hashes.length - currentGroup) {
        // Check if there were any hashes in the string that we skipped over
        if (hashes.substring(hashesStart, i).any { it == '#' }) break

        val sub = hashes.substring(i, i + currentGroup)
        val next = hashes.elementAtOrElse(i + currentGroup) { '.' }
        if (sub.all { it == '#' || it == '?' } && next != '#') {
            // Try placing the remaining hashes
            numWays += numPossibleArrangements(hashes, remainingGroups, i + currentGroup + 1, cache)
        }
    }

    // Cache the result
    cache[input] = numWays

    return numWays
}