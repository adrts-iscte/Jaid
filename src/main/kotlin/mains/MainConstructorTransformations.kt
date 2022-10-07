package mains

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.FactoryOfTransformations
import java.io.File

fun main() {
//    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java"))

    val factoryOfTransformations = FactoryOfTransformations(base, left)
    println(factoryOfTransformations)
    val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(base)

}