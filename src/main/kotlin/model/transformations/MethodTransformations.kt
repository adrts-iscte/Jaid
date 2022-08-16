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


class AddMethod(private val method : MethodDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val clazz = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val newMethod = clazz.addMethod(method.nameAsString, *method.modifiers.map { it.keyword }.toTypedArray())
        newMethod.type = method.type
        newMethod.parameters = method.parameters
        newMethod.setBody(method.body.get())
        newMethod.generateUUID()
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "ADD METHOD ${(getNode() as MethodDeclaration).name}"
    }
}

class RemoveMethod(private val method : MethodDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val clazz = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val methodToRemove = clazz.methods.find { it.uuid == method.uuid }!!
        clazz.remove(methodToRemove)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "REMOVE METHOD ${(getNode() as MethodDeclaration).name}"
    }
}

class RenameMethod(private val method : MethodDeclaration, private val newName: String) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val methodToRename = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
        renameAllMethodCalls(cu, methodToRename, newName)
        methodToRename.setName(newName)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "RENAME METHOD ${(getNode() as MethodDeclaration).name} TO $newName"
    }
}

class ModifiersChangedMethod(private val method : MethodDeclaration, private val modifiers: NodeList<Modifier>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
        methodToChangeModifiers.modifiers = modifiers
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE MODIFIERS OF METHOD ${method.nameAsString} FROM ${method.modifiers} TO $modifiers"
    }
}

class ReturnTypeChangedMethod(private val method : MethodDeclaration, private val newType: Type) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
        methodToChangeModifiers.type = newType
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE RETURN TYPE OF METHOD ${method.nameAsString} FROM ${method.type} TO $newType"
    }

}

class BodyChangedMethod(private val method : MethodDeclaration, private val newBody: BlockStmt) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
        methodToChangeModifiers.setBody(newBody)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE BODY OF METHOD ${method.nameAsString}"
    }

}