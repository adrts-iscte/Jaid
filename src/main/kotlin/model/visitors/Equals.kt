package model.visitors

import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.modules.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.*
import com.github.javaparser.ast.visitor.GenericVisitor
import com.github.javaparser.ast.visitor.Visitable
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.uuid
import java.util.*

class EqualsVisitor private constructor() : GenericVisitor<Boolean, Visitable> {

    private fun commonNodeEquality(n: Node, n2: Node): Boolean {
        return if (!this.nodeEquals(n.comment, n2.comment)) false else this.nodesEquals(
            n.orphanComments,
            n2.orphanComments
        )
    }

    private fun <T : Node?> nodesEquals(nodes1: List<T>?, nodes2: List<T>?): Boolean {
        return if (nodes1 == null) {
            nodes2 == null
        } else if (nodes2 == null) {
            false
        } else if (nodes1.size != nodes2.size) {
            false
        } else {
            for (i in nodes1.indices) {
                if (!this.nodeEquals(nodes1[i] as Node, nodes2[i] as Node)) {
                    return false
                }
            }
            true
        }
    }

    private fun <N : Node?> nodesEquals(n: NodeList<N>?, n2: NodeList<N>?): Boolean {
        return if (n === n2) {
            true
        } else if (n != null && n2 != null) {
            if (n.size != n2.size) {
                false
            } else {
                for (i in n.indices) {
                    if (!this.nodeEquals<N>(n[i], n2[i])) {
                        return false
                    }
                }
                true
            }
        } else {
            false
        }
    }

    private fun <T : Node?> nodeEquals(n: T?, n2: T?): Boolean {
        return if (n === n2) {
            true
        } else if (n != null && n2 != null) {
            if (n.javaClass != n2.javaClass) {
                false
            } else {
                if (!commonNodeEquality(n, n2)) false else (n.accept(this, n2) as Boolean)
            }
        } else {
            false
        }
    }

    private fun <T : Node?> nodeEquals(n: Optional<T?>, n2: Optional<T?>): Boolean {
        return this.nodeEquals<Node?>(n.orElse(null) as Node?, n2.orElse(null) as Node?)
    }

    private fun <T : Node?> nodesEquals(n: Optional<NodeList<T>?>, n2: Optional<NodeList<T>?>): Boolean {
        return this.nodesEquals(n.orElse(null) as NodeList<*>?, n2.orElse(null) as NodeList<*>?)
    }

    private fun objEquals(n: Any?, n2: Any?): Boolean {
        return if (n === n2) {
            true
        } else {
            if (n != null && n2 != null) n == n2 else false
        }
    }

    override fun visit(n: CompilationUnit, arg: Visitable): Boolean {
        val n2 = arg as CompilationUnit
        return if (!this.nodesEquals(n.imports, n2.imports)) {
            false
        } else if (!this.nodeEquals(n.module, n2.module)) {
            false
        } else if (!this.nodeEquals(n.packageDeclaration, n2.packageDeclaration)) {
            false
        } else if (!this.nodesEquals(n.types, n2.types)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: PackageDeclaration, arg: Visitable): Boolean {
        val n2 = arg as PackageDeclaration
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: TypeParameter, arg: Visitable): Boolean {
        val n2 = arg as TypeParameter
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.typeBound, n2.typeBound)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LineComment, arg: Visitable): Boolean {
        val n2 = arg as LineComment
        return if (!objEquals(n.content, n2.content)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: BlockComment, arg: Visitable): Boolean {
        val n2 = arg as BlockComment
        return if (!objEquals(n.content, n2.content)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ClassOrInterfaceDeclaration
        return if (!this.nodesEquals(n.extendedTypes, n2.extendedTypes)) {
            false
        } else if (!this.nodesEquals(n.implementedTypes, n2.implementedTypes)) {
            false
        } else if (!objEquals(n.isInterface, n2.isInterface)) {
            false
        } else if (!this.nodesEquals(n.typeParameters, n2.typeParameters)) {
            false
        } else if (!this.nodesEquals(n.members, n2.members)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: EnumDeclaration, arg: Visitable): Boolean {
        val n2 = arg as EnumDeclaration
        return if (!this.nodesEquals(n.entries, n2.entries)) {
            false
        } else if (!this.nodesEquals(n.implementedTypes, n2.implementedTypes)) {
            false
        } else if (!this.nodesEquals(n.members, n2.members)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: EnumConstantDeclaration, arg: Visitable): Boolean {
        val n2 = arg as EnumConstantDeclaration
        return if (!this.nodesEquals(n.arguments, n2.arguments)) {
            false
        } else if (!this.nodesEquals(n.classBody, n2.classBody)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: AnnotationDeclaration, arg: Visitable): Boolean {
        val n2 = arg as AnnotationDeclaration
        return if (!this.nodesEquals(n.members, n2.members)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: AnnotationMemberDeclaration, arg: Visitable): Boolean {
        val n2 = arg as AnnotationMemberDeclaration
        return if (!this.nodeEquals(n.defaultValue, n2.defaultValue)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: FieldDeclaration, arg: Visitable): Boolean {
        val n2 = arg as FieldDeclaration
        return if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodesEquals(n.variables, n2.variables)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: VariableDeclarator, arg: Visitable): Boolean {
        val n2 = arg as VariableDeclarator
        return if (!this.nodeEquals(n.initializer, n2.initializer)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ConstructorDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ConstructorDeclaration
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.parameters, n2.parameters)) {
            false
        } else if (!this.nodeEquals(n.receiverParameter, n2.receiverParameter)) {
            false
        } else if (!this.nodesEquals(n.thrownExceptions, n2.thrownExceptions)) {
            false
        } else if (!this.nodesEquals(n.typeParameters, n2.typeParameters)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: MethodDeclaration, arg: Visitable): Boolean {
        val n2 = arg as MethodDeclaration
        return if (!this.nodeEquals(n.body, n2.body)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.parameters, n2.parameters)) {
            false
        } else if (!this.nodeEquals(n.receiverParameter, n2.receiverParameter)) {
            false
        } else if (!this.nodesEquals(n.thrownExceptions, n2.thrownExceptions)) {
            false
        } else if (!this.nodesEquals(n.typeParameters, n2.typeParameters)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: Parameter, arg: Visitable): Boolean {
        val n2 = arg as Parameter
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!objEquals(n.isVarArgs, n2.isVarArgs)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else if (!this.nodesEquals(n.varArgsAnnotations, n2.varArgsAnnotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: InitializerDeclaration, arg: Visitable): Boolean {
        val n2 = arg as InitializerDeclaration
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!objEquals(n.isStatic, n2.isStatic)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: JavadocComment, arg: Visitable): Boolean {
        val n2 = arg as JavadocComment
        return if (!objEquals(n.content, n2.content)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ClassOrInterfaceType, arg: Visitable): Boolean {
        val n2 = arg as ClassOrInterfaceType

        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n.name)
        val classDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserClassDeclaration)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2.name)
        val classDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserClassDeclaration)?.wrappedNode
        } else null

        return if (classDeclN1 != null && classDeclN2 != null && classDeclN1.uuid != classDeclN2.uuid) {
            false
        } else if (!this.nodeEquals(n.scope, n2.scope)) {
            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: PrimitiveType, arg: Visitable): Boolean {
        val n2 = arg as PrimitiveType
        return if (!objEquals(n.type, n2.type)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ArrayType, arg: Visitable): Boolean {
        val n2 = arg as ArrayType
        return if (!this.nodeEquals(n.componentType as Node, n2.componentType as Node)) {
            false
        } else if (!objEquals(n.origin, n2.origin)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ArrayCreationLevel, arg: Visitable): Boolean {
        val n2 = arg as ArrayCreationLevel
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!this.nodeEquals(n.dimension, n2.dimension)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: IntersectionType, arg: Visitable): Boolean {
        val n2 = arg as IntersectionType
        return if (!this.nodesEquals(n.elements, n2.elements)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: UnionType, arg: Visitable): Boolean {
        val n2 = arg as UnionType
        return if (!this.nodesEquals(n.elements, n2.elements)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: VoidType, arg: Visitable): Boolean {
        val n2 = arg as VoidType
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: WildcardType, arg: Visitable): Boolean {
        val n2 = arg as WildcardType
        return if (!this.nodeEquals(n.extendedType, n2.extendedType)) {
            false
        } else if (!this.nodeEquals(n.superType, n2.superType)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: UnknownType, arg: Visitable): Boolean {
        val n2 = arg as UnknownType
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ArrayAccessExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayAccessExpr
        return if (!this.nodeEquals(n.index as Node, n2.index as Node)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ArrayCreationExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayCreationExpr
        return if (!this.nodeEquals(n.elementType as Node, n2.elementType as Node)) {
            false
        } else if (!this.nodeEquals(n.initializer, n2.initializer)) {
            false
        } else if (!this.nodesEquals(n.levels, n2.levels)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ArrayInitializerExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayInitializerExpr
        return if (!this.nodesEquals(n.values, n2.values)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: AssignExpr, arg: Visitable): Boolean {
        val n2 = arg as AssignExpr
        return if (!objEquals(n.operator, n2.operator)) {
            false
        } else if (!this.nodeEquals(n.target as Node, n2.target as Node)) {
            false
        } else if (!this.nodeEquals(n.value as Node, n2.value as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: BinaryExpr, arg: Visitable): Boolean {
        val n2 = arg as BinaryExpr
        return if (!this.nodeEquals(n.left as Node, n2.left as Node)) {
            false
        } else if (!objEquals(n.operator, n2.operator)) {
            false
        } else if (!this.nodeEquals(n.right as Node, n2.right as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: CastExpr, arg: Visitable): Boolean {
        val n2 = arg as CastExpr
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ClassExpr, arg: Visitable): Boolean {
        val n2 = arg as ClassExpr
        return if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ConditionalExpr, arg: Visitable): Boolean {
        val n2 = arg as ConditionalExpr
        return if (!this.nodeEquals(n.condition as Node, n2.condition as Node)) {
            false
        } else if (!this.nodeEquals(n.elseExpr as Node, n2.elseExpr as Node)) {
            false
        } else if (!this.nodeEquals(n.thenExpr as Node, n2.thenExpr as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: EnclosedExpr, arg: Visitable): Boolean {
        val n2 = arg as EnclosedExpr
        return if (!this.nodeEquals(n.inner as Node, n2.inner as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: FieldAccessExpr, arg: Visitable): Boolean {
        val n2 = arg as FieldAccessExpr

        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val fieldDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        val fieldDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
        } else null

        return if(fieldDeclN1 != null && fieldDeclN2 != null && fieldDeclN1.uuid != fieldDeclN2.uuid) {
            false
//        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
//            false
        } else if (!this.nodeEquals(n.scope as Node, n2.scope as Node)) {
            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: InstanceOfExpr, arg: Visitable): Boolean {
        val n2 = arg as InstanceOfExpr
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else if (!this.nodeEquals(n.pattern, n2.pattern)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: StringLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as StringLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: IntegerLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as IntegerLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LongLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as LongLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: CharLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as CharLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: DoubleLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as DoubleLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: BooleanLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as BooleanLiteralExpr
        return if (!objEquals(n.isValue, n2.isValue)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: NullLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as NullLiteralExpr
        return this.nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MethodCallExpr, arg: Visitable): Boolean {
        val n2 = arg as MethodCallExpr
        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val methodDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        val methodDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
        } else null

        return if (!this.nodesEquals(n.arguments, n2.arguments)) {
            false
//        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
//            false
        } else if (methodDeclN1 != null && methodDeclN2 != null && methodDeclN1.uuid != methodDeclN2.uuid) {
            false
        } else if (!this.nodeEquals(n.scope, n2.scope)) {
            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: NameExpr, arg: Visitable): Boolean {
        val n2 = arg as NameExpr
        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val fieldDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        val fieldDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
        } else null

        return if(fieldDeclN1 != null && fieldDeclN2 != null && fieldDeclN1.uuid != fieldDeclN2.uuid) {
            false
//        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
//            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ObjectCreationExpr, arg: Visitable): Boolean {
        val n2 = arg as ObjectCreationExpr
        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val classDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        val classDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
        } else null

        return if (!this.nodesEquals(n.anonymousClassBody, n2.anonymousClassBody)) {
            false
        } else if (!this.nodesEquals(n.arguments, n2.arguments)) {
            false
        } else if (!this.nodeEquals(n.scope, n2.scope)) {
            false
//        } else if (classDeclN1?.parentNode?.orElse(null) == null || classDeclN2?.parentNode?.orElse(null) == null) {
//            false
        } else if (classDeclN1 != null && classDeclN2 != null && classDeclN1.uuid != classDeclN2.uuid) {
            false
//        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
//            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: Name, arg: Visitable): Boolean {
        val n2 = arg as Name
        return if (!objEquals(n.identifier, n2.identifier)) {
            false
        } else if (!this.nodeEquals(n.qualifier, n2.qualifier)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SimpleName, arg: Visitable): Boolean {
        val n2 = arg as SimpleName

        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        if (!jpfN1.isSolved || !jpfN2.isSolved)
            return false
        val declN1 = jpfN1.correspondingDeclaration
        val declN2 = jpfN2.correspondingDeclaration
        val classDeclN1 = when(declN1) {
                is JavaParserClassDeclaration -> (declN1 as? JavaParserClassDeclaration)?.wrappedNode
                is JavaParserConstructorDeclaration<*> -> (declN1 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
                else -> null
        }
        val classDeclN2 = when(declN2) {
            is JavaParserClassDeclaration -> (declN1 as? JavaParserClassDeclaration)?.wrappedNode
            is JavaParserConstructorDeclaration<*> -> (declN2 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            else -> null
        }

        return if (!objEquals(n.identifier, n2.identifier)) {
            false
        } else if (classDeclN1 != null && classDeclN2 != null && classDeclN1.uuid != classDeclN2.uuid) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ThisExpr, arg: Visitable): Boolean {
        val n2 = arg as ThisExpr

        val solver = CombinedTypeSolver()
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val classDeclN1 = if (jpfN1.isSolved) {
            (jpfN1.correspondingDeclaration as? JavaParserClassDeclaration)?.wrappedNode
        } else null
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        val classDeclN2 = if (jpfN2.isSolved) {
            (jpfN2.correspondingDeclaration as? JavaParserClassDeclaration)?.wrappedNode
        } else null
        return if (!this.nodeEquals(n.typeName, n2.typeName)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SuperExpr, arg: Visitable): Boolean {
        val n2 = arg as SuperExpr
        return if (!this.nodeEquals(n.typeName, n2.typeName)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: UnaryExpr, arg: Visitable): Boolean {
        val n2 = arg as UnaryExpr
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else if (!objEquals(n.operator, n2.operator)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: VariableDeclarationExpr, arg: Visitable): Boolean {
        val n2 = arg as VariableDeclarationExpr
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodesEquals(n.variables, n2.variables)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: MarkerAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as MarkerAnnotationExpr
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SingleMemberAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as SingleMemberAnnotationExpr
        return if (!this.nodeEquals(n.memberValue as Node, n2.memberValue as Node)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: NormalAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as NormalAnnotationExpr
        return if (!this.nodesEquals(n.pairs, n2.pairs)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: MemberValuePair, arg: Visitable): Boolean {
        val n2 = arg as MemberValuePair
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.value as Node, n2.value as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ExplicitConstructorInvocationStmt, arg: Visitable): Boolean {
        val n2 = arg as ExplicitConstructorInvocationStmt
        return if (!this.nodesEquals(n.arguments, n2.arguments)) {
            false
        } else if (!this.nodeEquals(n.expression, n2.expression)) {
            false
        } else if (!objEquals(n.isThis, n2.isThis)) {
            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LocalClassDeclarationStmt, arg: Visitable): Boolean {
        val n2 = arg as LocalClassDeclarationStmt
        return if (!this.nodeEquals(n.classDeclaration as Node, n2.classDeclaration as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LocalRecordDeclarationStmt, arg: Visitable): Boolean {
        val n2 = arg as LocalRecordDeclarationStmt
        return if (!this.nodeEquals(n.recordDeclaration as Node, n2.recordDeclaration as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: AssertStmt, arg: Visitable): Boolean {
        val n2 = arg as AssertStmt
        return if (!this.nodeEquals(n.check as Node, n2.check as Node)) {
            false
        } else if (!this.nodeEquals(n.message, n2.message)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: BlockStmt, arg: Visitable): Boolean {
        val n2 = arg as BlockStmt
        return if (!this.nodesEquals(n.statements, n2.statements)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LabeledStmt, arg: Visitable): Boolean {
        val n2 = arg as LabeledStmt
        return if (!this.nodeEquals(n.label as Node, n2.label as Node)) {
            false
        } else if (!this.nodeEquals(n.statement as Node, n2.statement as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: EmptyStmt, arg: Visitable): Boolean {
        val n2 = arg as EmptyStmt
        return this.nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ExpressionStmt, arg: Visitable): Boolean {
        val n2 = arg as ExpressionStmt
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SwitchStmt, arg: Visitable): Boolean {
        val n2 = arg as SwitchStmt
        return if (!this.nodesEquals(n.entries, n2.entries)) {
            false
        } else if (!this.nodeEquals(n.selector as Node, n2.selector as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SwitchEntry, arg: Visitable): Boolean {
        val n2 = arg as SwitchEntry
        return if (!this.nodesEquals(n.labels, n2.labels)) {
            false
        } else if (!this.nodesEquals(n.statements, n2.statements)) {
            false
        } else if (!objEquals(n.type, n2.type)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: BreakStmt, arg: Visitable): Boolean {
        val n2 = arg as BreakStmt
        return if (!this.nodeEquals(n.label, n2.label)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ReturnStmt, arg: Visitable): Boolean {
        val n2 = arg as ReturnStmt
        return if (!this.nodeEquals(n.expression, n2.expression)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: IfStmt, arg: Visitable): Boolean {
        val n2 = arg as IfStmt
        return if (!this.nodeEquals(n.condition as Node, n2.condition as Node)) {
            false
        } else if (!this.nodeEquals(n.elseStmt, n2.elseStmt)) {
            false
        } else if (!this.nodeEquals(n.thenStmt as Node, n2.thenStmt as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: WhileStmt, arg: Visitable): Boolean {
        val n2 = arg as WhileStmt
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.condition as Node, n2.condition as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ContinueStmt, arg: Visitable): Boolean {
        val n2 = arg as ContinueStmt
        return if (!this.nodeEquals(n.label, n2.label)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: DoStmt, arg: Visitable): Boolean {
        val n2 = arg as DoStmt
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.condition as Node, n2.condition as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ForEachStmt, arg: Visitable): Boolean {
        val n2 = arg as ForEachStmt
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.iterable as Node, n2.iterable as Node)) {
            false
        } else if (!this.nodeEquals(n.variable as Node, n2.variable as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ForStmt, arg: Visitable): Boolean {
        val n2 = arg as ForStmt
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.compare, n2.compare)) {
            false
        } else if (!this.nodesEquals(n.initialization, n2.initialization)) {
            false
        } else if (!this.nodesEquals(n.update, n2.update)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ThrowStmt, arg: Visitable): Boolean {
        val n2 = arg as ThrowStmt
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SynchronizedStmt, arg: Visitable): Boolean {
        val n2 = arg as SynchronizedStmt
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: TryStmt, arg: Visitable): Boolean {
        val n2 = arg as TryStmt
        return if (!this.nodesEquals(n.catchClauses, n2.catchClauses)) {
            false
        } else if (!this.nodeEquals(n.finallyBlock, n2.finallyBlock)) {
            false
        } else if (!this.nodesEquals(n.resources, n2.resources)) {
            false
        } else if (!this.nodeEquals(n.tryBlock as Node, n2.tryBlock as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: CatchClause, arg: Visitable): Boolean {
        val n2 = arg as CatchClause
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodeEquals(n.parameter as Node, n2.parameter as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: LambdaExpr, arg: Visitable): Boolean {
        val n2 = arg as LambdaExpr
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!objEquals(n.isEnclosingParameters, n2.isEnclosingParameters)) {
            false
        } else if (!this.nodesEquals(n.parameters, n2.parameters)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: MethodReferenceExpr, arg: Visitable): Boolean {
        val n2 = arg as MethodReferenceExpr
        return if (!objEquals(n.identifier, n2.identifier)) {
            false
        } else if (!this.nodeEquals(n.scope as Node, n2.scope as Node)) {
            false
        } else if (!this.nodesEquals(n.typeArguments, n2.typeArguments)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: TypeExpr, arg: Visitable): Boolean {
        val n2 = arg as TypeExpr
        return if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ImportDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ImportDeclaration
        return if (!objEquals(n.isAsterisk, n2.isAsterisk)) {
            false
        } else if (!objEquals(n.isStatic, n2.isStatic)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: NodeList<*>?, arg: Visitable): Boolean {
        return this.nodesEquals(n, arg as NodeList<*>)
    }

    override fun visit(n: ModuleDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ModuleDeclaration
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!this.nodesEquals(n.directives, n2.directives)) {
            false
        } else if (!objEquals(n.isOpen, n2.isOpen)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ModuleRequiresDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleRequiresDirective
        return if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ModuleExportsDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleExportsDirective
        return if (!this.nodesEquals(n.moduleNames, n2.moduleNames)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ModuleProvidesDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleProvidesDirective
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.with, n2.with)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ModuleUsesDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleUsesDirective
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: ModuleOpensDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleOpensDirective
        return if (!this.nodesEquals(n.moduleNames, n2.moduleNames)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: UnparsableStmt, arg: Visitable): Boolean {
        val n2 = arg as UnparsableStmt
        return this.nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ReceiverParameter, arg: Visitable): Boolean {
        val n2 = arg as ReceiverParameter
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: VarType, arg: Visitable): Boolean {
        val n2 = arg as VarType
        return if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: Modifier, arg: Visitable): Boolean {
        val n2 = arg as Modifier
        return if (!objEquals(n.keyword, n2.keyword)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: SwitchExpr, arg: Visitable): Boolean {
        val n2 = arg as SwitchExpr
        return if (!this.nodesEquals(n.entries, n2.entries)) {
            false
        } else if (!this.nodeEquals(n.selector as Node, n2.selector as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: YieldStmt, arg: Visitable): Boolean {
        val n2 = arg as YieldStmt
        return if (!this.nodeEquals(n.expression as Node, n2.expression as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: TextBlockLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as TextBlockLiteralExpr
        return if (!objEquals(n.value, n2.value)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: PatternExpr, arg: Visitable): Boolean {
        val n2 = arg as PatternExpr
        return if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodeEquals(n.type as Node, n2.type as Node)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: RecordDeclaration, arg: Visitable): Boolean {
        val n2 = arg as RecordDeclaration
        return if (!this.nodesEquals(n.implementedTypes, n2.implementedTypes)) {
            false
        } else if (!this.nodesEquals(n.parameters, n2.parameters)) {
            false
        } else if (!this.nodeEquals(n.receiverParameter, n2.receiverParameter)) {
            false
        } else if (!this.nodesEquals(n.typeParameters, n2.typeParameters)) {
            false
        } else if (!this.nodesEquals(n.members, n2.members)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    override fun visit(n: CompactConstructorDeclaration, arg: Visitable): Boolean {
        val n2 = arg as CompactConstructorDeclaration
        return if (!this.nodeEquals(n.body as Node, n2.body as Node)) {
            false
        } else if (!this.nodesEquals(n.modifiers, n2.modifiers)) {
            false
        } else if (!this.nodeEquals(n.name as Node, n2.name as Node)) {
            false
        } else if (!this.nodesEquals(n.thrownExceptions, n2.thrownExceptions)) {
            false
        } else if (!this.nodesEquals(n.typeParameters, n2.typeParameters)) {
            false
        } else if (!this.nodesEquals(n.annotations, n2.annotations)) {
            false
        } else {
            this.nodeEquals(n.comment, n2.comment)
        }
    }

    companion object {
        private val SINGLETON: EqualsVisitor = EqualsVisitor()
        fun equals(n: Node?, n2: Node?): Boolean {
            return SINGLETON.nodeEquals(n, n2)
        }
    }
}