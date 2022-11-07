package model.transformations

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.ClassOrInterfaceType
import model.Conflict
import model.generateUUID
import model.renameAllConstructorCalls
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

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
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

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class AddClassOrInterface(private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val newAddedClassOrInterface = if (!clazz.isInterface) {
            cu.addClass(clazz.nameAsString, *clazz.modifiers.map { it.keyword }.toTypedArray())
        } else {
            cu.addInterface(clazz.nameAsString, *clazz.modifiers.map { it.keyword }.toTypedArray())
        }
        newAddedClassOrInterface.setComment(clazz.comment.orElse(null))
        newAddedClassOrInterface.generateUUID()
        clazz.members.forEach {
            val newClonedMember = it.clone()
            newAddedClassOrInterface.addMember(newClonedMember)
            newClonedMember.generateUUID()
        }
        clazz.orphanComments.forEach {
            when(it) {
                is LineComment -> newAddedClassOrInterface.addOrphanComment(LineComment(it.content))
                is BlockComment -> newAddedClassOrInterface.addOrphanComment(BlockComment(it.content))
            }
        }
        newAddedClassOrInterface.implementedTypes = clazz.implementedTypes
        newAddedClassOrInterface.extendedTypes = clazz.extendedTypes
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return if (clazz.isInterface) {
            "ADD INTERFACE ${clazz.nameAsString}"
        } else {
            "ADD CLASS ${clazz.nameAsString}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class RemoveClassOrInterface(private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToRemove = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }
        cu.remove(classToRemove)
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return if (clazz.isInterface) {
            "REMOVE INTERFACE ${clazz.nameAsString}"
        } else {
            "REMOVE CLASS ${clazz.nameAsString}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class RenameClass(private val clazz : ClassOrInterfaceDeclaration, private val newName: String) : Transformation {
    private val oldClassOrInterfaceName: String = clazz.nameAsString

    override fun applyTransformation(cu: CompilationUnit) {
        val classToRename = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        renameAllConstructorCalls(cu, clazz, newName)
        classToRename.name = SimpleName(newName)
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return if (clazz.isInterface) {
            "RENAME INTERFACE $oldClassOrInterfaceName TO $newName"
        } else {
            "RENAME CLASS $oldClassOrInterfaceName TO $newName"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class ModifiersChangedClass(private val clazz : ClassOrInterfaceDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveModifiersChanged = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        classToHaveModifiersChanged.modifiers = modifiers
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "CHANGE MODIFIERS OF CLASS ${clazz.nameAsString} FROM ${clazz.modifiers} TO $modifiers"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class ChangeImplementsTypes(private val clazz : ClassOrInterfaceDeclaration, private val implements: NodeList<ClassOrInterfaceType>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToBeModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        clazz.implementedTypes.forEach {
            classToBeModified.addImplementedType(it.nameAsString)
        }
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "CHANGE IMPLEMENTS TYPES OF CLASS ${clazz.nameAsString}"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class ChangeExtendedTypes(private val clazz : ClassOrInterfaceDeclaration, private val extends: NodeList<ClassOrInterfaceType>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToBeModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        clazz.extendedTypes.forEach {
            classToBeModified.addExtendedType(it.nameAsString)
        }
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "CHANGE EXTENDS TYPES OF CLASS ${clazz.nameAsString}"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

