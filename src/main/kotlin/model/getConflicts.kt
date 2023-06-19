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