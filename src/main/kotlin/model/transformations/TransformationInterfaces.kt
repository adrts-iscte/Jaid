package model.transformations

import com.github.javaparser.ast.Node
import model.Project

interface Transformation {
    fun applyTransformation(proj: Project)
    fun getNode() : Node
    fun getText(): String
}

interface AddNodeTransformation : Transformation {
    fun getNewNode() : Node
    fun getParentNode() : Node
}

interface RemoveNodeTransformation : Transformation {
    fun getRemovedNode() : Node
    fun getParentNode() : Node
}

interface MoveTransformationIntraTypeOrCompilationUnit : Transformation {
    fun getOrderIndex() : Int
}

interface MoveTransformationInterClassOrCompilationUnit : Transformation {
    fun getRemoveTransformation() : RemoveNodeTransformation
    fun getAddTransformation() : AddNodeTransformation
}