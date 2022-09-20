package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.EmptyStmt
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.visitors.FieldUsesVisitor
import model.visitors.MethodCallsVisitor
import java.util.*

val Node.uuid: String?
    get() {
        when (this) {
            is ClassOrInterfaceDeclaration -> {
                val orphan = this.orphanComments.find {
                    it.content.isValidUUID
                }
                orphan?.let {
                    return it.content
                }
            }
            is MethodDeclaration -> {
                val first = this.body.get().statements.firstOrNull()
                if (first?.isEmptyStmt == true) {
                    return first.uuid
                }
            }
            is ConstructorDeclaration -> {
                val first = this.body.statements.firstOrNull()
                if (first?.isEmptyStmt == true) {
                    return first.uuid
                }
            }
            else -> {
                val comment = this.comment.orElse(null)
                if (comment != null && comment.content.isValidUUID) {
                    return comment.content
                }
            }
        }
        return this.generateUUID()
    }

val String.isValidUUID: Boolean
    get() = this.matches(Regex("\\s?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))

fun Node.setUUID(newUUID: String) {
    this.setComment(LineComment(newUUID))
}
fun Node.generateUUID(): String {
    val uuid = UUID.randomUUID().toString()
    when (this) {
        is ClassOrInterfaceDeclaration -> {
            this.addOrphanComment(LineComment(uuid))
        }
        is MethodDeclaration -> {
            val emptyStmt = EmptyStmt()
            emptyStmt.setComment(LineComment(uuid))
            this.body.get().statements.addFirst(emptyStmt)
        }
        is ConstructorDeclaration -> {
            val emptyStmt = EmptyStmt()
            emptyStmt.setComment(LineComment(uuid))
            this.body.statements.addFirst(emptyStmt)
        }  else -> {
            this.setComment(LineComment(uuid))
        }
    }
    return uuid
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