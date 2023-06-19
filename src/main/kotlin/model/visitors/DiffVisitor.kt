package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration
import model.isClassOrInterfaceInsideAnotherClass
import model.uuid

class DiffVisitor(private val classOrInterfaceDeclarationsOnly: Boolean) : VoidVisitorAdapter<MutableList<Node>>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableList<Node>) {
        super.visit(n, arg)
        if (classOrInterfaceDeclarationsOnly) arg.add(n)
    }

    override fun visit(n: EnumDeclaration, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
    }

    override fun visit(n: FieldDeclaration, arg: MutableList<Node>) {
//        super.visit(n, arg)
        if (!classOrInterfaceDeclarationsOnly) arg.add(n)
    }

    override fun visit(n: MethodDeclaration, arg: MutableList<Node>) {
//        super.visit(n, arg)
        if (!classOrInterfaceDeclarationsOnly) arg.add(n)
    }

    override fun visit(n: ConstructorDeclaration, arg: MutableList<Node>) {
//        super.visit(n, arg)
        if (!classOrInterfaceDeclarationsOnly) arg.add(n)
    }
}