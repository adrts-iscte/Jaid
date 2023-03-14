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
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.*
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import model.uuid
import java.io.File
import java.util.*

class EqualsUuidVisitor private constructor() : GenericVisitor<Boolean, Visitable> {

    private val solver = CombinedTypeSolver()

    init {
        solver.add(ReflectionTypeSolver(false))
//        solver.add(JavaParserTypeSolver(File("src/main/kotlin/scenarios/transformations/fileTransformations/base/")))
//        solver.add(JavaParserTypeSolver(File("src/main/kotlin/scenarios/transformations/fileTransformations/left/")))
    }

    companion object {
        private val SINGLETON: EqualsUuidVisitor = EqualsUuidVisitor()
        fun equals(n: Node?, n2: Node?): Boolean {
            return SINGLETON.nodeEquals(n, n2)
        }
    }

    /**
     * Check for equality that can be applied to each kind of node,
     * to not repeat it in every method we store that here.
     */
    private fun commonNodeEquality(n: Node, n2: Node): Boolean {
        return if (!nodeEquals(n.comment, n2.comment)) {
            false
        } else nodesEquals(n.orphanComments, n2.orphanComments)
    }

    private fun <T : Node?> nodesEquals(nodes1: List<T>?, nodes2: List<T>?): Boolean {
        if (nodes1 == null) {
            return nodes2 == null
        } else if (nodes2 == null) {
            return false
        }
        if (nodes1.size != nodes2.size) {
            return false
        }
        for (i in nodes1.indices) {
            if (!nodeEquals<T>(nodes1[i], nodes2[i])) {
                return false
            }
        }
        return true
    }

    private fun <N : Node?> nodesEquals(n: NodeList<N>?, n2: NodeList<N>?): Boolean {
        if (n === n2) {
            return true
        }
        if (n == null || n2 == null) {
            return false
        }
        if (n.size != n2.size) {
            return false
        }
        for (i in n.indices) {
            if (!nodeEquals<N>(n[i], n2[i])) {
                return false
            }
        }
        return true
    }

    private fun <T : Node?> nodeEquals(n: T?, n2: T?): Boolean {
        if (n === n2) {
            return true
        }
        if (n == null || n2 == null) {
            return false
        }
        if (n.javaClass != n2.javaClass) {
            return false
        }
        return if (!commonNodeEquality(n, n2)) {
            false
        } else n.accept(this, n2)
    }

    private fun <T : Node?> nodeEquals(n: Optional<T?>, n2: Optional<T?>): Boolean {
        return nodeEquals<T?>(n.orElse(null), n2.orElse(null))
    }

    private fun <T : Node?> nodesEquals(n: Optional<NodeList<T>?>, n2: Optional<NodeList<T>?>): Boolean {
        return nodesEquals(n.orElse(null), n2.orElse(null))
    }

    private fun objEquals(n: Any?, n2: Any?): Boolean {
        if (n === n2) {
            return true
        }
        return if (n == null || n2 == null) {
            false
        } else n == n2
    }

    override fun visit(n: CompilationUnit, arg: Visitable): Boolean {
        val n2 = arg as CompilationUnit
        if (!nodesEquals(n.imports, n2.imports)) return false
        if (!nodeEquals(n.module, n2.module)) return false
        if (!nodeEquals(n.packageDeclaration, n2.packageDeclaration)) return false
        if (!nodesEquals(n.types, n2.types)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: PackageDeclaration, arg: Visitable): Boolean {
        val n2 = arg as PackageDeclaration
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: TypeParameter, arg: Visitable): Boolean {
        val n2 = arg as TypeParameter
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.typeBound, n2.typeBound)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LineComment, arg: Visitable): Boolean {
        val n2 = arg as LineComment
        if (!objEquals(n.content, n2.content)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: BlockComment, arg: Visitable): Boolean {
        val n2 = arg as BlockComment
        if (!objEquals(n.content, n2.content)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ClassOrInterfaceDeclaration
        if (!nodesEquals(n.extendedTypes, n2.extendedTypes)) return false
        if (!nodesEquals(n.implementedTypes, n2.implementedTypes)) return false
        if (!objEquals(n.isInterface, n2.isInterface)) return false
        if (!nodesEquals(n.typeParameters, n2.typeParameters)) return false
        if (!nodesEquals(n.members, n2.members)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: EnumDeclaration, arg: Visitable): Boolean {
        val n2 = arg as EnumDeclaration
        if (!nodesEquals(n.entries, n2.entries)) return false
        if (!nodesEquals(n.implementedTypes, n2.implementedTypes)) return false
        if (!nodesEquals(n.members, n2.members)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: EnumConstantDeclaration, arg: Visitable): Boolean {
        val n2 = arg as EnumConstantDeclaration
        if (!nodesEquals(n.arguments, n2.arguments)) return false
        if (!nodesEquals(n.classBody, n2.classBody)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: AnnotationDeclaration, arg: Visitable): Boolean {
        val n2 = arg as AnnotationDeclaration
        if (!nodesEquals(n.members, n2.members)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: AnnotationMemberDeclaration, arg: Visitable): Boolean {
        val n2 = arg as AnnotationMemberDeclaration
        if (!nodeEquals(n.defaultValue, n2.defaultValue)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: FieldDeclaration, arg: Visitable): Boolean {
        val n2 = arg as FieldDeclaration
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodesEquals(n.variables, n2.variables)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: VariableDeclarator, arg: Visitable): Boolean {
        val n2 = arg as VariableDeclarator
        if (!nodeEquals(n.initializer, n2.initializer)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ConstructorDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ConstructorDeclaration
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.parameters, n2.parameters)) return false
        if (!nodeEquals(n.receiverParameter, n2.receiverParameter)) return false
        if (!nodesEquals(n.thrownExceptions, n2.thrownExceptions)) return false
        if (!nodesEquals(n.typeParameters, n2.typeParameters)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MethodDeclaration, arg: Visitable): Boolean {
        val n2 = arg as MethodDeclaration
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.parameters, n2.parameters)) return false
        if (!nodeEquals(n.receiverParameter, n2.receiverParameter)) return false
        if (!nodesEquals(n.thrownExceptions, n2.thrownExceptions)) return false
        if (!nodesEquals(n.typeParameters, n2.typeParameters)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: Parameter, arg: Visitable): Boolean {
        val n2 = arg as Parameter
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!objEquals(n.isVarArgs, n2.isVarArgs)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        if (!nodesEquals(n.varArgsAnnotations, n2.varArgsAnnotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: InitializerDeclaration, arg: Visitable): Boolean {
        val n2 = arg as InitializerDeclaration
        if (!nodeEquals(n.body, n2.body)) return false
        if (!objEquals(n.isStatic, n2.isStatic)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: JavadocComment, arg: Visitable): Boolean {
        val n2 = arg as JavadocComment
        if (!objEquals(n.content, n2.content)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ClassOrInterfaceType, arg: Visitable): Boolean {
        val n2 = arg as ClassOrInterfaceType

        val jpfN1 = JavaParserFacade.get(solver).convertToUsage(n)
        val jpfN2 = JavaParserFacade.get(solver).convertToUsage(n2)
        if (!jpfN1.isReferenceType || !jpfN2.isReferenceType)
            return false
        val declN1 = jpfN1.asReferenceType().typeDeclaration.get()
        val declN2 = jpfN2.asReferenceType().typeDeclaration.get()
        val classDeclN1 = when (declN1) {
            is JavaParserClassDeclaration -> (declN1 as? JavaParserClassDeclaration)?.wrappedNode
            is JavaParserInterfaceDeclaration -> (declN1 as? JavaParserInterfaceDeclaration)?.wrappedNode
            else -> null
        }
        val classDeclN2 = when (declN2) {
            is JavaParserClassDeclaration -> (declN2 as? JavaParserClassDeclaration)?.wrappedNode
            is JavaParserInterfaceDeclaration -> (declN2 as? JavaParserInterfaceDeclaration)?.wrappedNode
            else -> null
        }

        if (classDeclN1 != null && classDeclN2 != null && classDeclN1.uuid != classDeclN2.uuid) return false
//        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.scope, n2.scope)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: PrimitiveType, arg: Visitable): Boolean {
        val n2 = arg as PrimitiveType
        if (!objEquals(n.type, n2.type)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ArrayType, arg: Visitable): Boolean {
        val n2 = arg as ArrayType
        if (!nodeEquals(n.componentType, n2.componentType)) return false
        if (!objEquals(n.origin, n2.origin)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ArrayCreationLevel, arg: Visitable): Boolean {
        val n2 = arg as ArrayCreationLevel
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!nodeEquals(n.dimension, n2.dimension)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: IntersectionType, arg: Visitable): Boolean {
        val n2 = arg as IntersectionType
        if (!nodesEquals(n.elements, n2.elements)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: UnionType, arg: Visitable): Boolean {
        val n2 = arg as UnionType
        if (!nodesEquals(n.elements, n2.elements)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: VoidType, arg: Visitable): Boolean {
        val n2 = arg as VoidType
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: WildcardType, arg: Visitable): Boolean {
        val n2 = arg as WildcardType
        if (!nodeEquals(n.extendedType, n2.extendedType)) return false
        if (!nodeEquals(n.superType, n2.superType)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: UnknownType, arg: Visitable): Boolean {
        val n2 = arg as UnknownType
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ArrayAccessExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayAccessExpr
        if (!nodeEquals(n.index, n2.index)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ArrayCreationExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayCreationExpr
        if (!nodeEquals(n.elementType, n2.elementType)) return false
        if (!nodeEquals(n.initializer, n2.initializer)) return false
        if (!nodesEquals(n.levels, n2.levels)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ArrayInitializerExpr, arg: Visitable): Boolean {
        val n2 = arg as ArrayInitializerExpr
        if (!nodesEquals(n.values, n2.values)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: AssignExpr, arg: Visitable): Boolean {
        val n2 = arg as AssignExpr
        if (!objEquals(n.operator, n2.operator)) return false
        if (!nodeEquals(n.target, n2.target)) return false
        if (!nodeEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: BinaryExpr, arg: Visitable): Boolean {
        val n2 = arg as BinaryExpr
        if (!nodeEquals(n.left, n2.left)) return false
        if (!objEquals(n.operator, n2.operator)) return false
        if (!nodeEquals(n.right, n2.right)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: CastExpr, arg: Visitable): Boolean {
        val n2 = arg as CastExpr
        if (!nodeEquals(n.expression, n2.expression)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ClassExpr, arg: Visitable): Boolean {
        val n2 = arg as ClassExpr
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ConditionalExpr, arg: Visitable): Boolean {
        val n2 = arg as ConditionalExpr
        if (!nodeEquals(n.condition, n2.condition)) return false
        if (!nodeEquals(n.elseExpr, n2.elseExpr)) return false
        if (!nodeEquals(n.thenExpr, n2.thenExpr)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: EnclosedExpr, arg: Visitable): Boolean {
        val n2 = arg as EnclosedExpr
        if (!nodeEquals(n.inner, n2.inner)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: FieldAccessExpr, arg: Visitable): Boolean {
        val n2 = arg as FieldAccessExpr

        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        if (!jpfN1.isSolved || !jpfN2.isSolved)
            return false
        val declN1 = jpfN1.correspondingDeclaration
        val declN2 = jpfN2.correspondingDeclaration
        val fieldDeclN1 = when (declN1) {
            is JavaParserFieldDeclaration -> (declN1 as? JavaParserFieldDeclaration)?.wrappedNode
            else -> null
        }
        val fieldDeclN2 = when (declN2) {
            is JavaParserFieldDeclaration -> (declN2 as? JavaParserFieldDeclaration)?.wrappedNode
            else -> null
        }

        if(fieldDeclN1 != null && fieldDeclN2 != null && fieldDeclN1.uuid != fieldDeclN2.uuid) return false
//        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.scope, n2.scope)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: InstanceOfExpr, arg: Visitable): Boolean {
        val n2 = arg as InstanceOfExpr
        if (!nodeEquals(n.expression, n2.expression)) return false
        if (!nodeEquals(n.pattern, n2.pattern)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: StringLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as StringLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: IntegerLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as IntegerLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LongLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as LongLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: CharLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as CharLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: DoubleLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as DoubleLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: BooleanLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as BooleanLiteralExpr
        if (!objEquals(n.isValue, n2.isValue)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: NullLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as NullLiteralExpr
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MethodCallExpr, arg: Visitable): Boolean {
        val n2 = arg as MethodCallExpr

        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        if (!jpfN1.isSolved || !jpfN2.isSolved)
            return false
        val declN1 = jpfN1.correspondingDeclaration
        val declN2 = jpfN2.correspondingDeclaration
        val methodDeclN1 = when (declN1) {
            is JavaParserMethodDeclaration -> (declN1 as? JavaParserMethodDeclaration)?.wrappedNode
            else -> null
        }
        val methodDeclN2 = when (declN2) {
            is JavaParserMethodDeclaration -> (declN2 as? JavaParserMethodDeclaration)?.wrappedNode
            else -> null
        }

        if (methodDeclN1 != null && methodDeclN2 != null && methodDeclN1.uuid != methodDeclN2.uuid) return false
        if (!nodesEquals(n.arguments, n2.arguments)) return false
//        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.scope, n2.scope)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: NameExpr, arg: Visitable): Boolean {
        val n2 = arg as NameExpr
        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        if (jpfN1.isSolved && jpfN2.isSolved) {
            val declN1 = jpfN1.correspondingDeclaration
            val declN2 = jpfN2.correspondingDeclaration
            val fieldDeclN1 = when (declN1) {
                is JavaParserFieldDeclaration -> (declN1 as? JavaParserFieldDeclaration)?.wrappedNode
                else -> null
            }
            val fieldDeclN2 = when (declN2) {
                is JavaParserFieldDeclaration -> (declN2 as? JavaParserFieldDeclaration)?.wrappedNode
                else -> null
            }
            if (fieldDeclN1 != null && fieldDeclN2 != null && fieldDeclN1.uuid != fieldDeclN2.uuid) return false
        } else {
            val jpfN1 = n.calculateResolvedType()
            val jpfN2 = n2.calculateResolvedType()
            if (jpfN1.isReferenceType && jpfN2.isReferenceType) {
                val declN1 = jpfN1.asReferenceType().typeDeclaration.orElse(null)
                val declN2 = jpfN2.asReferenceType().typeDeclaration.orElse(null)
                if (declN1 != null && declN2 != null && declN1 != declN2) return false
                if (declN1 == null && declN2 == null && !this.nodeEquals(n.name as Node, n2.name as Node)) return false
//                val classDeclN1 = when (declN1) {
//                    is JavaParserClassDeclaration -> (declN1 as? JavaParserClassDeclaration)?.wrappedNode
//                    is JavaParserInterfaceDeclaration -> (declN1 as? JavaParserInterfaceDeclaration)?.wrappedNode
//                    else -> null
//                }
//                val classDeclN2 = when (declN2) {
//                    is JavaParserClassDeclaration -> (declN2 as? JavaParserClassDeclaration)?.wrappedNode
//                    is JavaParserInterfaceDeclaration -> (declN2 as? JavaParserInterfaceDeclaration)?.wrappedNode
//                    else -> null
//                }
            }
        }
//        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ObjectCreationExpr, arg: Visitable): Boolean {
        val n2 = arg as ObjectCreationExpr

        val jpfN1 = JavaParserFacade.get(solver).solve(n)
        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
        if (!jpfN1.isSolved || !jpfN2.isSolved)
            return false
        val declN1 = jpfN1.correspondingDeclaration
        val declN2 = jpfN2.correspondingDeclaration
        val consDeclN1 = when (declN1) {
            is JavaParserConstructorDeclaration<*> -> (declN1 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            else -> null
        }
        val consDeclN2 = when (declN2) {
            is JavaParserConstructorDeclaration<*> -> (declN2 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            else -> null
        }

        if (consDeclN1 != null && consDeclN2 != null && consDeclN1.uuid != consDeclN2.uuid) return false
        if (!nodesEquals(n.anonymousClassBody, n2.anonymousClassBody)) return false
        if (!nodesEquals(n.arguments, n2.arguments)) return false
        if (!nodeEquals(n.scope, n2.scope)) return false
//        if (!nodeEquals(n.type, n2.type)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: Name, arg: Visitable): Boolean {
        val n2 = arg as Name
        if (!objEquals(n.identifier, n2.identifier)) return false
        if (!nodeEquals(n.qualifier, n2.qualifier)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SimpleName, arg: Visitable): Boolean {
        val n2 = arg as SimpleName

//        val jpfN1 = JavaParserFacade.get(solver).solve(n)
//        val jpfN2 = JavaParserFacade.get(solver).solve(n2)
//        if (!jpfN1.isSolved || !jpfN2.isSolved)
//            return false
//        val declN1 = jpfN1.correspondingDeclaration
//        val declN2 = jpfN2.correspondingDeclaration
//        val classDeclN1 = when(declN1) {
//                is JavaParserClassDeclaration -> (declN1 as? JavaParserClassDeclaration)?.wrappedNode
//                is JavaParserConstructorDeclaration<*> -> (declN1 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
//                is JavaParserMethodDeclaration -> (declN1 as? JavaParserMethodDeclaration)?.wrappedNode
//                is JavaParserFieldDeclaration -> (declN1 as? JavaParserFieldDeclaration)?.wrappedNode
//                is JavaParserVariableDeclaration -> (declN1 as? JavaParserVariableDeclaration)?.wrappedNode
//                is JavaParserParameterDeclaration -> (declN1 as? JavaParserParameterDeclaration)?.wrappedNode
//                else -> null
//        }
//        val classDeclN2 = when(declN2) {
//            is JavaParserClassDeclaration -> (declN2 as? JavaParserClassDeclaration)?.wrappedNode
//            is JavaParserConstructorDeclaration<*> -> (declN2 as? JavaParserConstructorDeclaration<*>)?.wrappedNode
//            is JavaParserMethodDeclaration -> (declN2 as? JavaParserMethodDeclaration)?.wrappedNode
//            is JavaParserFieldDeclaration -> (declN2 as? JavaParserFieldDeclaration)?.wrappedNode
//            is JavaParserVariableDeclaration -> (declN2 as? JavaParserVariableDeclaration)?.wrappedNode
//            is JavaParserParameterDeclaration -> (declN2 as? JavaParserParameterDeclaration)?.wrappedNode
//            else -> null
//        }

//        if (classDeclN1 is Parameter && classDeclN2 is Parameter && !this.nodeEquals(classDeclN1, classDeclN2)) return false
//        if (classDeclN1 != null && classDeclN2 != null && classDeclN1.uuid != classDeclN2.uuid) return false
        if (!objEquals(n.identifier, n2.identifier)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ThisExpr, arg: Visitable): Boolean {
        val n2 = arg as ThisExpr
        if (!nodeEquals(n.typeName, n2.typeName)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SuperExpr, arg: Visitable): Boolean {
        val n2 = arg as SuperExpr
        if (!nodeEquals(n.typeName, n2.typeName)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: UnaryExpr, arg: Visitable): Boolean {
        val n2 = arg as UnaryExpr
        if (!nodeEquals(n.expression, n2.expression)) return false
        if (!objEquals(n.operator, n2.operator)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: VariableDeclarationExpr, arg: Visitable): Boolean {
        val n2 = arg as VariableDeclarationExpr
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodesEquals(n.variables, n2.variables)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MarkerAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as MarkerAnnotationExpr
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SingleMemberAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as SingleMemberAnnotationExpr
        if (!nodeEquals(n.memberValue, n2.memberValue)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: NormalAnnotationExpr, arg: Visitable): Boolean {
        val n2 = arg as NormalAnnotationExpr
        if (!nodesEquals(n.pairs, n2.pairs)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MemberValuePair, arg: Visitable): Boolean {
        val n2 = arg as MemberValuePair
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ExplicitConstructorInvocationStmt, arg: Visitable): Boolean {
        val n2 = arg as ExplicitConstructorInvocationStmt
        if (!nodesEquals(n.arguments, n2.arguments)) return false
        if (!nodeEquals(n.expression, n2.expression)) return false
        if (!objEquals(n.isThis, n2.isThis)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LocalClassDeclarationStmt, arg: Visitable): Boolean {
        val n2 = arg as LocalClassDeclarationStmt
        if (!nodeEquals(n.classDeclaration, n2.classDeclaration)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LocalRecordDeclarationStmt, arg: Visitable): Boolean {
        val n2 = arg as LocalRecordDeclarationStmt
        if (!nodeEquals(n.recordDeclaration, n2.recordDeclaration)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: AssertStmt, arg: Visitable): Boolean {
        val n2 = arg as AssertStmt
        if (!nodeEquals(n.check, n2.check)) return false
        if (!nodeEquals(n.message, n2.message)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: BlockStmt, arg: Visitable): Boolean {
        val n2 = arg as BlockStmt
        if (!nodesEquals(n.statements, n2.statements)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LabeledStmt, arg: Visitable): Boolean {
        val n2 = arg as LabeledStmt
        if (!nodeEquals(n.label, n2.label)) return false
        if (!nodeEquals(n.statement, n2.statement)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: EmptyStmt, arg: Visitable): Boolean {
        val n2 = arg as EmptyStmt
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ExpressionStmt, arg: Visitable): Boolean {
        val n2 = arg as ExpressionStmt

        if (!nodeEquals(n.expression, n2.expression)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SwitchStmt, arg: Visitable): Boolean {
        val n2 = arg as SwitchStmt
        if (!nodesEquals(n.entries, n2.entries)) return false
        if (!nodeEquals(n.selector, n2.selector)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SwitchEntry, arg: Visitable): Boolean {
        val n2 = arg as SwitchEntry
        if (!nodesEquals(n.labels, n2.labels)) return false
        if (!nodesEquals(n.statements, n2.statements)) return false
        if (!objEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: BreakStmt, arg: Visitable): Boolean {
        val n2 = arg as BreakStmt
        if (!nodeEquals(n.label, n2.label)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ReturnStmt, arg: Visitable): Boolean {
        val n2 = arg as ReturnStmt
        if (!nodeEquals(n.expression, n2.expression)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: IfStmt, arg: Visitable): Boolean {
        val n2 = arg as IfStmt
        if (!nodeEquals(n.condition, n2.condition)) return false
        if (!nodeEquals(n.elseStmt, n2.elseStmt)) return false
        if (!nodeEquals(n.thenStmt, n2.thenStmt)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: WhileStmt, arg: Visitable): Boolean {
        val n2 = arg as WhileStmt
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.condition, n2.condition)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ContinueStmt, arg: Visitable): Boolean {
        val n2 = arg as ContinueStmt
        if (!nodeEquals(n.label, n2.label)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: DoStmt, arg: Visitable): Boolean {
        val n2 = arg as DoStmt
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.condition, n2.condition)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ForEachStmt, arg: Visitable): Boolean {
        val n2 = arg as ForEachStmt
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.iterable, n2.iterable)) return false
        if (!nodeEquals(n.variable, n2.variable)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ForStmt, arg: Visitable): Boolean {
        val n2 = arg as ForStmt
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.compare, n2.compare)) return false
        if (!nodesEquals(n.initialization, n2.initialization)) return false
        if (!nodesEquals(n.update, n2.update)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ThrowStmt, arg: Visitable): Boolean {
        val n2 = arg as ThrowStmt
        if (!nodeEquals(n.expression, n2.expression)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SynchronizedStmt, arg: Visitable): Boolean {
        val n2 = arg as SynchronizedStmt
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.expression, n2.expression)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: TryStmt, arg: Visitable): Boolean {
        val n2 = arg as TryStmt
        if (!nodesEquals(n.catchClauses, n2.catchClauses)) return false
        if (!nodeEquals(n.finallyBlock, n2.finallyBlock)) return false
        if (!nodesEquals(n.resources, n2.resources)) return false
        if (!nodeEquals(n.tryBlock, n2.tryBlock)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: CatchClause, arg: Visitable): Boolean {
        val n2 = arg as CatchClause
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodeEquals(n.parameter, n2.parameter)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: LambdaExpr, arg: Visitable): Boolean {
        val n2 = arg as LambdaExpr
        if (!nodeEquals(n.body, n2.body)) return false
        if (!objEquals(n.isEnclosingParameters, n2.isEnclosingParameters)) return false
        if (!nodesEquals(n.parameters, n2.parameters)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: MethodReferenceExpr, arg: Visitable): Boolean {
        val n2 = arg as MethodReferenceExpr
        if (!objEquals(n.identifier, n2.identifier)) return false
        if (!nodeEquals(n.scope, n2.scope)) return false
        if (!nodesEquals(n.typeArguments, n2.typeArguments)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: TypeExpr, arg: Visitable): Boolean {
        val n2 = arg as TypeExpr
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ImportDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ImportDeclaration
        if (!objEquals(n.isAsterisk, n2.isAsterisk)) return false
        if (!objEquals(n.isStatic, n2.isStatic)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: NodeList<*>?, arg: Visitable?): Boolean {
        return nodesEquals(n as NodeList<Node>?, arg as NodeList<Node>?)
    }

    override fun visit(n: ModuleDeclaration, arg: Visitable): Boolean {
        val n2 = arg as ModuleDeclaration
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!nodesEquals(n.directives, n2.directives)) return false
        if (!objEquals(n.isOpen, n2.isOpen)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ModuleRequiresDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleRequiresDirective
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ModuleExportsDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleExportsDirective
        if (!nodesEquals(n.moduleNames, n2.moduleNames)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ModuleProvidesDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleProvidesDirective
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.with, n2.with)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ModuleUsesDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleUsesDirective
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ModuleOpensDirective, arg: Visitable): Boolean {
        val n2 = arg as ModuleOpensDirective
        if (!nodesEquals(n.moduleNames, n2.moduleNames)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: UnparsableStmt, arg: Visitable): Boolean {
        val n2 = arg as UnparsableStmt
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: ReceiverParameter, arg: Visitable): Boolean {
        val n2 = arg as ReceiverParameter
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: VarType, arg: Visitable): Boolean {
        val n2 = arg as VarType
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: Modifier, arg: Visitable): Boolean {
        val n2 = arg as Modifier
        if (!objEquals(n.keyword, n2.keyword)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: SwitchExpr, arg: Visitable): Boolean {
        val n2 = arg as SwitchExpr
        if (!nodesEquals(n.entries, n2.entries)) return false
        if (!nodeEquals(n.selector, n2.selector)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: YieldStmt, arg: Visitable): Boolean {
        val n2 = arg as YieldStmt
        if (!nodeEquals(n.expression, n2.expression)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: TextBlockLiteralExpr, arg: Visitable): Boolean {
        val n2 = arg as TextBlockLiteralExpr
        if (!objEquals(n.value, n2.value)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: PatternExpr, arg: Visitable): Boolean {
        val n2 = arg as PatternExpr
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodeEquals(n.type, n2.type)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: RecordDeclaration, arg: Visitable): Boolean {
        val n2 = arg as RecordDeclaration
        if (!nodesEquals(n.implementedTypes, n2.implementedTypes)) return false
        if (!nodesEquals(n.parameters, n2.parameters)) return false
        if (!nodeEquals(n.receiverParameter, n2.receiverParameter)) return false
        if (!nodesEquals(n.typeParameters, n2.typeParameters)) return false
        if (!nodesEquals(n.members, n2.members)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }

    override fun visit(n: CompactConstructorDeclaration, arg: Visitable): Boolean {
        val n2 = arg as CompactConstructorDeclaration
        if (!nodeEquals(n.body, n2.body)) return false
        if (!nodesEquals(n.modifiers, n2.modifiers)) return false
        if (!nodeEquals(n.name, n2.name)) return false
        if (!nodesEquals(n.thrownExceptions, n2.thrownExceptions)) return false
        if (!nodesEquals(n.typeParameters, n2.typeParameters)) return false
        if (!nodesEquals(n.annotations, n2.annotations)) return false
        return nodeEquals(n.comment, n2.comment)
    }
}