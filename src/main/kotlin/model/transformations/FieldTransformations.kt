package model.transformations

import Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.Type
import com.github.javaparser.printer.configuration.DefaultConfigurationOption
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import model.renameAllFieldUses
import model.setUUID
import model.uuid

class AddField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldAdded = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        val firstMethod = cu.findFirst(MethodDeclaration::class.java).orElse(null)
        val newField = if (firstMethod != null) {
            val field = FieldDeclaration(field.modifiers, fieldVariableDeclarator.type, fieldVariableDeclarator.nameAsString)
            classToHaveFieldAdded.members.addBefore(field, firstMethod)
            field
        } else {
            classToHaveFieldAdded.addField(fieldVariableDeclarator.type, fieldVariableDeclarator.nameAsString, *field.modifiers.map { it.keyword }.toTypedArray())
        }
        newField.setUUID(field.uuid!!)
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
}

class RemoveField(private val clazz : ClassOrInterfaceDeclaration, private val field : FieldDeclaration) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classToHaveFieldRemoved = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        val fieldToRemove = clazz.fields.find { it.uuid == field.uuid }!!
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
}

class RenameField(private val field : FieldDeclaration, private val newName: String) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val fieldToRename = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().fields.find { it.uuid == field.uuid }!!
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
        return "RENAME FIELD ${(field.variables.first() as VariableDeclarator).nameAsString} TO $newName"
        //return "RENAME FIELD ${getNode()} TO $newName"
    }
}

class TypeChangedField(private val field : FieldDeclaration, private val newType: Type) : Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val fieldToChangeType = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().fields.find { it.uuid == field.uuid }!!
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

}

class ModifiersChangedField(private val field : FieldDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val fieldToChangeModifiers = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().fields.find { it.uuid == field.uuid }!!
        fieldToChangeModifiers.modifiers = modifiers
    }

    override fun getNode(): Node {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE MODIFIERS OF FIELD ${fieldVariableDeclarator.nameAsString} FROM ${field.modifiers} TO $modifiers"
    }

}

class InitializerChangedField(private val field: FieldDeclaration, private val initializer: Expression) :
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val fieldToChangeInitializer = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get().fields.find { it.uuid == field.uuid }!!
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

}