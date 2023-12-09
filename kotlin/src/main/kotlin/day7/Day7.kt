package day7

import java.io.File

val cardOrder = "AKQJT98765432".reversed().withIndex().associate { (index, value) -> value to index }

enum class HandType {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND,
}

data class Bid(val hand: String, val bid: Int) {
    val handType: HandType =
        hand.groupingBy { it }.eachCount().values.sorted().let {
            when (it) {
                listOf(1, 1, 1, 1, 1) -> HandType.HIGH_CARD
                listOf(1, 1, 1, 2) -> HandType.ONE_PAIR
                listOf(1, 2, 2) -> HandType.TWO_PAIR
                listOf(1, 1, 3) -> HandType.THREE_OF_A_KIND
                listOf(2, 3) -> HandType.FULL_HOUSE
                listOf(1, 4) -> HandType.FOUR_OF_A_KIND
                listOf(5) -> HandType.FIVE_OF_A_KIND
                else -> throw Exception("Unknown hand type: $it")
            }
        }

    val orderNumber = hand.map { cardOrder[it]!! }.reduce { acc, i -> acc * cardOrder.size + i }

    // Parses from a string like "AKQ10 324"
    companion object {
        fun parse(input: String): Bid {
            val (hand, bid) = input.split(" ")
            return Bid(hand, bid.toInt())
        }
    }
}

fun main(args: Array<String>) {
    val bids = File(args[0])
        .readLines()
        .filter { it.isNotEmpty() }
        .map { Bid.parse(it) }
        .sortedWith(compareBy( { it.handType }, { it.orderNumber }))
    println(bids.joinToString("\n"))

    val totalWinnings = bids.withIndex().sumOf { (index, bid) -> bid.bid * (index + 1) }
    println("Part 1: $totalWinnings")

}