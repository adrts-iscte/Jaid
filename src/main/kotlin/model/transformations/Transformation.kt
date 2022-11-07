package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import model.Conflict

interface Transformation {
    fun applyTransformation(cu: CompilationUnit)
    fun getNode() : Node
    fun getText(): String
    fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>) : List<Conflict>
}