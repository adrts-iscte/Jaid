package model.transformations

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.ClassOrInterfaceType
import model.*

class ChangePackage(private val compilationUnit: CompilationUnit, private val packageDeclaration: String): Transformation {

    override fun applyTransformation(proj: Project) {
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.setPackageDeclaration(packageDeclaration)
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

class ChangeImports(private val compilationUnit: CompilationUnit, private val imports: NodeList<ImportDeclaration>): Transformation {

    override fun applyTransformation(proj: Project) {
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.imports = imports
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

class AddClassOrInterface(private val compilationUnit: CompilationUnit, private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(proj: Project) {
        val compilationUnitToHaveClassOrInterfaceAdded = proj.getCompilationUnitByPath(compilationUnit.correctPath)
        compilationUnitToHaveClassOrInterfaceAdded?.let {
            val newAddedClassOrInterface = clazz.clone()
            val index = calculateIndexOfTypeToAdd(compilationUnit, compilationUnitToHaveClassOrInterfaceAdded, clazz.uuid)
            compilationUnitToHaveClassOrInterfaceAdded.types.add(index, newAddedClassOrInterface)
        }
//        /*
//        val newAddedClassOrInterface = if (!clazz.isInterface) {
//            cu.addClass(clazz.nameAsString, *clazz.modifiers.map { it.keyword }.toTypedArray())
//        } else {
//            cu.addInterface(clazz.nameAsString, *clazz.modifiers.map { it.keyword }.toTypedArray())
//        }
//        newAddedClassOrInterface.setComment(clazz.comment.orElse(null))
////        newAddedClassOrInterface.generateUUID()
//        clazz.members.forEach {
//            val newClonedMember = it.clone()
//            newAddedClassOrInterface.addMember(newClonedMember)
////            newClonedMember.generateUUID()
//        }
//        clazz.orphanComments.forEach {
//            when(it) {
//                is LineComment -> newAddedClassOrInterface.addOrphanComment(LineComment(it.content))
//                is BlockComment -> newAddedClassOrInterface.addOrphanComment(BlockComment(it.content))
//            }
//        }
//        newAddedClassOrInterface.implementedTypes = clazz.implementedTypes
//        newAddedClassOrInterface.extendedTypes = clazz.extendedTypes
//        */
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

class RemoveClassOrInterface(private val compilationUnit: CompilationUnit, private val clazz : ClassOrInterfaceDeclaration) : Transformation {

    override fun applyTransformation(proj: Project) {
        val compilationUnitToHaveClassOrInterfaceRemoved = proj.getCompilationUnitByPath(compilationUnit.correctPath)
        compilationUnitToHaveClassOrInterfaceRemoved?.let {
            val classToRemove = proj.getClassOrInterfaceByUUID(clazz.uuid)
            compilationUnitToHaveClassOrInterfaceRemoved.remove(classToRemove)
        }
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

class RenameClassOrInterface(private val clazz : ClassOrInterfaceDeclaration, private val newName: String) : Transformation {
    private val oldClassOrInterfaceName: String = clazz.nameAsString

    override fun applyTransformation(proj: Project) {
        val classToRename = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToRename?.let {
        renameAllConstructorCalls(proj, clazz, newName)
            classToRename.setName(newName)
        }
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

class ModifiersChangedClassOrInterface(private val clazz : ClassOrInterfaceDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(modifiers)

    override fun applyTransformation(proj: Project) {
        val classToHaveModifiersChanged = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToHaveModifiersChanged?.let {
            classToHaveModifiersChanged.parentNode.get() as CompilationUnit
            classToHaveModifiersChanged.modifiers =
                ModifierSet(classToHaveModifiersChanged.modifiers).replaceModifiersBy(newModifiersSet).toNodeList()
        }
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return if (clazz.isInterface) {
            "CHANGE MODIFIERS OF INTERFACE ${clazz.nameAsString} FROM ${clazz.modifiers} TO $modifiers"
        } else {
            "CHANGE MODIFIERS OF CLASS ${clazz.nameAsString} FROM ${clazz.modifiers} TO $modifiers"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class ChangeImplementsTypes(private val clazz : ClassOrInterfaceDeclaration, private val implements: NodeList<ClassOrInterfaceType>) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val classToBeModified = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToBeModified?.let {
            implements.forEach {
                classToBeModified.addImplementedType(it.nameAsString)
            }
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

    override fun applyTransformation(proj: Project) {
        val classToBeModified = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToBeModified?.let {
            extends.forEach {
                classToBeModified.addExtendedType(it.nameAsString)
            }
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

