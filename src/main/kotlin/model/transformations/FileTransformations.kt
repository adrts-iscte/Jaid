package model.transformations

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.ClassOrInterfaceType
import model.*
import model.visitors.CorrectAllReferencesVisitor

class ChangePackage(private val compilationUnit: CompilationUnit, private val packageDeclaration: Name): Transformation {

    override fun applyTransformation(proj: Project) {
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.setPackageDeclaration(packageDeclaration.clone().asString())
    }

    override fun getNode(): CompilationUnit = compilationUnit

    override fun getText(): String {
        return "CHANGED PACKAGE TO $packageDeclaration"
    }

    fun getNewPackage() = packageDeclaration
}

class ChangeImports(private val compilationUnit: CompilationUnit, private val imports: NodeList<ImportDeclaration>): Transformation {

    override fun applyTransformation(proj: Project) {
        proj.getCompilationUnitByPath(compilationUnit.correctPath)?.imports = NodeList(imports.toMutableList().map { it.clone() })
    }

    override fun getNode(): CompilationUnit = compilationUnit

    override fun getText(): String {
        return "CHANGE IMPORTS"
    }

    fun getImportsList() : NodeList<ImportDeclaration> = imports
}

class AddType(private val originalProject : Project, private val parentNode: Node, private val type : TypeDeclaration<*>) : AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val newAddedType = type.clone()
        newAddedType.accept(CorrectAllReferencesVisitor(originalProject, type), proj)
        when(parentNode) {
            is CompilationUnit -> {
                val parentNodeToHaveTypeAdded = proj.getCompilationUnitByPath(parentNode.correctPath)
                parentNodeToHaveTypeAdded?.let {
                    val index = calculateIndexOfTypeToAdd(parentNode, parentNodeToHaveTypeAdded, type.uuid)
                    parentNodeToHaveTypeAdded.types.add(index, newAddedType)
                }
            } else -> {
                val parentNodeToHaveTypeAdded = proj.getTypeByUUID((parentNode as TypeDeclaration<*>).uuid)
                val index = calculateIndexOfMemberToAdd(parentNode, parentNodeToHaveTypeAdded, type.uuid)
                parentNodeToHaveTypeAdded.members.add(index, newAddedType)
            }
        }
        proj.initializeAllIndexes()
    }

    override fun getNode(): TypeDeclaration<*> = type

    override fun getText(): String {
        val parent = if(parentNode is CompilationUnit) {
            "FILE ${parentNode.storage.get().fileName}"
        } else if (parentNode is TypeDeclaration<*>){
            parentNode.nameAsString
        } else {
            ""
        }
        return "ADD ${type.asString} ${type.nameAsString} TO $parent"
    }

    override fun getNewNode(): TypeDeclaration<*> = type

    override fun getParentNode(): Node = parentNode

    fun getCompilationUnit(): CompilationUnit? = if (parentNode is CompilationUnit) parentNode else null

    fun getOriginalProject() = originalProject
}

class RemoveType(private val parentNode: Node, private val type : TypeDeclaration<*>) : RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val parentNodeToHaveTypeRemoved = when(parentNode) {
            is CompilationUnit -> proj.getCompilationUnitByPath(parentNode.correctPath)
            else -> proj.getTypeByUUID(parentNode.uuid)
        }
        parentNodeToHaveTypeRemoved?.let {
            val typeToRemove = proj.getTypeByUUID(type.uuid)
            parentNodeToHaveTypeRemoved.remove(typeToRemove)
        }
    }

    override fun getNode(): TypeDeclaration<*> = type

    override fun getText(): String {
        val parent = when(parentNode) {
            is CompilationUnit -> "FILE ${parentNode.storage.get().fileName}"
            else -> (parentNode as TypeDeclaration<*>).nameAsString
        }
//        if(parentNode is CompilationUnit) {
//            "FILE ${parentNode.storage.get().fileName}"
//        } else if (parentNode is TypeDeclaration<*>){
//            parentNode.nameAsString
//        } else {
//            ""
//        }
        return "REMOVE ${type.asString} ${type.nameAsString} FROM $parent"
    }

    override fun getRemovedNode(): TypeDeclaration<*> = type

    override fun getParentNode(): Node = parentNode

    fun getCompilationUnit(): CompilationUnit? = if (parentNode is CompilationUnit) parentNode else null
}

class RenameType(private val type : TypeDeclaration<*>, private val newName: SimpleName) : Transformation {
    private val oldTypeName: SimpleName = type.name
    private val parentNode = type.parentNode.get()
//    private val compilationUnit = type.findCompilationUnit().get()

    override fun applyTransformation(proj: Project) {
        val typeToRename = proj.getTypeByUUID(type.uuid)
        val realNameToBeSet = newName.clone()
        proj.renameAllTypeUsageCalls(type.uuid, realNameToBeSet.asString())
        typeToRename.name = realNameToBeSet
    }

    override fun getNode(): Node {
        return type
    }

    override fun getText(): String {
        return "RENAME ${type.asString} $oldTypeName TO $newName"
    }

    fun getNewName() : SimpleName = newName

    fun getParentNode() : Node = parentNode

    fun getCompilationUnit(): CompilationUnit? = if (parentNode is CompilationUnit) parentNode else null
}

class ModifiersChangedType(private val type : TypeDeclaration<*>, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))

    override fun applyTransformation(proj: Project) {
        val typeToHaveModifiersChanged = proj.getTypeByUUID(type.uuid)
        typeToHaveModifiersChanged.modifiers =
            ModifierSet(typeToHaveModifiersChanged.modifiers).replaceModifiersBy(newModifiersSet).toNodeList()
    }

    override fun getNode(): Node {
        return type
    }

    override fun getText(): String {
        return "CHANGE MODIFIERS OF ${type.asString} ${type.nameAsString} FROM ${type.modifiers} TO $modifiers"
    }


    fun getNewModifiers() : NodeList<Modifier> = modifiers

    //Makes sense?
    fun setNewModifiers(newModifiers : NodeList<Modifier>) {
        modifiers.clear()
        modifiers.addAll(newModifiers)
    }

    private fun mergeModifiersWith(other : ModifiersChangedType) {
        val mergedModifiers = ModifierSet(modifiers).merge(ModifierSet(other.getNewModifiers()))
        setNewModifiers(mergedModifiers)
        other.setNewModifiers(mergedModifiers)
    }

}

class ChangeImplementsTypes(private val originalProject : Project, private val type : TypeDeclaration<*>, private val implements: NodeList<ClassOrInterfaceType>) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val typeToBeModified = if (type.isEnumDeclaration) {
            proj.getEnumByUUID(type.uuid)
        } else {
            proj.getClassOrInterfaceByUUID(type.uuid)
        }
        typeToBeModified.implementedTypes.clear()
        implements.forEach {
            val newImplementsType = it.clone()
            newImplementsType.accept(CorrectAllReferencesVisitor(originalProject, it), proj)
            typeToBeModified.addImplementedType(newImplementsType)
        }
        proj.initializeAllIndexes()
    }

    override fun getNode(): Node {
        return type
    }

    override fun getText(): String {
        return "CHANGE IMPLEMENTS TYPES OF ${type.asString} ${type.nameAsString}"
    }

    fun getNewImplementsTypes() : NodeList<ClassOrInterfaceType> = implements
}

class ChangeExtendedTypes(private val originalProject : Project, private val clazz : ClassOrInterfaceDeclaration, private val extends: NodeList<ClassOrInterfaceType>) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val classToBeModified = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToBeModified.extendedTypes.clear()
        extends.forEach {
            val newExtendedType = it.clone()
            newExtendedType.accept(CorrectAllReferencesVisitor(originalProject, it), proj)
            classToBeModified.addExtendedType(newExtendedType)
        }
        proj.initializeAllIndexes()
    }

    override fun getNode(): Node {
        return clazz
    }

    override fun getText(): String {
        return "CHANGE EXTENDS TYPES OF CLASS ${clazz.nameAsString}"
    }

    fun getNewExtendedTypes() : NodeList<ClassOrInterfaceType> = extends
}

class MoveTypeIntraFile(private val cuTypes : List<TypeDeclaration<*>>,
                          private val type : TypeDeclaration<*>,
                          private val locationIndex: Int,
                          private val orderIndex: Int) : MoveTransformationIntraTypeOrCompilationUnit {

    private val compilationUnit = type.parentNode.get() as CompilationUnit

    override fun applyTransformation(proj: Project) {
        val compilationUnitToBeChanged = proj.getCompilationUnitByPath(compilationUnit.correctPath)
        compilationUnitToBeChanged?.let {
            val classToBeMoved = proj.getTypeByUUID(type.uuid)
            compilationUnitToBeChanged.types.move(locationIndex, classToBeMoved)
        }
    }

    override fun getNode(): Node {
        return type
    }

    override fun getText(): String {
        val appendix = if((locationIndex + 1) >= cuTypes.size) {
            "AT THE END"
        } else {
            val member = cuTypes[locationIndex + 1]
            "BEFORE ${member.name}"
        }
        return "MOVE ${type.asString} ${type.nameAsString} $appendix"
    }

    override fun getOrderIndex() = orderIndex

    fun getClass() = type
}

class MoveTypeInterFiles(private val addTransformation : AddType,
                         private val removeTransformation : RemoveType) : MoveTransformationInterClassOrCompilationUnit {
    private val type = addTransformation.getNode()

    override fun applyTransformation(proj: Project) {
        addTransformation.applyTransformation(proj)
        removeTransformation.applyTransformation(proj)
    }

    override fun getNode(): TypeDeclaration<*> = type

    override fun getText(): String {
        return "MOVE ${type.asString} ${type.nameAsString} FROM FILE ${removeTransformation.getCompilationUnit()!!.storage.get().fileName} TO FILE ${addTransformation.getCompilationUnit()!!.storage.get().fileName}"
    }

    fun getParentNode() = addTransformation.getParentNode()

    override fun getRemoveTransformation() = removeTransformation

    override fun getAddTransformation() = addTransformation
}

class AddEnumConstant(private val parentEnum: EnumDeclaration, private val enumConstant : EnumConstantDeclaration) : AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val newAddedEnumConstant = enumConstant.clone()
        val parentEnumToHaveEnumConstantAdded = proj.getEnumByUUID(parentEnum.uuid)
        val index = calculateIndexOfMemberToAdd(parentEnum, parentEnumToHaveEnumConstantAdded, enumConstant.uuid)
        parentEnumToHaveEnumConstantAdded.entries.add(index, newAddedEnumConstant)
        proj.initializeAllIndexes()
    }

    override fun getNode(): EnumConstantDeclaration = enumConstant

    override fun getText(): String {
        return "ADD ENUM CONSTANT ${enumConstant.nameAsString} TO ENUM ${parentEnum.nameAsString}"
    }

    override fun getNewNode(): EnumConstantDeclaration = enumConstant

    override fun getParentNode(): Node = parentEnum

}

class RemoveEnumConstant(private val parentEnum: EnumDeclaration, private val enumConstant : EnumConstantDeclaration) : RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val parentEnumToHaveEnumConstantRemoved = proj.getEnumByUUID(parentEnum.uuid)
        val enumConstantToRemove = proj.getEnumConstantByUUID(enumConstant.uuid)
        parentEnumToHaveEnumConstantRemoved.remove(enumConstantToRemove)
    }

    override fun getNode(): EnumConstantDeclaration = enumConstant

    override fun getText(): String {
        return "REMOVE ENUM CONSTANT ${enumConstant.nameAsString} FROM ENUM ${parentEnum.nameAsString}"
    }

    override fun getRemovedNode(): EnumConstantDeclaration = enumConstant

    override fun getParentNode(): EnumDeclaration = parentEnum

}

class RenameEnumConstant(private val enumConstant: EnumConstantDeclaration, private val newName: SimpleName) : Transformation {
    private val oldClassOrInterfaceName: SimpleName = enumConstant.name
    private val parentNode = enumConstant.parentNode.get()

    override fun applyTransformation(proj: Project) {
        val enumConstantToRename = proj.getEnumConstantByUUID(enumConstant.uuid)
        val realNameToBeSet = newName.clone()
        proj.renameAllEnumConstantUses(enumConstant.uuid, realNameToBeSet.asString())
        enumConstantToRename.setName(realNameToBeSet)
    }

    override fun getNode(): EnumConstantDeclaration = enumConstant

    override fun getText(): String {
        return "RENAME ENUM CONSTANT ${enumConstant.nameAsString} $oldClassOrInterfaceName TO $newName"
    }

    fun getNewName() : SimpleName = newName

    fun getParentNode() : Node = parentNode
}

class MoveEnumConstantIntraEnum(private val enumEntries : List<EnumConstantDeclaration>,
                                private val enumConstantDeclaration : EnumConstantDeclaration,
                                private val locationIndex: Int,
                                private val orderIndex: Int) : MoveTransformationIntraTypeOrCompilationUnit {

    private val enumDeclaration = enumConstantDeclaration.parentNode.get() as EnumDeclaration

    override fun applyTransformation(proj: Project) {
        val enumToBeChanged = proj.getEnumByUUID(enumDeclaration.uuid)
        val enumConstantToBeMoved = proj.getEnumConstantByUUID(enumConstantDeclaration.uuid)
        enumToBeChanged.entries.move(locationIndex, enumConstantToBeMoved)
    }

    override fun getNode(): EnumConstantDeclaration = enumConstantDeclaration

    override fun getText(): String {
        val appendix = if((locationIndex + 1) >= enumEntries.size) {
            "AT THE END"
        } else {
            val enumEntry = enumEntries[locationIndex + 1]
            "BEFORE ${enumEntry.name}"
        }
        return "MOVE ENUM CONSTANT ${enumConstantDeclaration.nameAsString} $appendix"
    }

    override fun getOrderIndex() = orderIndex

    fun getClass() = enumConstantDeclaration
}