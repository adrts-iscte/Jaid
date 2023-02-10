package modifiers

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import model.ModifierSet
import model.transformations.ParametersAndOrNameChangedCallable
import java.io.File

fun main() {
    val base = StaticJavaParser.parse(File("src/main/kotlin/modifiers/Class.java"))
    val baseClass = base.getClassByName("ClassTest").get()
    val allMethods = baseClass.findAll(MethodDeclaration::class.java)
    val firstMethod = allMethods[0]
    val secondMethod = allMethods[1]

    val firstModifierSet = ModifierSet(firstMethod.modifiers)
    val secondModifierSet = ModifierSet(secondMethod.modifiers)
    val isConflitious = firstModifierSet.isConflictiousWith(secondModifierSet)
    println("Is conlictious? $isConflitious")
    if (!isConflitious) {
        println(firstModifierSet.merge(secondModifierSet))
    }

    firstModifierSet.replaceModifiersBy(secondModifierSet)
    println(firstModifierSet.toNodeList())

    val transformation = ParametersAndOrNameChangedCallable(baseClass, firstMethod, secondMethod.parameters, secondMethod.nameAsString)
    println(transformation.getText())
    println(transformation.signatureChanged())
}