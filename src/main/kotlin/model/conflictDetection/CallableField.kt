package model.conflictDetection

import model.Project
import model.transformations.*
import kotlin.reflect.KClass

val allCallableFieldConflictTypes: List<ConflictType> = listOf(
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = RemoveField::class
        override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

        override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = (a as? RemoveField) ?: b as RemoveField
            val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
            val secondTransformationBody = secondTransformation.getNewBody()
            if (secondTransformationBody != null && secondTransformation.getProject().getNodeReferencesToReferencedNode(firstTransformation.getNode(), secondTransformationBody).isNotEmpty()){
                listOfConflicts.add(createConflict(a, b, "The changes to the callable in BodyChangedCallable Transformation make use of the removed field", this))
            }
        }
    },
//        Needs to be verified!
//        object : ConflictType {
//            override fun getFirst(): KClass<out Transformation> = TypeChangedField::class
//            override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class
//
//            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
//                val firstTransformation = (a as? TypeChangedField) ?: b as TypeChangedField
//                val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
//                if (!commonAncestor.isCorrectASTafterApplyingBothTransformations(firstTransformation, secondTransformation)){
//                    listOfConflicts.add(createConflict(a, b, "The changes to the callable in BodyChangedCallable Transformation make use of the removed field", this))
//                }
//            }
//        }
)