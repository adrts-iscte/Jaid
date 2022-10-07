package model.transformations

import Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.Type
import model.generateUUID
import model.renameAllMethodCalls
import model.uuid

class AddCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) : Transformation {

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
}

class RemoveCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) : Transformation {

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
}

class ParametersChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val newParameters: NodeList<Parameter>) : Transformation {

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
}

class BodyChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val newBody: BlockStmt) : Transformation {

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
}

class ModifiersChangedCallable(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val modifiers: NodeList<Modifier>) : Transformation {

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
}

class ReturnTypeChangedMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration, private val newType: Type) : Transformation {

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

}

class RenameMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration, private val newName: String) : Transformation {
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
}

