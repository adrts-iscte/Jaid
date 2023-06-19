package model.conflictDetection

import model.Project
import model.transformations.*
import model.uuid
import kotlin.reflect.KClass

val allCallableFileConflictTypes = listOf(
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddCallable::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? AddCallable) ?: b as AddCallable
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have callable added", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = SignatureChanged::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? SignatureChanged) ?: b as SignatureChanged
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with parameters and/or name changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = BodyChangedCallable::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? BodyChangedCallable) ?: b as BodyChangedCallable
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with body changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ModifiersChangedCallable::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? ModifiersChangedCallable) ?: b as ModifiersChangedCallable
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with modifiers changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ReturnTypeChangedMethod::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? ReturnTypeChangedMethod) ?: b as ReturnTypeChangedMethod
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a method with return type changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveCallableInterTypes::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveCallableInterTypes) ?: b as MoveCallableInterTypes
        val secondTransformation = (b as? RemoveType) ?: a as MoveCallableInterTypes
        if (firstTransformation.getAddTransformation().getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the destiny class for where the callable is being moved", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = AddCallable::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveEnumConstant) ?: b as RemoveEnumConstant
        val secondTransformation = (b as? AddCallable) ?: a as AddCallable
        if (secondTransformation.getOriginalProject().hasUsesIn(firstTransformation.getNode(), secondTransformation.getNode())) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is used in the new callable",this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveEnumConstant) ?: b as RemoveEnumConstant
        val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
        val secondTransformationBody = secondTransformation.getNewBody()
        if (secondTransformationBody != null && secondTransformation.getOriginalProject().hasUsesIn(firstTransformation.getNode(), secondTransformationBody)) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is used in the callable that will have its body changed",this))
        }
    }
}
)