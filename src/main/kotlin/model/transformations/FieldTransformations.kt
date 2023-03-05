package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.type.Type
import com.github.javaparser.printer.configuration.DefaultConfigurationOption
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import model.*
import model.visitors.CorrectAllReferencesVisitor
import java.lang.UnsupportedOperationException

class AddField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) : AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val classToHaveFieldAdded = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToHaveFieldAdded?.let {
            val newField = field.clone()
            val index = calculateIndexOfMemberToAdd(clazz, classToHaveFieldAdded, field.uuid)
            classToHaveFieldAdded.members.add(index, newField)
        }
//        newField.generateUUID()
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val printerConfiguration = DefaultPrinterConfiguration().removeOption(
            DefaultConfigurationOption(
            DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, false)
        )
        return "ADD FIELD ${getNode().toString(printerConfiguration)}"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

    override fun getNewNode(): FieldDeclaration = field

    override fun getParentNode() : ClassOrInterfaceDeclaration = clazz

}

class RemoveField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) :
    RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val classToHaveFieldRemoved = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToHaveFieldRemoved?.let {
            val fieldToRemove = proj.getFieldByUUID(field.uuid)
            fieldToRemove?.let {
                classToHaveFieldRemoved.remove(fieldToRemove)
            }
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val printerConfiguration = DefaultPrinterConfiguration().removeOption(
            DefaultConfigurationOption(
                DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, false)
        )
        return "REMOVE FIELD ${getNode().toString(printerConfiguration)}"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

    override fun getRemovedNode(): FieldDeclaration = field

    override fun getParentNode() : ClassOrInterfaceDeclaration = clazz
}

class RenameField(private val field: FieldDeclaration, private val newName: SimpleName) :
    Transformation {
    private val oldFieldName: String = (field.variables.first() as VariableDeclarator).nameAsString

    override fun applyTransformation(proj: Project) {
        val fieldToRename = proj.getFieldByUUID(field.uuid)
        fieldToRename?.let {
            val fieldVariableDeclarator = fieldToRename.variables.first() as VariableDeclarator
            val realNameToBeSet = newName.clone()
            proj.renameAllFieldUses(fieldToRename.uuid, realNameToBeSet.asString())
            fieldVariableDeclarator.setName(realNameToBeSet)
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        return "RENAME FIELD $oldFieldName TO $newName"
        //return "RENAME FIELD ${getNode()} TO $newName"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class TypeChangedField(private val field: FieldDeclaration, private val newType: Type) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val fieldToChangeType = proj.getFieldByUUID(field.uuid)
        fieldToChangeType?.let {
            val fieldVariableDeclarator = fieldToChangeType.variables.first() as VariableDeclarator
            fieldVariableDeclarator.type = newType.clone()
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val printerConfiguration = DefaultPrinterConfiguration().removeOption(
            DefaultConfigurationOption(
            DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, false)
        )
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE TYPE OF FIELD ${fieldVariableDeclarator.nameAsString} FROM ${fieldVariableDeclarator.type} TO $newType"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

}

class ModifiersChangedField(private val field: FieldDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))

    override fun applyTransformation(proj: Project) {
        val fieldToChangeModifiers = proj.getFieldByUUID(field.uuid)
        fieldToChangeModifiers?.let {
            fieldToChangeModifiers.modifiers =
                ModifierSet(fieldToChangeModifiers.modifiers).replaceModifiersBy(newModifiersSet).toNodeList()
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE MODIFIERS OF FIELD ${fieldVariableDeclarator.nameAsString} FROM ${field.modifiers} TO $modifiers"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

}

class InitializerChangedField(private val field: FieldDeclaration, private val initializer: Expression) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val fieldToChangeInitializer = proj.getFieldByUUID(field.uuid)
        fieldToChangeInitializer?.let {
            val fieldVariableDeclarator = fieldToChangeInitializer.variables.first() as VariableDeclarator
            val realInitializerToBeAdded = initializer.clone()
            fieldVariableDeclarator.setInitializer(realInitializerToBeAdded)
            realInitializerToBeAdded.accept(CorrectAllReferencesVisitor(initializer), proj)
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE INITIALIZER OF FIELD ${fieldVariableDeclarator.nameAsString} TO $initializer"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

}

class MoveFieldIntraClass(private val clazzMembers : List<BodyDeclaration<*>>,
                          private val field : FieldDeclaration,
                          private val locationIndex: Int,
                          private val orderIndex: Int) : MoveTransformationIntraClassOrCompilationUnit {

    private val clazz = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val classToBeChanged = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToBeChanged?.let {
            val fieldToBeMoved = proj.getFieldByUUID(field.uuid)
            fieldToBeMoved?.let {
                classToBeChanged.members.move(locationIndex, fieldToBeMoved)
            }
        }
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val appendix = if((locationIndex + 1) >= clazzMembers.size) {
            "AT THE END"
        } else {
            val member = clazzMembers[locationIndex + 1]
            val memberName = when (member) {
                is CallableDeclaration<*> -> member.nameAsString
                is FieldDeclaration -> (member.variables.first() as VariableDeclarator).nameAsString
                else -> throw UnsupportedOperationException("This type is not supported!")
            }
            "BEFORE $memberName"
        }
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "MOVE FIELD ${fieldVariableDeclarator.nameAsString} $appendix"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

    override fun getOrderIndex() = orderIndex

    fun getClass() = clazz
}

class MoveFieldInterClasses(private val addTransformation : AddField,
                               private val removeTransformation : RemoveField) : MoveTransformationInterClassOrCompilationUnit {
    private val field = addTransformation.getNode() as FieldDeclaration

    override fun applyTransformation(proj: Project) {
        addTransformation.applyTransformation(proj)
        removeTransformation.applyTransformation(proj)
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "MOVE FIELD ${fieldVariableDeclarator.nameAsString} FROM CLASS ${removeTransformation.getParentNode().nameAsString} TO CLASS ${addTransformation.getParentNode().nameAsString}"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }

//    fun getClass() = addTransformation.getClass()

    override fun getRemoveTransformation() = removeTransformation

    override fun getAddTransformation() = addTransformation
}