package day22

import shared.getInputFile

data class Point3D(val x: Int, val y: Int, val z: Int) {

    operator fun plus(other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }

    companion object {
        fun fromString(s: String): Point3D {
            val (x, y, z) = s.split(",").map { it.toInt() }
            return Point3D(x, y, z)
        }
    }
}

data class Brick(val name: String, val min: Point3D, val max: Point3D) {

    operator fun plus(other: Point3D): Brick {
        return Brick(name, min + other, max + other)
    }

    val allPoints: List<Point3D>
        get() {
            return (min.x..max.x).flatMap { x ->
                (min.y..max.y).flatMap { y ->
                    (min.z..max.z).map { z ->
                        Point3D(x, y, z)
                    }
                }
            }
        }

    companion object {
        fun fromString(name: String, line: String): Brick {
            val (point1, point2) = line.split("~").map { Point3D.fromString(it) }
            val minPoint = Point3D(
                minOf(point1.x, point2.x),
                minOf(point1.y, point2.y),
                minOf(point1.z, point2.z)
            )
            val maxPoint = Point3D(
                maxOf(point1.x, point2.x),
                maxOf(point1.y, point2.y),
                maxOf(point1.z, point2.z)
            )
            return Brick(name, minPoint, maxPoint)
        }
    }
}

class Grid3D(val maxX: Int, val maxY: Int, val maxZ: Int) {
    private val grid: Array<Array<Array<Brick?>>> = Array(maxZ + 1) {
        Array(maxY + 1) {
            Array(maxX + 1) {
                null
            }
        }
    }

    fun insert(brick: Brick) {
        brick.allPoints.forEach { point ->
            grid[point.z][point.y][point.x] = brick
        }
    }

    fun remove(brick: Brick) {
        brick.allPoints.forEach { point ->
            grid[point.z][point.y][point.x] = null
        }
    }

    fun canInsert(brick: Brick): Boolean {
        if (brick.min.x < 0 || brick.min.y < 0 || brick.min.z < 0) {
            return false
        }
        return brick.allPoints.all { grid[it.z][it.y][it.x] == null }
    }

    fun copy(): Grid3D {
        val newGrid = Grid3D(maxX, maxY, maxZ)
        for (z in 0..maxZ) {
            for (y in 0..maxY) {
                for (x in 0..maxX) {
                    newGrid.grid[z][y][x] = grid[z][y][x]
                }
            }
        }
        return newGrid
    }
}

fun main() {
    val bricks = getInputFile(22)
        .readLines()
        .filter { it.isNotEmpty() }
        .mapIndexed { index, it ->
            Brick.fromString(('A' + index).toString(), it)
        }
    val maxX = bricks.maxOf { it.max.x }
    val maxY = bricks.maxOf { it.max.y }
    val maxZ = bricks.maxOf { it.max.z }

    val grid = Grid3D(maxX, maxY, maxZ)

    // Insert the bricks
    bricks.forEach { grid.insert(it) }

    val droppedBricks = makeBricksFall(bricks, grid).newBricks

    // Part 1:
    // See how many bricks can be removed without making others fall.
    var numRemovableBricks = 0
    for (brick in droppedBricks) {
        grid.remove(brick)
        val withoutBrick = droppedBricks.filter { it != brick }
        if (!canAnyBricksGoLower(withoutBrick, grid)) {
            numRemovableBricks++
        }
        grid.insert(brick)
    }

    println("Part 1: $numRemovableBricks")

    // Part 2: Figure out how many bricks fall for each brick.
    var totalFell = 0
    for (brick in droppedBricks) {
        println("Removing brick ${brick.name}:")
        val gridCopy = grid.copy()
        gridCopy.remove(brick)
        val withoutBrick = droppedBricks.filter { it != brick }
        val fallResult = makeBricksFall(withoutBrick, gridCopy)
        println("- Total fell: ${fallResult.numberOfFallenBricks}")
        totalFell += fallResult.numberOfFallenBricks
    }
    println("Part 2: $totalFell")
}

data class FallResult(val numberOfFallenBricks: Int, val newBricks: List<Brick>)

fun makeBricksFall(bricks: List<Brick>, grid: Grid3D): FallResult {
    // Drop bricks downwards.
    var numberOfFallenBricks = 0
    val newBricks: MutableList<Brick> = mutableListOf()
    for (brick in bricks.sortedBy { it.min.z }) {
        grid.remove(brick)
        var lowestBrickPosition = brick
        var brickFell = false
        for (dz in 1..brick.min.z) {
            val newBrick = brick + Point3D(0, 0, -dz)
            if (grid.canInsert(newBrick)) {
                lowestBrickPosition = newBrick
                brickFell = true
            } else {
                break
            }
        }
        if (brickFell) {
            numberOfFallenBricks++
            println("- Brick ${brick.name} fell to $lowestBrickPosition")
        }
        grid.insert(lowestBrickPosition)
        newBricks.add(lowestBrickPosition)
    }
    return FallResult(numberOfFallenBricks, newBricks)
}

fun canAnyBricksGoLower(bricks: List<Brick>, grid: Grid3D): Boolean {
    // Make sure all the bricks can't go lower
    for (brink in bricks) {
        grid.remove(brink)
        val next = brink + Point3D(0, 0, -1)
        if (grid.canInsert(next)) {
            return true
        }
        grid.insert(brink)
    }
    return false
}