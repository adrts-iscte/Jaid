package model.conflictDetection

import model.ModifierSet
import model.Project
import model.name
import model.transformations.*
import model.uuid
import model.visitors.EqualsUuidVisitor
import kotlin.reflect.KClass

val allFieldFieldConflictTypes = listOf(

object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveFieldInterTypes::class
    override fun getSecond(): KClass<out Transformation> = AddField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveFieldInterTypes) ?: b as MoveFieldInterTypes
        val secondTransformation = (b as? AddField) ?: a as AddField
        if(secondTransformation.getParentNode().uuid == firstTransformation.getAddTransformation().getParentNode().uuid &&
            firstTransformation.getNode().name == secondTransformation.getNode().name) {
            listOfConflicts.add(createConflict(a, b, "The two added fields in the destiny class have the same name", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveField::class
    override fun getSecond(): KClass<out Transformation> = MoveFieldInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveField) ?: b as RemoveField
        val secondTransformation = (b as? MoveFieldInterTypes) ?: a as MoveFieldInterTypes
        if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed field is the one to be moved to another class", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameField::class
    override fun getSecond(): KClass<out Transformation> = MoveFieldInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RenameField) ?: b as RenameField
        val secondTransformation = (b as? MoveFieldInterTypes) ?: a as MoveFieldInterTypes
        if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
            if(firstTransformation.getParentNode().uuid == secondTransformation.getAddTransformation().getParentNode().uuid &&
                secondTransformation.getNode().name == firstTransformation.getNewName()) {
                listOfConflicts.add(createConflict(a, b, "The class will have two fields with the same name after applying both Transformation's", this))
            }
        } else {
            val commonAncestorClazz = commonAncestor.getClassOrInterfaceByUUID(secondTransformation.getAddTransformation().getParentNode().uuid)
            commonAncestorClazz?.let {
                if (commonAncestorClazz.fields.any { clazzMethod ->
                        clazzMethod.name == firstTransformation.getNewName()
                    }) {
                    listOfConflicts.add(createConflict(a, b,"Another field has the same name of this one after applying both Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveFieldInterTypes::class
    override fun getSecond(): KClass<out Transformation> = MoveFieldInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as MoveFieldInterTypes
        val secondTransformation = b as MoveFieldInterTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getAddTransformation().getParentNode().uuid !=
            secondTransformation.getAddTransformation().getParentNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The same field is being moved to two different classes", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddField::class
    override fun getSecond(): KClass<out Transformation> = RenameField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? AddField) ?: b as AddField
        val secondTransformation = (b as? RenameField) ?: a as RenameField
        if(firstTransformation.getNode().name == secondTransformation.getNewName() &&
            firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "Both field's name become equal after applying both Transformation's", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddField::class
    override fun getSecond(): KClass<out Transformation> = AddField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as AddField
        val secondTransformation = b as AddField
        if(firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid &&
            firstTransformation.getNode().name == secondTransformation.getNode().name) {
            listOfConflicts.add(createConflict(a, b, "The two added fields have the same name", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveField::class
    override fun getSecond(): KClass<out Transformation> = RenameField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveField) ?: b as RemoveField
        val secondTransformation = (b as? RenameField) ?: a as RenameField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed field is the one to have name changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveField::class
    override fun getSecond(): KClass<out Transformation> = InitializerChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveField) ?: b as RemoveField
        val secondTransformation = (b as? InitializerChangedField) ?: a as InitializerChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed field is the one to have initializer changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveField::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveField) ?: b as RemoveField
        val secondTransformation = (b as? ModifiersChangedField) ?: a as ModifiersChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed field is the one to have modifiers changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveField::class
    override fun getSecond(): KClass<out Transformation> = TypeChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveField) ?: b as RemoveField
        val secondTransformation = (b as? TypeChangedField) ?: a as TypeChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed field is the one to have type changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameField::class
    override fun getSecond(): KClass<out Transformation> = RenameField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as RenameField
        val secondTransformation = b as RenameField
        if (firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid) {
            if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                if (firstTransformation.getNewName() == secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Both field's name become equal after applying both Transformation's",this))
                }
            } else {
                if (firstTransformation.getNewName() != secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Different new names for the same field after applying both RenameMethod Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = TypeChangedField::class
    override fun getSecond(): KClass<out Transformation> = InitializerChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? TypeChangedField) ?: b as TypeChangedField
        val secondTransformation = (b as? InitializerChangedField) ?: a as InitializerChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getNewType() != secondTransformation.getType() &&
            secondTransformation.getNewInitializer() != null) {
            listOfConflicts.add(createConflict(a, b, "Both Transformation's cannot be applied because the new type and the initializer's expression type are different", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = TypeChangedField::class
    override fun getSecond(): KClass<out Transformation> = TypeChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as TypeChangedField
        val secondTransformation = b as TypeChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getNewType() != secondTransformation.getNewType()) {
            listOfConflicts.add(createConflict(a, b, "Both TypeChangedField Transformation's cannot be applied because the new types are different", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ModifiersChangedField::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ModifiersChangedField
        val secondTransformation = b as ModifiersChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            ModifierSet(firstTransformation.getNewModifiers()).isConflictiousWith(ModifierSet(secondTransformation.getNewModifiers()))) {
            listOfConflicts.add(createConflict(a, b, "Both ModifiersChangedField Transformation's changes are conflictious", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = InitializerChangedField::class
    override fun getSecond(): KClass<out Transformation> = InitializerChangedField::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as InitializerChangedField
        val secondTransformation = b as InitializerChangedField
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            !EqualsUuidVisitor(firstTransformation.getOriginalProject(), secondTransformation.getOriginalProject()).
            equals(firstTransformation.getNewInitializer(), secondTransformation.getNewInitializer())) {
            listOfConflicts.add(createConflict( a, b, "Both InitializerChangedField Transformation's changes cannot be applied because they are different", this))
        }
    }
}
)