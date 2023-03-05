package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class FieldUsesVisitor : VoidVisitorAdapter<MutableList<Node>>() {

    override fun visit(n: FieldAccessExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: NameExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    /*override fun visit(n: VariableDeclarationExpr, arg: MutableList<Node>) {
        super.visit(n, arg)
        if (n.variables.first().name.asString() == fieldName) {
            arg.add(n)
        }
    }*/
}