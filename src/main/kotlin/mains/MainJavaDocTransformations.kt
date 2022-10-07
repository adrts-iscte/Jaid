package mains

import com.github.javaparser.StaticJavaParser
import model.FactoryOfTransformations
import java.io.File

fun main() {
//    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/javaDocTransformations/base/JavaDocTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/javaDocTransformations/left/JavaDocTransformationsLeftClass.java"))

    val factoryOfTransformations = FactoryOfTransformations(base, left)
    println(factoryOfTransformations)
    val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(base)
}