package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.Type
import model.Conflict
import model.generateUUID
import model.renameAllMethodCalls
import model.uuid

class AddCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveCallableAdded = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if(callable.isConstructorDeclaration) {
            val newConstructor = classToHaveCallableAdded.addConstructor(*callable.modifiers.map { it.keyword }.toTypedArray())
            newConstructor.parameters = callable.parameters
            newConstructor.body = (callable as ConstructorDeclaration).body
            newConstructor.setComment(callable.comment.orElse(null))
            newConstructor.generateUUID()
        } else {
            val method = callable as MethodDeclaration
            val newMethod = classToHaveCallableAdded.addMethod(method.nameAsString, *method.modifiers.map { it.keyword }.toTypedArray())
            newMethod.type = method.type
            newMethod.parameters = method.parameters
            newMethod.setBody(method.body.get())
            newMethod.setComment(callable.comment.orElse(null))
            newMethod.generateUUID()
        }
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
                                                                         it is ParametersChangedCallable ||
                                                                         it is RenameMethod }
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
                is ParametersChangedCallable -> {
                    if(callable.nameAsString == (it.getNode() as CallableDeclaration<*>).nameAsString &&
                        callable.parameters == it.getNewParameters()) {
                        listOfConflict.add(object : Conflict {
                            override val first: Transformation get() = this@AddCallableDeclaration
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both callable's signature become equal after applying both Transformation's"}
                        )
                    }
                }
                is RenameMethod -> {
                    if(callable.nameAsString == it.getNewName() && callable.parameters == (it.getNode() as MethodDeclaration).parameters) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@AddCallableDeclaration
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Both method's signature become equal after applying both Transformation's"}
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
        val filteredListOfTransformation = listOfTransformation.filter {(it is ParametersChangedCallable ||
                                                                         it is BodyChangedCallable ||
                                                                         it is ModifiersChangedCallable ||
                                                                         it is ReturnTypeChangedMethod ||
                                                                         it is RenameMethod) &&
                                                                         callable.uuid == it.getNode().uuid}
        filteredListOfTransformation.forEach {
            val message = when(it) {
                is ParametersChangedCallable -> {
                    "The removed callable is the one to have parameters changed"
                }
                is BodyChangedCallable -> {
                    "The removed callable is the one to have body changed"
                }
                is ModifiersChangedCallable -> {
                    "The removed callable is the one to have modifiers changed"
                }
                is ReturnTypeChangedMethod -> {
                    "The removed method is the one to have return type changed"
                }
                else -> {"The removed method is the one to have name changed"}
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
                        newParameters == it.getNewCallable().parameters) {
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
                    if(callable.uuid == it.getNode().uuid && newParameters != it.getNewParameters()) {
                        listOfConflict.add(object : Conflict{
                            override val first: Transformation get() = this@ParametersChangedCallable
                            override val second: Transformation get() = it
                            override val message: String
                                get() = "Different modifications on the parameters of the same callable"}
                        )
                    } else if (callable.uuid != it.getNode().uuid && callable.nameAsString == (it.getNode() as CallableDeclaration<*>).nameAsString &&
                        newParameters == it.getNewParameters()) {
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
                        newParameters == clazzMethod.parameters
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
            constructorToChangeModifiers.modifiers = modifiers
        } else {
            val method = callable as MethodDeclaration
            val methodToChangeModifiers = classToHaveCallableModified.methods.find { it.uuid == method.uuid }!!
            methodToChangeModifiers.modifiers = modifiers
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
                    listOfConflict.add(object : Conflict {
                        override val first: Transformation get() = this@ModifiersChangedCallable
                        override val second: Transformation get() = it
                        override val message: String
                            get() = "Both BodyChangedCallable Transformation's changes cannot be applied because they are different"
                    })
                }
            }
        }
        return listOfConflict
    }

    fun getNewModifiers() : NodeList<Modifier> = modifiers
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
                    if(newName == it.getNewCallable().nameAsString && method.parameters == it.getNewCallable().parameters) {
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
                                it.getNewParameters() == clazzMethod.parameters
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
                        newName == it.getNewName() && method.parameters == (it.getNode() as MethodDeclaration).parameters) {
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

