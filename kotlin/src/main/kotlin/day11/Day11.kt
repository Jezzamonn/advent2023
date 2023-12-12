package day11

import shared.getInputFile

fun main() {
    val input = getInputFile(11).readText().trim()
    val lines = input.lines()
    val numGalaxiesPerRow = lines.map { line -> line.count { it == '#' }.toLong() }
    val colLength = lines[0].length
    val numGalaxiesPerCol = (0..<colLength)
        .map { i -> lines.map { it[i] }.count { it == '#'}.toLong() }

    val part1 = countDistances(numGalaxiesPerRow) + countDistances(numGalaxiesPerCol)
    println(part1)

}

fun countDistances(numGalaxiesPerAxis: List<Long>): Long {
    // Keep track of the number of empty rows / columns encountered
    return numGalaxiesPerAxis.withIndex()
        // Map to the distance to the galaxies in the rows above
        .map { (i, numInI) ->
            // Distance to each galaxy in each row above
            numGalaxiesPerAxis.withIndex()
                .take(i)
                .sumOf { (j, numInJ) ->
                    // Count the number of empty rows between i and j
                    val numEmpty = numGalaxiesPerAxis.subList(j + 1, i).count { it == 0L }
                    numInI * numInJ * (i - j + 999_999 * numEmpty)
                }
        }
        .sum()
    // too high: 406726138763
}