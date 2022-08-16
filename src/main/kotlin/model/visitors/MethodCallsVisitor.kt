package model.visitors

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.uuid

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

}