package day25

import shared.getInputFile

// Graph
data class Node(val name: String, val edges: MutableList<String> = mutableListOf())

fun main() {
    // Read input, parse lines like "cmg: qnr nvd lhk bvb" into a list of nodes.
    val nodeMap = mutableMapOf<String, Node>()
    getInputFile(25).useLines { lines ->
        lines
            .filter { it.isNotEmpty() }
            .forEach { line ->
                val (name, edges) = line.split(":").map { it.trim() }
                val node = nodeMap.getOrPut(name) { Node(name) }
                edges.split(" ").forEach { edge ->
                    val otherNode = nodeMap.getOrPut(edge) { Node(edge) }
                    node.edges.add(edge)
                    otherNode.edges.add(name)
                }
            }
    }

    // Find the three edges to remove in order to split the graph into two groups.
    // Not sure the best way, but what I'm going to try is to traverse the graph and create a tree.
    // Then check if by removing all those edges, the graph is split into two groups.

}