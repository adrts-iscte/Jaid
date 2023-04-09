package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.*
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import model.Project
import model.uuid

class CorrectAllReferencesVisitor(private val baseNode : Node) : VoidVisitorAdapter<Project>() {

    private val solver = CombinedTypeSolver()
    //Meter o solver do project

    init {
        solver.add(ReflectionTypeSolver())
    }

    override fun visit(n: ObjectCreationExpr, arg: Project) {
        val baseObjectCreationExpr = baseNode.findFirst(ObjectCreationExpr::class.java) { n == it }.get()
        val jpf = JavaParserFacade.get(solver).solve(baseObjectCreationExpr)
        if (jpf.isSolved) {
            val constructorDecl = (jpf.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            constructorDecl?.let {
                n.type.setName(arg.getConstructorByUUID(constructorDecl.uuid)?.name)
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: ClassOrInterfaceType, arg: Project) {
        val allBaseClassOrInterfaceType = baseNode.findAll(ClassOrInterfaceType::class.java)
        allBaseClassOrInterfaceType.stream().forEach {
            val jpf = JavaParserFacade.get(solver).convertToUsage(it)
            if (jpf.isReferenceType) {
                val decl = jpf.asReferenceType().typeDeclaration.get()
                val classOrInterfaceDecl = when (decl) {
                    is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                    is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                    else -> null
                }
                classOrInterfaceDecl?.let {
                    n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)?.name)
                }
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: FieldAccessExpr, arg: Project) {
        val baseFieldAccessExpr = baseNode.findFirst(FieldAccessExpr::class.java) { n == it }.get()
        val jpf = JavaParserFacade.get(solver).solve(baseFieldAccessExpr)
        if (jpf.isSolved) {
            val fieldDecl = (jpf.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
            fieldDecl?.let {
                val fieldReference = arg.getFieldByUUID(fieldDecl.uuid)
                fieldReference?.let {
                    n.setName((fieldReference.variables.first() as VariableDeclarator).name)
                }
            }
        }
        super.visit(n, arg)

    }

    override fun visit(n: NameExpr, arg: Project) {
        val baseNameExpr = baseNode.findFirst(NameExpr::class.java) { n == it }.get()
        val jpf = JavaParserFacade.get(solver).solve(baseNameExpr)
        if (jpf.isSolved) {
            val decl = jpf.correspondingDeclaration
            val classOrInterfaceDecl = when (decl) {
                is JavaParserClassDeclaration -> decl.wrappedNode
                is JavaParserInterfaceDeclaration -> decl.wrappedNode
                else -> null
            }
            if (classOrInterfaceDecl != null) {
                n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)?.name)
            } else {
                val fieldDecl = (jpf.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
                fieldDecl?.let {
                    val fieldReference = arg.getFieldByUUID(fieldDecl.uuid)
                    fieldReference?.let {
                        n.setName((fieldReference.variables.first() as VariableDeclarator).name)
                    }
                }
            }
        } else {
            val resolvedType = baseNameExpr.calculateResolvedType()
            if(resolvedType.isReferenceType) {
                val decl = resolvedType.asReferenceType().typeDeclaration.get()
                val classOrInterfaceDecl = when (decl) {
                    is JavaParserClassDeclaration -> decl.wrappedNode
                    is JavaParserInterfaceDeclaration -> decl.wrappedNode
                    else -> null
                }
                classOrInterfaceDecl?.let {
                    n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)?.name)
                }
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr, arg: Project) {
        val baseMethodCallExpr = baseNode.findFirst(MethodCallExpr::class.java) { n == it }.get()
        val jpf = JavaParserFacade.get(solver).solve(baseMethodCallExpr)
        if (jpf.isSolved) {
            val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
            methodDecl?.let {
                n.setName(arg.getMethodByUUID(methodDecl.uuid)?.name)
            }
        }
        super.visit(n, arg)
    }
}