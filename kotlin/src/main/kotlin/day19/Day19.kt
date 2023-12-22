package day19

import shared.getInputFile


enum class Inequality {
    LESS_THAN, GREATER_THAN, ANYTHING
}

data class Condition(val property: String, val inequality: Inequality, val compareAgainst: Int, val dest: String) {
    companion object {
    }
}
data class Rule(val name: String, val conditions: List<Condition>)

fun main() {
    val input = getInputFile(19).readText()
    val (rulesStr, partsStr) = input.split("\n\n")

}