package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.visitors.FieldUsesVisitor
import model.visitors.MethodCallsVisitor
import java.util.*

val Node.uuid: String?
    get() = this.comment.orElse(null)?.content

fun Node.generateUUID() {
    this.setComment(LineComment(UUID.randomUUID().toString()))
}

fun renameAllFieldUses(cu: CompilationUnit, fieldToRename: FieldDeclaration, oldName: String, newName: String) {
    val listOfFieldUses = mutableListOf<Node>()
    val fieldUsesVisitor = FieldUsesVisitor(oldName)
    cu.accept(fieldUsesVisitor, listOfFieldUses)

    val solver = CombinedTypeSolver()
    solveNameExpr(listOfFieldUses, solver, fieldToRename, newName)
    solveFieldAccessExpr(listOfFieldUses, solver, fieldToRename, newName)
//    model.solveVariableDeclarationExpr(listOfFieldUses, solver, fieldToRename, newName)

    println(listOfFieldUses)
}

fun solveNameExpr(listOfFieldUses: MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration, newName: String) {
    listOfFieldUses.filterIsInstance<NameExpr>().forEach {
        val jpf = JavaParserFacade.get(solver).solve(it)
        if (jpf.isSolved) {
            val asd = jpf.correspondingDeclaration
            when (jpf.correspondingDeclaration) {
                is JavaParserFieldDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                        it.setName(newName)
                    }
                }
                is JavaParserVariableDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                        it.setName(newName)
                    }
                }
            }
        }
    }
}

fun solveFieldAccessExpr(listOfFieldUses :  MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration , newName: String) {
    listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
        val jpf = JavaParserFacade.get(solver).solve(it)
        if (jpf.isSolved) {
            when (jpf.correspondingDeclaration) {
                is JavaParserFieldDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                        it.setName(newName)
                    }
                }
                is JavaParserVariableDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                        it.setName(newName)
                    }
                }
            }
        }
    }
}

fun solveVariableDeclarationExpr(listOfFieldUses :  MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration , newName: String) {
    listOfFieldUses.filterIsInstance<VariableDeclarationExpr>().forEach {
        val jpf = JavaParserFacade.get(solver).solve(it)
        if (jpf.isSolved) {
            when (jpf.correspondingDeclaration) {
                is JavaParserFieldDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                        it.variables.first().setName(newName)
                    }
                }
                is JavaParserVariableDeclaration -> if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
                    it.variables.first().setName(newName)
                }
            }
        }
    }
}

fun renameAllMethodCalls(cu: CompilationUnit, fieldToRename: MethodDeclaration, newName: String) {
    val listOfMethodCalls = mutableListOf<MethodCallExpr>()
    val fieldUsesVisitor = MethodCallsVisitor(fieldToRename)
    cu.accept(fieldUsesVisitor, listOfMethodCalls)

    listOfMethodCalls.forEach {
        it.setName(newName)
    }
}