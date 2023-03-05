package model.transformations

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.ClassOrInterfaceType
import model.*
import java.lang.UnsupportedOperationException

class ChangePackage(private val compilationUnit: CompilationUnit, private val packageDeclaration: Name): Transformation {

    override fun applyTransformation(proj: Project) {
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.setPackageDeclaration(packageDeclaration.clone().asString())
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
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.imports = NodeList(imports.toMutableList().map { it.clone() })
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

class AddClassOrInterface(private val compilationUnit: CompilationUnit, private val clazz : ClassOrInterfaceDeclaration) : AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val compilationUnitToHaveClassOrInterfaceAdded = proj.getCompilationUnitByPath(compilationUnit.correctPath)
        compilationUnitToHaveClassOrInterfaceAdded?.let {
            val newAddedClassOrInterface = clazz.clone()
            val index = calculateIndexOfTypeToAdd(compilationUnit, compilationUnitToHaveClassOrInterfaceAdded, clazz.uuid)
            compilationUnitToHaveClassOrInterfaceAdded.types.add(index, newAddedClassOrInterface)
        }
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

    override fun getNewNode(): ClassOrInterfaceDeclaration = clazz

    override fun getParentNode(): CompilationUnit = compilationUnit

}

class RemoveClassOrInterface(private val compilationUnit: CompilationUnit, private val clazz : ClassOrInterfaceDeclaration) : RemoveNodeTransformation {

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

    override fun getRemovedNode(): ClassOrInterfaceDeclaration = clazz

    override fun getParentNode(): CompilationUnit = compilationUnit

}

class RenameClassOrInterface(private val clazz : ClassOrInterfaceDeclaration, private val newName: SimpleName) : Transformation {
    private val oldClassOrInterfaceName: SimpleName = clazz.name

    override fun applyTransformation(proj: Project) {
        val classToRename = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToRename?.let {
            val realNameToBeSet = newName.clone()
            proj.renameAllConstructorCalls(clazz.uuid, realNameToBeSet.asString())
            classToRename.setName(realNameToBeSet)
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
    private val newModifiersSet = ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))

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
            classToBeModified.implementedTypes.clear()
            implements.forEach {
                classToBeModified.addImplementedType(it.clone().nameAsString)
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
            classToBeModified.extendedTypes.clear()
            extends.forEach {
                classToBeModified.addExtendedType(it.clone().nameAsString)
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

class MoveTypeIntraFile(private val cuTypes : List<TypeDeclaration<*>>,
                          private val type : TypeDeclaration<*>,
                          private val locationIndex: Int,
                          private val orderIndex: Int) : MoveTransformationIntraClassOrCompilationUnit {

    private val compilationUnit = type.parentNode.get() as CompilationUnit

    override fun applyTransformation(proj: Project) {
        val compilationUnitToBeChanged = proj.getCompilationUnitByPath(compilationUnit.correctPath)
        compilationUnitToBeChanged?.let {
            val classToBeMoved = proj.getClassOrInterfaceByUUID(type.uuid)
            classToBeMoved?.let {
                compilationUnitToBeChanged.types.move(locationIndex, classToBeMoved)
            }
        }
    }

    override fun getNode(): Node {
        return type
    }

    override fun getText(): String {
        if (type !is ClassOrInterfaceDeclaration) {
            throw UnsupportedOperationException("This type is not supported!")
        }
        val appendix = if((locationIndex + 1) >= cuTypes.size) {
            "AT THE END"
        } else {
            val member = cuTypes[locationIndex + 1]
            "BEFORE ${member.name}"
        }
        return if(type.isInterface) {
            "MOVE INTERFACE ${type.nameAsString} $appendix"
        } else {
            "MOVE CLASS ${type.nameAsString} $appendix"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

    override fun getOrderIndex() = orderIndex

    fun getClass() = type
}

class MoveTypeInterFiles(private val addTransformation : AddClassOrInterface,
                            private val removeTransformation : RemoveClassOrInterface) : MoveTransformationInterClassOrCompilationUnit {
    private val clazz = addTransformation.getNode() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        addTransformation.applyTransformation(proj)
        removeTransformation.applyTransformation(proj)
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return if(clazz.isInterface) {
            "MOVE INTERFACE ${clazz.nameAsString} FROM FILE ${removeTransformation.getParentNode().storage.get().fileName} TO FILE ${addTransformation.getParentNode().storage.get().fileName}"
        } else {
            "MOVE METHOD ${clazz.nameAsString} FROM CLASS ${removeTransformation.getParentNode().storage.get().fileName} TO FILE ${addTransformation.getParentNode().storage.get().fileName}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

//    fun getClass() = addTransformation.getClass()

    override fun getRemoveTransformation() = removeTransformation

    override fun getAddTransformation() = addTransformation
}
