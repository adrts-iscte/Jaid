package model

import Transformation

fun getConflicts(listOfTransformationsMergedBranch : MutableSet<Transformation>,
                 listOfTransformationsBranchToBeMerged : MutableSet<Transformation>)
                : Set<Pair<Transformation,Transformation>> {

    val setOfConflicts = mutableSetOf<Pair<Transformation,Transformation>>()

    val listOfTransformationsMergedBranchIterator = listOfTransformationsMergedBranch.iterator()
    while (listOfTransformationsMergedBranchIterator.hasNext()) {
        val mergedBranchTransformation = listOfTransformationsMergedBranchIterator.next()
        listOfTransformationsBranchToBeMerged.filter { it.getNode().uuid == mergedBranchTransformation.getNode().uuid }
                                            .forEach {
                                                setOfConflicts.add(Pair(mergedBranchTransformation, it))
                                            }
    }

    return setOfConflicts
}