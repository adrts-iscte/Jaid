package model

import model.transformations.*
import model.visitors.EqualsUuidVisitor
import kotlin.reflect.KClass

interface Conflict {
    val first : Transformation
    val second : Transformation
    fun getConflictType() : ConflictType
    val message : String
}

fun createConflict(first: Transformation, second: Transformation, message: String, conflictType: ConflictType) : Conflict {
    return object : Conflict {
        override val first: Transformation get() = first
        override val second: Transformation get() = second
        override fun getConflictType(): ConflictType = conflictType
        override val message: String
            get() = message
    }
}

interface ConflictType {
    fun getFirst() : KClass<out Transformation>
    fun getSecond() : KClass<out Transformation>

    fun verifyIfExistsConflict(a: Transformation, b: Transformation,
                               commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        require(applicable(a, b))
        if (a::class != getFirst() || b::class != getSecond()) {
            return verifyIfExistsConflict(b, a, commonAncestor, listOfConflicts)
        }
        check(a, b, commonAncestor, listOfConflicts)
    }

    fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>)

    fun applicable(a: Transformation, b : Transformation) : Boolean {
        return (a::class == getFirst() && b::class == getSecond()) || (b::class == getFirst() && a::class == getSecond())
    }
}

object ConflictTypeLibrary {

    fun applicableConflict(a: Transformation, b : Transformation) : ConflictType? {
        return allConflictTypes.find { it.applicable(a, b) }
    }

    fun getConflictTypeByKClasses(a: KClass<out Transformation>, b : KClass<out Transformation>) : ConflictType {
        return allConflictTypes.find { (a == it.getFirst() && b == it.getSecond()) ||
                (b == it.getFirst() && a == it.getSecond())  }!!
    }

    private val allConflictTypes = listOf(

        /* CallableCallable */

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
            override fun getFirst():  KClass<out Transformation> = AddCallable::class
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
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? AddCallable) ?: b as AddCallable
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
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
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveCallable) ?: b as RemoveCallable
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
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
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? SignatureChanged) ?: b as SignatureChanged
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
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
            override fun getFirst(): KClass<out Transformation> = MoveCallableInterClasses::class
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as MoveCallableInterClasses
                val secondTransformation = b as MoveCallableInterClasses
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    firstTransformation.getAddTransformation().getParentNode().uuid !=
                    secondTransformation.getAddTransformation().getParentNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The same callable is being moved to two different classes", this))
                }
            }
        },

        /* FieldField */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveFieldInterClasses::class
            override fun getSecond(): KClass<out Transformation> = AddField::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveFieldInterClasses) ?: b as MoveFieldInterClasses
                val secondTransformation = (b as? AddField) ?: a as AddField
                if(secondTransformation.getParentNode().uuid == firstTransformation.getAddTransformation().getParentNode().uuid &&
                    firstTransformation.getNode().name == secondTransformation.getNode().name) {
                    listOfConflicts.add(createConflict(a, b, "The two added fields in the destiny class have the same name", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveField::class
            override fun getSecond(): KClass<out Transformation> = MoveFieldInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveField) ?: b as RemoveField
                val secondTransformation = (b as? MoveFieldInterClasses) ?: a as MoveFieldInterClasses
                if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The removed field is the one to be moved to another class", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RenameField::class
            override fun getSecond(): KClass<out Transformation> = MoveFieldInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RenameField) ?: b as RenameField
                val secondTransformation = (b as? MoveFieldInterClasses) ?: a as MoveFieldInterClasses
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
            override fun getFirst(): KClass<out Transformation> = MoveFieldInterClasses::class
            override fun getSecond(): KClass<out Transformation> = MoveFieldInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as MoveFieldInterClasses
                val secondTransformation = b as MoveFieldInterClasses
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    firstTransformation.getAddTransformation().getParentNode().uuid !=
                    secondTransformation.getAddTransformation().getParentNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The same field is being moved to two different classes", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst():  KClass<out Transformation> = AddField::class
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
                    !EqualsUuidVisitor(firstTransformation.getProject(), secondTransformation.getProject()).
                        equals(firstTransformation.getNewInitializer(), secondTransformation.getNewInitializer())) {
                    listOfConflicts.add(createConflict( a, b, "Both InitializerChangedField Transformation's changes cannot be applied because they are different", this))
                }
            }
        },

        /* FileFile */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
            override fun getSecond(): KClass<out Transformation> = AddClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
                val secondTransformation = (b as? AddClassOrInterface) ?: a as AddClassOrInterface
                if (firstTransformation.getNode().name == secondTransformation.getNode().name &&
                    firstTransformation.getAddTransformation().getParentNode().correctPath == secondTransformation.getParentNode().correctPath) {
                    listOfConflicts.add(createConflict( a, b, "The two added types in the destiny file have the same name", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to be moved to another file", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
            override fun getSecond(): KClass<out Transformation> = RenameClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
                val secondTransformation = (b as? RenameClassOrInterface) ?: a as RenameClassOrInterface
                if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                    if(firstTransformation.getAddTransformation().getParentNode().correctPath == secondTransformation.getParentNode().correctPath &&
                        firstTransformation.getNode().name == secondTransformation.getNewName()) {
                        listOfConflicts.add(createConflict(a, b, "The file will have two types with the same name after applying both Transformation's", this))
                    }
                } else {
                    val commonAncestorFile = commonAncestor.getCompilationUnitByPath(firstTransformation.getAddTransformation().getParentNode().correctPath)
                    commonAncestorFile?.let {
                        if (commonAncestorFile.types.any { clazzMethod ->
                                clazzMethod.name == secondTransformation.getNewName()
                            }) {
                            listOfConflicts.add(createConflict(a, b,"Another type has the same name of this one after applying both Transformation's", this))
                        }
                    }
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
            override fun getSecond(): KClass<out Transformation> = MoveTypeInterFiles::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as MoveTypeInterFiles
                val secondTransformation = b as MoveTypeInterFiles
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    firstTransformation.getAddTransformation().getParentNode().correctPath != secondTransformation.getAddTransformation().getParentNode().correctPath) {
                    listOfConflicts.add(createConflict( a, b, "The same type is being moved to two different files", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ChangePackage::class
            override fun getSecond(): KClass<out Transformation> = ChangePackage::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as ChangePackage
                val secondTransformation = b as ChangePackage
                if (firstTransformation.getNode().storage.get().fileName == secondTransformation.getNode().storage.get().fileName &&
                    !arePackageDeclarationEqual(firstTransformation.getNewPackage(), secondTransformation.getNewPackage())) {
                    listOfConflicts.add(createConflict( a, b, "The same file's package is being changed to two different packages", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ChangeImports::class
            override fun getSecond(): KClass<out Transformation> = ChangeImports::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as ChangeImports
                val secondTransformation = b as ChangeImports
                if (firstTransformation.getImportsList() != secondTransformation.getImportsList() &&
                    firstTransformation.getNode().correctPath == secondTransformation.getNode().correctPath) {
                    listOfConflicts.add(createConflict( a, b, "The same file's imports are being changed to two different import lists", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst():  KClass<out Transformation> = AddClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = RenameClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? AddClassOrInterface) ?: b as AddClassOrInterface
                val secondTransformation = (b as? RenameClassOrInterface) ?: a as RenameClassOrInterface
                if(firstTransformation.getNode().name == secondTransformation.getNewName() &&
                    firstTransformation.getParentNode().correctPath == secondTransformation.getParentNode().correctPath) {
                    listOfConflicts.add(createConflict(a, b, "Both type's name become equal after applying both Transformation's", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = AddClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = AddClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as AddClassOrInterface
                val secondTransformation = b as AddClassOrInterface
                if(firstTransformation.getParentNode().correctPath == secondTransformation.getParentNode().correctPath &&
                    firstTransformation.getNode().name == secondTransformation.getNode().name) {
                    listOfConflicts.add(createConflict(a, b, "The two added types have the same name", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = ChangeExtendedTypes::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveClassOrInterface) ?: b as RemoveClassOrInterface
                val secondTransformation = (b as? ChangeExtendedTypes) ?: a as ChangeExtendedTypes
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed type is the one to have extended types changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = ChangeImplementsTypes::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveClassOrInterface) ?: b as RemoveClassOrInterface
                val secondTransformation = (b as? ChangeImplementsTypes) ?: a as ChangeImplementsTypes
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed type is the one to have implements types changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = ModifiersChangedClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveClassOrInterface) ?: b as RemoveClassOrInterface
                val secondTransformation = (b as? ModifiersChangedClassOrInterface) ?: a as ModifiersChangedClassOrInterface
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed type is the one to have modifiers changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = RenameClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveClassOrInterface) ?: b as RemoveClassOrInterface
                val secondTransformation = (b as? RenameClassOrInterface) ?: a as RenameClassOrInterface
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed type is the one to have name changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RenameClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = RenameClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as RenameClassOrInterface
                val secondTransformation = b as RenameClassOrInterface
                if (firstTransformation.getParentNode().correctPath == secondTransformation.getParentNode().correctPath) {
                    if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                        if (firstTransformation.getNewName() == secondTransformation.getNewName()) {
                            listOfConflicts.add(createConflict(a, b, "Both type's name become equal after applying both Transformation's",this))
                        }
                    } else {
                        if (firstTransformation.getNewName() != secondTransformation.getNewName()) {
                            listOfConflicts.add(createConflict(a, b, "Different new names for the same type after applying both RenameMethod Transformation's", this))
                        }
                    }
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ModifiersChangedClassOrInterface::class
            override fun getSecond(): KClass<out Transformation> = ModifiersChangedClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as ModifiersChangedClassOrInterface
                val secondTransformation = b as ModifiersChangedClassOrInterface
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    ModifierSet(firstTransformation.getNewModifiers()).isConflictiousWith(ModifierSet(secondTransformation.getNewModifiers()))) {
                    listOfConflicts.add(createConflict(a, b, "Both ModifiersChangedClassOrInterface Transformation's changes are conflictious", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ChangeImplementsTypes::class
            override fun getSecond(): KClass<out Transformation> = ChangeImplementsTypes::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as ChangeImplementsTypes
                val secondTransformation = b as ChangeImplementsTypes
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    firstTransformation.getNewImplementsTypes() != secondTransformation.getNewImplementsTypes()) {
                    listOfConflicts.add(createConflict(a, b, "Both lists of implements types are different", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ChangeExtendedTypes::class
            override fun getSecond(): KClass<out Transformation> = ChangeExtendedTypes::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = a as ChangeExtendedTypes
                val secondTransformation = b as ChangeExtendedTypes
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
                    firstTransformation.getNewExtendedTypes() != secondTransformation.getNewExtendedTypes()) {
                    listOfConflicts.add(createConflict(a, b, "Both lists of extended types are different", this))
                }
            }
        },

        /* JavadocJavadoc */

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
        },

        /* CallableJavadoc */

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
        },

        /* FieldJavadoc */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
            override fun getSecond(): KClass<out Transformation> = RemoveField::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? SetJavaDoc) ?: b as SetJavaDoc
                val secondTransformation = (b as? RemoveField) ?: a as RemoveField
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed field is the one to have javadoc added/changed", this))
                }
            }
        },

        /* FileJavadoc */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = SetJavaDoc::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? SetJavaDoc) ?: b as SetJavaDoc
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have javadoc added/changed", this))
                }
            }
        },

        /* CallableFile */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = AddCallable::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? AddCallable) ?: b as AddCallable
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have callable added", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = SignatureChanged::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? SignatureChanged) ?: b as SignatureChanged
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with parameters and/or name changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = BodyChangedCallable::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? BodyChangedCallable) ?: b as BodyChangedCallable
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with body changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ModifiersChangedCallable::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? ModifiersChangedCallable) ?: b as ModifiersChangedCallable
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a callable with modifiers changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ReturnTypeChangedMethod::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? ReturnTypeChangedMethod) ?: b as ReturnTypeChangedMethod
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a method with return type changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveCallableInterClasses::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveCallableInterClasses) ?: b as MoveCallableInterClasses
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as MoveCallableInterClasses
                if (firstTransformation.getAddTransformation().getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the destiny class for where the callable is being moved", this))
                }
            }
        },

        /* FieldFile */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = AddField::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? AddField) ?: b as AddField
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have field added", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RenameField::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RenameField) ?: b as RenameField
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field renamed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = InitializerChangedField::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? InitializerChangedField) ?: b as InitializerChangedField
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with initializer changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ModifiersChangedField::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? ModifiersChangedField) ?: b as ModifiersChangedField
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with modifiers changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = TypeChangedField::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? TypeChangedField) ?: b as TypeChangedField
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as RemoveClassOrInterface
                if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the one to have a field with type changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = MoveFieldInterClasses::class
            override fun getSecond(): KClass<out Transformation> = RemoveClassOrInterface::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? MoveFieldInterClasses) ?: b as MoveFieldInterClasses
                val secondTransformation = (b as? RemoveClassOrInterface) ?: a as MoveCallableInterClasses
                if (firstTransformation.getAddTransformation().getParentNode().uuid == secondTransformation.getNode().uuid){
                    listOfConflicts.add(createConflict(a, b, "The removed type is the destiny class for where the field is being moved", this))
                }
            }
        },

        /* CallableField */

        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveField::class
            override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? RemoveField) ?: b as RemoveField
                val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
                if (getNodeReferencesToReferencedNode(secondTransformation.getProject(), firstTransformation.getNode(), secondTransformation.getNewBody()).isNotEmpty()){
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
//        },

    )

}
