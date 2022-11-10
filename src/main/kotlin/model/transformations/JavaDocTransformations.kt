package model.transformations

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import model.Conflict
import model.uuid

class SetJavaDoc(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>?, private val field : FieldDeclaration?, private val javaDocComment: JavadocComment, private val setOperation: String):
    Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if (callable == null && field == null) {
            classModified.setJavadocComment(javaDocComment.content)
        } else if (callable != null) {
            if(callable.isConstructorDeclaration) {
                val constructor = callable as ConstructorDeclaration
                val constructorToChangeJavaDoc = classModified.constructors.find { it.uuid == constructor.uuid }!!
                constructorToChangeJavaDoc.setJavadocComment(javaDocComment.content)
            } else {
                val method = callable as MethodDeclaration
                val methodToChangeJavaDoc = classModified.methods.find { it.uuid == method.uuid }!!
                methodToChangeJavaDoc.setJavadocComment(javaDocComment.content)
            }
        } else {
            val fieldToChangeJavaDoc = classModified.fields.find { it.uuid == field!!.uuid }!!
            fieldToChangeJavaDoc.setJavadocComment(javaDocComment.content)
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

    override fun applyTransformation(cu: CompilationUnit) {
        val classModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if (callable == null && field == null) {
            classModified.removeJavaDocComment()
            classModified.setComment(LineComment(clazz.uuid))
        } else if (callable != null) {
            if(callable.isConstructorDeclaration) {
                val constructor = callable as ConstructorDeclaration
                val constructorToChangeJavaDoc = classModified.constructors.find { it.uuid == constructor.uuid }!!
                constructorToChangeJavaDoc.removeJavaDocComment()
                constructorToChangeJavaDoc.setComment(LineComment(constructor.uuid))
            } else {
                val method = callable as MethodDeclaration
                val methodToChangeJavaDoc = classModified.methods.find { it.uuid == method.uuid }!!
                methodToChangeJavaDoc.removeJavaDocComment()
                methodToChangeJavaDoc.setComment(LineComment(method.uuid))
            }
        } else {
            val fieldToChangeJavaDoc = classModified.fields.find { it.uuid == field!!.uuid }!!
            fieldToChangeJavaDoc.removeJavaDocComment()
            fieldToChangeJavaDoc.setComment(LineComment(field!!.uuid))
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
