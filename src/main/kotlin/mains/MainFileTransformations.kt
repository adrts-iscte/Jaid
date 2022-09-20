package mains

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import model.getListOfTransformationsOfFile
import java.io.File

fun main() {
    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fileTransformations/base/FileTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fileTransformations/left/FileTransformationsLeftClass.java"))

    val listOfTransformations = getListOfTransformationsOfFile(base, left)
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(base)

//    val listOfTransformations = model.getListOfTransformations(leftClass, baseClass)
//    listOfTransformations.forEach { it.applyTransformation(left) }
//    println(leftClass)
}