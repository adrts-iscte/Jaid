package model.conflictDetection

import model.Project
import model.transformations.*
import model.uuid
import kotlin.reflect.KClass
import com.github.javaparser.ast.Node

val allFieldFileConflictTypes = listOf(
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddField::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? AddField) ?: b as AddField
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have field added", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameField::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RenameField) ?: b as RenameField
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field renamed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = InitializerChangedField::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? InitializerChangedField) ?: b as InitializerChangedField
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with initializer changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ModifiersChangedField::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? ModifiersChangedField) ?: b as ModifiersChangedField
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with modifiers changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = TypeChangedField::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? TypeChangedField) ?: b as TypeChangedField
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with type changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveFieldInterTypes::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveFieldInterTypes) ?: b as MoveFieldInterTypes
        val secondTransformation = (b as? RemoveType) ?: a as MoveCallableInterTypes
        if (firstTransformation.getAddTransformation().getParentNode().uuid == secondTransformation.getNode().uuid){
            listOfConflicts.add(createConflict(a, b, "The removed type is the destiny class for where the field is being moved", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = InitializerChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveEnumConstant) ?: b as RemoveEnumConstant
        val secondTransformation = (b as? InitializerChangedField) ?: a as InitializerChangedField
        if (secondTransformation.getOriginalProject().hasUsesIn(firstTransformation.getNode(), (secondTransformation.getNewInitializer() as Node))) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is used in the field that will have its initializer changed",this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = AddField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveEnumConstant) ?: b as RemoveEnumConstant
        val secondTransformation = (b as? AddField) ?: a as AddField
        if (secondTransformation.getOriginalProject().hasUsesIn(firstTransformation.getNode(), secondTransformation.getNode())) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is used in the new field",this))
        }
    }
}
)