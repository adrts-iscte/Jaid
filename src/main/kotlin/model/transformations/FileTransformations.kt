package model.transformations

import Transformation
import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import model.renameAllFieldUses
import model.uuid

class ChangePackage(private val packageDeclaration: String): Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        cu.setPackageDeclaration(packageDeclaration)
    }

    override fun getNode(): Node {
        return ClassOrInterfaceDeclaration()
    }

    override fun getText(): String {
        return "CHANGED PACKAGE TO $packageDeclaration"
    }
}

class ChangeImports(private val imports: NodeList<ImportDeclaration>): Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        cu.imports = imports
    }

    override fun getNode(): Node {
        return ClassOrInterfaceDeclaration()
    }

    override fun getText(): String {
        return "CHANGE IMPORTS"
    }
}

class AddClass(private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        cu.addClass(clazz.nameAsString, *clazz.modifiers.map { it.keyword }.toTypedArray())
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "ADD CLASS ${clazz.name}"
    }
}

class RemoveClass(private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToRemove = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }
        cu.remove(classToRemove)
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "REMOVE CLASS ${clazz.name}"
    }
}

class RenameClass(private val clazz : ClassOrInterfaceDeclaration, private val newName: String) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToRename = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        classToRename.setName(newName)
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "RENAME CLASS ${clazz.name} TO $newName"
    }
}

class ModifiersChangedClass(private val clazz : ClassOrInterfaceDeclaration, private val modifiers: NodeList<Modifier>) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveModifiersChanged = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        classToHaveModifiersChanged.modifiers = modifiers
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "CHANGE MODIFIERS OF CLASS ${clazz.name} FROM ${clazz.modifiers} TO $modifiers"
    }
}

class AddInterface(private val interfaze : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        cu.addClass(interfaze.nameAsString, *interfaze.modifiers.map { it.keyword }.toTypedArray())
    }

    override fun getNode(): Node {
        return interfaze
    }

    override fun getText(): String {
        return "ADD INTERFACE ${interfaze.name}"
    }
}

class RemoveInterface(private val interfaze : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToRemove = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == interfaze.uuid }
        cu.remove(classToRemove)
    }

    override fun getNode(): Node {
        return interfaze
    }

    override fun getText(): String {
        return "REMOVE INTERFACE ${interfaze.name}"
    }
}
