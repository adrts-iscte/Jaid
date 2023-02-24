package model.visitors

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import model.UUID
import model.uuid
import kotlin.io.path.Path

class SetupProjectVisitor : VoidVisitorAdapter<MutableMap<UUID, Node>>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableMap<UUID, Node>) {
        super.visit(n, arg)
        arg[n.uuid] = n
    }

    override fun visit(n: FieldDeclaration, arg: MutableMap<UUID, Node>) {
        super.visit(n, arg)
        arg[n.uuid] = n
    }

    override fun visit(n: MethodDeclaration, arg: MutableMap<UUID, Node>) {
        super.visit(n, arg)
        arg[n.uuid] = n
    }

    override fun visit(n: ConstructorDeclaration, arg: MutableMap<UUID, Node>) {
        super.visit(n, arg)
        arg[n.uuid] = n
    }
}

fun loadFile(path : String) : CompilationUnit {
    val project = StaticJavaParser.parse(Path(path))
    val setupProjectVisitor = SetupProjectVisitor()
    project.accept(setupProjectVisitor, null)
    return project
}
