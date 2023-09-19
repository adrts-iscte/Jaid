package model

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations
import model.transformations.*
import kotlin.reflect.KClass

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
        checkForConflictingRenames(it, listOfTransformations, destinyProject)
        it.applyTransformation(destinyProject)
    }

    listOfTransformations.shuffle()
    listOfTransformations.forEach { it.applyTransformation(destinyProject) }
}

fun checkForConflictingRenames(addNodeTransformation: AddNodeTransformation, listOfTransformations: MutableList<Transformation>, destinyProject: Project) {
    val mapOfAddTransformationToRenameClass = mapOf(
        AddType::class to RenameType::class,
        AddEnumConstant::class to RenameEnumConstant::class,
        AddCallable::class to SignatureChanged::class,
        AddField::class to RenameField::class,
    )

    val conflictingRenameTransformation = listOfTransformations
        .filter { it::class == mapOfAddTransformationToRenameClass[addNodeTransformation::class] }
        .find {
            when(addNodeTransformation) {
                is AddType -> (it as RenameType).getNode().nameAsString == addNodeTransformation.getNode().nameAsString
                is AddEnumConstant -> (it as RenameEnumConstant).getNode().nameAsString == addNodeTransformation.getNode().nameAsString
                is AddCallable -> (it as SignatureChanged).getNode().nameAsString == addNodeTransformation.getNode().nameAsString
                else -> /*AddField */ (it as RenameField).getNode().name.toString() == (addNodeTransformation as AddField).getNode().name.toString()
            }
        }

    conflictingRenameTransformation?.let {
        it.applyTransformation(destinyProject)
        listOfTransformations.remove(it)
    }
}

