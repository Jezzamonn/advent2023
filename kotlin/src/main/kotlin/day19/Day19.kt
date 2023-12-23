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

data class Condition(val property: String, val inequality: Inequality, val compareAgainst: Int, val dest: String) {

    fun matches(part: Map<String, Int>): Boolean = when (inequality) {
        Inequality.LESS_THAN -> part[property]!! < compareAgainst
        Inequality.GREATER_THAN -> part[property]!! > compareAgainst
        Inequality.ANYTHING -> true
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
}