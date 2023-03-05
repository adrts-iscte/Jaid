package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import model.Conflict
import model.Project

interface Transformation {
    fun applyTransformation(proj: Project)
    fun getNode() : Node
    fun getText(): String
    fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>) : List<Conflict>
}

interface AddNodeTransformation : Transformation {
    fun getNewNode() : Node
    fun getParentNode() : Node
}

interface RemoveNodeTransformation : Transformation {
    fun getRemovedNode() : Node
    fun getParentNode() : Node
}

interface MoveTransformationIntraClassOrCompilationUnit : Transformation {
    fun getOrderIndex() : Int
}

interface MoveTransformationInterClassOrCompilationUnit : Transformation {
    fun getRemoveTransformation() : RemoveNodeTransformation
    fun getAddTransformation() : AddNodeTransformation
}