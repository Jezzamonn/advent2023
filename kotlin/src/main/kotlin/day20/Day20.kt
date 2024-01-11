package day20

import shared.getInputFile
import java.io.File

enum class Pulse {
    LOW, HIGH
}

data class PulseDestination(val source: String, val dest: String, val pulse: Pulse) {
    override fun toString(): String {
        return "$source -${pulse.name.lowercase()}-> $dest"
    }
}

abstract class Module(val name: String, val outputs: List<String>) {
    // Set when building the modules
    var numInputs: Int = 0

    abstract fun handlePulse(source: String, pulse: Pulse): List<PulseDestination>

    open val prefixAndName: String get() = name
}

class NullModule(name: String, outputs: List<String>) : Module(name, outputs) {

    override fun handlePulse(source: String, pulse: Pulse): List<PulseDestination> {
        return emptyList()
    }
}

class FlipFlopModule(name: String, outputs: List<String>) : Module(name, outputs) {

    var isOn = false

    override fun handlePulse(source: String, pulse: Pulse): List<PulseDestination> {
        if (pulse == Pulse.LOW) {
            isOn = !isOn
            val newPulse = if (isOn) Pulse.HIGH else Pulse.LOW
            return outputs.map { PulseDestination(name, it, newPulse) }
        }
        return emptyList()
    }

    override val prefixAndName get() = "%$name"
}

class ConjunctionModule(name: String, outputs: List<String>) : Module(name, outputs) {

    private val rememberedPulses = mutableMapOf<String, Pulse>()

    override fun handlePulse(source: String, pulse: Pulse): List<PulseDestination> {
        rememberedPulses[source] = pulse
        val numHigh = rememberedPulses.values.count { it == Pulse.HIGH }
        val newPulse = if (numHigh == numInputs) Pulse.LOW else Pulse.HIGH
        return outputs.map { PulseDestination(name, it, newPulse) }
    }

    override val prefixAndName: String get() = "&$name"
}

class BroadcastModule(name: String, outputs: List<String>) : Module(name, outputs) {

    override fun handlePulse(source: String, pulse: Pulse): List<PulseDestination> {
        return outputs.map { PulseDestination(name, it, pulse) }
    }
}

val modulePattern = Regex("""(.+) -> (.+)""")

data class Chain(val conjunctionModule: ConjunctionModule, val flipFlopModules: List<FlipFlopModule>)

fun main() {
    val file = getInputFile(20)
    val input = file.readText().trim()

    // writeDotFile(file)

//    solvePart1(input)

    solvePart2(input)
}

fun writeDotFile(file: File) {
    val input = file.readText().trim()
    // Helper: Write a dot file to visualize the modules
    val dotContents = inputToDotFile(input)
    println(dotContents)
    // If the input file is demo1.txt, write to demo1.dot
    val dotFileName = file.nameWithoutExtension + ".dot"
    val dotFile = file.resolveSibling(dotFileName)
    println("Writing to $dotFile")
    dotFile.writeText(dotContents)
}

fun solvePart1(input: String) {
    val modules = buildModules(input)
    // Simulate 1000 button presses
    val numPulses = mutableMapOf(Pulse.LOW to 0L, Pulse.HIGH to 0L)
    for (i in 1..1000) {
        val toSend = ArrayDeque(listOf(PulseDestination("button", "broadcaster", Pulse.LOW)))
        while (toSend.isNotEmpty()) {
            val next = toSend.removeFirst()
            numPulses[next.pulse] = numPulses[next.pulse]!! + 1

            val module = modules[next.dest]!!
            val newPulses = module.handlePulse(next.source, next.pulse)
            toSend.addAll(newPulses)
        }
    }
    val part1 = numPulses[Pulse.LOW]!! * numPulses[Pulse.HIGH]!!
    println("Part 1: $part1")
}

fun solvePart2(input: String) {
    val modules = buildModules(input)

    // To start with, lets draw a coloured graph of the modules.
    // To help with the layout, we find the chains of % modules first.
    val chainStarts = modules["broadcaster"]!!.outputs.map { modules[it]!! as FlipFlopModule }
    val chains = chainStarts.map { start ->
        val flipFlopModules = mutableListOf<FlipFlopModule>()
        var current: FlipFlopModule? = start
        while (current != null) {
            flipFlopModules.add(current)
            current = current.outputs.map { modules[it]!! }.firstOrNull { it is FlipFlopModule } as? FlipFlopModule
        }
        val conjunctionModule = modules.values.first { it.outputs.contains(start.name) && it is ConjunctionModule } as ConjunctionModule
        Chain(conjunctionModule, flipFlopModules)
    }

    // Clear the console
    print("\u001B[2J")

    // Simulate some pulses and animate each chain.
    // Just do an infinite loop for now.
    while (true) {
        val toSend = ArrayDeque(listOf(PulseDestination("button", "broadcaster", Pulse.LOW)))
        var hadConjunctionPulse = false
        while (toSend.isNotEmpty()) {
            val next = toSend.removeFirst()
            if (setOf("hd", "fl", "kc", "tb").contains(next.source) && next.pulse == Pulse.LOW) {
                hadConjunctionPulse = true
            }
            val module = modules[next.dest]!!
            val newPulses = module.handlePulse(next.source, next.pulse)
            toSend.addAll(newPulses)

//            Thread.sleep(10)
        }
        if (hadConjunctionPulse) {
            showChainsAnimationFrame(chains, modules)
            Thread.sleep(1000)
        }
    }
}

/** Move the cursor to the top of the console and draws the chains. */
fun showChainsAnimationFrame(chains: List<Chain>, modules: Map<String, Module>) {
    // Move to the top (don't clear)
    print("\u001B[0;0H")
    // Draw the chains
    println(drawChains(chains, modules))
}

/** Draws things to the console output. */
fun drawChains(chains: List<Chain>, modules: Map<String, Module>): String {
    // Chains will look like this:
    //
    //     |             |
    //     v             v
    // -> %nd -> %nd -> %nd -> %nd
    //            |             |
    //            v             v
    //
    //     |             |
    //     v             v
    // -> %nd -> %nd -> %nd -> %nd
    //            |             |
    //            v             v
    //
    // Where nodes have arrows coming from the top if they have an input from the & node,
    // and arrows on the bottom if they have an output from the & node.

    // Ansi colors codes
    val grey = "\u001B[90m"
    val normal = "\u001B[0m"

    return chains.joinToString("\n\n") { chain ->
        // Everything is grey by default.
        val inputsLine1 = StringBuilder(grey)
        val inputsLine2 = StringBuilder(grey)
        val nodesLine = StringBuilder(grey)
        val outputsLine1 = StringBuilder(grey)
        val outputsLine2 = StringBuilder(grey)
        for (node in chain.flipFlopModules) {
            val hasInputFromConjunctionModule = chain.conjunctionModule.outputs.contains(node.name)
            val hasOutputToConjunctionModule = node.outputs.contains(chain.conjunctionModule.name)

            if (hasInputFromConjunctionModule) {
                inputsLine1.append("     | ")
                inputsLine2.append("     v ")
            } else {
                inputsLine1.append("       ")
                inputsLine2.append("       ")
            }
            // If the module is off, draw it in grey using the ansi escape code for grey.
            val color = if (node.isOn) normal else grey
            nodesLine.append(" -> $color%${node.name}$grey")

            if (hasOutputToConjunctionModule) {
                outputsLine1.append("     | ")
                outputsLine2.append("     v ")
            } else {
                outputsLine1.append("       ")
                outputsLine2.append("       ")
            }
        }
        // Return all the lines as a single string
        listOf(inputsLine1, inputsLine2, nodesLine, outputsLine1, outputsLine2).joinToString("\n")
    }
}

fun inputToDotFile(input: String): String {
    val modules = buildModules(input)
    val dotFile = StringBuilder()
    dotFile.appendLine("digraph {")

    for (module in modules.values) {
        for (output in module.outputs) {
            val outputModule = modules[output]!!
            dotFile.appendLine("    \"n_${module.prefixAndName}\" -> \"n_${outputModule.prefixAndName}\"")
        }
    }

    dotFile.appendLine("}")
    return dotFile.toString()
}

fun buildModules(input: String): Map<String, Module> {
    val modules = input.lines().associate { line ->
        val (prefixAndName, outputsStr) = modulePattern.matchEntire(line)!!.destructured
        val outputs = outputsStr.split(", ")
        val module = when (prefixAndName[0]) {
            '%' -> FlipFlopModule(prefixAndName.drop(1), outputs)
            '&' -> ConjunctionModule(prefixAndName.drop(1), outputs)
            else -> BroadcastModule(prefixAndName, outputs)
        }
        module.name to module
    }.toMutableMap()

    // Add the input counts to each module
    for (module in modules.values.toList()) {
        for (output in module.outputs) {
            if (output !in modules) {
                println("Adding null module for $output")
                modules[output] = NullModule(output, emptyList())
            }
            modules[output]!!.numInputs++
        }
    }
    return modules
}
