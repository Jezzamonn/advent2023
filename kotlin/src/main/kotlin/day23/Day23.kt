package day23

import shared.getInputFile
import java.io.File

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}

val deltas = listOf(
    Point(-1, 0),
    Point(1, 0),
    Point(0, -1),
    Point(0, 1),
)

data class Grid(private val grid: List<List<Char>>) {

    val width: Int get() = grid[0].size
    val height: Int get() = grid.size

    fun get(point: Point): Char {
        return grid[point.y][point.x]
    }

    fun contains(point: Point): Boolean {
        return point.x >= 0 &&
                point.y >= 0 &&
                point.x < width &&
                point.y < height
    }

    fun deltasFromPosition(point: Point): List<Point> {
        // Part 1:
        return when (get(point)) {
            '.' -> deltas
            '>' -> listOf(Point(1, 0))
            '<' -> listOf(Point(-1, 0))
            '^' -> listOf(Point(0, -1))
            'v' -> listOf(Point(0, 1))
            else -> throw RuntimeException("Invalid character: ${get(point)}")
        }
//        // Part 2: (but just using this is too slow)
//        return deltas
    }
}

// Visited includes the last position point.
data class SearchState(val lastPosition: Point, val visited: Set<Point>) {

    fun getChildStates(grid: Grid): List<SearchState> {
        return grid.deltasFromPosition(lastPosition)
            .map { lastPosition + it }
            .filter { point ->
                grid.contains(point) &&
                grid.get(point) != '#' &&
                point !in visited
            }.map { SearchState(it, visited + it) }
    }

    fun numReachableSpaces(grid: Grid): Int {
        val toVisit = ArrayDeque(listOf(lastPosition))
        val newVisited = visited.toMutableSet()
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            newVisited.add(current)
            grid
                .deltasFromPosition(current)
                .map { current + it }
                .filter {
                    grid.contains(it) &&
                            grid.get(it) != '#' &&
                            it !in newVisited
                }
                .forEach { toVisit.addLast(it) }
        }
        return newVisited.size
    }
}

fun main() {
    val file = getInputFile(23)

//    solvePart1(file)

    solvePart2(file)
}

fun solvePart1(file: File) {
    val chars = file
        .readLines()
        .filter { it.isNotEmpty() }
        .map { line -> line.map { it }}
    val grid = Grid(chars)

    val start = Point(chars.first().indexOfFirst { it == '.' }, 0)
    val end = Point(chars.last().indexOfFirst { it == '.' }, chars.size - 1)

    val startState = SearchState(start, setOf(start))

    val searchStates = ArrayDeque(listOf(startState))
    var longestPath = 0
    while (searchStates.isNotEmpty()) {
        // Going from the end does a depth-first search, that's more likely to reach the end earlier.
        val current = searchStates.removeLast()
        if (current.lastPosition == end) {
            if (current.visited.size > longestPath) {
                println("Found new longest path: ${current.visited.size}")
                longestPath = current.visited.size
            }
            continue
        }
        if (current.numReachableSpaces(grid) <= longestPath) {
            // This can't be a new optimal solution, don't try searching child states.
            continue
        }
        searchStates.addAll(current.getChildStates(grid))
    }

    // Subtract 1 because the start point is included in the path.
    println("Part 1: ${longestPath - 1}")
}

data class Node(val index: Int, val edges: MutableList<Edge> = mutableListOf())
// Edges are directionless
data class Edge(val node1Index: Int, val node2Index: Int, val cost: Int) {

    fun has(nodeIndex: Int): Boolean {
        return node1Index == nodeIndex || node2Index == nodeIndex
    }

    fun otherNode(nodeIndex: Int): Int {
        return if (nodeIndex == node1Index) {
            node2Index
        } else {
            node1Index
        }
    }

    override fun toString(): String {
        return "Edge(${node1Index} --$cost-- ${node2Index})"
    }
}

data class SearchState2(val lastNode: Int, val nodeBitMap: Long, val totalDist: Long) {

    fun getChildStates(nodes: List<Node>): List<SearchState2> {
        return nodes[lastNode].edges
            .filter {
                val newNode = it.otherNode(lastNode)
                ((1L shl newNode) and nodeBitMap) == 0L }
            .map {
                val newNode = it.otherNode(lastNode)
                SearchState2(
                    newNode,
                    1L shl newNode or nodeBitMap,
                    totalDist + it.cost)
            }
    }

//    fun longestPossiblePath(nodes: List<Node>): Long {
//        val nodesNotVisited = nodes.filter { it.index !in nodeIndices }
//        // Assuming we visit each unvisited node and we take the longest edge from that node.
//        // This will double count things but maybe is sufficient for the bound?
//        val sumOfLongestEdges = nodesNotVisited.sumOf { node ->
//            node.edges
//                .filter { it.otherNode(node.index) !in nodeIndices }
//                .maxOfOrNull { it.cost } ?: 0
//        }
//        return totalDist + sumOfLongestEdges
//    }

    override fun toString(): String {
        return "SearchState2(${nodeBitMap.toString(2)}, $totalDist)"
    }

}

fun solvePart2(file: File) {
    val chars = file
        .readLines()
        .filter { it.isNotEmpty() }
        .map { line -> line.map { it }}
    val grid = Grid(chars)

    // Build a graph representation of the grid, so we can search it quicker using less memory.
    // Starting with a node / edges representation, may need to change that if that's not fast enough.
    val nodesMap = mutableMapOf<Point, Node>()

    // First, find all the points we want to make nodes. This is any intersection, and the start and end points.
    var nextIndex = 0
    for (y in 0..<grid.height) {
        for (x in 0..<grid.width) {
            val point = Point(x, y)
            if (grid.get(point) == '#') {
                continue
            }
            val adjacentPositions = deltas
                .map { point + it }
                .filter { grid.contains(it) && grid.get(it) != '#' }
            if (adjacentPositions.size >= 3 || point.y == 0 || point.y == grid.height - 1) {
                val node = Node(nextIndex++)
                nodesMap[point] = node
            }
        }
    }

    // Now, find all the edges between the nodes.

    // Visited non-node positions.
    for ((nodePosition, node) in nodesMap) {
        println("Finding edges for node ${node.index}")
        val toVisit = mutableListOf(nodePosition to 0)
        val visited = mutableSetOf<Point>()
        while (toVisit.isNotEmpty()) {
            val (visitingPosition, distSoFar) = toVisit.removeLast()
            visited.add(visitingPosition)
            // We've found another node. Connect the two, don't add any more to the toVisit queue.
            if (visitingPosition in nodesMap && nodesMap[visitingPosition] != node) {
                val otherNode = nodesMap[visitingPosition]!!
                val edge = Edge(node.index, otherNode.index, distSoFar)

                println("Found edge: $edge")

                node.edges.add(edge)
                continue
            }

            val nextPositions = deltas
                .map { delta -> visitingPosition + delta }
                .filter {
                    grid.contains(it) &&
                            grid.get(it) != '#' &&
                            it !in visited
                }
            toVisit.addAll(nextPositions.map { it to distSoFar + 1 })
        }
    }

    val nodes = nodesMap.values.toList()
    println("Nodes:\n${nodes.joinToString("\n") }}")

    // Print an ascii diagram of the map showing the node positions
    val chars2 = chars.map { it.toMutableList() }
    for ((nodePosition, node) in nodesMap) {
        // The node index might be more than 1 character long,
        // in that case we want to just overwrite the next position in the line.
        val nodeIndexString = node.index.toString()
        for ((i, c) in nodeIndexString.withIndex()) {
            chars2[nodePosition.y][nodePosition.x + i] = c
        }
    }
    println("Map:")
    println(chars2.joinToString("\n") { it.joinToString("") })

    // Now do the search - hopefully the smaller search space will make this faster.

    val startNodeIndex = 0
    val endNodeIndex = nodes.size - 1

    val startState = SearchState2(startNodeIndex, 1 shl 0, 0)
    val searchStates = ArrayDeque(listOf(startState))
    var longestPath = 0L
    while (searchStates.isNotEmpty()) {
        // Going from the end does a depth-first search, that's more likely to reach the end earlier.
        val current = searchStates.removeLast()
//        println(current)
        if (current.lastNode == endNodeIndex) {
            if (current.totalDist > longestPath) {
                println("Found new longest path: $current")
                longestPath = current.totalDist
            }
            continue
        }
//        if (current.longestPossiblePath(nodes) <= longestPath) {
//            // This can't be a new optimal solution, don't try searching child states.
//            continue
//        }
        searchStates.addAll(current.getChildStates(nodes))
    }
}
