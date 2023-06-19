package mains.threeWayMerge

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.FactoryOfTransformations
import model.getConflicts
import java.io.File
import kotlin.reflect.KClass

fun main() {

//    val commonAncestor = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/commonAncestor/Bill.java"))
//    val mergedBranch = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/mergedBranch/Bill.java"))
//    val branchToBeMerged = StaticJavaParser.parse(File("src/main/kotlin/scenarios/threeWayMerge/renameConflict/branchToBeMerged/Bill.java"))
//
//    val listOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch).getFinalListOfTransformations()
//    val listOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged).getFinalListOfTransformations()
//
//    val listOfConflicts = getConflicts(commonAncestor, listOfTransformationsBranchToBeMerged, listOfTransformationsMergedBranch)
//    listOfConflicts.forEach {
//        println("${it.first.javaClass.simpleName}/${it.second.javaClass.simpleName} - ${it.message}")
//    }

}