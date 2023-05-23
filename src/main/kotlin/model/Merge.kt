package model

import model.transformations.*

fun merge(destinyProject : Project, factoryOfTransformationsLeft: FactoryOfTransformations, factoryOfTransformationsRight: FactoryOfTransformations) : Project {

    applyTransformationsTo(destinyProject, factoryOfTransformationsLeft)
    applyTransformationsTo(destinyProject, factoryOfTransformationsRight)

    return destinyProject
}

fun applyTransformationsTo(destinyProject : Project, factoryOfTransformations: FactoryOfTransformations, ignoreChangePackage : Boolean = false) {
    val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()

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

//    destinyProject.initializeAllIndexes()

    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }

    globalMoveTransformations.forEach { it.getAddTransformation().applyTransformation(destinyProject) }
    val addTransformations = listOfTransformations.filterIsInstance<AddNodeTransformation>().toMutableSet()
    listOfTransformations.removeAll(addTransformations)
    addTransformations.forEach {
        it.applyTransformation(destinyProject)
        destinyProject.initializeAllIndexes()
    }

//    destinyProject.initializeAllIndexes()

    listOfTransformations.shuffle()
    listOfTransformations.forEach { it.applyTransformation(destinyProject) }
}

//fun applyTransformationsTo(destinyProject : Project, factoryOfTransformations: FactoryOfTransformations, ignoreChangePackage : Boolean = false) {
//    val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
//
//    if (ignoreChangePackage) {
//        listOfTransformations.removeIf { it is ChangePackage}
//    }
//
//    val globalMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationInterClassOrCompilationUnit>().toMutableSet()
//    val localMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationIntraClassOrCompilationUnit>().toMutableSet()
//    listOfTransformations.removeAll(globalMoveTransformations)
//    listOfTransformations.removeAll(localMoveTransformations)
//
//    /*
//    val globalMoveTransformationsClassUUIDs = globalMoveTransformations.associateBy { it.getClass().uuid }
//    val localMoveTransformationsClassUUIDs = localMoveTransformations.associateBy { it.getClass().uuid }
//
////    val setOfOrderedMoveTransformations = mutableSetOf<Pair<Transformation, Transformation>>()
//    val setOfDelayedGlobalMoveTransformation = mutableSetOf<Transformation>()
//    val intersection = globalMoveTransformationsClassUUIDs.keys.intersect(localMoveTransformationsClassUUIDs.keys)
//    intersection.forEach {
//        val globalTransformation = globalMoveTransformationsClassUUIDs[it]!!
////        val localTransformation = localMoveTransformationsClassUUIDs[it]!!
////        setOfOrderedMoveTransformations.add(Pair(localTransformation, globalTransformation))
//        setOfDelayedGlobalMoveTransformation.add(globalTransformation)
//        globalMoveTransformations.remove(globalTransformation)
////        localMoveTransformations.remove(localTransformation)
//    }
//
//    globalMoveTransformations.forEach { it.applyTransformation(destinyProject) }
//    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }
//    setOfDelayedGlobalMoveTransformation.forEach { it.applyTransformation(destinyProject) }
//
////    globalMoveTransformations.elementAt(0).applyTransformation(destinyProject)
//*/
//
//    globalMoveTransformations.forEach { it.getRemoveTransformation().applyTransformation(destinyProject) }
//    val removeTransformations = listOfTransformations.filterIsInstance<RemoveNodeTransformation>().toMutableSet()
//    listOfTransformations.removeAll(removeTransformations)
//    removeTransformations.forEach { it.applyTransformation(destinyProject) }
//
////    localMoveTransformations.elementAt(0).applyTransformation(destinyProject)
////    localMoveTransformations.elementAt(2).applyTransformation(destinyProject)
////    localMoveTransformations.elementAt(1).applyTransformation(destinyProject)
////    localMoveTransformations.elementAt(3).applyTransformation(destinyProject)
//    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }
//
//    globalMoveTransformations.forEach { it.getAddTransformation().applyTransformation(destinyProject) }
//    val addTransformations = listOfTransformations.filterIsInstance<AddNodeTransformation>().toMutableSet()
//    listOfTransformations.removeAll(addTransformations)
//    addTransformations.forEach { it.applyTransformation(destinyProject) }
//
//    listOfTransformations.shuffle()
//    listOfTransformations.forEach { it.applyTransformation(destinyProject) }
//
//}