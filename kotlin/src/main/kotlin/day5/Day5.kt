package day5

import java.io.File

data class SeedMapRange(val sourceRange: LongRange, val destDelta: Long)

data class RangeTransition(
    val position: Long,
    val seedMapRangeStart: SeedMapRange? = null,
    val seedMapRangeEnd: SeedMapRange? = null,
    val seedRangeStart: LongRange? = null,
    val seedRangeEnd: LongRange? = null,
) {
    override fun toString(): String {
        return "RangeTransition(position=$position, " + when {
            seedMapRangeStart != null -> "seedMapRangeStart=$seedMapRangeStart"
            seedMapRangeEnd != null -> "seedMapRangeEnd=$seedMapRangeEnd"
            seedRangeStart != null -> "seedRangeStart=$seedRangeStart"
            seedRangeEnd != null -> "seedRangeEnd=$seedRangeEnd"
            else -> "Unknown"
        }
    }
}

fun main(args: Array<String>) {
    val input = File(args[0]).readText().trim()
    val sections = input.split("\n\n")
    val seeds = sections[0].split(" ").drop(1).map { it.toLong() }
    val seedMaps = sections.drop(1).map { section ->
        section.lines().drop(1).map { line ->
            val (destStart, sourceStart, length) = line.split(" ").map { it.toLong() }
            SeedMapRange(
                sourceRange = sourceStart..<(sourceStart+length),
                destDelta = destStart - sourceStart)
        }.sortedBy { it.sourceRange.first }
    }

    val lowestLocation = seeds.minOf {  seed ->
        var currentValue = seed
        println("Seed: $seed")
        for (seedMap in seedMaps) {
            val matchingRange = seedMap.find {it.sourceRange.contains(currentValue)}
            val delta = matchingRange?.destDelta ?: 0

            val newValue = currentValue + delta
            println("$currentValue -> $newValue")
            currentValue = newValue
        }
        currentValue
    }
    println("Part 1: $lowestLocation")

    // Part 2
    val seedRanges = (seeds.indices step 2).map { i -> LongRange(seeds[i], seeds[i] + seeds[i+1]) }.sortedBy { it.first }
    var currentSeedRanges = seedRanges
    for ((index, seedMap) in seedMaps.withIndex()) {
        println("Seed map $index")
        // Put everything in one big list to iterate through.
        val mapTransitions = seedMap.map { seedMapRange ->
            listOf(
                RangeTransition(
                    position = seedMapRange.sourceRange.first,
                    seedMapRangeStart = seedMapRange),
                RangeTransition(
                    position = seedMapRange.sourceRange.last,
                    seedMapRangeEnd = seedMapRange)
            )
        }.flatten()
        val seedTransitions = currentSeedRanges.map { seedRange ->
            listOf(
                RangeTransition(
                    position = seedRange.first,
                    seedRangeStart = seedRange),
                RangeTransition(
                    position = seedRange.last,
                    seedRangeEnd = seedRange)
            )
        }.flatten()
        val allTransitions = (mapTransitions + seedTransitions).sortedBy { it.position }

        val nextSeedRanges = mutableListOf<LongRange>()
        var seedRangeStart: Long? = null
        var numRanges = 0
        var activeMapDelta: Long? = null
        for (transition in allTransitions) {
            println("$transition")
            if (transition.seedRangeStart != null) {
                if (numRanges == 0) {
                    seedRangeStart = transition.position
                }
                numRanges++
            }
            if (transition.seedRangeEnd != null) {
                if (numRanges == 0) {
                    throw IllegalStateException("No active seed range.")
                }

                numRanges--;
                if (numRanges > 0) {
                    continue
                }

                // Add some range to the list
                val mappedRange = LongRange(
                    seedRangeStart!! + (activeMapDelta ?: 0),
                    transition.position + (activeMapDelta ?: 0))
                nextSeedRanges.add(mappedRange)

                seedRangeStart = null
            }

            if (transition.seedMapRangeStart != null) {
                if (activeMapDelta != null) {
                    throw IllegalStateException("Already have an active map.")
                }

                if (seedRangeStart != null && transition.position != seedRangeStart) {
                    // Map the previous range, start a new range
                    val mappedRange = LongRange(
                        seedRangeStart,
                        transition.position - 1)
                    nextSeedRanges.add(mappedRange)
                    seedRangeStart = transition.position
                }
                activeMapDelta = transition.seedMapRangeStart.destDelta
            }
            if (transition.seedMapRangeEnd != null) {
                if (activeMapDelta == null) {
                    throw IllegalStateException("No active map.")
                }

                if (seedRangeStart != null) {
                    // Map the previous range, start a new range
                    val mappedRange = LongRange(
                        seedRangeStart + activeMapDelta,
                        transition.position + activeMapDelta)
                    nextSeedRanges.add(mappedRange)
                    seedRangeStart = transition.position + 1
                }

                activeMapDelta = null
            }
        }
        if (numRanges > 0 || seedRangeStart != null) {
            throw IllegalStateException("Unmatched seed range.")
        }
        if (activeMapDelta != null) {
            throw IllegalStateException("Unmatched seed map.")
        }
        currentSeedRanges = nextSeedRanges;
    }
    val part2 = currentSeedRanges.minOf { it.first }
    println("Part 2: $part2")

    // Too low: 107701658
    // Too low also: 130970867
}