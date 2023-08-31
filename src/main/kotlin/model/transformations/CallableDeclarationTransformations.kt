package model.transformations

import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.Type
import model.*
import model.visitors.CorrectAllReferencesVisitor
import java.lang.UnsupportedOperationException

class AddCallable(private val originalProject : Project, private val type : TypeDeclaration<*>, private val callable : CallableDeclaration<*>) :
    AddNodeTransformation, TransformationWithReferences(originalProject) {

    override fun applyTransformation(proj: Project) {
        val typeToHaveCallableAdded = proj.getTypeByUUID(type.uuid)!!
        val newCallable = callable.clone()
        newCallable.accept(CorrectAllReferencesVisitor(originalProject, callable), proj)
        val index = calculateIndexOfMemberToAdd(type, typeToHaveCallableAdded, callable.uuid)
        typeToHaveCallableAdded.members.add(index, newCallable)
        proj.updateIndexesWithNode(newCallable)
    }

    override fun getNode(): CallableDeclaration<*> {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "ADD CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
        } else {
            "ADD METHOD ${(getNode() as MethodDeclaration).name}"
        }
    }

    override fun getNewNode() : CallableDeclaration<*> = callable

    override fun getParentNode() : TypeDeclaration<*> = type

    override fun equals(other: Any?): Boolean {
        if (other !is AddCallable)
            return false
        return this.callable.content == other.callable.content && this.type.uuid == other.type.uuid
    }
}

class RemoveCallable(private val type : TypeDeclaration<*>, private val callable : CallableDeclaration<*>) :
    RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val typeToHaveCallableRemoved = proj.getTypeByUUID(type.uuid)!!
        val callableToRemove = if (callable.isConstructorDeclaration) {
            proj.getConstructorByUUID(callable.uuid)
        } else {
            proj.getMethodByUUID(callable.uuid)
        }
        typeToHaveCallableRemoved.remove(callableToRemove)
    }

    override fun getNode(): CallableDeclaration<*> {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "REMOVE CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
        } else {
            "REMOVE METHOD ${(getNode() as MethodDeclaration).name}"
        }
    }

    override fun getRemovedNode(): CallableDeclaration<*> = callable

    override fun getParentNode() : TypeDeclaration<*> = type

    override fun equals(other: Any?): Boolean {
        if (other !is RemoveCallable)
            return false
        return this.callable.uuid == other.callable.uuid
    }
}

class BodyChangedCallable(private val originalProject : Project, private val callable: CallableDeclaration<*>, private val newBody: BlockStmt?) :
    Transformation, TransformationWithReferences(originalProject) {
    private val type: TypeDeclaration<*> = callable.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val realBodyToBeAdded = newBody?.clone()
        newBody?.let {
            realBodyToBeAdded!!.accept(CorrectAllReferencesVisitor(originalProject, newBody), proj)
        }
        if(callable.isConstructorDeclaration) {
            val constructorToChangeBody = proj.getConstructorByUUID(callable.uuid)!!
            constructorToChangeBody.body = realBodyToBeAdded
        } else {
            val methodToChangeBody = proj.getMethodByUUID(callable.uuid)!!
            methodToChangeBody.setBody(realBodyToBeAdded)
        }
        proj.updateIndexesWithNode(realBodyToBeAdded)
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "CHANGE BODY OF CONSTRUCTOR ${(callable as ConstructorDeclaration).nameAsString}"
        } else {
            "CHANGE BODY OF METHOD ${(callable as MethodDeclaration).nameAsString}"
        }
    }

    fun getNewBody() : BlockStmt? = newBody

    fun getParentNode() : TypeDeclaration<*> = type

    fun getProject() = originalProject

    override fun equals(other: Any?): Boolean {
        if (other !is BodyChangedCallable)
            return false
        return this.callable.uuid == other.callable.uuid && this.newBody == other.newBody
    }
}

class ModifiersChangedCallable(private val callable: CallableDeclaration<*>, private val modifiers: NodeList<Modifier>) :
    Transformation {
    private val type: TypeDeclaration<*> = callable.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        if(callable.isConstructorDeclaration) {
            val constructorToChangeModifiers = proj.getConstructorByUUID(callable.uuid)
            constructorToChangeModifiers?.let{
                constructorToChangeModifiers.modifiers =
                    ModifierSet(constructorToChangeModifiers.modifiers).replaceModifiersBy(ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))).toNodeList()
            }
        } else {
            val methodToChangeModifiers = proj.getMethodByUUID(callable.uuid)
            methodToChangeModifiers?.let {
                methodToChangeModifiers.modifiers =
                    ModifierSet(methodToChangeModifiers.modifiers).replaceModifiersBy(ModifierSet(NodeList(modifiers.toMutableList().map { it.clone() }))).toNodeList()
            }
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE MODIFIERS OF CONSTRUCTOR ${constructor.nameAsString} FROM ${constructor.modifiers} TO $modifiers"
        } else {
            val method = callable as MethodDeclaration
            "CHANGE MODIFIERS OF METHOD ${method.nameAsString} FROM ${method.modifiers} TO $modifiers"
        }
    }

    fun getParentNode() : TypeDeclaration<*> = type

    fun getNewModifiers() : NodeList<Modifier> = modifiers
    fun setNewModifiers(newModifiers : NodeList<Modifier>) {
        modifiers.clear()
        modifiers.addAll(newModifiers)
    }

    private fun mergeModifiersWith(other : ModifiersChangedCallable) {
        val mergedModifiers = ModifierSet(modifiers).merge(ModifierSet(other.getNewModifiers()))
        setNewModifiers(mergedModifiers)
        other.setNewModifiers(mergedModifiers)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ModifiersChangedCallable)
            return false
        return this.callable.uuid == other.callable.uuid && this.modifiers == other.modifiers
    }
}

class ReturnTypeChangedMethod(private val originalProject : Project, private val method: MethodDeclaration, private val returnType: Type) :
    Transformation, TransformationWithReferences(originalProject) {
    private val type: TypeDeclaration<*> = method.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val methodToChangeReturnType = proj.getMethodByUUID(method.uuid)!!
        val newReturnType = returnType.clone()
        newReturnType.accept(CorrectAllReferencesVisitor(originalProject, returnType), proj)
        methodToChangeReturnType.type = newReturnType
        proj.updateIndexesWithNode(newReturnType)
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE RETURN TYPE OF METHOD ${method.nameAsString} FROM ${method.type} TO $returnType"
    }

    fun getNewReturnType() : Type = returnType

    fun getParentNode() : TypeDeclaration<*> = type

    override fun equals(other: Any?): Boolean {
        if (other !is ReturnTypeChangedMethod)
            return false
        return this.method.uuid == other.method.uuid && this.returnType == other.returnType
    }
}

class SignatureChanged(private val originalProject : Project,
                       private val callable: CallableDeclaration<*>,
                       private val parameters: NodeList<Parameter>,
                       private val newName: SimpleName
) : Transformation, TransformationWithReferences(originalProject) {
    private val oldCallableParameters: NodeList<Parameter> = callable.parameters
    private val oldMethodName: SimpleName = callable.name
    private val type: TypeDeclaration<*> = callable.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val newParameters = NodeList(parameters.toMutableList().map { it.clone() })
        newParameters.types.forEach { type ->
            val originalType = parameters.types.find { type == it }!!
            type.accept(CorrectAllReferencesVisitor(originalProject, originalType), proj)
        }
        if(callable.isConstructorDeclaration) {
            val constructorToBeChanged = proj.getConstructorByUUID(callable.uuid)!!
            constructorToBeChanged.parameters = newParameters
        } else {
            val methodToBeChanged = proj.getMethodByUUID(callable.uuid)!!
            methodToBeChanged.parameters = newParameters
            val realNameToBeSet = newName.clone()
            proj.renameAllMethodCalls(methodToBeChanged.uuid, realNameToBeSet.asString())
            methodToBeChanged.name = realNameToBeSet
        }
        newParameters.forEach {
            proj.updateIndexesWithNode(it)
        }
    }

    override fun getNode(): CallableDeclaration<*> = callable

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE PARAMETERS OF CONSTRUCTOR ${constructor.nameAsString} TO $parameters"
        } else {
            val method = callable as MethodDeclaration
            if (parametersChanged() && nameChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $parameters AND RENAME TO $newName"
            } else if (parametersChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $parameters"
            } else {
                "RENAME METHOD $oldMethodName TO $newName"
            }
        }

    }

    fun getParentNode() : TypeDeclaration<*> = type

    fun signatureChanged() : Boolean = nameChanged() || parametersTypesChanged()

    fun nameChanged() : Boolean = oldMethodName != newName
    fun parametersChanged() : Boolean = oldCallableParameters != parameters
    private fun parametersTypesChanged() : Boolean = oldCallableParameters.types != parameters.types
    fun getNewParameters() : NodeList<Parameter> = parameters
    fun getNewName() : SimpleName = newName

    override fun equals(other: Any?): Boolean {
        if (other !is SignatureChanged)
            return false
        return this.callable.uuid == other.callable.uuid && this.parameters == other.parameters && this.newName == other.newName
    }
}

class MoveCallableIntraType(private val clazzMembers : List<BodyDeclaration<*>>,
                            private val callable : CallableDeclaration<*>,
                            private val locationIndex: Int,
                            private val orderIndex: Int) : MoveTransformationIntraTypeOrCompilationUnit {

    private val type = callable.parentNode.get() as TypeDeclaration<*>

    override fun applyTransformation(proj: Project) {
        val classToBeChanged = proj.getTypeByUUID(type.uuid)!!
        val callableToBeMoved = if (callable.isConstructorDeclaration) {
            proj.getConstructorByUUID(callable.uuid)
        } else {
            proj.getMethodByUUID(callable.uuid)
        }
//                classToBeChanged.remove(callableToBeMoved)
//                classToBeChanged.members.add(max(0, locationIndex - 1), callableToBeMoved)
//                val locationIndex = if (moveBeforeNode != null) {
//                    val nextNode = classToBeChanged.members.find { it.uuid == moveBeforeNode.uuid }!!
//                    classToBeChanged.members.indexOf(nextNode)
//                } else {
//                    clazz.members.size
//                }
        classToBeChanged.members.move(locationIndex, callableToBeMoved)
    }

    override fun getNode(): Node {
        return callable
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
        return if(callable.isConstructorDeclaration) {
            "MOVE CONSTRUCTOR ${callable.nameAsString} $appendix"
        } else {
            "MOVE METHOD ${callable.nameAsString} $appendix"
        }
    }

    override fun getOrderIndex() = orderIndex

    fun getClass() = type

    override fun equals(other: Any?): Boolean {
        if (other !is MoveCallableIntraType)
            return false
        return this.callable.uuid == other.callable.uuid && this.locationIndex == other.locationIndex && this.orderIndex == other.orderIndex
    }
}

class MoveCallableInterTypes(private val addTransformation : AddCallable,
                             private val removeTransformation : RemoveCallable) : MoveTransformationInterClassOrCompilationUnit {
    private val callable = addTransformation.getNode()

    override fun applyTransformation(proj: Project) {
        addTransformation.applyTransformation(proj)
        removeTransformation.applyTransformation(proj)
    }

    override fun getNode(): CallableDeclaration<*> {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            "MOVE CONSTRUCTOR ${callable.nameAsString} FROM CLASS ${removeTransformation.getParentNode().nameAsString} TO CLASS ${addTransformation.getParentNode().nameAsString}"
        } else {
            "MOVE METHOD ${callable.nameAsString} FROM CLASS ${removeTransformation.getParentNode().nameAsString} TO CLASS ${addTransformation.getParentNode().nameAsString}"
        }
    }

    //    fun getClass() = addTransformation.getClass()

    override fun getRemoveTransformation() = removeTransformation

    override fun getAddTransformation() = addTransformation

    override fun equals(other: Any?): Boolean {
        if (other !is MoveCallableInterTypes)
            return false
        return this.removeTransformation.getParentNode().uuid == other.removeTransformation.getParentNode().uuid &&
               this.addTransformation.getParentNode().uuid == other.addTransformation.getParentNode().uuid &&
               this.addTransformation.getNode().uuid == other.addTransformation.getNode().uuid
    }
}
