package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.*
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import model.Project
import model.uuid
import java.lang.NullPointerException

class CorrectAllReferencesVisitor(private val baseNode : Node) : VoidVisitorAdapter<Project>() {

//    private val solver = CombinedTypeSolver()
//    //Meter o solver do project
//
//    init {
//        solver.add(ReflectionTypeSolver())
//    }

    private val debug = false

    override fun visit(n: ObjectCreationExpr, arg: Project) {
        val solver = arg.getSolver()
        val baseObjectCreationExpr = baseNode.findFirst(ObjectCreationExpr::class.java) { n == it }.get()
        try {
            val jpf = JavaParserFacade.get(solver).solve(baseObjectCreationExpr)
            if (jpf.isSolved) {
                val constructorDecl = (jpf.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
                constructorDecl?.let {
                    try {
                        n.type.setName(arg.getConstructorByUUID(constructorDecl.uuid)!!.name)
                    } catch (ex : NullPointerException) {
                        if (debug) {
                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${constructorDecl.parentNode.get()::class.java}---")
                        } else {}
                    }
                }
            }
        } catch (ex: UnsolvedSymbolException) {
            if (debug) {
                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
            } else {}
        }
        super.visit(n, arg)
    }

    override fun visit(n: ClassOrInterfaceType, arg: Project) {
        val solver = arg.getSolver()
        val allBaseClassOrInterfaceType = baseNode.findAll(ClassOrInterfaceType::class.java) {it.name == n.name}
        allBaseClassOrInterfaceType.stream().forEach {
            try {
                val jpf = JavaParserFacade.get(solver).convertToUsage(it)
                if (jpf.isReferenceType) {
                    val decl = jpf.asReferenceType().typeDeclaration.get()
                    val classOrInterfaceDecl = when (decl) {
                        is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                        is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                        else -> null
                    }
                    classOrInterfaceDecl?.let {
                        try {
                            n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)!!.name)
                        } catch (ex : NullPointerException) {
                            if (debug) {
                                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${classOrInterfaceDecl.parentNode.get()::class.java}---")
                            } else {}
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: FieldAccessExpr, arg: Project) {
        val solver = arg.getSolver()
        val baseFieldAccessExpr = baseNode.findFirst(FieldAccessExpr::class.java) { n == it }.get()
        try {
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
        } catch (ex: UnsolvedSymbolException) {
            if (debug) {
                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
            } else {}
        }
        super.visit(n, arg)

    }

    override fun visit(n: NameExpr, arg: Project) {
        val solver = arg.getSolver()
        val baseNameExpr = baseNode.findFirst(NameExpr::class.java) { n == it }.get()
        try {
            val jpf = JavaParserFacade.get(solver).solve(baseNameExpr)
            if (jpf.isSolved) {
                val decl = jpf.correspondingDeclaration
                val classOrInterfaceDecl = when (decl) {
                    is JavaParserClassDeclaration -> decl.wrappedNode
                    is JavaParserInterfaceDeclaration -> decl.wrappedNode
                    else -> null
                }
                if (classOrInterfaceDecl != null) {
                    try{
                        n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)!!.name)
                    } catch (ex : NullPointerException) {
                        if (debug) {
                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${classOrInterfaceDecl.parentNode.get()::class.java}---")
                        } else {}
                    }
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
                        try {
                            n.setName(arg.getClassOrInterfaceByUUID(classOrInterfaceDecl.uuid)!!.name)
                        } catch (ex : NullPointerException) {
                            if (debug) {
                                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${classOrInterfaceDecl.parentNode.get()::class.java}---")
                            }  else {}
                        }
                    }
                }
            }
        } catch (ex: UnsolvedSymbolException) {
            if (debug) {
                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
            } else {}
        }
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr, arg: Project) {
        val solver = arg.getSolver()
        val baseMethodCallExpr = baseNode.findFirst(MethodCallExpr::class.java) { n == it }.get()
        try {
            val jpf = JavaParserFacade.get(solver).solve(baseMethodCallExpr)
            if (jpf.isSolved) {
                val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
                methodDecl?.let {
                    try {
                        n.setName(arg.getMethodByUUID(methodDecl.uuid)!!.name)
                    } catch (ex : NullPointerException) {
                        if (debug) {
                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${methodDecl.parentNode.get()::class.java}---")
                        } else {}
                    }
                }
            }
        } catch (ex: UnsolvedSymbolException) {
            if (debug) {
                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
            }else {}
        }
        super.visit(n, arg)
    }
}