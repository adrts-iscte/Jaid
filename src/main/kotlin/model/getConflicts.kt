package model

import com.github.javaparser.ast.CompilationUnit
import model.transformations.*

fun getConflicts(commonAncestor: CompilationUnit, listOfTransformationsMergedBranch : Set<Transformation>,
                 listOfTransformationsBranchToBeMerged : Set<Transformation>)
        : Set<Conflict> {

    val setOfConflicts = mutableSetOf<Conflict>()

    val allCombinationOfTransformations = getProductOfTwoCollectionsOfTransformations(listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)

    allCombinationOfTransformations.forEach { pair ->
        val conflictType = applicableConflict(pair.first, pair.second)
        conflictType?.let {
            conflictType.verifyIfExistsConflict(pair.first, pair.second, setOfConflicts)
        }
    }

    return setOfConflicts
}
//
//fun getConflicts(commonAncestor: CompilationUnit, listOfTransformationsMergedBranch : Set<Transformation>,
//                 listOfTransformationsBranchToBeMerged : Set<Transformation>)
//                : Set<Conflict> {
//
//    val setOfConflicts = mutableSetOf<Conflict>()
//
////    listOfTransformationsMergedBranch.forEach {
////        if (it is RemoveCallableDeclaration) {
////            setOfConflicts.addAll(it.getListOfConflicts(commonAncestor, listOfTransformationsBranchToBeMerged))
////        }
////    }
//
//    listOfTransformationsBranchToBeMerged.forEach {
//        if (it is ParametersAndOrNameChangedCallable) {
//            setOfConflicts.addAll(it.getListOfConflicts(commonAncestor, listOfTransformationsMergedBranch))
//        }
//    }
//
////    listOfTransformationsMergedBranch.filterIsInstance<ModifiersChangedCallable>().forEach {
////        println(it.getNewModifiers())
////        it.applyTransformation(commonAncestor)
////    }
////    println(commonAncestor)
//
//    return setOfConflicts
//}