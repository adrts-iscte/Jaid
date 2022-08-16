package mains

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.getListOfTransformations
import java.io.File

fun main() {
    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/methodTransformations/base/MethodTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/methodTransformations/left/MethodTransformationsLeftClass.java"))
    val baseClass : ClassOrInterfaceDeclaration = base.getClassByName("Class").get()
    val leftClass : ClassOrInterfaceDeclaration = left.getClassByName("Class").get()

//    testFunction(baseClass, leftClass)
    val listOfTransformations = getListOfTransformations(baseClass, leftClass)
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(baseClass)

//    val listOfTransformations = getListOfTransformations(leftClass, baseClass)
//    listOfTransformations.forEach { it.applyTransformation(left) }
//    println(leftClass)
}