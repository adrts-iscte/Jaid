package model

import com.github.javaparser.ast.CompilationUnit
import model.transformations.*

fun getConflicts(commonAncestor: CompilationUnit, listOfTransformationsMergedBranch : Set<Transformation>,
                 listOfTransformationsBranchToBeMerged : Set<Transformation>)
                : Set<Conflict> {

    val setOfConflicts = mutableSetOf<Conflict>()

    listOfTransformationsMergedBranch.forEach {
        if (it is RenameMethod) {
            setOfConflicts.addAll(it.getListOfConflicts(commonAncestor, listOfTransformationsBranchToBeMerged))
        }
    }

    return setOfConflicts
}