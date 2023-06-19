package model.visitors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import model.Project
import model.content
import model.uuid

class UpdateRedundantNodesVisitor(private val originalRedundantNode : Node) : VoidVisitorAdapter<Project>() {

    override fun visit(n: CompilationUnit, redundantProject: Project) {
        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(CompilationUnit::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: ClassOrInterfaceDeclaration, redundantProject: Project) {
        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(ClassOrInterfaceDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: EnumDeclaration, redundantProject: Project) {
        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(EnumDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: EnumConstantDeclaration, redundantProject: Project) {
//        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(EnumConstantDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: FieldDeclaration, redundantProject: Project) {
//        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(FieldDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: MethodDeclaration, redundantProject: Project) {
//        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(MethodDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }

    override fun visit(n: ConstructorDeclaration, redundantProject: Project) {
//        super.visit(n, redundantProject)
        val correspondingRedundantNode = originalRedundantNode.findFirst(ConstructorDeclaration::class.java) {it.content == n.content}.get()
        redundantProject.updateUUIDOfNode(n.uuid, correspondingRedundantNode)
    }
    
}