package day6

import java.io.File
import kotlin.math.*;

fun main(args: Array<String>) {
    val input = File(args[0]).readText().trim()
    val lines = input.lines()
    val times = lines[0].split("\\s+".toRegex()).drop(1).filter { it.isNotEmpty() }.map { it.toLong() }
    val distances = lines[1].split("\\s+".toRegex()).drop(1).filter { it.isNotEmpty() }.map { it.toLong() }
    val part1 = times.zip(distances).map {
        (time, distance) ->
        // Our equation is y = x * (t - x). Find x where y > d.
        // x * (t - x) > d
        // -x^2 + t * x > d
        // -x^2 + t * x - d > 0
        // x^2 - t * x + d < 0
        // Put into quadratic formula:
        // x = (t +- sqrt(t^2 - 4 * d)) / 2
        val delta = sqrt((time * time - 4 * distance).toDouble())
        val minX = (time - delta) / 2
        val maxX = (time + delta) / 2
        val numSolutions = ceil(maxX).toLong() - floor(minX).toLong() - 1
        println("Time: $time, Distance: $distance, minX: $minX, maxX: $maxX, numSolutions: $numSolutions")
        numSolutions
    }.reduce(Long::times)
    println("Part 1: $part1")

    // Part 2
    val time = lines[0].split("\\s+".toRegex()).drop(1).joinToString("").toLong()
    val distance = lines[1].split("\\s+".toRegex()).drop(1).joinToString("").toLong()
    val delta = sqrt((time * time - 4 * distance).toDouble())
    val minX = (time - delta) / 2
    val maxX = (time + delta) / 2
    val numSolutions = ceil(maxX).toLong() - floor(minX).toLong() - 1
    println("Time: $time, Distance: $distance, minX: $minX, maxX: $maxX, numSolutions: $numSolutions")

}