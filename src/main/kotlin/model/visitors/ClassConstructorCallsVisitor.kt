package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.uuid

class ClassConstructorCallsVisitor : VoidVisitorAdapter<MutableList<Node>>() {
    // Ter atenção ao ExplicitConstructorInvocationExpr
    override fun visit(n: ObjectCreationExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: ClassOrInterfaceType, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }
}

class ObjectCreationVisitor(private val clazz : ClassOrInterfaceDeclaration) : VoidVisitorAdapter<MutableList<ObjectCreationExpr>>() {
    // Ter atenção ao ExplicitConstructorInvocationExpr
    override fun visit(n: ObjectCreationExpr, arg: MutableList<ObjectCreationExpr>) {
        super.visit(n, arg)
        val solver = CombinedTypeSolver()
        val jpf = JavaParserFacade.get(solver).solve(n)
        if (jpf.isSolved) {
            val constructorDecl = (jpf.correspondingDeclaration as JavaParserConstructorDeclaration<*>).wrappedNode
            if (constructorDecl.parentNode.get().uuid == clazz.uuid) {
                arg.add(n)
            }
        }
    }


}

class ClassOrInterfaceTypeVisitor(private val clazz : ClassOrInterfaceDeclaration) : VoidVisitorAdapter<MutableList<ClassOrInterfaceType>>() {

    override fun visit(n: ClassOrInterfaceType, arg: MutableList<ClassOrInterfaceType>) {
        super.visit(n, arg)
        if (clazz.name == n.name) {
            arg.add(n)
        }
    }


}

class ExplicitConstructorCallsVisitor(private val clazz : ClassOrInterfaceDeclaration) : VoidVisitorAdapter<MutableList<ObjectCreationExpr>>() {

    override fun visit(n: ObjectCreationExpr, arg: MutableList<ObjectCreationExpr>) {
        super.visit(n, arg)
        arg.add(n)
    }

}
/*
class MethodCallsVisitor(private val method : MethodDeclaration) : VoidVisitorAdapter<MutableList<MethodCallExpr>>() {

    override fun visit(n: MethodCallExpr, arg: MutableList<MethodCallExpr>) {
        super.visit(n, arg)
        val solver = CombinedTypeSolver()
        val jpf = JavaParserFacade.get(solver).solve(n)
        if (jpf.isSolved) {
            val methodDecl = (jpf.correspondingDeclaration as JavaParserMethodDeclaration).wrappedNode
            if (methodDecl.uuid == method.uuid) {
                arg.add(n)
            }
        }
    }
}*/