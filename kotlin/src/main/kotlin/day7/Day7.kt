package day7

import java.io.File

val cardOrder = "AKQJT98765432".reversed().withIndex().associate { (index, value) -> value to index }
val jokerCardOrder = "AKQT98765432J".reversed().withIndex().associate { (index, value) -> value to index }

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
    val jokerHandType: HandType =
        hand.filterNot { it == 'J' }.groupingBy { it }.eachCount().values.sorted().let {
            when (it) {
                // Non joker hands
                listOf(1, 1, 1, 1, 1) -> HandType.HIGH_CARD
                listOf(1, 1, 1, 2) -> HandType.ONE_PAIR
                listOf(1, 2, 2) -> HandType.TWO_PAIR
                listOf(1, 1, 3) -> HandType.THREE_OF_A_KIND
                listOf(2, 3) -> HandType.FULL_HOUSE
                listOf(1, 4) -> HandType.FOUR_OF_A_KIND
                listOf(5) -> HandType.FIVE_OF_A_KIND
                // With 1 joker
                listOf(1, 1, 1, 1) -> HandType.ONE_PAIR
                listOf(1, 1, 2) -> HandType.THREE_OF_A_KIND
                listOf(2, 2) -> HandType.FULL_HOUSE
                listOf(1, 3) -> HandType.FOUR_OF_A_KIND
                listOf(4) -> HandType.FIVE_OF_A_KIND
                // With 2 jokers
                listOf(1, 1, 1) -> HandType.THREE_OF_A_KIND
                listOf(1, 2) -> HandType.FOUR_OF_A_KIND
                listOf(3) -> HandType.FIVE_OF_A_KIND
                // With 3 jokers
                listOf(1, 1) -> HandType.FOUR_OF_A_KIND
                listOf(2) -> HandType.FIVE_OF_A_KIND
                // With 4 jokers
                listOf(1) -> HandType.FIVE_OF_A_KIND
                // With 5 jokers
                listOf<Int>() -> HandType.FIVE_OF_A_KIND
                else -> throw Exception("Unknown hand type: $it")
            }
        }

    val orderNumber = hand.map { cardOrder[it]!! }.reduce { acc, i -> acc * cardOrder.size + i }
    val jokerOrderNumber = hand.map { jokerCardOrder[it]!! }.reduce { acc, i -> acc * jokerCardOrder.size + i }

    // Parses from a string like "AKQ10 324"
    companion object {
        fun parse(input: String): Bid {
            val (hand, bid) = input.split(" ")
            return Bid(hand, bid.toInt())
        }
    }

    val jokerHandDetails get() = "hand: $hand, bid: $bid jokerHandType: $jokerHandType"
}

fun main(args: Array<String>) {
    val bids = File(args[0])
        .readLines()
        .filter { it.isNotEmpty() }
        .map { Bid.parse(it) }

    val sortedByPart1Rules = bids.sortedWith(compareBy( { it.handType }, { it.orderNumber }))
    println(sortedByPart1Rules.joinToString("\n"))
    val totalWinnings = sortedByPart1Rules.withIndex().sumOf { (index, bid) -> bid.bid * (index + 1) }
    println("Part 1: $totalWinnings")

    val sortedByPart2Rules = bids.sortedWith(compareBy( { it.jokerHandType }, { it.jokerOrderNumber }))
    println(sortedByPart2Rules.map { it.jokerHandDetails }.joinToString("\n"))
    val totalWinnings2 = sortedByPart2Rules.withIndex().sumOf { (index, bid) -> bid.bid * (index + 1) }
    println("Part 2: $totalWinnings2")

}