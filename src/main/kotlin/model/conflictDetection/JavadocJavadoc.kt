package model.conflictDetection

import model.Project
import model.transformations.RemoveJavaDoc
import model.transformations.SetJavaDoc
import model.transformations.Transformation
import model.uuid
import kotlin.reflect.KClass

val allJavadocJavadocConflictTypes = listOf(
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
        override fun getSecond(): KClass<out Transformation> = SetJavaDoc::class

        override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = a as SetJavaDoc
            val secondTransformation = b as SetJavaDoc
            if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                firstTransformation.getJavaDocComment() != secondTransformation.getJavaDocComment()) {
                if (!firstTransformation.isAddOperation() && !secondTransformation.isAddOperation()) {
                    listOfConflicts.add(createConflict(a, b, "Both javadoc changes to this element are different", this))
                } else if (firstTransformation.isAddOperation() && secondTransformation.isAddOperation()) {
                    listOfConflicts.add(createConflict(a, b, "Both javadoc additions to this element are different", this))
                }
            }
        }
    },
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
        override fun getSecond(): KClass<out Transformation> = RemoveJavaDoc::class

        override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = a as SetJavaDoc
            val secondTransformation = b as RemoveJavaDoc
            if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
                listOfConflicts.add(createConflict(a, b, "The removed javadoc's node is the one to have the javadoc changed", this))
            }
        }
    }
)