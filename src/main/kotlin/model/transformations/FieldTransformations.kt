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
import model.Conflict
import model.generateUUID
import model.renameAllFieldUses
import model.uuid

class AddField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldAdded = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val firstMethod = classToHaveFieldAdded.findFirst(MethodDeclaration::class.java).orElse(null)
        val newField = field.clone()
        if (firstMethod != null) {
            classToHaveFieldAdded.members.addBefore(newField, firstMethod)
        } else {
            classToHaveFieldAdded.addMember(newField)
        }
        newField.generateUUID()
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldRemoved = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToRemove = classToHaveFieldRemoved.fields.find { it.uuid == field.uuid }!!
        classToHaveFieldRemoved.remove(fieldToRemove)
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldRenamed = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToRename = classToHaveFieldRenamed.fields.find { it.uuid == field.uuid }!!
        val fieldVariableDeclarator = fieldToRename.variables.first() as VariableDeclarator
        renameAllFieldUses(cu, fieldToRename, fieldVariableDeclarator.nameAsString, newName)
        fieldVariableDeclarator.setName(newName)
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToChangeType = classToHaveFieldModified.fields.find { it.uuid == field.uuid }!!
        val fieldVariableDeclarator = fieldToChangeType.variables.first() as VariableDeclarator
        fieldVariableDeclarator.type = newType
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToChangeModifiers = classToHaveFieldModified.fields.find { it.uuid == field.uuid }!!
        fieldToChangeModifiers.modifiers = modifiers
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToChangeInitializer = classToHaveFieldModified.fields.find { it.uuid == field.uuid }!!
        val fieldVariableDeclarator = fieldToChangeInitializer.variables.first() as VariableDeclarator
        fieldVariableDeclarator.setInitializer(initializer)
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