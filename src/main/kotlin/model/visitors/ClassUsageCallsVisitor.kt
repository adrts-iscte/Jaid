package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class ClassUsageCallsVisitor : VoidVisitorAdapter<MutableList<Node>>() {
    // Ter atenção ao ExplicitConstructorInvocationExpr
    override fun visit(n: ObjectCreationExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: ClassOrInterfaceType, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: NameExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }
}