package model.conflictDetection

import model.*
import model.transformations.*
import model.visitors.EqualsUuidVisitor
import kotlin.reflect.KClass

val allCallableCallableConflictTypes = listOf(

object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddCallable::class
    override fun getSecond(): KClass<out Transformation> = AddCallable::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = a as AddCallable
        val secondTransformation = b as AddCallable
        if(firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid &&
            firstTransformation.getNode().signature == secondTransformation.getNode().signature) {
            listOfConflicts.add(createConflict(a, b, "The two added callables have the same signature", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddCallable::class
    override fun getSecond(): KClass<out Transformation> = SignatureChanged::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? AddCallable) ?: b as AddCallable
        val secondTransformation = (b as? SignatureChanged) ?: a as SignatureChanged
        if(firstTransformation.getNode().name == secondTransformation.getNewName() &&
            firstTransformation.getNode().parameterTypes == secondTransformation.getNewParameters().types &&
            firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "Both callable's signature become equal after applying both Transformation's", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddCallable::class
    override fun getSecond(): KClass<out Transformation> = MoveCallableInterTypes::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = (a as? AddCallable) ?: b as AddCallable
        val secondTransformation = (b as? MoveCallableInterTypes) ?: a as MoveCallableInterTypes
        if(firstTransformation.getParentNode().uuid == secondTransformation.getAddTransformation().getParentNode().uuid &&
            firstTransformation.getNode().signature == secondTransformation.getNode().signature) {
            listOfConflicts.add(createConflict(a, b, "The two added callables in the destiny class have the same signature", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveCallable::class
    override fun getSecond(): KClass<out Transformation> = SignatureChanged::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
        val secondTransformation = (b as? SignatureChanged) ?: a as SignatureChanged
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            val message = if (secondTransformation.nameChanged() && secondTransformation.parametersChanged()) {
                "The removed method is the one to have parameters and name changed"
            } else if(secondTransformation.nameChanged()) {
                "The removed method is the one to have name changed"
            } else {
                "The removed method is the one to have parameters changed"
            }
            listOfConflicts.add(createConflict( a, b, message, this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveCallable::class
    override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
        val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed callable is the one to have body changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveCallable::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedCallable::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
        val secondTransformation = (b as? ModifiersChangedCallable) ?: a as ModifiersChangedCallable
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed callable is the one to have modifiers changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveCallable::class
    override fun getSecond(): KClass<out Transformation> = ReturnTypeChangedMethod::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
        val secondTransformation = (b as? ReturnTypeChangedMethod) ?: a as ReturnTypeChangedMethod
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed method is the one to have return type changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveCallable::class
    override fun getSecond(): KClass<out Transformation> = MoveCallableInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
        val secondTransformation = (b as? MoveCallableInterTypes) ?: a as MoveCallableInterTypes
        if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed method is the one to be moved to another class", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = SignatureChanged::class
    override fun getSecond(): KClass<out Transformation> = SignatureChanged::class

    override fun check(
        a: Transformation,
        b: Transformation,
        commonAncestor: Project,
        listOfConflicts: MutableSet<Conflict>
    ) {
        val firstTransformation = a as SignatureChanged
        val secondTransformation = b as SignatureChanged
        if (firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid) {
            if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                if (firstTransformation.getNewName() == secondTransformation.getNewName() &&
                    firstTransformation.getNewParameters().types == secondTransformation.getNewParameters().types
                ) {
                    listOfConflicts.add(createConflict(a, b, "Both callable's signature become equal after applying both Transformation's",this))
                }
            } else {
                if (firstTransformation.nameChanged() &&
                    secondTransformation.nameChanged() &&
                    firstTransformation.getNewName() != secondTransformation.getNewName()
                ) {
                    listOfConflicts.add(createConflict(a, b, "Different new names for the same method after applying both RenameMethod Transformation's", this))
                }
                if (firstTransformation.parametersChanged() &&
                    secondTransformation.parametersChanged() &&
                    firstTransformation.getNewParameters() != secondTransformation.getNewParameters()
                ) {
                    listOfConflicts.add(createConflict(a, b, "Different modifications on the parameters of the same callable", this))
                }
                val commonAncestorClazz = commonAncestor.getClassOrInterfaceByUUID(firstTransformation.getParentNode().uuid)
                commonAncestorClazz?.let {
                    if(firstTransformation.nameChanged() && !firstTransformation.parametersChanged() &&
                        !secondTransformation.nameChanged() && secondTransformation.parametersChanged()) {
                        if (commonAncestorClazz.methods.any { clazzMethod ->
                                clazzMethod.name == firstTransformation.getNewName() &&
                                        clazzMethod.parameterTypes == secondTransformation.getNewParameters().types
                            }) {
                            listOfConflicts.add(createConflict(a, b,"Another callable has the same signature of this one after applying both Transformation's", this))
                        }
                    } else if(!firstTransformation.nameChanged() && firstTransformation.parametersChanged() &&
                        secondTransformation.nameChanged() && !secondTransformation.parametersChanged()) {
                        if (commonAncestorClazz.methods.any { clazzMethod ->
                                clazzMethod.name == secondTransformation.getNewName() &&
                                        clazzMethod.parameterTypes == firstTransformation.getNewParameters().types
                            }) {
                            listOfConflicts.add(createConflict(a, b,"Another callable has the same signature of this one after applying both Transformation's", this))
                        }
                    }
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = SignatureChanged::class
    override fun getSecond(): KClass<out Transformation> = MoveCallableInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? SignatureChanged) ?: b as SignatureChanged
        val secondTransformation = (b as? MoveCallableInterTypes) ?: a as MoveCallableInterTypes
        if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
            if(firstTransformation.getParentNode().uuid == secondTransformation.getAddTransformation().getParentNode().uuid &&
                secondTransformation.getNode().name == firstTransformation.getNewName() &&
                secondTransformation.getNode().parameterTypes == firstTransformation.getNewParameters().types) {
                listOfConflicts.add(createConflict(a, b, "The class will have two callables with the same signature after applying both Transformation's", this))
            }
        } else {
            val commonAncestorClazz = commonAncestor.getClassOrInterfaceByUUID(secondTransformation.getAddTransformation().getParentNode().uuid)
            commonAncestorClazz?.let {
                if (commonAncestorClazz.methods.any { clazzMethod ->
                        clazzMethod.name == firstTransformation.getNewName() &&
                                clazzMethod.parameterTypes == firstTransformation.getNewParameters().types
                    }) {
                    listOfConflicts.add(createConflict(a, b,"Another callable has the same signature of this one after applying both Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = BodyChangedCallable::class
    override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as BodyChangedCallable
        val secondTransformation = b as BodyChangedCallable
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            !EqualsUuidVisitor(firstTransformation.getProject(), secondTransformation.getProject()).
            equals(firstTransformation.getNewBody(), secondTransformation.getNewBody())) {
            listOfConflicts.add(createConflict(a, b, "Both BodyChangedCallable Transformation's changes cannot be applied because they are different", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ModifiersChangedCallable::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedCallable::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ModifiersChangedCallable
        val secondTransformation = b as ModifiersChangedCallable
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            ModifierSet(firstTransformation.getNewModifiers()).isConflictiousWith(ModifierSet(secondTransformation.getNewModifiers()))) {
            listOfConflicts.add(createConflict(a, b, "Both ModifiersChangedCallable Transformation's changes are conflictious", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ReturnTypeChangedMethod::class
    override fun getSecond(): KClass<out Transformation> = ReturnTypeChangedMethod::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ReturnTypeChangedMethod
        val secondTransformation = b as ReturnTypeChangedMethod
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getNewReturnType() != secondTransformation.getNewReturnType()) {
            listOfConflicts.add(createConflict(a, b, "Both ReturnTypeChangedMethod Transformation's cannot be applied because the new return types are different", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveCallableInterTypes::class
    override fun getSecond(): KClass<out Transformation> = MoveCallableInterTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as MoveCallableInterTypes
        val secondTransformation = b as MoveCallableInterTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getAddTransformation().getParentNode().uuid !=
            secondTransformation.getAddTransformation().getParentNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The same callable is being moved to two different classes", this))
        }
    }
}
)