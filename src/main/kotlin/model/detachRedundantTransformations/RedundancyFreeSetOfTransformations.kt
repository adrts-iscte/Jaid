package model.detachRedundantTransformations

import model.FactoryOfTransformations
import model.getProductOfTwoCollections
import model.transformations.Transformation
import model.transformations.TransformationWithReferences
import model.visitors.UpdateRedundantNodesVisitor

class RedundancyFreeSetOfTransformations(left: FactoryOfTransformations, right: FactoryOfTransformations) {

    private val leftSetOfTransformations = left.getListOfAllTransformations().toMutableSet()
    private val rightSetOfTransformations = right.getListOfAllTransformations().toMutableSet()

    private val sharedSetOfTransformations = mutableSetOf<Pair<Transformation, Transformation>>()

    init {
        findRedundantTransformations()
    }

    private fun findRedundantTransformations() {
        val allCombinationOfTransformations = getProductOfTwoCollections(leftSetOfTransformations, rightSetOfTransformations)
        val pairsOfSameType = allCombinationOfTransformations.filter { it.first::class == it.second::class }
        pairsOfSameType.forEach {
            if (it.first == it.second) {
                sharedSetOfTransformations.add(it)
                leftSetOfTransformations.remove(it.first)
                rightSetOfTransformations.remove(it.second)
                if (it.second is TransformationWithReferences) {
                    it.first.getNode().accept(UpdateRedundantNodesVisitor(it.second.getNode()), (it.second as TransformationWithReferences).getOriginalProject())
                }
            }
        }
    }

    fun getLeftSetOfTransformations() = leftSetOfTransformations

    fun getRightSetOfTransformations() = rightSetOfTransformations

    fun getSharedSetOfTransformations() = sharedSetOfTransformations.map { it.first }

    fun getSharedPairsOfTransformations() = sharedSetOfTransformations

    fun getFinalSetOfTransformations() = leftSetOfTransformations + rightSetOfTransformations + sharedSetOfTransformations.map { it.first }

    fun getFinalSetOfCombinations(): List<Pair<Transformation, Transformation>> {
        return getProductOfTwoCollections(leftSetOfTransformations, rightSetOfTransformations) +
               getProductOfTwoCollections(leftSetOfTransformations, getSharedSetOfTransformations()) +
               getProductOfTwoCollections(getSharedSetOfTransformations(), rightSetOfTransformations)
    }
}