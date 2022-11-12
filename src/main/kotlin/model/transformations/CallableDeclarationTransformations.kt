package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.Type
import model.*

class AddCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableAdded = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val newCallable = callable.clone()
        classToHaveCallableAdded.addMember(newCallable)
//        newCallable.generateUUID()
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "ADD CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
        } else {
            "ADD METHOD ${(getNode() as MethodDeclaration).name}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { it is AddCallableDeclaration ||
                                                                         it is ParametersAndOrNameChangedCallable }
        filteredListOfTransformation.forEach {
            when(it) {
                is AddCallableDeclaration -> {
                    if(callable.signature == it.getNewCallable().signature) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@AddCallableDeclaration
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "The two added callables have the same signature"}
                        )
                    }
                }
                is ParametersAndOrNameChangedCallable -> {
                    if(callable.nameAsString == it.getNewName() &&
                        callable.parameterTypes == it.getNewParameters().types) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@AddCallableDeclaration
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewCallable() : CallableDeclaration<*> = callable
}

class RemoveCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableRemoved = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            val constructorToRemove = classToHaveCallableRemoved.constructors.find { it.uuid == constructor.uuid }!!
            classToHaveCallableRemoved.remove(constructorToRemove)
        } else {
            val method = callable as MethodDeclaration
            val methodToRemove = classToHaveCallableRemoved.methods.find { it.uuid == method.uuid }!!
            classToHaveCallableRemoved.remove(methodToRemove)
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "REMOVE CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
        } else {
            "REMOVE METHOD ${(getNode() as MethodDeclaration).name}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter {(it is ParametersAndOrNameChangedCallable ||
                                                                         it is BodyChangedCallable ||
                                                                         it is ModifiersChangedCallable ||
                                                                         it is ReturnTypeChangedMethod) &&
                                                                         callable.uuid == it.getNode().uuid}
        filteredListOfTransformation.forEach {
            val message = when(it) {
                is ParametersAndOrNameChangedCallable -> {
                    if(it.nameChanged() && it.parametersChanged()) {
                        "The removed method is the one to have parameters and name changed"
                    } else if(it.nameChanged()) {
                        "The removed method is the one to have name changed"
                    } else {
                        "The removed method is the one to have parameters changed"
                    }
                }
                is ModifiersChangedCallable -> {
                    "The removed callable is the one to have modifiers changed"
                }
                is ReturnTypeChangedMethod -> {
                    "The removed method is the one to have return type changed"
                }
                else -> {"The removed callable is the one to have body changed"}
            }
            listOfConflict.add(object : Conflict {
                override val first: Transformation get() = this@RemoveCallableDeclaration
                override val second: Transformation get() = it
                override val message: String
                    get() = message
                })
        }
        return listOfConflict
    }
}

class ParametersChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val newParameters: NodeList<Parameter>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            val constructorToChangeParameters = classToHaveCallableModified.constructors.find { it.uuid == constructor.uuid }!!
            constructorToChangeParameters.parameters = newParameters
        } else {
            val method = callable as MethodDeclaration
            val methodToChangeParameters = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
            methodToChangeParameters.parameters = newParameters
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE PARAMETERS OF CONSTRUCTOR ${constructor.nameAsString} TO $newParameters"
        } else {
            val method = callable as MethodDeclaration
            "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { it is AddCallableDeclaration ||
                                                                         it is RemoveCallableDeclaration ||
                                                                         it is ParametersChangedCallable ||
                                                                         it is RenameMethod }
        filteredListOfTransformation.forEach {
            when(it) {
                is AddCallableDeclaration -> {
                    if(callable.nameAsString == it.getNewCallable().nameAsString &&
                        newParameters.types == it.getNewCallable().parameterTypes) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
                is RemoveCallableDeclaration -> {
                    if (callable.uuid == it.getNode().uuid) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "The removed callable is the one to have parameters changed"
                        })
                    }
                }
                is ParametersChangedCallable -> {
                    if(callable.uuid == it.getNode().uuid && newParameters.types != it.getNewParameters().types) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Different modifications on the parameters of the same callable"}
                        )
                    } else if (callable.uuid != it.getNode().uuid && callable.nameAsString == (it.getNode() as CallableDeclaration<*>).nameAsString &&
                        newParameters.types == it.getNewParameters().types) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
                is RenameMethod -> {
                    val commonAncestorClazz = commonAncestor.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { clazz2 -> clazz2.uuid == clazz.uuid }!!
                    if(commonAncestorClazz.methods.any {
                                clazzMethod -> clazzMethod.nameAsString == it.getNewName() &&
                        newParameters.types == clazzMethod.parameterTypes
                    }) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewParameters() : NodeList<Parameter> = newParameters
}

class BodyChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val newBody: BlockStmt) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            val constructorToChangeBody = classToHaveCallableModified.constructors.find { it.uuid == constructor.uuid }!!
            constructorToChangeBody.body = newBody
        } else {
            val method = callable as MethodDeclaration
            val methodToChangeBody = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
            methodToChangeBody.setBody(newBody)
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "CHANGE BODY OF CONSTRUCTOR ${(callable as ConstructorDeclaration).nameAsString}"
        } else {
            "CHANGE BODY OF METHOD ${(callable as MethodDeclaration).nameAsString}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { it is RemoveCallableDeclaration ||
                                                                         it is BodyChangedCallable ||
                                                                         it is ReturnTypeChangedMethod }
        filteredListOfTransformation.forEach {
            when(it) {
                is RemoveCallableDeclaration -> {
                    if (callable.uuid == it.getNode().uuid) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@BodyChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "The removed callable is the one to have body changed"
                        })
                    }
                }
                is BodyChangedCallable -> {
                    if(callable.uuid == it.getNode().uuid && newBody != it.getNewBody()) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@BodyChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both BodyChangedCallable Transformation's changes cannot be applied because they are different"
                        })
                    }
                }
                is ReturnTypeChangedMethod -> {
//                    if(callable.uuid == it.getNode().uuid) {
//                        listOfConflict.add(object : Conflict {
//                            override val first: Transformation get() = this@BodyChangedCallable
//                            override val second: Transformation get() = it
//                            override val message: String
//                                get() = "Both BodyChangedCallable Transformation's changes cannot be applied because they are different"
//                        })
//                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewBody() : BlockStmt = newBody

}

class ModifiersChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val modifiers: NodeList<Modifier>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            val constructorToChangeModifiers = classToHaveCallableModified.constructors.find { it.uuid == constructor.uuid }!!
            constructorToChangeModifiers.modifiers =
                ModifierSet(constructorToChangeModifiers.modifiers).replaceModifiersBy(ModifierSet(modifiers)).toNodeList()
        } else {
            val method = callable as MethodDeclaration
            val methodToChangeModifiers = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
            methodToChangeModifiers.modifiers =
                ModifierSet(methodToChangeModifiers.modifiers).replaceModifiersBy(ModifierSet(modifiers)).toNodeList()
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE MODIFIERS OF CONSTRUCTOR ${constructor.nameAsString} FROM ${constructor.modifiers} TO $modifiers"
        } else {
            val method = callable as MethodDeclaration
            "CHANGE MODIFIERS OF METHOD ${method.nameAsString} FROM ${method.modifiers} TO $modifiers"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { (it is RemoveCallableDeclaration ||
                                                                         it is ModifiersChangedCallable) && callable.uuid == it.getNode().uuid }
        filteredListOfTransformation.forEach {
            when(it) {
                is RemoveCallableDeclaration -> {
                    listOfConflict.add(object : Conflict {
                        override val first: Transformation get() = this@ModifiersChangedCallable
                        override val second: Transformation get() = it
                        override val message: String
                            get() = "The removed callable is the one to have modifiers changed"
                    })
                }
                is ModifiersChangedCallable -> {
                    if (ModifierSet(modifiers).isConflictiousWith(ModifierSet(it.getNewModifiers()))) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@ModifiersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both ModifiersChangedCallable Transformation's changes are conflictious"
                        })
                    } else {
                        mergeModifiersWith(it)
                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewModifiers() : NodeList<Modifier> = modifiers
    fun setNewModifiers(newModifiers : NodeList<Modifier>) {
        modifiers.clear()
        modifiers.addAll(newModifiers)
    }

    private fun mergeModifiersWith(other : ModifiersChangedCallable) {
        val mergedModifiers = ModifierSet(modifiers).merge(ModifierSet(other.getNewModifiers()))
        setNewModifiers(mergedModifiers)
        other.setNewModifiers(mergedModifiers)
    }
}

class ReturnTypeChangedMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration, private val newType: Type) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val methodToChangeReturnType = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
        methodToChangeReturnType.type = newType
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE RETURN TYPE OF METHOD ${method.nameAsString} FROM ${method.type} TO $newType"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { (it is RemoveCallableDeclaration ||
                                                                         it is BodyChangedCallable ||
                                                                         it is ReturnTypeChangedMethod) &&
                                                                         method.uuid == it.getNode().uuid }
        filteredListOfTransformation.forEach {
            when(it) {
                is RemoveCallableDeclaration -> {
                    listOfConflict.add(object : Conflict {
                        override val first: Transformation get() = this@ReturnTypeChangedMethod
                        override val second: Transformation get() = it
                        override val message: String
                            get() = "The removed method is the one to have return type changed"
                    })
                }
                is BodyChangedCallable -> {
//                    if(newBody != it.getNewBody()) {
//                        listOfConflict.add(object : Conflict {
//                            override val first: Transformation get() = this@BodyChangedCallable
//                            override val second: Transformation get() = it
//                            override val message: String
//                                get() = "Both BodyChangedCallable Transformation's changes cannot be applied because they are different"
//                        })
//                    }
                }
                is ReturnTypeChangedMethod -> {
                    if(newType != it.getNewReturnType()) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@ReturnTypeChangedMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both ReturnTypeChangedMethod Transformation's cannot be applied because the new return types are different"
                        })
                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewReturnType() : Type = newType

}

class RenameMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration, private val newName: String) :
    Transformation {
    private val oldMethodName: String = method.nameAsString

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveMethodRenamed = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val methodToRename = classToHaveMethodRenamed.methods.find { it.uuid == method.uuid }!!
        renameAllMethodCalls(cu, methodToRename, newName)
        methodToRename.setName(newName)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "RENAME METHOD $oldMethodName TO $newName"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { it is AddCallableDeclaration ||
                                                                         it is RemoveCallableDeclaration ||
                                                                         it is ParametersChangedCallable ||
                                                                         it is RenameMethod }
        filteredListOfTransformation.forEach {
            when(it) {
                is AddCallableDeclaration -> {
                    if(newName == it.getNewCallable().nameAsString && method.parameterTypes == it.getNewCallable().parameterTypes) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@RenameMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both method's signature become equal after applying both Transformation's"}
                        )
                    }
                }
                is RemoveCallableDeclaration -> {
                    if (method.uuid == it.getNode().uuid) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@RenameMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "The removed method is the one to have name changed"
                        })
                    }
                }
                is ParametersChangedCallable -> {
                    val commonAncestorClazz = commonAncestor.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { clazz2 -> clazz2.uuid == clazz.uuid }!!
                    if (commonAncestorClazz.methods.any {
                                clazzMethod -> clazzMethod.nameAsString == newName &&
                                it.getNewParameters().types == clazzMethod.parameterTypes
                        }) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@RenameMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
                is RenameMethod -> {
                    if(method.uuid != it.getNode().uuid &&
                        newName == it.getNewName() && method.parameterTypes == (it.getNode() as MethodDeclaration).parameterTypes) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@RenameMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both method's signature become equal after applying both RenameMethod Transformation's"}
                        )
                    } else if (method.uuid == it.getNode().uuid && newName != it.getNewName()) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@RenameMethod
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Different new names for the same method after applying both RenameMethod Transformation's"}
                        )
                    }
                }
            }
        }
        return listOfConflict
    }

    fun getNewName() : String = newName

}

class ParametersAndOrNameChangedCallable(private val clazz : ClassOrInterfaceDeclaration,
                                         private val callable : CallableDeclaration<*>,
                                         private val newParameters: NodeList<Parameter>,
                                         private val newName: String) : Transformation {
    private val oldCallableParameters: NodeList<Parameter> = callable.parameters
    private val oldMethodName: String = callable.nameAsString

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            val constructorToBeChanged = classToHaveCallableModified.constructors.find { it.uuid == constructor.uuid }!!
            constructorToBeChanged.parameters = newParameters
        } else {
            val method = callable as MethodDeclaration
            val methodToBeChanged = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
            methodToBeChanged.parameters = newParameters
            renameAllMethodCalls(cu, methodToBeChanged, newName)
            methodToBeChanged.setName(newName)
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE PARAMETERS OF CONSTRUCTOR ${constructor.nameAsString} TO $newParameters"
        } else {
            val method = callable as MethodDeclaration
            if (parametersChanged() && nameChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters AND RENAME TO $newName"
            } else if (parametersChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters"
            } else {
                "RENAME METHOD $oldMethodName TO $newName"
            }
        }

    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        val listOfConflict = mutableListOf<Conflict>()
        val filteredListOfTransformation = listOfTransformation.filter { it is AddCallableDeclaration ||
                                                                         it is RemoveCallableDeclaration ||
                                                                         it is ParametersAndOrNameChangedCallable }
        filteredListOfTransformation.forEach {
            when(it) {
                is AddCallableDeclaration -> {
                    if(newName == it.getNewCallable().nameAsString &&
                        newParameters.types == it.getNewCallable().parameterTypes) {
                        listOfConflict.add(
                            createConflict(this@ParametersAndOrNameChangedCallable, it,
                                "Both callable's signature become equal after applying both Transformation's")
                        )
                    }
                }
                is RemoveCallableDeclaration -> {
                    if (callable.uuid == it.getNode().uuid) {
                        val message = if(nameChanged() && parametersChanged()) {
                            "The removed method is the one to have parameters and name changed"
                        } else if(nameChanged()) {
                            "The removed method is the one to have name changed"
                        } else {
                            "The removed method is the one to have parameters changed"
                        }
                        listOfConflict.add(
                            createConflict(this@ParametersAndOrNameChangedCallable, it, message)
                        )
                    }
                }
                is ParametersAndOrNameChangedCallable -> {
                    if(callable.uuid == it.getNode().uuid) {
                        if (parametersChanged() && it.parametersChanged() && newParameters != it.getNewParameters()) {
                            listOfConflict.add(
                                createConflict(this@ParametersAndOrNameChangedCallable, it,
                                    "Different modifications on the parameters of the same callable")
                            )
                        }
                        if (nameChanged() && it.nameChanged() && newName != it.getNewName()) {
                            listOfConflict.add(
                                createConflict(this@ParametersAndOrNameChangedCallable, it,
                                        "Different new names for the same method after applying both Transformation's")
                            )
                        }
                    } else {
                        if (signature == it.signature) {
                            listOfConflict.add(
                                createConflict(this@ParametersAndOrNameChangedCallable, it,
                                    "Both callable's signature become equal after applying both Transformation's")
                            )
                        }
                    }
                    val commonAncestorClazz = commonAncestor.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { clazz2 -> clazz2.uuid == clazz.uuid }!!
                    val onlyParametersAndOrNameChangedCallable = listOfTransformation.filterIsInstance<ParametersAndOrNameChangedCallable>()
                    if(nameChanged() && it.parametersTypesChanged()) {
                        val foundMethods = commonAncestorClazz.methods.filter { clazzMethod ->
                                clazzMethod.nameAsString == newName &&
                                it.getNewParameters().types == clazzMethod.parameterTypes
                        }
                        foundMethods.forEach {
                            onlyParametersAndOrNameChangedCallable.any {
                                    filteredTransformation -> filteredTransformation.getNode().uuid == it.uuid && !filteredTransformation.signatureChanged()
                            }
                        }
                    } else if (parametersTypesChanged() && it.nameChanged()) {
                        if(commonAncestorClazz.methods.any {
                                            clazzMethod -> clazzMethod.nameAsString == it.getNewName() &&
                                            newParameters.types == clazzMethod.parameterTypes
                                    }) {
                            listOfConflict.add(
                                createConflict(this@ParametersAndOrNameChangedCallable, it,
                                    "Both callable's signature become equal after applying all Transformation's")
                            )
                        }
                    }
                }
            }
        }
        return listOfConflict
    }

    val signature:String
        get() = "$newName${newParameters.types}"

    fun signatureChanged() : Boolean = nameChanged() || parametersTypesChanged()

    fun nameChanged() : Boolean = oldMethodName != newName
    fun parametersChanged() : Boolean = oldCallableParameters != newParameters
    fun parametersTypesChanged() : Boolean = oldCallableParameters.types != newParameters.types
    fun getNewParameters() : NodeList<Parameter> = newParameters
    fun getNewName() : String = newName
}

