package model.transformations

import Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.Type
import model.renameAllMethodCalls
import model.setUUID
import model.uuid


class AddMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveMethodAdded = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val newMethod = clazz.addMethod(method.nameAsString, *method.modifiers.map { it.keyword }.toTypedArray())
        newMethod.type = method.type
        newMethod.parameters = method.parameters
        newMethod.setBody(method.body.get())
        classToHaveMethodAdded.members.add(newMethod)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "ADD METHOD ${(getNode() as MethodDeclaration).name}"
    }
}

class RemoveMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveMethodRemoved = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val methodToRemove = classToHaveMethodRemoved.methods.find { it.uuid == method.uuid }!!
        classToHaveMethodRemoved.remove(methodToRemove)
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

class ParametersChangedMethod(private val method : MethodDeclaration, private val newParameters: NodeList<Parameter>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
        methodToChangeModifiers.parameters = newParameters
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters"
    }

}

/*
class ParameterAddedToMethod(private val method : MethodDeclaration, private val parameter: IndexedValue<Parameter>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
//        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
//        methodToChangeModifiers.setBody(newBody)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "PARAMETER $parameter ADDED TO METHOD ${method.nameAsString}"
    }

}

class ParameterRemovedFromMethod(private val method : MethodDeclaration, private val parameter: IndexedValue<Parameter>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
//        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
//        methodToChangeModifiers.setBody(newBody)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "PARAMETER $parameter REMOVED FROM METHOD ${method.nameAsString}"
    }

}

class ParameterChangedOnMethod(private val method : MethodDeclaration, private val parameter: Parameter) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
//        val methodToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().methods.find { it.uuid == method.uuid }!!
//        methodToChangeModifiers.setBody(newBody)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "PARAMETER $parameter CHANGED ON METHOD ${method.nameAsString}"
    }

}
*/