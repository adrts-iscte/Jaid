package model

import model.transformations.*

//fun merge(projLeft : Project, projBase : Project, projRight : Project?) {
//
//}

fun applyTransformationsTo(destinyProject : Project, factoryOfTransformations: FactoryOfTransformations, ignoreChangePackage : Boolean = false) {
    val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()

    if (ignoreChangePackage) {
        listOfTransformations.removeIf { it is ChangePackage}
    }

    val globalMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationInterClassOrCompilationUnit>().toMutableSet()
    val localMoveTransformations = listOfTransformations.filterIsInstance<MoveTransformationIntraClassOrCompilationUnit>().toMutableSet()
    listOfTransformations.removeAll(globalMoveTransformations)
    listOfTransformations.removeAll(localMoveTransformations)

    /*
    val globalMoveTransformationsClassUUIDs = globalMoveTransformations.associateBy { it.getClass().uuid }
    val localMoveTransformationsClassUUIDs = localMoveTransformations.associateBy { it.getClass().uuid }

//    val setOfOrderedMoveTransformations = mutableSetOf<Pair<Transformation, Transformation>>()
    val setOfDelayedGlobalMoveTransformation = mutableSetOf<Transformation>()
    val intersection = globalMoveTransformationsClassUUIDs.keys.intersect(localMoveTransformationsClassUUIDs.keys)
    intersection.forEach {
        val globalTransformation = globalMoveTransformationsClassUUIDs[it]!!
//        val localTransformation = localMoveTransformationsClassUUIDs[it]!!
//        setOfOrderedMoveTransformations.add(Pair(localTransformation, globalTransformation))
        setOfDelayedGlobalMoveTransformation.add(globalTransformation)
        globalMoveTransformations.remove(globalTransformation)
//        localMoveTransformations.remove(localTransformation)
    }

    globalMoveTransformations.forEach { it.applyTransformation(destinyProject) }
    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }
    setOfDelayedGlobalMoveTransformation.forEach { it.applyTransformation(destinyProject) }

//    globalMoveTransformations.elementAt(0).applyTransformation(destinyProject)
*/

    globalMoveTransformations.forEach { it.getRemoveTransformation().applyTransformation(destinyProject) }
    val removeTransformations = listOfTransformations.filterIsInstance<RemoveNodeTransformation>().toMutableSet()
    listOfTransformations.removeAll(removeTransformations)
    removeTransformations.forEach { it.applyTransformation(destinyProject) }

//    localMoveTransformations.elementAt(0).applyTransformation(destinyProject)
//    localMoveTransformations.elementAt(2).applyTransformation(destinyProject)
//    localMoveTransformations.elementAt(1).applyTransformation(destinyProject)
//    localMoveTransformations.elementAt(3).applyTransformation(destinyProject)
    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }

    globalMoveTransformations.forEach { it.getAddTransformation().applyTransformation(destinyProject) }
    val addTransformations = listOfTransformations.filterIsInstance<AddNodeTransformation>().toMutableSet()
    listOfTransformations.removeAll(addTransformations)
    addTransformations.forEach { it.applyTransformation(destinyProject) }

    listOfTransformations.shuffle()
    listOfTransformations.forEach { it.applyTransformation(destinyProject) }

}

//fun applyTransformationsTo(destinyProject : Project, factoryOfTransformations: FactoryOfTransformations) {
//    val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
//
//    val globalMoveTransformations = listOfTransformations.filterIsInstance<MoveCallableInterClasses>()
//    val localMoveTransformations = listOfTransformations.filterIsInstance<MoveCallableIntraClass>()
//
//    listOfTransformations.removeAll(globalMoveTransformations)
//    listOfTransformations.removeAll(localMoveTransformations)
//
//    val globalMoveTransformationsClassUUIDs = globalMoveTransformations.map { it.getClass().uuid }
//    val localMoveTransformationsClassUUIDs = localMoveTransformations.map { it.getClass().uuid }
//
//    globalMoveTransformations.elementAt(1).applyTransformation(destinyProject)
//
////    globalMoveTransformations.forEach { it.applyTransformation(destinyProject) }
//
//    localMoveTransformations.sortedBy { it.getOrderIndex() }.forEach { it.applyTransformation(destinyProject) }
//    globalMoveTransformations.elementAt(0).applyTransformation(destinyProject)
//
//    listOfTransformations.shuffle()
//    listOfTransformations.forEach { it.applyTransformation(destinyProject) }
//
//}