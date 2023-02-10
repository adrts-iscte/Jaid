package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
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
        val comment = this.comment.orElse(null) ?: return this.generateUUID()
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
        return this.generateUUID()
    }

fun Node.generateUUID() : String {
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
    return uuid
}

val String.isValidUUID: Boolean
    get() = this.matches(Regex("\\s?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))

val CallableDeclaration<*>.parameterTypes : List<Type>
    get() = this.parameters.map { it.type }

val NodeList<Parameter>.types : List<Type>
    get() = this.map { it.type }

val Modifier.isAccessModifier: Boolean
    get() = this.keyword.name == "PUBLIC" || this.keyword.name == "PRIVATE" || this.keyword.name == "PROTECTED"

val Modifier.isAbstractModifier: Boolean
    get() = this.keyword.name == "ABSTRACT"

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

fun areClassesEqual(c1 : ClassOrInterfaceDeclaration, c2 : ClassOrInterfaceDeclaration) : Boolean {
    if (c1.isInterface != c2.isInterface) return false
    if (c1.isInnerClass != c2.isInnerClass) return false
    if (c1.name != c2.name) return false
    if (c1.comment != c2.comment) return false
    if (c1.typeParameters.toSet() != c2.typeParameters.toSet()) return false
    if (c1.implementedTypes.toSet() != c2.implementedTypes.toSet()) return false
    if (c1.extendedTypes.toSet() != c2.extendedTypes.toSet()) return false
    if (c1.members != c2.members) return false
    if (c1.modifiers.toSet() != c2.modifiers.toSet()) return false
    return true

}

fun areFilesEqual(f1 : CompilationUnit, f2 : CompilationUnit) : Boolean {
    if (f1.imports != f2.imports) return false
    if (f1.packageDeclaration != f2.packageDeclaration) return false
    if (f1.types != f2.types) return false
    return true

}

fun calculateIndexOfMemberToAdd(clazz : ClassOrInterfaceDeclaration, classToHaveCallableAdded : ClassOrInterfaceDeclaration, newMemberUUID : String) : Int {
    val clazzMembers = clazz.members
    val clazzMembersUUID = clazzMembers.map { it.uuid }
    val classToHaveCallableAddedMembers = classToHaveCallableAdded.members
    val classToHaveCallableAddedMembersUUID = classToHaveCallableAddedMembers.map { it.uuid }

    var i = clazzMembersUUID.indexOf(newMemberUUID)
    while (i > 0) {
        i--
        val similarMember = classToHaveCallableAddedMembers.find { it.uuid == clazzMembersUUID[i]}
        similarMember?.let {
            return classToHaveCallableAddedMembers.indexOf(similarMember) + 1
        }
    }

    return 0
}

fun calculateIndexOfTypeToAdd(compilationUnit : CompilationUnit,
                              compilationUnitToHaveCallableAdded : CompilationUnit,
                              newTypeUUID : String) : Int {
    val compilationUnitMembers = compilationUnit.types
    val compilationUnitMembersUUID = compilationUnitMembers.map { it.uuid }
    val compilationUnitToHaveCallableAddedMembers = compilationUnitToHaveCallableAdded.types
    val compilationUnitToHaveCallableAddedMembersUUID = compilationUnitToHaveCallableAddedMembers.map { it.uuid }

    var i = compilationUnitMembersUUID.indexOf(newTypeUUID)
    while (i > 0) {
        i--
        val similarType = compilationUnitToHaveCallableAddedMembers.find { it.uuid == compilationUnitMembersUUID[i]}
        similarType?.let {
            return compilationUnitToHaveCallableAddedMembers.indexOf(similarType) + 1
        }
    }

    return 0
}