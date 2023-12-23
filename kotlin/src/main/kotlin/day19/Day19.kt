package day19

import shared.getInputFile


enum class Inequality {
    LESS_THAN, GREATER_THAN, ANYTHING
}

// Parse a string like "a<2006:qkq"
val conditionPattern = Regex("""(\w)([<>]?)(\d+):(\w+)""")
val destOnlyPattern = Regex("""(\w+)""")
// A line like px{a<2006:qkq,m>2090:A,rfg}
val rulePattern = Regex("""(\w+)\{(.+)\}""")

val partSectionPattern = Regex("""(\w)=(\d+)""")

typealias RuleRange = Map<String, IntRange>

data class RuleRangeAndDest(val ruleRange: RuleRange, val dest: String)

data class SplitResult(val mapped: RuleRangeAndDest, val leftOver: RuleRange)

data class Condition(val property: String, val inequality: Inequality, val compareAgainst: Int, val dest: String) {

    fun matches(part: Map<String, Int>): Boolean = when (inequality) {
        Inequality.LESS_THAN -> part[property]!! < compareAgainst
        Inequality.GREATER_THAN -> part[property]!! > compareAgainst
        Inequality.ANYTHING -> true
    }

    fun splitRuleRange(ruleRange: RuleRange): SplitResult {
        if (inequality == Inequality.ANYTHING) {
            return SplitResult(RuleRangeAndDest(ruleRange, dest), emptyMap())
        }
        val range = ruleRange[property]!!
        val (newRange, otherRange) = when (inequality) {
            Inequality.LESS_THAN -> {
                val newRange = range.first..(compareAgainst - 1)
                val otherRange = compareAgainst..range.last
                newRange to otherRange
            }
            Inequality.GREATER_THAN -> {
                val newRange = (compareAgainst + 1)..range.last
                val otherRange = range.first..compareAgainst
                newRange to otherRange
            }
            else -> throw IllegalStateException("Shouldn't be here")
        }
        val newRuleRange = ruleRange.toMutableMap()
        newRuleRange[property] = newRange
        val otherRuleRange = ruleRange.toMutableMap()
        otherRuleRange[property] = otherRange
        return SplitResult(RuleRangeAndDest(newRuleRange, dest), otherRuleRange)
    }

    companion object {
        fun fromString(str: String): Condition {
            if (destOnlyPattern.matches(str)) {
                return Condition("", Inequality.ANYTHING, 0, str)
            }
            val match = conditionPattern.matchEntire(str) ?: throw IllegalArgumentException("Invalid condition: $str")
            val (property, inequalityStr, compareAgainstStr, dest) = match.destructured
            val inequality = when (inequalityStr) {
                "<" -> Inequality.LESS_THAN
                ">" -> Inequality.GREATER_THAN
                else -> throw IllegalArgumentException("Invalid inequality: $inequalityStr")
            }
            val compareAgainst = compareAgainstStr.toInt()
            return Condition(property, inequality, compareAgainst, dest)
        }
    }
}
data class Rule(val name: String, val conditions: List<Condition>) {
    fun getNextRule(part: Map<String, Int>): String {
        return conditions.first { it.matches(part) }.dest
    }

    fun applyToRuleRange(ruleRange: RuleRange): List<RuleRangeAndDest> {
        val newRanges = mutableListOf<RuleRangeAndDest>()
        var leftOverRange = ruleRange
        for (condition in conditions) {
            val (newRange, otherRange) = condition.splitRuleRange(leftOverRange)
            newRanges.add(newRange)
            if (otherRange.isEmpty()) {
                break
            }
            leftOverRange = otherRange
        }
        return newRanges
    }
}

fun main() {
    val input = getInputFile(19).readText().trim()
    val (rulesStr, partsStr) = input.split("\n\n")
    val rules = rulesStr.lines().associate { line ->
        // Parse a line like "px{a<2006:qkq,m>2090:A,rfg}"
        val (name, conditionsStr) = rulePattern.matchEntire(line)!!.destructured
        val conditions = conditionsStr.split(",").map { Condition.fromString(it) }
        name to Rule(name, conditions)
    }
//    println(rules)
    val parts = partsStr.lines().map { line ->
        partSectionPattern.findAll(line).map { match ->
            val (name, value) = match.destructured
            name to value.toInt()
        }.toMap()
    }
//    println(parts)

    val part1 = parts.filter { part ->
        // Run the parts through the rules until it either lands at an accepted or rejected state.
        var ruleName = "in"
        while (ruleName != "A" && ruleName != "R") {
            ruleName = rules[ruleName]!!.getNextRule(part)
        }
        ruleName == "A"
    }.sumOf { part -> part.values.sum() }

    println("Part 1: $part1")

    // Part 2:
    val fullRange = mapOf(
        "x" to 1..4000,
        "m" to 1..4000,
        "a" to 1..4000,
        "s" to 1..4000,
    )
    val toBeSplit = mutableListOf(RuleRangeAndDest(fullRange, "in"))
    val accepted = mutableListOf<RuleRange>()
    while (toBeSplit.isNotEmpty()) {
        println("To be split: $toBeSplit")
        println("Accepted: $accepted")
        val (ruleRange, ruleName) = toBeSplit.removeAt(0)
        val rule = rules[ruleName]!!
        val newRanges = rule.applyToRuleRange(ruleRange)
        for (newRange in newRanges) {
            if (newRange.ruleRange.isEmpty()) {
                continue
            }
            when (newRange.dest) {
                "A" -> accepted.add(newRange.ruleRange)
                "R" -> {} // Drop rejected.
                else -> toBeSplit.add(newRange)
            }
        }
    }

    val part2 = accepted.sumOf {
        // Multiply the number of elements in each range.
        it.values.fold(1L) { acc, range -> acc * range.count() }
    }
    println("Part 2: $part2")
}