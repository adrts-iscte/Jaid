package model.conflictDetection

import model.Project
import model.transformations.*
import model.uuid
import kotlin.reflect.KClass

val allFileJavadocConflictTypes: List<ConflictType> = listOf(
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? SetJavaDoc) ?: b as SetJavaDoc
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have javadoc added/changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
    override fun getSecond(): KClass<out Transformation> = RemoveEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? SetJavaDoc) ?: b as SetJavaDoc
        val secondTransformation = (b as? RemoveEnumConstant) ?: a as RemoveEnumConstant
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is the one to have javadoc added/changed", this))
        }
    }
},
)