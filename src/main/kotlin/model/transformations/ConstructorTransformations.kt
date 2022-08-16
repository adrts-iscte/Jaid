package model.transformations

import Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import model.generateUUID
import model.uuid

class AddConstructor(private val constructor : ConstructorDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val clazz = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val newConstructor = clazz.addConstructor(*constructor.modifiers.map { it.keyword }.toTypedArray())
        newConstructor.parameters = constructor.parameters
        newConstructor.body = constructor.body
        newConstructor.generateUUID()
    }

    override fun getNode(): Node {
        return constructor
    }

    override fun getText(): String {
        return "ADD CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
    }
}

class RemoveConstructor(private val constructor : ConstructorDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val clazz = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val constructorToRemove = clazz.constructors.find { it.uuid == constructor.uuid }!!
        clazz.remove(constructorToRemove)
    }

    override fun getNode(): Node {
        return constructor
    }

    override fun getText(): String {
        return "REMOVE CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
    }
}

class ModifiersChangedConstructor(private val constructor : ConstructorDeclaration, private val modifiers: NodeList<Modifier>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val constructorToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().constructors.find { it.uuid == constructor.uuid }!!
        constructorToChangeModifiers.modifiers = modifiers
    }

    override fun getNode(): Node {
        return constructor
    }

    override fun getText(): String {
        return "CHANGE MODIFIERS OF CONSTRUCTOR ${constructor.nameAsString} FROM ${constructor.modifiers} TO $modifiers"
    }
}

class BodyChangedConstructor(private val constructor : ConstructorDeclaration, private val newBody: BlockStmt) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val constructorToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().constructors.find { it.uuid == constructor.uuid }!!
        constructorToChangeModifiers.body = newBody
    }

    override fun getNode(): Node {
        return constructor
    }

    override fun getText(): String {
        return "CHANGE BODY OF CONSTRUCTOR ${constructor.nameAsString}"
    }

}
