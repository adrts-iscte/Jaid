package model.visitors

import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class MethodCallExprVisitor : VoidVisitorAdapter<MutableList<MethodCallExpr>>() {

    override fun visit(n: MethodCallExpr, arg: MutableList<MethodCallExpr>) {
        super.visit(n, arg)
        arg.add(n)
    }
}