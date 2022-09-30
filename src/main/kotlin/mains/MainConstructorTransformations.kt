package mains

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.getListOfTransformationsOfClass
import java.io.File

fun main() {
//    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java"))
    val baseNotClonedClass = base.getClassByName("Class").get()
    val clonedBase = base.clone()
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java"))
    val clonedLeft = left.clone()
    val baseClass : ClassOrInterfaceDeclaration = clonedBase.getClassByName("Class").get()
    val leftClass : ClassOrInterfaceDeclaration = clonedLeft.getClassByName("Class").get()

//    testFunction(baseClass, leftClass)
    val listOfTransformations = getListOfTransformationsOfClass(baseClass, leftClass, clonedBase)
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(baseClass)

//    val listOfTransformations = getListOfTransformations(leftClass, baseClass)
//    listOfTransformations.forEach { it.applyTransformation(left) }
//    println(leftClass)
}