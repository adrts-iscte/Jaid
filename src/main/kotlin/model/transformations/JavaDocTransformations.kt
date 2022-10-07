package model.transformations

import Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.JavadocComment
import model.uuid

class SetJavaDoc(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>?, private val javaDocComment: JavadocComment, private val setOperation: String): Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if (callable == null) {
            classModified.setJavadocComment(javaDocComment.content)
        } else {
            if(callable.isConstructorDeclaration) {
                val constructor = callable as ConstructorDeclaration
                val constructorToChangeJavaDoc = classModified.constructors.find { it.uuid == constructor.uuid }!!
                constructorToChangeJavaDoc.setJavadocComment(javaDocComment.content)
            } else {
                val method = callable as MethodDeclaration
                val methodToChangeJavaDoc = classModified.methods.find { it.uuid == method.uuid }!!
                methodToChangeJavaDoc.setJavadocComment(javaDocComment.content)
            }
        }
    }

    override fun getNode(): Node {
        return callable ?: clazz
    }

    override fun getText(): String {
        return if (callable == null) {
            "$setOperation JAVADOC OF CLASS ${clazz.nameAsString}"
        } else {
            if (callable.isConstructorDeclaration) {
                "$setOperation JAVADOC OF CONSTRUCTOR ${(callable as ConstructorDeclaration).name}"
            } else {
                "$setOperation JAVADOC OF METHOD ${(callable as MethodDeclaration).name}"
            }
        }
    }
}

class RemoveJavaDoc(private val clazz : ClassOrInterfaceDeclaration, private val callable : CallableDeclaration<*>?): Transformation {

    override fun applyTransformation(cu: CompilationUnit) {
        val classModified = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == clazz.uuid }!!
        if (callable == null) {
            classModified.removeJavaDocComment()
        } else {
            if(callable.isConstructorDeclaration) {
                val constructor = callable as ConstructorDeclaration
                val constructorToChangeJavaDoc = classModified.constructors.find { it.uuid == constructor.uuid }!!
                constructorToChangeJavaDoc.removeJavaDocComment()
            } else {
                val method = callable as MethodDeclaration
                val methodToChangeJavaDoc = classModified.methods.find { it.uuid == method.uuid }!!
                methodToChangeJavaDoc.removeJavaDocComment()
            }
        }
    }

    override fun getNode(): Node {
        return callable ?: clazz
    }

    override fun getText(): String {
        return if (callable == null) {
            "REMOVE JAVADOC FROM CLASS ${clazz.nameAsString}"
        } else {
            if (callable.isConstructorDeclaration) {
                "REMOVE JAVADOC FROM CONSTRUCTOR ${(getNode() as ConstructorDeclaration).name}"
            } else {
                "REMOVE JAVADOC FROM METHOD ${(getNode() as MethodDeclaration).name}"
            }
        }
    }
}
