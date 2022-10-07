package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.EmptyStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.visitors.ClassOrInterfaceTypeVisitor
import model.visitors.FieldUsesVisitor
import model.visitors.MethodCallsVisitor
import model.visitors.ObjectCreationVisitor
import java.util.*

val Node.uuid: String
    get() {
        val comment = this.comment.orElse(null) ?: return UUID.randomUUID().toString()
        when (comment) {
            is LineComment -> {
                if (comment.content.trim().isValidUUID) {
                    return comment.content.trim()
                }
            }
            else -> {
                val content = comment.content.replace("(\\n){0,}(\\r){0,}(\\t){0,}","").trim().takeLast(36)
                if (content.isValidUUID) {
                    return content
                }
            }
        }
        return UUID.randomUUID().toString()
    }

fun Node.generateUUID() {
    val uuid = UUID.randomUUID().toString()
    val comment = this.comment.orElse(null)
    if (comment == null) {
        this.setComment(LineComment(uuid))
    } else {
        when (comment) {
            is LineComment, is BlockComment -> {
                this.setComment(BlockComment(comment.content + "\n\t " + uuid))
            }
            else -> {
                this.setComment(JavadocComment(comment.content + "\n\t * " + uuid))
            }
        }
    }
}

val String.isValidUUID: Boolean
    get() = this.matches(Regex("\\s?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))

/*
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
            is CallableDeclaration<*> -> {
                val first = if (isConstructorDeclaration) {
                    (this as ConstructorDeclaration).body.statements.firstOrNull()
                } else {
                    (this as MethodDeclaration).body.get().statements.firstOrNull()
                }
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
        return null
    }

fun Node.generateUUID(): String {
    val uuid = UUID.randomUUID().toString()
    when (this) {
        is ClassOrInterfaceDeclaration -> {
            this.addOrphanComment(LineComment(uuid))
        }
        is CallableDeclaration<*> -> {
            val emptyStmt = EmptyStmt()
            emptyStmt.setComment(LineComment(uuid))
            if (isConstructorDeclaration) {
                (this as ConstructorDeclaration).body.statements.addFirst(emptyStmt)
            } else {
                (this as MethodDeclaration).body.get().statements.addFirst(emptyStmt)
            }
        } else -> {
            this.setComment(LineComment(uuid))
        }
    }
    return uuid
}
*/

fun renameAllFieldUses(cu: CompilationUnit, fieldToRename: FieldDeclaration, oldName: String, newName: String) {
    val listOfFieldUses = mutableListOf<Node>()
    val fieldUsesVisitor = FieldUsesVisitor(oldName)
    cu.accept(fieldUsesVisitor, listOfFieldUses)

    val solver = CombinedTypeSolver()
    solveNameExpr(listOfFieldUses, solver, fieldToRename, newName)
    solveFieldAccessExpr(listOfFieldUses, solver, fieldToRename, newName)
//    model.solveVariableDeclarationExpr(listOfFieldUses, solver, fieldToRename, newName)

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

fun renameAllMethodCalls(cu: CompilationUnit, methodToRename: MethodDeclaration, newName: String) {
    val listOfMethodCalls = mutableListOf<MethodCallExpr>()
    val methodCallsVisitor = MethodCallsVisitor(methodToRename)
    cu.accept(methodCallsVisitor, listOfMethodCalls)

    listOfMethodCalls.forEach {
        it.setName(newName)
    }
}

fun renameAllConstructorCalls(cu: CompilationUnit, classToRename: ClassOrInterfaceDeclaration, newName: String) {
    val newClassToRename = cu.childNodes.filterIsInstance<ClassOrInterfaceDeclaration>().find { it.uuid == classToRename.uuid }!!

    newClassToRename.constructors.forEach {
        it.setName(newName)
    }

    val listOfConstructorCalls = mutableListOf<ObjectCreationExpr>()
    val constructorCallsVisitor = ObjectCreationVisitor(newClassToRename)
    cu.accept(constructorCallsVisitor, listOfConstructorCalls)

    listOfConstructorCalls.forEach {
        it.type.setName(newName)
    }

    val listOfClassTypes = mutableListOf<ClassOrInterfaceType>()
    val classTypesVisitor = ClassOrInterfaceTypeVisitor(newClassToRename)
    cu.accept(classTypesVisitor, listOfClassTypes)

    listOfClassTypes.forEach {
        it.setName(newName)
    }
}
