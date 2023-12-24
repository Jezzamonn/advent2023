package day20

import shared.getInputFile

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

    private var isOn = false

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

fun main() {
    val file = getInputFile(20)
    val input = file.readText().trim()

    // Helper: Write a dot file to visualize the modules
    val dotContents = inputToDotFile(input)
    println(dotContents)
//    // If the input file is demo1.txt, write to demo1.dot
//    val dotFile = file.nameWithoutExtension + ".dot"
//    file.resolveSibling(dotFile).writeText(dotContents)

    // Part 1:
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

    // Part 2:
//    val modules2 = buildModules(input)
//    for (i in generateSequence(1) { it + 1 }) {
//        val sentToRx = mutableMapOf(Pulse.LOW to 0L, Pulse.HIGH to 0L)
//        val toSend = ArrayDeque(listOf(PulseDestination("button", "broadcaster", Pulse.LOW)))
//        while (toSend.isNotEmpty()) {
//            val next = toSend.removeFirst()
//            if (next.dest == "rx" && next.pulse == Pulse.LOW) {
//                sentToRx[next.pulse] = sentToRx[next.pulse]!! + 1
//            }
//
//            val module = modules2[next.dest]!!
//            val newPulses = module.handlePulse(next.source, next.pulse)
//            toSend.addAll(newPulses)
//        }
//
//        if (sentToRx == mapOf(Pulse.LOW to 1L, Pulse.HIGH to 0L)) {
//            println("Part 2: $i")
//            break
//        }
//    }
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
