package model.transformations

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import model.Project
import model.asString
import model.getNodesName
import model.uuid

class SetJavaDoc(private val commentedNode : Node, private val javaDocComment: JavadocComment, private val setOperation: String):
    Transformation {

    private val newJavadocComment = javaDocComment.clone()

    override fun applyTransformation(proj: Project) {
        when(commentedNode) {
            is TypeDeclaration<*> -> {
                val typeModified = proj.getTypeByUUID(commentedNode.uuid)!!
                typeModified.setJavadocComment(newJavadocComment.content)
            }
            is CallableDeclaration<*> -> {
                val callableToChangeJavaDoc = if (commentedNode.isConstructorDeclaration) {
                    proj.getConstructorByUUID(commentedNode.uuid)
                } else {
                    proj.getMethodByUUID(commentedNode.uuid)
                }!!
                callableToChangeJavaDoc.setJavadocComment(newJavadocComment.content)
            }
            is FieldDeclaration -> {
                val fieldToChangeJavaDoc = proj.getFieldByUUID(commentedNode.uuid)!!
                fieldToChangeJavaDoc.setJavadocComment(newJavadocComment.content)
            }
            is EnumConstantDeclaration -> {
                val enumConstantToChangeJavaDoc = proj.getEnumConstantByUUID(commentedNode.uuid)!!
                enumConstantToChangeJavaDoc.setJavadocComment(newJavadocComment.content)
            }
        }
    }

    override fun getNode() = commentedNode

    override fun getText(): String {
        return "$setOperation JAVADOC OF ${commentedNode.asString} ${commentedNode.getNodesName}"
    }

    fun isAddOperation() = setOperation == "ADD"

    fun getJavaDocComment() : JavadocComment = javaDocComment
}

class RemoveJavaDoc(private val commentedNode : Node):
    Transformation {

    override fun applyTransformation(proj: Project) {
        when(commentedNode) {
            is TypeDeclaration<*> -> {
                val typeModified = proj.getClassOrInterfaceByUUID(commentedNode.uuid)!!
                typeModified.setComment(LineComment(commentedNode.uuid.toString()))
            }
            is CallableDeclaration<*> -> {
                val callableToRemoveJavaDoc = if (commentedNode.isConstructorDeclaration) {
                    proj.getConstructorByUUID(commentedNode.uuid)
                } else {
                    proj.getMethodByUUID(commentedNode.uuid)
                }!!
                callableToRemoveJavaDoc.setComment(LineComment(commentedNode.uuid.toString()))
            }
            is FieldDeclaration -> {
                val fieldToChangeJavaDoc = proj.getFieldByUUID(commentedNode.uuid)!!
                fieldToChangeJavaDoc.setComment(LineComment(commentedNode.uuid.toString()))
            }
            is EnumConstantDeclaration -> {
                val enumConstantToChangeJavaDoc = proj.getEnumConstantByUUID(commentedNode.uuid)!!
                enumConstantToChangeJavaDoc.setComment(LineComment(commentedNode.uuid.toString()))
            }
        }
    }

    override fun getNode() = commentedNode

    override fun getText(): String {
        return "REMOVE JAVADOC FROM ${commentedNode.asString} ${commentedNode.getNodesName}"
    }

}
