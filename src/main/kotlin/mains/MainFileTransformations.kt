package mains

import com.github.javaparser.StaticJavaParser
import model.FactoryOfTransformations
import java.io.File
/*
fun main() {
//    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fileTransformations/base/FileTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fileTransformations/left/FileTransformationsLeftClass.java"))

    val factoryOfTransformations = FactoryOfTransformations(base, left)
    println(factoryOfTransformations)
    val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(base)

}*/