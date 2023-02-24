package model.visitors

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver


class MethodCallExprVisitor : VoidVisitorAdapter<MutableMap<MethodCallExpr, MethodDeclaration>>() {

    override fun visit(n: MethodCallExpr, arg: MutableMap<MethodCallExpr, MethodDeclaration>) {
        super.visit(n, arg)
        val solver = CombinedTypeSolver()
        solver.add(ReflectionTypeSolver())
        val jpf = JavaParserFacade.get(solver).solve(n)
        if (jpf.isSolved) {
            val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
            methodDecl?.let {
                arg[n] = methodDecl
            }
        }
    }

}