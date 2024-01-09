package day25

import shared.getInputFile

class Graph(pairs: List<Pair<String, String>>) {

    // Weight = current flow through edge. 0 = no flow, 1 = flow from a to b.
    val edgeWeights = pairs.flatMap { listOf(it, it.second to it.first) }.associateWith { 0 }.toMutableMap()
    val allNodes = pairs.flatMap { listOf(it.first, it.second) }.toSortedSet()

    fun getEdgeWeight(node1: String, node2: String): Int? {
        return edgeWeights[node1 to node2]
    }

    fun getNeighbors(node: String): List<String> {
        return allNodes.filter { it != node && getEdgeWeight(node, it) != null }
    }

    fun setEdgeWeight(node1: String, node2: String, weight: Int) {
        edgeWeights[node1 to node2] = weight
        edgeWeights[node2 to node1] = -weight
    }

    fun clearFlow() {
        for ((node1, node2) in edgeWeights.keys) {
            edgeWeights[node1 to node2] = 0
        }
    }

    init {
        for ((node1, node2) in pairs) {
            setEdgeWeight(node1, node2, 0)
        }
    }

}

fun main() {
    // Read input, parse lines like "cmg: qnr nvd lhk bvb" into a list of node pairs
    val pairs = getInputFile(25).readLines()
        .filter { it.isNotEmpty() }
        .flatMap { line ->
            val (node, edges) = line.split(":").map { it.trim() }
            edges.split(" ").map { edge ->
                node to edge
            }
        }

    val allNodes = pairs.flatMap { (node1, node2) -> listOf(node1, node2) }.toSortedSet()

    val graph = Graph(pairs)

    // Pick two random nodes, find maximum flow. If 3, then we have two sections of the min cut.

    var start: String
    var end: String
    while (true) {
        start = allNodes.random()
        end = allNodes.subtract(setOf(start)).random()
        println("Start: $start, end: $end")

        val maxFlow = findMaxFlow(graph, start, end)
        println("Max flow: $maxFlow")
        println()

        if (maxFlow == 3) {
            break
        }
        graph.clearFlow()
    }

    val reachable = reachableNodes(graph, start)
    println("Nodes in first section: ${reachable.size}")
    println("Answer for part 2: ${reachable.size * (allNodes.size - reachable.size)}")
}

fun findMaxFlow(graph: Graph, start: String, end: String): Int {
    // Find a path from start to end, add flow to all edges in the path.
    // Repeat until no path found.
    var maxFlow = 0
    while (true) {
        val path = findPath(graph, start, end) ?: break
        // Update the flow in the graph.
        for (i in 0..<path.size - 1) {
            val node1 = path[i]
            val node2 = path[i + 1]
            val weight = graph.getEdgeWeight(node1, node2)!!
            graph.setEdgeWeight(node1, node2, weight + 1)
        }
        maxFlow++
    }
    return maxFlow
}

fun findPath(graph: Graph, start: String, end: String): List<String>? {
    // Find a path from start to end, using BFS.
    val queue = ArrayDeque<List<String>>()
    queue.add(listOf(start))
    val visited = mutableSetOf<String>()
    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val node = path.last()
        if (node == end) {
            return path
        }
        if (node in visited) {
            continue
        }
        visited.add(node)

        val possibleNeighbors = graph
            .getNeighbors(node)
            // Maximum flow is 1, so filter out neighbors that are already at the maximum flow.
            .filter { graph.getEdgeWeight(node, it)!! < 1 }

        for (nextNode in possibleNeighbors) {
            if (nextNode in visited) {
                continue
            }
            queue.add(path + nextNode)
        }
    }
    return null
}

fun reachableNodes(graph: Graph, start: String): Set<String> {
    val queue = ArrayDeque<String>()
    queue.add(start)
    val visited = mutableSetOf<String>()
    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        if (node in visited) {
            continue
        }
        visited.add(node)

        val possibleNeighbors = graph
            .getNeighbors(node)
            // Maximum flow is 1, so filter out neighbors that are already at the maximum flow.
            .filter { graph.getEdgeWeight(node, it)!! < 1 }

        for (nextNode in possibleNeighbors) {
            if (nextNode in visited) {
                continue
            }
            queue.add(nextNode)
        }
    }
    return visited
}