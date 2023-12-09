package shared

import java.io.File

fun getInputFile(day: Int): File {
    // First request the filename from the command line,
    // Then search recursive for a folder named "dayX" in the current directory,
    // and then the filename in that folder.
    val filename = readln()
    val folder = File(".").walk().find { it.name == "day$day" } ?: throw Exception("Could not find folder day$day")
    return folder.listFiles()?.find { it.name == filename }
        ?: throw Exception("Could not find file $filename in folder day$day")
}