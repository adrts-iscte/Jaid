package mains

import com.github.javaparser.StaticJavaParser
import model.FactoryOfTransformations
import java.io.File
/*
fun main() {
//    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fieldTransformations/base/FieldTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fieldTransformations/left/FieldTransformationsLeftClass.java"))

    val factoryOfTransformations = FactoryOfTransformations(base, left)
    println(factoryOfTransformations)
    val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(base)

}*/