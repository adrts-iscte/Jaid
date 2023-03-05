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