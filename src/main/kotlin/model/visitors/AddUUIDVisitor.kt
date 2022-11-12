package model.visitors

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import model.uuid
import java.io.File
import kotlin.io.path.Path

class AddUUIDVisitor : VoidVisitorAdapter<Node>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Node?) {
        super.visit(n, arg)
        n.uuid
    }

    override fun visit(n: FieldDeclaration, arg: Node?) {
        super.visit(n, arg)
        n.uuid
    }

    override fun visit(n: MethodDeclaration, arg: Node?) {
        super.visit(n, arg)
        n.uuid
    }

    override fun visit(n: ConstructorDeclaration, arg: Node?) {
        super.visit(n, arg)
        n.uuid
    }
}

fun loadProject(path : String) : CompilationUnit {
    val project = StaticJavaParser.parse(Path(path))
    val addUUIDVisitor = AddUUIDVisitor()
    project.accept(addUUIDVisitor, null)
    return project
}
