package mains.threeWayMerge

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.getConflicts
import java.io.File

fun main() {
    /*
    val commonAncestor = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/commonAncestor/Bill.java"))
    val mergedBranch = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/mergedBranch/Bill.java"))
    val branchToBeMerged = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/branchToBeMerged/Bill.java"))

    val listOfTransformationsMergedBranch = getListOfTransformationsOfFile(commonAncestor, mergedBranch)
    val listOfTransformationsBranchToBeMerged = getListOfTransformationsOfFile(commonAncestor, branchToBeMerged)

    // Remover as transformações do body apenas devido a Renames de Métodos
    val newListOfTransformationsMergedBranch = listOfTransformationsMergedBranch.toMutableSet()
    newListOfTransformationsMergedBranch.removeIf { it is BodyChangedMethod }
    val newListOfTransformationsBranchToBeMerged = listOfTransformationsBranchToBeMerged.toMutableSet()
    newListOfTransformationsBranchToBeMerged.removeIf { it is BodyChangedMethod }

    val setOfConflicts = getConflicts(newListOfTransformationsMergedBranch, newListOfTransformationsBranchToBeMerged)
    setOfConflicts.forEach {
        println(it)
    }
     */
}