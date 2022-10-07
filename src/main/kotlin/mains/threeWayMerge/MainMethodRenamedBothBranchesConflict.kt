package mains.threeWayMerge

import com.github.javaparser.StaticJavaParser
import model.getConflicts
import java.io.File

fun main() {
    /*
    val commonAncestor = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/methodRenamedBothBranchesConflict/commonAncestor/Bill.java"))
    val mergedBranch = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/methodRenamedBothBranchesConflict/mergedBranch/Bill.java"))
    val branchToBeMerged = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/methodRenamedBothBranchesConflict/branchToBeMerged/Bill.java"))

    val listOfTransformationsMergedBranch = getListOfTransformationsOfFile(commonAncestor, mergedBranch)
    val listOfTransformationsBranchToBeMerged = getListOfTransformationsOfFile(commonAncestor, branchToBeMerged)

    // Remover as transformações do body apenas devido a Renames de Métodos
    val newListOfTransformationsMergedBranch = listOfTransformationsMergedBranch.toMutableSet()
    newListOfTransformationsMergedBranch.removeIf { it is BodyChangedMethod }
    val newListOfTransformationsBranchToBeMerged = listOfTransformationsBranchToBeMerged.toMutableSet()
    newListOfTransformationsBranchToBeMerged.removeIf { it is BodyChangedMethod }

    val setOfConflicts = getConflicts(newListOfTransformationsMergedBranch, newListOfTransformationsBranchToBeMerged)
    setOfConflicts.forEach {
        println("Conflict between ${it.first.getText()} and ${it.second.getText()}")
    }
     */
}