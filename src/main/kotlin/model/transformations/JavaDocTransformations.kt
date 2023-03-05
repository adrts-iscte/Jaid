package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import model.Conflict
import model.Project
import model.uuid

class SetJavaDoc(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>?, private val field : FieldDeclaration?, private val javaDocComment: JavadocComment, private val setOperation: String):
    Transformation {

    private val newJavadocComment = javaDocComment.clone()

    override fun applyTransformation(proj: Project) {
        if (callable == null && field == null) {
            val classModified = proj.getClassOrInterfaceByUUID(clazz.uuid)
            classModified?.setJavadocComment(newJavadocComment.content)
        } else if (callable != null) {
            if (callable.isConstructorDeclaration) {
                val constructorToChangeJavaDoc = proj.getConstructorByUUID(callable.uuid)
                constructorToChangeJavaDoc?.setJavadocComment(newJavadocComment.content)
            } else {
                val methodToChangeJavaDoc = proj.getMethodByUUID(callable.uuid)
                methodToChangeJavaDoc?.setJavadocComment(newJavadocComment.content)
            }
        } else {
            val fieldToChangeJavaDoc = proj.getFieldByUUID(field!!.uuid)
            fieldToChangeJavaDoc?.setJavadocComment(newJavadocComment.content)
        }
    }

    override fun getNode(): Node {
        return field ?: callable ?: clazz
    }

    override fun getText(): String {
        return if (callable == null && field == null) {
            "$setOperation JAVADOC OF CLASS ${clazz.nameAsString}"
        } else if (callable != null) {
            if (callable.isConstructorDeclaration) {
                "$setOperation JAVADOC OF CONSTRUCTOR ${(callable as ConstructorDeclaration).name}"
            } else {
                "$setOperation JAVADOC OF METHOD ${(callable as MethodDeclaration).name}"
            }
        } else {
            "$setOperation JAVADOC OF FIELD ${(field!!.variables.first() as VariableDeclarator).name}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}

class RemoveJavaDoc(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>?, private val field : FieldDeclaration?):
    Transformation {

    override fun applyTransformation(proj: Project) {
        if (callable == null && field == null) {
            val classModified = proj.getClassOrInterfaceByUUID(clazz.uuid)
            classModified?.setComment(LineComment(clazz.uuid.toString()))
        } else if (callable != null) {
            if(callable.isConstructorDeclaration) {
                val constructorToChangeJavaDoc = proj.getConstructorByUUID(callable.uuid)
                constructorToChangeJavaDoc?.setComment(LineComment(callable.uuid.toString()))
            } else {
                val methodToChangeJavaDoc = proj.getMethodByUUID(callable.uuid)
                methodToChangeJavaDoc?.setComment(LineComment(callable.uuid.toString()))
            }
        } else {
            val fieldToChangeJavaDoc = proj.getFieldByUUID(field!!.uuid)
            fieldToChangeJavaDoc?.setComment(LineComment(field.uuid.toString()))
        }
    }

    override fun getNode(): Node {
        return callable ?: clazz
    }

    override fun getText(): String {
        return if (callable == null && field == null) {
            "REMOVE JAVADOC FROM CLASS ${clazz.nameAsString}"
        } else if (callable != null) {
            if (callable.isConstructorDeclaration) {
                "REMOVE JAVADOC FROM CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
            } else {
                "REMOVE JAVADOC FROM METHOD ${(getNode() as MethodDeclaration).name}"
            }
        } else {
            "REMOVE JAVADOC FROM FIELD ${(field!!.variables.first() as VariableDeclarator).name}"
        }
    }

    override fun getListOfConflicts(commonAncestor: CompilationUnit, listOfTransformation: Set<Transformation>): List<Conflict> {
        TODO("Not yet implemented")
    }
}
