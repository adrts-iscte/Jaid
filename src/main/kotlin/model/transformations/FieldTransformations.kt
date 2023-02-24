package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.Type
import com.github.javaparser.printer.configuration.DefaultConfigurationOption
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import model.*

class AddField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) : Transformation {

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
}

class RemoveField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) :
    Transformation {

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
}

class RenameField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration, private val newName: String) :
    Transformation {
    private val oldFieldName: String = (field.variables.first() as VariableDeclarator).nameAsString

    override fun applyTransformation(proj: Project) {
        val fieldToRename = proj.getFieldByUUID(field.uuid)
        fieldToRename?.let {
            val fieldVariableDeclarator = fieldToRename.variables.first() as VariableDeclarator
            renameAllFieldUses(proj, fieldToRename, fieldVariableDeclarator.nameAsString, newName)
            fieldVariableDeclarator.setName(newName)
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
        return "RENAME FIELD $oldFieldName TO $newName"
        //return "RENAME FIELD ${getNode()} TO $newName"
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class TypeChangedField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration, private val newType: Type) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val fieldToChangeType = proj.getFieldByUUID(field.uuid)
        fieldToChangeType?.let {
            val fieldVariableDeclarator = fieldToChangeType.variables.first() as VariableDeclarator
            fieldVariableDeclarator.type = newType
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

class ModifiersChangedField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(modifiers)

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

class InitializerChangedField(private val clazz : ClassOrInterfaceDeclaration, private val field: FieldDeclaration, private val initializer: Expression) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val fieldToChangeInitializer = proj.getFieldByUUID(field.uuid)
        fieldToChangeInitializer?.let {
            val fieldVariableDeclarator = fieldToChangeInitializer.variables.first() as VariableDeclarator
            fieldVariableDeclarator.setInitializer(initializer)
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