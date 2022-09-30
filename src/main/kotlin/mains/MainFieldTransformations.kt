import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.getListOfTransformationsOfClass
import model.getListOfTransformationsOfFile
import java.io.File

fun main() {
    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fieldTransformations/base/FieldTransformationsBaseClass.java"))
    val left = StaticJavaParser.parse(File("src/main/kotlin/scenarios/fieldTransformations/left/FieldTransformationsLeftClass.java"))
    val baseClass : ClassOrInterfaceDeclaration = base.getClassByName("Class").get()
    val leftClass : ClassOrInterfaceDeclaration = left.getClassByName("Class").get()

    val clonedBase = base.clone()
//    testFunction(baseClass, leftClass)
    val listOfTransformations = getListOfTransformationsOfClass(baseClass, leftClass, clonedBase)
    listOfTransformations.forEach { it.applyTransformation(base) }
    println(baseClass)

//    val listOfTransformations = model.getListOfTransformations(leftClass, baseClass)
//    listOfTransformations.forEach { it.applyTransformation(left) }
//    println(leftClass)
}