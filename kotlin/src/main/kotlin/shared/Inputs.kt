package shared

import java.io.File

fun getInputFile(day: Int): File {
    // First request the filename from the command line,
    // Then search recursive for a folder named "dayX" in the current directory,
    // and then the filename in that folder.
    val filename = readln().ifEmpty { "input" }
    val folder = File(".").walk()
        // Exclude the out folder
        .find { it.name == "day$day" && !it.path.contains("out")} ?: throw Exception("Could not find folder day$day")
    return folder.listFiles()?.find { it.name.contains(filename) && it.name.endsWith(".txt") }
        ?: throw Exception("Could not find file $filename in folder day$day")
}