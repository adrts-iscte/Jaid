package model.transformations

import com.github.javaparser.ast.CompilationUnit
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

class AddCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) :
    AddNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val classToHaveCallableAdded = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToHaveCallableAdded?.let {
            val newCallable = callable.clone()
            val index = calculateIndexOfMemberToAdd(clazz, classToHaveCallableAdded, callable.uuid)
            classToHaveCallableAdded.members.add(index, newCallable)
        }
//        newCallable.generateUUID()
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

    override fun getParentNode() : ClassOrInterfaceDeclaration = clazz
}

class RemoveCallableDeclaration(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>) :
    RemoveNodeTransformation {

    override fun applyTransformation(proj: Project) {
        val classToHaveCallableRemoved = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToHaveCallableRemoved?.let {
            if (callable.isConstructorDeclaration) {
                val constructor = callable as ConstructorDeclaration
                val constructorToRemove = proj.getConstructorByUUID(constructor.uuid)
                constructorToRemove?.let {
                    classToHaveCallableRemoved.remove(constructorToRemove)
                }
            } else {
                val method = callable as MethodDeclaration
                val methodToRemove = proj.getMethodByUUID(method.uuid)
                methodToRemove?.let {
                    classToHaveCallableRemoved.remove(methodToRemove)
                }
            }
        }
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

    override fun getParentNode() : ClassOrInterfaceDeclaration = clazz
}

class ParametersChangedCallable(private val clazz: ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>, private val newParameters: NodeList<Parameter>) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        if(callable.isConstructorDeclaration) {
            val constructorToChangeParameters = proj.getConstructorByUUID(callable.uuid)
            constructorToChangeParameters?.parameters = NodeList(newParameters.toMutableList().map { it.clone() })
        } else {
            val methodToChangeParameters = proj.getMethodByUUID(callable.uuid)
            methodToChangeParameters?.parameters = NodeList(newParameters.toMutableList().map { it.clone() })
        }
    }

    override fun getNode(): Node {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE PARAMETERS OF CONSTRUCTOR ${constructor.nameAsString} TO $newParameters"
        } else {
            val method = callable as MethodDeclaration
            "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters"
        }
    }

    fun getNewParameters() : NodeList<Parameter> = newParameters
}

class BodyChangedCallable(private val callable: CallableDeclaration<*>, private val newBody: BlockStmt) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        if(callable.isConstructorDeclaration) {
            val constructorToChangeBody = proj.getConstructorByUUID(callable.uuid)
            constructorToChangeBody?.body = newBody.clone()
        } else {
            val methodToChangeBody = proj.getMethodByUUID(callable.uuid)
            methodToChangeBody?.let {
                val realBodyToBeAdded = newBody.clone()
                methodToChangeBody.setBody(realBodyToBeAdded)
                realBodyToBeAdded.accept(CorrectAllReferencesVisitor(newBody), proj)
            }
        }
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

    fun getNewBody() : BlockStmt = newBody.clone()

}

class ModifiersChangedCallable(private val callable: CallableDeclaration<*>, private val modifiers: NodeList<Modifier>) :
    Transformation {

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
}

class ReturnTypeChangedMethod(private val method: MethodDeclaration, private val newType: Type) :
    Transformation {

    override fun applyTransformation(proj: Project) {
        val methodToChangeReturnType = proj.getMethodByUUID(method.uuid)
        methodToChangeReturnType?.type = newType.clone()
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "CHANGE RETURN TYPE OF METHOD ${method.nameAsString} FROM ${method.type} TO $newType"
    }

    fun getNewReturnType() : Type = newType

}

class RenameMethod(private val clazz : ClassOrInterfaceDeclaration, private val method : MethodDeclaration, private val newName: String) :
    Transformation {
    private val oldMethodName: String = method.nameAsString

    override fun applyTransformation(proj: Project) {
        val methodToRename = proj.getMethodByUUID(method.uuid)
        println(methodToRename)
        methodToRename?.let {
            proj.renameAllMethodCalls(methodToRename.uuid, newName)
            methodToRename.setName(newName)
        }
    }

    override fun getNode(): Node {
        return method
    }

    override fun getText(): String {
        return "RENAME METHOD $oldMethodName TO $newName"
    }

    fun getNewName() : String = newName

}

class ParametersAndOrNameChangedCallable(
    private val callable: CallableDeclaration<*>,
    private val newParameters: NodeList<Parameter>,
    private val newName: SimpleName
) : Transformation {
    private val oldCallableParameters: NodeList<Parameter> = callable.parameters
    private val oldMethodName: SimpleName = callable.name
    private val clazz: ClassOrInterfaceDeclaration = callable.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        if(callable.isConstructorDeclaration) {
            val constructorToBeChanged = proj.getConstructorByUUID(callable.uuid)
            constructorToBeChanged?.parameters = NodeList(newParameters.toMutableList().map { it.clone() })
        } else {
            val methodToBeChanged = proj.getMethodByUUID(callable.uuid)
            methodToBeChanged?.let {
                methodToBeChanged.parameters = NodeList(newParameters.toMutableList().map { it.clone() })
                val realNameToBeSet = newName.clone()
                proj.renameAllMethodCalls(methodToBeChanged.uuid, realNameToBeSet.asString())
                methodToBeChanged.name = realNameToBeSet
            }
        }
    }

    override fun getNode(): CallableDeclaration<*> {
        return callable
    }

    override fun getText(): String {
        return if(callable.isConstructorDeclaration) {
            val constructor = callable as ConstructorDeclaration
            "CHANGE PARAMETERS OF CONSTRUCTOR ${constructor.nameAsString} TO $newParameters"
        } else {
            val method = callable as MethodDeclaration
            if (parametersChanged() && nameChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters AND RENAME TO $newName"
            } else if (parametersChanged()) {
                "CHANGE PARAMETERS OF METHOD ${method.nameAsString} TO $newParameters"
            } else {
                "RENAME METHOD $oldMethodName TO $newName"
            }
        }

    }

    fun getParentNode() : ClassOrInterfaceDeclaration = clazz

    fun signatureChanged() : Boolean = nameChanged() || parametersTypesChanged()

    fun nameChanged() : Boolean = oldMethodName != newName
    fun parametersChanged() : Boolean = oldCallableParameters != newParameters
    fun parametersTypesChanged() : Boolean = oldCallableParameters.types != newParameters.types
    fun getNewParameters() : NodeList<Parameter> = newParameters
    fun getNewName() : SimpleName = newName
}

class MoveCallableIntraClass(private val clazzMembers : List<BodyDeclaration<*>>,
                            private val callable : CallableDeclaration<*>,
                            private val locationIndex: Int,
                            private val orderIndex: Int) : MoveTransformationIntraClassOrCompilationUnit {

    private val clazz = callable.parentNode.get() as ClassOrInterfaceDeclaration

    override fun applyTransformation(proj: Project) {
        val classToBeChanged = proj.getClassOrInterfaceByUUID(clazz.uuid)
        classToBeChanged?.let {
            val callableToBeMoved = if (callable.isConstructorDeclaration) {
                proj.getConstructorByUUID(callable.uuid)
            } else {
                proj.getMethodByUUID(callable.uuid)
            }
            callableToBeMoved?.let {
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
        }
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

    fun getClass() = clazz
}

class MoveCallableInterClasses(private val addTransformation : AddCallableDeclaration,
                   private val removeTransformation : RemoveCallableDeclaration) : MoveTransformationInterClassOrCompilationUnit {
    private val callable = addTransformation.getNode() as CallableDeclaration<*>

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
}
