package evaluation

import java.io.File

fun main() {

    val part1 = File("src/main/resources/repositories/jsoup with MergeFiles - Part 1")
    val part2 = File("src/main/resources/repositories/jsoup with MergeFiles - Part 2")
    val withoutUUID = File("src/main/resources/repositories/jsoup without UUIDs")

    val filesPart1 = part1.listFiles()!!.map { it.name }.toSet()
    val filesPart2 = part2.listFiles()!!.map { it.name }.toSet()
    val filesWithoutUUID = withoutUUID.listFiles()!!.map { it.name }.toSet()

    val finalList = filesWithoutUUID.filter { !filesPart1.contains(it) && !filesPart2.contains(it)}
    println(finalList)
}