package day25

import shared.getInputFile

// Graph
data class Node(val name: String, val edges: MutableList<String> = mutableListOf()) {

    fun deepCopy(): Node {
        return Node(name, edges.toMutableList())
    }

}

data class Graph(val nodes: MutableMap<String, Node> = mutableMapOf()) {

    fun deepCopy(): Graph {
        val newGraph = Graph()
        nodes.forEach { (name, node) ->
            newGraph.nodes[name] = node.deepCopy()
        }
        return newGraph
    }

    fun getNumberOfDisconnectedGroups(): Int {
        val visited = mutableSetOf<String>()
        var numDisconnected = 0
        while (visited.size < nodes.size) {
            val start = nodes.values.first { it.name !in visited }
            val toVisit = mutableListOf(start)
            while (toVisit.isNotEmpty()) {
                val visiting = toVisit.removeLast()
                visited.add(visiting.name)
                visiting.edges.forEach { edge ->
                    if (edge !in visited) {
                        toVisit.add(nodes[edge]!!)
                    }
                }
            }
            numDisconnected++
        }
        return numDisconnected
    }

    fun removeEdges(other: Graph): Graph {
        val copy = this.deepCopy()
        for (node in copy.nodes.values) {
            node.edges.removeIf { other.nodes[it]?.edges?.contains(node.name) ?: false }
        }
        return copy
    }
}

fun main() {
    // Read input, parse lines like "cmg: qnr nvd lhk bvb" into a list of nodes.
    val graph = Graph()
    getInputFile(25).useLines { lines ->
        lines
            .filter { it.isNotEmpty() }
            .forEach { line ->
                val (name, edges) = line.split(":").map { it.trim() }
                val node = graph.nodes.getOrPut(name) { Node(name) }
                edges.split(" ").forEach { edge ->
                    val otherNode = graph.nodes.getOrPut(edge) { Node(edge) }
                    node.edges.add(edge)
                    otherNode.edges.add(name)
                }
            }
    }

    // Find the three edges to remove in order to split the graph into two groups.
    // Not sure the best way, but what I'm going to try is to traverse the graph and create a tree.
    // Then check if by removing all those edges, the graph is split into two groups.
    // If not, try again.

    println("Before removing tree, graph has ${graph.getNumberOfDisconnectedGroups()} groups")

    println("graph num nodes: ${graph.nodes.size}")
    val tree = makeTreeGraph(graph)
    println("tree num nodes: ${tree.nodes.size}")

    val graphWithoutTree = graph.removeEdges(tree)
    println("graphWithoutTree num nodes: ${graphWithoutTree.nodes.size}")
    println("After removing tree, graph has ${graphWithoutTree.getNumberOfDisconnectedGroups()} groups")
    // Again! ?
    val tree2 = makeTreeGraph(graphWithoutTree)
    println("tree2 num nodes: ${tree2.nodes.size}")
    val graphWithoutTree2 = graphWithoutTree.removeEdges(tree2)
    println("graphWithoutTree2 num nodes: ${graphWithoutTree2.nodes.size}")
    println("After removing tree2, graph has ${graphWithoutTree2.getNumberOfDisconnectedGroups()} groups")
    // Again! ?
    val tree3 = makeTreeGraph(graphWithoutTree2)
    val graphWithoutTree3 = graphWithoutTree2.removeEdges(tree3)
    println("After removing tree3, graph has ${graphWithoutTree3.getNumberOfDisconnectedGroups()} groups")
}

fun makeTreeGraph(graph: Graph): Graph {
    // Oh hey, creating a random tree is maze generation stuff. I'll just go with the easiest one.
    val treeGraph = Graph()
    val start = graph.nodes.values.first()
    val toVisit = mutableListOf(start.name)

    treeGraph.nodes[start.name] = Node(start.name)

    while (toVisit.isNotEmpty()) {
        // Remove a random node from the list.
        val r = toVisit.indices.random()
        val visiting = toVisit[r]
        val possibleEdges = graph.nodes[visiting]!!.edges.filter { it !in treeGraph.nodes }
        if (possibleEdges.isEmpty()) {
            toVisit.removeAt(r)
            continue
        }
        val edge = possibleEdges.random()
        treeGraph.nodes[edge] = Node(edge)
        // Connect the two nodes
        treeGraph.nodes[visiting]!!.edges.add(edge)
        treeGraph.nodes[edge]!!.edges.add(visiting)

        toVisit.add(edge)
    }
    return treeGraph
}