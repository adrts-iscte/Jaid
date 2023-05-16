package model.conflictDetection

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.TypeDeclaration
import model.*
import model.transformations.*
import kotlin.reflect.KClass

val allFileFileConflictTypes = listOf(
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RenameEnumConstant) ?: b as RenameEnumConstant
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if (firstTransformation.getParentNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed enum is the enum that has the renamed enum constant", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = AddEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RenameEnumConstant) ?: b as RenameEnumConstant
        val secondTransformation = (b as? AddEnumConstant) ?: a as AddEnumConstant
        if (firstTransformation.getNewName() == secondTransformation.getNode().name &&
            firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "Both enum constant's name become equal after applying both Transformation's", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = RemoveEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RenameEnumConstant) ?: b as RenameEnumConstant
        val secondTransformation = (b as? RemoveEnumConstant) ?: a as RemoveEnumConstant
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is the one to have name changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = RenameEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as RenameEnumConstant
        val secondTransformation = b as RenameEnumConstant
        if (firstTransformation.getParentNode() == secondTransformation.getParentNode()) {
            if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                if (firstTransformation.getNewName() == secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Both enum's name become equal after applying both Transformation's",this))
                }
            } else {
                if (firstTransformation.getNewName() != secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Different new names for the same enum constant after applying both RenameEnumConstant Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = AddType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveEnumConstant) ?: b as RemoveEnumConstant
        val secondTransformation = (b as? AddType) ?: a as AddType
        if (secondTransformation.getOriginalProject().hasUsesIn(firstTransformation.getNode(), secondTransformation.getNode())) {
            listOfConflicts.add(createConflict(a, b, "The removed enum constant is used in the new type",this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveType::class
    override fun getSecond(): KClass<out Transformation> = AddEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveType) ?: b as RemoveType
        val secondTransformation = (b as? AddEnumConstant) ?: a as AddEnumConstant
        if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
            if (firstTransformation.getNode().uuid == secondTransformation.getParentNode().uuid) {
                listOfConflicts.add(createConflict(a, b, "The removed enum is the enum that has the enum constant to be added",this))
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddEnumConstant::class
    override fun getSecond(): KClass<out Transformation> = AddEnumConstant::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as AddEnumConstant
        val secondTransformation = b as AddEnumConstant
        if(firstTransformation.getParentNode().uuid == secondTransformation.getParentNode().uuid &&
            firstTransformation.getNode().name == secondTransformation.getNode().name) {
            listOfConflicts.add(createConflict(a, b, "The two added enum constant have the same name", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
    override fun getSecond(): KClass<out Transformation> = AddType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
        val secondTransformation = (b as? AddType) ?: a as AddType
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if (firstTransformation.getNode().name == secondTransformation.getNode().name && areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode)) {
            listOfConflicts.add(createConflict( a, b, "The two added types in the destiny file have the same name", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
    override fun getSecond(): KClass<out Transformation> = RemoveType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
        val secondTransformation = (b as? RemoveType) ?: a as RemoveType
        if(firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict(a, b, "The removed type is the one to be moved to another file", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
    override fun getSecond(): KClass<out Transformation> = RenameType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? MoveTypeInterFiles) ?: b as MoveTypeInterFiles
        val secondTransformation = (b as? RenameType) ?: a as RenameType
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
            if(areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode) &&
                firstTransformation.getNode().name == secondTransformation.getNewName()) {
                listOfConflicts.add(createConflict(a, b, "The file will have two types with the same name after applying both Transformation's", this))
            }
        } else {
            val commonAncestorFile = if (firstTransformationParentNode is CompilationUnit) {
                commonAncestor.getCompilationUnitByPath(firstTransformation.getAddTransformation().getCompilationUnit()!!.correctPath)
            } else {
                commonAncestor.getTypeByUUID(firstTransformationParentNode.uuid)
            }
            commonAncestorFile?.let {
                if (commonAncestorFile.childNodes.filterIsInstance<TypeDeclaration<*>>().any { typeMethod ->
                        typeMethod.name == secondTransformation.getNewName()
                    }) {
                    listOfConflicts.add(createConflict(a, b,"Another type has the same name of this one after applying both Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = MoveTypeInterFiles::class
    override fun getSecond(): KClass<out Transformation> = MoveTypeInterFiles::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as MoveTypeInterFiles
        val secondTransformation = b as MoveTypeInterFiles
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid && !areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode)) {
            listOfConflicts.add(createConflict(a, b, "The same type is being moved to two different files", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ChangePackage::class
    override fun getSecond(): KClass<out Transformation> = ChangePackage::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ChangePackage
        val secondTransformation = b as ChangePackage
        if (firstTransformation.getNode().storage.get().fileName == secondTransformation.getNode().storage.get().fileName &&
            !arePackageDeclarationEqual(firstTransformation.getNewPackage(), secondTransformation.getNewPackage())
        ) {
            listOfConflicts.add(createConflict( a, b, "The same file's package is being changed to two different packages", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ChangeImports::class
    override fun getSecond(): KClass<out Transformation> = ChangeImports::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ChangeImports
        val secondTransformation = b as ChangeImports
        if (firstTransformation.getImportsList() != secondTransformation.getImportsList() &&
            firstTransformation.getNode().correctPath == secondTransformation.getNode().correctPath) {
            listOfConflicts.add(createConflict( a, b, "The same file's imports are being changed to two different import lists", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddType::class
    override fun getSecond(): KClass<out Transformation> = RenameType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? AddType) ?: b as AddType
        val secondTransformation = (b as? RenameType) ?: a as RenameType
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if(firstTransformation.getNode().name == secondTransformation.getNewName() && areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode)) {
            listOfConflicts.add(createConflict(a, b, "Both type's name become equal after applying both Transformation's", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = AddType::class
    override fun getSecond(): KClass<out Transformation> = AddType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as AddType
        val secondTransformation = b as AddType
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if(areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode) &&
            firstTransformation.getNode().name == secondTransformation.getNode().name) {
            listOfConflicts.add(createConflict(a, b, "The two added types have the same name", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveType::class
    override fun getSecond(): KClass<out Transformation> = ChangeExtendedTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveType) ?: b as RemoveType
        val secondTransformation = (b as? ChangeExtendedTypes) ?: a as ChangeExtendedTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed type is the one to have extended types changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveType::class
    override fun getSecond(): KClass<out Transformation> = ChangeImplementsTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveType) ?: b as RemoveType
        val secondTransformation = (b as? ChangeImplementsTypes) ?: a as ChangeImplementsTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed type is the one to have implements types changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveType::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveType) ?: b as RemoveType
        val secondTransformation = (b as? ModifiersChangedType) ?: a as ModifiersChangedType
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed type is the one to have modifiers changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RemoveType::class
    override fun getSecond(): KClass<out Transformation> = RenameType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = (a as? RemoveType) ?: b as RemoveType
        val secondTransformation = (b as? RenameType) ?: a as RenameType
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid) {
            listOfConflicts.add(createConflict( a, b, "The removed type is the one to have name changed", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = RenameType::class
    override fun getSecond(): KClass<out Transformation> = RenameType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as RenameType
        val secondTransformation = b as RenameType
        val firstTransformationParentNode = firstTransformation.getParentNode()
        val secondTransformationParentNode = secondTransformation.getParentNode()
        if (areParentNodesEqual(firstTransformationParentNode, secondTransformationParentNode)) {
            if (firstTransformation.getNode().uuid != secondTransformation.getNode().uuid) {
                if (firstTransformation.getNewName() == secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Both type's name become equal after applying both Transformation's",this))
                }
            } else {
                if (firstTransformation.getNewName() != secondTransformation.getNewName()) {
                    listOfConflicts.add(createConflict(a, b, "Different new names for the same type after applying both RenameType Transformation's", this))
                }
            }
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ModifiersChangedType::class
    override fun getSecond(): KClass<out Transformation> = ModifiersChangedType::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ModifiersChangedType
        val secondTransformation = b as ModifiersChangedType
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            ModifierSet(firstTransformation.getNewModifiers()).isConflictiousWith(ModifierSet(secondTransformation.getNewModifiers()))) {
            listOfConflicts.add(createConflict(a, b, "Both ModifiersChangedClassOrInterface Transformation's changes are conflictious", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ChangeImplementsTypes::class
    override fun getSecond(): KClass<out Transformation> = ChangeImplementsTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ChangeImplementsTypes
        val secondTransformation = b as ChangeImplementsTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getNewImplementsTypes() != secondTransformation.getNewImplementsTypes()) {
            listOfConflicts.add(createConflict(a, b, "Both lists of implements types are different", this))
        }
    }
},
object : ConflictType {
    override fun getFirst(): KClass<out Transformation> = ChangeExtendedTypes::class
    override fun getSecond(): KClass<out Transformation> = ChangeExtendedTypes::class

    override fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        val firstTransformation = a as ChangeExtendedTypes
        val secondTransformation = b as ChangeExtendedTypes
        if (firstTransformation.getNode().uuid == secondTransformation.getNode().uuid &&
            firstTransformation.getNewExtendedTypes() != secondTransformation.getNewExtendedTypes()) {
            listOfConflicts.add(createConflict(a, b, "Both lists of extended types are different", this))
        }
    }
}
)