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
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = AddCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = AddCallableDeclaration::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = a as AddCallableDeclaration
                val secondTransformation = b as AddCallableDeclaration
                if(firstTransformation.getNode().signature == secondTransformation.getNode().signature) {
                    listOfConflicts.add(createConflict(a, b, "The two added callables have the same signature", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst():  KClass<out Transformation> = AddCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = ParametersAndOrNameChangedCallable::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? AddCallableDeclaration) ?: b as AddCallableDeclaration
                val secondTransformation = (b as? ParametersAndOrNameChangedCallable) ?: a as ParametersAndOrNameChangedCallable
                if(firstTransformation.getNode().name == secondTransformation.getNewName() &&
                   firstTransformation.getNode().parameterTypes == secondTransformation.getNewParameters().types) {
                    listOfConflicts.add(createConflict(a, b, "Both callable's signature become equal after applying both Transformation's", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = AddCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? AddCallableDeclaration) ?: b as AddCallableDeclaration
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
                if(firstTransformation.getNode().parentNode.get().uuid == secondTransformation.getAddTransformation().getNode().parentNode.get().uuid &&
                    firstTransformation.getNode().signature == secondTransformation.getNode().signature) {
                    listOfConflicts.add(createConflict(a, b, "The two added callables in the destiny class have the same signature", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = ParametersAndOrNameChangedCallable::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? RemoveCallableDeclaration) ?: b as RemoveCallableDeclaration
                val secondTransformation = (b as? ParametersAndOrNameChangedCallable) ?: a as ParametersAndOrNameChangedCallable
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
            override fun getFirst(): KClass<out Transformation> = RemoveCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = BodyChangedCallable::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? RemoveCallableDeclaration) ?: b as RemoveCallableDeclaration
                val secondTransformation = (b as? BodyChangedCallable) ?: a as BodyChangedCallable
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed callable is the one to have body changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = ModifiersChangedCallable::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? RemoveCallableDeclaration) ?: b as RemoveCallableDeclaration
                val secondTransformation = (b as? ModifiersChangedCallable) ?: a as ModifiersChangedCallable
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed callable is the one to have modifiers changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = ReturnTypeChangedMethod::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? RemoveCallableDeclaration) ?: b as RemoveCallableDeclaration
                val secondTransformation = (b as? ReturnTypeChangedMethod) ?: a as ReturnTypeChangedMethod
                if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict( a, b, "The removed method is the one to have return type changed", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = RemoveCallableDeclaration::class
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = (a as? RemoveCallableDeclaration) ?: b as RemoveCallableDeclaration
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
                if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The removed method is the one to be moved to another class", this))
                }
            }
        },
        object : ConflictType {
            override fun getFirst(): KClass<out Transformation> = ParametersAndOrNameChangedCallable::class
            override fun getSecond(): KClass<out Transformation> = ParametersAndOrNameChangedCallable::class

            override fun check(
                a: Transformation,
                b: Transformation,
                commonAncestor: Project,
                listOfConflicts: MutableSet<Conflict>
            ) {
                val firstTransformation = a as ParametersAndOrNameChangedCallable
                val secondTransformation = b as ParametersAndOrNameChangedCallable
                if (firstTransformation.getNode().parentNode.get().uuid == secondTransformation.getNode().parentNode.get().uuid) {
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
                        val commonAncestorClazz = commonAncestor.getClassOrInterfaceByUUID(firstTransformation.getNode().parentNode.get().uuid)
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
            override fun getFirst(): KClass<out Transformation> = ParametersAndOrNameChangedCallable::class
            override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

            override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
                val firstTransformation = (a as? ParametersAndOrNameChangedCallable) ?: b as ParametersAndOrNameChangedCallable
                val secondTransformation = (b as? MoveCallableInterClasses) ?: a as MoveCallableInterClasses
                if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                    if(firstTransformation.getNode().parentNode.get().uuid == secondTransformation.getAddTransformation().getNode().parentNode.get().uuid &&
                        secondTransformation.getNode().name == firstTransformation.getNewName() &&
                        secondTransformation.getNode().parameterTypes == firstTransformation.getNewParameters().types) {
                        listOfConflicts.add(createConflict(a, b, "The class will have two callables with the same signature after applying both Transformation's", this))
                    }
                } else {
                    val commonAncestorClazz = commonAncestor.getClassOrInterfaceByUUID(secondTransformation.getAddTransformation().getNode().parentNode.get().uuid)
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
                    !EqualsUuidVisitor.equals(firstTransformation.getNewBody(), secondTransformation.getNewBody())) {
                    listOfConflicts.add(createConflict( a, b, "Both BodyChangedCallable Transformation's changes cannot be applied because they are different", this))
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
                    firstTransformation.getAddTransformation().getNode().parentNode.get().uuid !=
                    secondTransformation.getAddTransformation().getNode().parentNode.get().uuid) {
                    listOfConflicts.add(createConflict(a, b, "The same callable is being moved to two different classes", this))
                }
            }
        }

    )

}
