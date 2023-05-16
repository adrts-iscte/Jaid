package model.transformations

import com.github.javaparser.ast.Modifier
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

class AddField(private val originalProject : Project, private val type : TypeDeclaration<*>, private val field : FieldDeclaration) : AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val typeToHaveFieldAdded = proj.getTypeByUUID(type.uuid)
        val newField = field.clone()
        newField.accept(CorrectAllReferencesVisitor(originalProject, field), proj)
        val index = calculateIndexOfMemberToAdd(type, typeToHaveFieldAdded, field.uuid)
        typeToHaveFieldAdded.members.add(index, newField)
        proj.initializeAllIndexes()
    }

    override fun getNode(): FieldDeclaration = field

    override fun getText(): String {
        val printerConfiguration = DefaultPrinterConfiguration().removeOption(
            DefaultConfigurationOption(
            DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, false)
        )
        return "ADD FIELD ${getNode().toString(printerConfiguration)}"
    }

    override fun getNewNode(): FieldDeclaration = field

    override fun getParentNode() : TypeDeclaration<*> = type

    fun getOriginalProject() = originalProject
}

class RemoveField(private val type : TypeDeclaration<*>, private val field : FieldDeclaration) :
    RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val typeToHaveFieldRemoved = proj.getClassOrInterfaceByUUID(type.uuid)
        val fieldToRemove = proj.getFieldByUUID(field.uuid)
        typeToHaveFieldRemoved.remove(fieldToRemove)
    }

    override fun getNode(): FieldDeclaration = field

    override fun getText(): String {
        val printerConfiguration = DefaultPrinterConfiguration().removeOption(
            DefaultConfigurationOption(
                DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, false)
        )
        return "REMOVE FIELD ${getNode().toString(printerConfiguration)}"
    }

    override fun getRemovedNode(): FieldDeclaration = field

    override fun getParentNode() : TypeDeclaration<*> = type
}

class RenameField(private val field: FieldDeclaration, private val newName: SimpleName) :
    Transformation {
    private val oldFieldName: String = field.name.asString()
    private val type: TypeDeclaration<*> = field.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val fieldToRename = proj.getFieldByUUID(field.uuid)
        val fieldVariableDeclarator = fieldToRename.variables.first() as VariableDeclarator
        val realNameToBeSet = newName.clone()
        proj.renameAllFieldUses(fieldToRename.uuid, realNameToBeSet.asString())
        fieldVariableDeclarator.setName(realNameToBeSet)
    }

    override fun getNode(): FieldDeclaration {
        return field
    }

    override fun getText(): String {
        return "RENAME FIELD $oldFieldName TO $newName"
    }

    fun getNewName() : SimpleName = newName

    fun getParentNode() : TypeDeclaration<*> = type
}

class TypeChangedField(private val originalProject : Project, private val field: FieldDeclaration, private val type: Type) :
    Transformation {
    private val clazz: ClassOrInterfaceDeclaration = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val fieldToChangeType = proj.getFieldByUUID(field.uuid)
        val fieldVariableDeclarator = fieldToChangeType.variables.first() as VariableDeclarator
        val newType = type.clone()
        newType.accept(CorrectAllReferencesVisitor(originalProject, type), proj)
        fieldVariableDeclarator.type = newType
        proj.initializeAllIndexes()
    }

    override fun getNode(): FieldDeclaration {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE TYPE OF FIELD ${fieldVariableDeclarator.nameAsString} FROM ${fieldVariableDeclarator.type} TO $type"
    }

    fun getNewType() : Type = type

    fun getParentNode() : ClassOrInterfaceDeclaration = clazz
}

class ModifiersChangedField(private val field: FieldDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))
    private val clazz: ClassOrInterfaceDeclaration = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val fieldToChangeModifiers = proj.getFieldByUUID(field.uuid)
        fieldToChangeModifiers.modifiers =
            ModifierSet(fieldToChangeModifiers.modifiers).replaceModifiersBy(newModifiersSet).toNodeList()
    }

    override fun getNode(): FieldDeclaration {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE MODIFIERS OF FIELD ${fieldVariableDeclarator.nameAsString} FROM ${field.modifiers} TO $modifiers"
    }

    fun getParentNode() : ClassOrInterfaceDeclaration = clazz

    fun getNewModifiers() : NodeList<Modifier> = modifiers

    fun setNewModifiers(newModifiers : NodeList<Modifier>) {
        modifiers.clear()
        modifiers.addAll(newModifiers)
    }

    private fun mergeModifiersWith(other : ModifiersChangedField) {
        val mergedModifiers = ModifierSet(modifiers).merge(ModifierSet(other.getNewModifiers()))
        setNewModifiers(mergedModifiers)
        other.setNewModifiers(mergedModifiers)
    }
}

class InitializerChangedField(private val originalProject : Project, private val field: FieldDeclaration, private val initializer: Expression?) :
    Transformation {
    private val clazz: ClassOrInterfaceDeclaration = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val fieldToChangeInitializer = proj.getFieldByUUID(field.uuid)
        val fieldVariableDeclarator = fieldToChangeInitializer.variables.first() as VariableDeclarator
        val realInitializerToBeAdded = initializer?.clone()
        initializer?.let {
            realInitializerToBeAdded!!.accept(CorrectAllReferencesVisitor(originalProject, initializer), proj)
        }
        fieldVariableDeclarator.setInitializer(realInitializerToBeAdded)
        proj.initializeAllIndexes()
    }

    override fun getNode(): FieldDeclaration {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "CHANGE INITIALIZER OF FIELD ${fieldVariableDeclarator.nameAsString} TO $initializer"
    }

    fun getNewInitializer() : Expression? = initializer

    fun getType() : Type = (field.variables.first() as VariableDeclarator).type

    fun getParentNode() : ClassOrInterfaceDeclaration = clazz

    fun getOriginalProject() = originalProject
}

class MoveFieldIntraType(private val typeMembers : List<BodyDeclaration<*>>,
                         private val field : FieldDeclaration,
                         private val locationIndex: Int,
                         private val orderIndex: Int) : MoveTransformationIntraTypeOrCompilationUnit {

    private val type = field.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val typeToBeChanged = proj.getTypeByUUID(type.uuid)
        val fieldToBeMoved = proj.getFieldByUUID(field.uuid)
        typeToBeChanged.members.move(locationIndex, fieldToBeMoved)
    }

    override fun getNode(): FieldDeclaration  = field

    override fun getText(): String {
        val appendix = if((locationIndex + 1) >= typeMembers.size) {
            "AT THE END"
        } else {
            val member = typeMembers[locationIndex + 1]
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

    override fun getOrderIndex() = orderIndex

    fun getClass() = type
}

class MoveFieldInterTypes(private val addTransformation : AddField,
                          private val removeTransformation : RemoveField) : MoveTransformationInterClassOrCompilationUnit {
    private val field = addTransformation.getNode()

    override fun applyTransformation(proj: Project) {
        addTransformation.applyTransformation(proj)
        removeTransformation.applyTransformation(proj)
    }

    override fun getNode(): FieldDeclaration {
        return field
    }

    override fun getText(): String {
        val fieldVariableDeclarator = field.variables.first() as VariableDeclarator
        return "MOVE FIELD ${fieldVariableDeclarator.nameAsString} FROM CLASS ${removeTransformation.getParentNode().nameAsString} TO CLASS ${addTransformation.getParentNode().nameAsString}"
    }

    //    fun getClass() = addTransformation.getClass()

    override fun getRemoveTransformation() = removeTransformation

    override fun getAddTransformation() = addTransformation
}