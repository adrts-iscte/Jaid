package model

import model.conflictDetection.Conflict
import model.conflictDetection.ConflictTypeLibrary.applicableConflict
import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations

fun getConflicts(commonAncestor: Project, redundancyFreeSetOfTransformations : RedundancyFreeSetOfTransformations) : Set<Conflict> {
    val setOfConflicts = mutableSetOf<Conflict>()

    val allCombinationOfTransformations = redundancyFreeSetOfTransformations.getFinalSetOfCombinations()

    allCombinationOfTransformations.forEach { pair ->
        val conflictType = applicableConflict(pair.first, pair.second)
        conflictType?.let {
            conflictType.verifyIfExistsConflict(pair.first, pair.second, commonAncestor, setOfConflicts)
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