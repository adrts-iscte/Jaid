package mains

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.getListOfTransformationsOfClass
import java.io.File

fun main() {
    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java"))
    val baseClass : ClassOrInterfaceDeclaration = base.getClassByName("Class").get()
    val leftClass : ClassOrInterfaceDeclaration = left.getClassByName("Class").get()

//    testFunction(baseClass, leftClass)
    val listOfTransformations = getListOfTransformationsOfClass(baseClass, leftClass)
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(baseClass)

//    val listOfTransformations = getListOfTransformations(leftClass, baseClass)
//    listOfTransformations.forEach { it.applyTransformation(left) }
//    println(leftClass)
}