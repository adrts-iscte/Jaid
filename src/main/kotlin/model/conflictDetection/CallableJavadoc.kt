package model.conflictDetection

import model.Project
import model.transformations.RemoveCallable
import model.transformations.SetJavaDoc
import model.transformations.Transformation
import model.uuid
import kotlin.reflect.KClass

val allCallableJavadocConflictTypes: List<ConflictType> = listOf(
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
        override fun getSecond(): KClass<out Transformation> = RemoveCallable::class

        override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = (a as? SetJavaDoc) ?: b as SetJavaDoc
            val secondTransformation = (b as? RemoveCallable) ?: a as RemoveCallable
            if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
                listOfConflicts.add(createConflict(a, b, "The removed callable is the one to have javadoc added/changed", this))
            }
        }
    }
)