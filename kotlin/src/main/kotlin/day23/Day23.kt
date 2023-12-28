package day23

import shared.getInputFile

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

val deltas = listOf(
    Point(-1, 0),
    Point(1, 0),
    Point(0, -1),
    Point(0, 1),
)

data class Grid(private val grid: List<List<Char>>) {
    fun get(point: Point): Char {
        return grid[point.y][point.x]
    }

    fun contains(point: Point): Boolean {
        return point.x >= 0 &&
                point.y >= 0 &&
                point.x < grid[0].size &&
                point.y < grid.size
    }

    fun deltasFromPosition(point: Point): List<Point> {
        // Part 1:
//        return when (get(point)) {
//            '.' -> deltas
//            '>' -> listOf(Point(1, 0))
//            '<' -> listOf(Point(-1, 0))
//            '^' -> listOf(Point(0, -1))
//            'v' -> listOf(Point(0, 1))
//            else -> throw RuntimeException("Invalid character: ${get(point)}")
//        }
        // Part 2:
        return deltas
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
    val chars = getInputFile(23)
        .readLines()
        .filter { it.isNotEmpty() }
        .map { line -> line.map { it }}
    val grid = Grid(chars)
    // Use branch-and-bound to find the longest path to the end.
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