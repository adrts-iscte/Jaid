package model.visitors

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class FieldAndEnumConstantsUsesVisitor : VoidVisitorAdapter<MutableList<Expression>>() {

    override fun visit(n: FieldAccessExpr, arg: MutableList<Expression>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: NameExpr, arg: MutableList<Expression>) {
        super.visit(n, arg)
        arg.add(n)
    }

    /*override fun visit(n: VariableDeclarationExpr, arg: MutableList<Expression>) {
        super.visit(n, arg)
        if (n.variables.first().name.asString() == fieldName) {
            arg.add(n)
        }
    }*/
}