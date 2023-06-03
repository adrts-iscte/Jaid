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

class AddField(private val originalProject : Project, private val type : TypeDeclaration<*>, private val field : FieldDeclaration) :
    AddNodeTransformation, TransformationWithReferences(originalProject) {

    override fun applyTransformation(proj: Project) {
        val typeToHaveFieldAdded = proj.getTypeByUUID(type.uuid)!!
        val newField = field.clone()
        newField.accept(CorrectAllReferencesVisitor(originalProject, field), proj)
        val index = calculateIndexOfMemberToAdd(type, typeToHaveFieldAdded, field.uuid)
        typeToHaveFieldAdded.members.add(index, newField)
        proj.updateIndexesWithNode(newField)
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

    override fun equals(other: Any?): Boolean {
        if (other !is AddField)
            return false
        return this.field.content == other.field.content && this.type.uuid == other.type.uuid
    }
}

class RemoveField(private val type : TypeDeclaration<*>, private val field : FieldDeclaration) :
    RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val typeToHaveFieldRemoved = proj.getClassOrInterfaceByUUID(type.uuid)!!
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

    override fun equals(other: Any?): Boolean {
        if (other !is RemoveField)
            return false
        return this.field.uuid == other.field.uuid
    }
}

class RenameField(private val field: FieldDeclaration, private val newName: SimpleName) :
    Transformation {
    private val oldFieldName: String = field.name.asString()
    private val type: TypeDeclaration<*> = field.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val fieldToRename = proj.getFieldByUUID(field.uuid)!!
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

    override fun equals(other: Any?): Boolean {
        if (other !is RenameField)
            return false
        return this.field.uuid == other.field.uuid && this.newName == other.newName
    }
}

class TypeChangedField(private val originalProject : Project, private val field: FieldDeclaration, private val type: Type) :
    Transformation, TransformationWithReferences(originalProject) {
    private val clazz: ClassOrInterfaceDeclaration = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val fieldToChangeType = proj.getFieldByUUID(field.uuid)!!
        val fieldVariableDeclarator = fieldToChangeType.variables.first() as VariableDeclarator
        val newType = type.clone()
        newType.accept(CorrectAllReferencesVisitor(originalProject, type), proj)
        fieldVariableDeclarator.type = newType
        proj.updateIndexesWithNode(newType)
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

    override fun equals(other: Any?): Boolean {
        if (other !is TypeChangedField)
            return false
        return this.field.uuid == other.field.uuid && this.type == other.type
    }
}

class ModifiersChangedField(private val field: FieldDeclaration, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val newModifiersSet = ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))
    private val type: TypeDeclaration<*> = field.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val fieldToChangeModifiers = proj.getFieldByUUID(field.uuid)!!
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

    fun getParentNode() : TypeDeclaration<*> = type

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

    override fun equals(other: Any?): Boolean {
        if (other !is ModifiersChangedField)
            return false
        return this.field.uuid == other.field.uuid && this.modifiers == other.modifiers
    }
}

class InitializerChangedField(private val originalProject : Project, private val field: FieldDeclaration, private val initializer: Expression?) :
    Transformation, TransformationWithReferences(originalProject) {
    private val clazz: ClassOrInterfaceDeclaration = field.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val fieldToChangeInitializer = proj.getFieldByUUID(field.uuid)!!
        val fieldVariableDeclarator = fieldToChangeInitializer.variables.first() as VariableDeclarator
        val realInitializerToBeAdded = initializer?.clone()
        initializer?.let {
            realInitializerToBeAdded!!.accept(CorrectAllReferencesVisitor(originalProject, initializer), proj)
        }
        fieldVariableDeclarator.setInitializer(realInitializerToBeAdded)
        proj.updateIndexesWithNode(realInitializerToBeAdded)
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

    override fun equals(other: Any?): Boolean {
        if (other !is InitializerChangedField)
            return false
        return this.field.uuid == other.field.uuid && this.initializer == other.initializer
    }
}

class MoveFieldIntraType(private val typeMembers : List<BodyDeclaration<*>>,
                         private val field : FieldDeclaration,
                         private val locationIndex: Int,
                         private val orderIndex: Int) : MoveTransformationIntraTypeOrCompilationUnit {

    private val type = field.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val typeToBeChanged = proj.getTypeByUUID(type.uuid)!!
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

    override fun equals(other: Any?): Boolean {
        if (other !is MoveFieldIntraType)
            return false
        return this.field.uuid == other.field.uuid && this.locationIndex == other.locationIndex && this.orderIndex == other.orderIndex
    }
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

    override fun equals(other: Any?): Boolean {
        if (other !is MoveFieldInterTypes)
            return false
        return this.removeTransformation.getParentNode().uuid == other.removeTransformation.getParentNode().uuid &&
               this.addTransformation.getParentNode().uuid == other.addTransformation.getParentNode().uuid &&
               this.addTransformation.getNode().uuid == other.addTransformation.getNode().uuid
    }
}