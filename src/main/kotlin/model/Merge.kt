package model

import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations
import model.transformations.*

fun merge(destinyProject : Project, redundancyFreeSetOfTransformations : RedundancyFreeSetOfTransformations, ignoreChangePackage : Boolean = false) : Project {

    val finalSetOfTransformation = redundancyFreeSetOfTransformations.getFinalSetOfTransformations()

    applyTransformationsTo(destinyProject, finalSetOfTransformation, ignoreChangePackage)
//    applyTransformationsTo(destinyProject, factoryOfTransformationsLeft)
//    applyTransformationsTo(destinyProject, factoryOfTransformationsRight)

    return destinyProject
}

fun applyTransformationsTo(destinyProject : Project, allTransformations: Set<Transformation>, ignoreChangePackage : Boolean = false) {
    val listOfTransformations = allTransformations.toMutableList()

    if (ignoreChangePackage) {
        listOfTransformations.removeIf { it is ChangePackage}
    }

    val addFileTransformations = listOfTransformations.filterIsInstance<AddFile>().toMutableSet()
    listOfTransformations.removeAll(addFileTransformations)
    addFileTransformations.forEach { it.applyTransformation(destinyProject) }

    val globalMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationInterClassOrCompilationUnit>().toMutableSet()
    val localMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationIntraTypeOrCompilationUnit>().toMutableSet()
    listOfTransformations.removeAll(globalMoveTransformations)
    listOfTransformations.removeAll(localMoveTransformations)

    globalMoveTransformations.forEach { it.getRemoveTransformation().applyTransformation(destinyProject) }
    val removeTransformations = listOfTransformations.filter { it is RemoveNodeTransformation || it is RemoveFile }.toMutableSet()
    listOfTransformations.removeAll(removeTransformations)
    removeTransformations.forEach { it.applyTransformation(destinyProject) }

    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }

    globalMoveTransformations.forEach { it.getAddTransformation().applyTransformation(destinyProject) }
    val addTransformations = listOfTransformations.filterIsInstance<AddNodeTransformation>().toMutableSet()
    listOfTransformations.removeAll(addTransformations)
    addTransformations.forEach {
        if (it is AddType && it.getNode().nameAsString == "FnExpr") {
            println()
        }
        it.applyTransformation(destinyProject)
    }

    listOfTransformations.shuffle()
    listOfTransformations.forEach { it.applyTransformation(destinyProject) }
}