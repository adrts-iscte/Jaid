package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.EnumConstantDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import model.Project
import model.uuid

class CorrectAllReferencesVisitor(private val originalProject: Project, private val originalNode : Node) : VoidVisitorAdapter<Project>() {

//    private val debug = false

    override fun visit(n: ObjectCreationExpr, arg: Project) {
        val baseObjectCreationExpr = originalNode.findFirst(ObjectCreationExpr::class.java) { n == it }.get()
        val constructorUuid = originalProject.getReferenceOfNode(baseObjectCreationExpr)
        constructorUuid?.let {
            n.type.setName(arg.getConstructorByUUID(constructorUuid).name)
        }
        super.visit(n, arg)
    }

    override fun visit(n: ClassOrInterfaceType, arg: Project) {
        val allBaseClassOrInterfaceType = originalNode.findAll(ClassOrInterfaceType::class.java) {it.name == n.name}
        allBaseClassOrInterfaceType.stream().forEach {
            val typeUuid = originalProject.getReferenceOfNode(it)
            typeUuid?.let {
                n.name = arg.getTypeByUUID(typeUuid).name
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: FieldAccessExpr, arg: Project) {
        val baseFieldAccessExpr = originalNode.findFirst(FieldAccessExpr::class.java) { n == it }.get()
        val fieldOrEnumConstantUuid = originalProject.getReferenceOfNode(baseFieldAccessExpr)
        fieldOrEnumConstantUuid?.let {
            val referencedNode = arg.getElementByUUID(fieldOrEnumConstantUuid)
            val referenceName = when (referencedNode) {
                is FieldDeclaration -> {
                    val fieldReference = arg.getFieldByUUID(referencedNode.uuid)
                    (fieldReference.variables.first() as VariableDeclarator).name
                }
                is EnumConstantDeclaration -> arg.getEnumConstantByUUID(referencedNode.uuid).name
                else -> null
            }
            referenceName?.let {
                n.setName(referenceName)
            }
        }
//            val jpf = JavaParserFacade.get(solver).solve(baseFieldAccessExpr)
//            if (jpf.isSolved) {
//                val decl = jpf.correspondingDeclaration
//                val fieldDecl = when (decl) {
//                    is JavaParserFieldDeclaration -> (decl as? JavaParserFieldDeclaration)?.wrappedNode
//                    is JavaParserEnumConstantDeclaration -> (decl as? JavaParserEnumConstantDeclaration)?.wrappedNode
//                    else -> throw UnsolvedSymbolException("Não conseguiu resolver!")
//                }
//                try{
//                    val referenceName = when (fieldDecl) {
//                        is FieldDeclaration -> {val fieldReference = arg.getFieldByUUID(fieldDecl.uuid)
//                                                (fieldReference.variables.first() as VariableDeclarator).name
//                        }
//                        is EnumConstantDeclaration -> arg.getEnumConstantByUUID(fieldDecl.uuid).name
//                        else -> throw UnsolvedSymbolException("Não conseguiu resolver!")
//                    }
//                    n.name = referenceName
//                } catch (ex : NullPointerException) {
//                    if (debug) {
//                        println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${fieldDecl!!.parentNode.get()::class.java}---")
//                    }
//                }
//            }
        super.visit(n, arg)
    }

    override fun visit(n: NameExpr, arg: Project) {
        val baseNameExpr = originalNode.findFirst(NameExpr::class.java) { n == it }.get()
        val fieldOrEnumConstantOrTypeUuid = originalProject.getReferenceOfNode(baseNameExpr)
        fieldOrEnumConstantOrTypeUuid?.let {
            val referencedNode = arg.getElementByUUID(fieldOrEnumConstantOrTypeUuid)
            val referenceName = when (referencedNode) {
                is FieldDeclaration -> {
                    val fieldReference = arg.getFieldByUUID(referencedNode.uuid)
                    (fieldReference.variables.first() as VariableDeclarator).name
                }
                is EnumConstantDeclaration -> arg.getEnumConstantByUUID(referencedNode.uuid).name
                is TypeDeclaration<*> -> arg.getTypeByUUID(referencedNode.uuid).name
                else -> null
            }
            referenceName?.let {
                n.setName(referenceName)
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr, arg: Project) {
        val baseMethodCallExpr = originalNode.findFirst(MethodCallExpr::class.java) { n == it }.get()
        val methodUuid = originalProject.getReferenceOfNode(baseMethodCallExpr)
        methodUuid?.let {
            n.setName(arg.getMethodByUUID(methodUuid).name)
        }
        super.visit(n, arg)
    }
}

//class CorrectAllReferencesVisitor(private val baseNode : Node) : VoidVisitorAdapter<Project>() {
//
//    private val debug = false
//
//    override fun visit(n: ObjectCreationExpr, arg: Project) {
//        val solver = arg.getSolver()
//        val baseObjectCreationExpr = baseNode.findFirst(ObjectCreationExpr::class.java) { n == it }.get()
//        try {
//            val jpf = JavaParserFacade.get(solver).solve(baseObjectCreationExpr)
//            if (jpf.isSolved) {
//                val constructorDecl = (jpf.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
//                constructorDecl?.let {
//                    try {
//                        n.type.setName(arg.getConstructorByUUID(constructorDecl.uuid).name)
//                    } catch (ex : NullPointerException) {
//                        if (debug) {
//                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${constructorDecl.parentNode.get()::class.java}---")
//                        } else {}
//                    }
//                }
//            }
//        } catch (ex: UnsolvedSymbolException) {
//            if (debug) {
//                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//            }
//        }
//        super.visit(n, arg)
//    }
//
//    override fun visit(n: ClassOrInterfaceType, arg: Project) {
//        val solver = arg.getSolver()
//        val allBaseClassOrInterfaceType = baseNode.findAll(ClassOrInterfaceType::class.java) {it.name == n.name}
//        allBaseClassOrInterfaceType.stream().forEach {
//            try {
//                val jpf = JavaParserFacade.get(solver).convertToUsage(it)
//                if (jpf.isReferenceType) {
//                    val decl = jpf.asReferenceType().typeDeclaration.get()
//                    val typeDecl = when (decl) {
//                        is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
//                        is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
//                        is JavaParserEnumDeclaration -> (decl as? JavaParserEnumDeclaration)?.wrappedNode
//                        else -> null
//                    }
//                    typeDecl?.let {
//                        try {
//                            n.setName(arg.getTypeByUUID(typeDecl.uuid).name)
//                        } catch (ex : NullPointerException) {
//                            if (debug) {
//                                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${typeDecl.parentNode.get()::class.java}---")
//                            } else {}
//                        }
//                    }
//                }
//            } catch (ex: UnsolvedSymbolException) {
//                if (debug) {
//                    println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//                }
//            }
//        }
//        super.visit(n, arg)
//    }
//
//    override fun visit(n: FieldAccessExpr, arg: Project) {
//        val solver = arg.getSolver()
//        val baseFieldAccessExpr = baseNode.findFirst(FieldAccessExpr::class.java) { n == it }.get()
//        try {
//            val jpf = JavaParserFacade.get(solver).solve(baseFieldAccessExpr)
//            if (jpf.isSolved) {
//                val decl = jpf.correspondingDeclaration
//                val fieldDecl = when (decl) {
//                    is JavaParserFieldDeclaration -> (decl as? JavaParserFieldDeclaration)?.wrappedNode
//                    is JavaParserEnumConstantDeclaration -> (decl as? JavaParserEnumConstantDeclaration)?.wrappedNode
//                    else -> throw UnsolvedSymbolException("Não conseguiu resolver!")
//                }
//                try{
//                    val fieldReference = arg.getFieldByUUID(fieldDecl!!.uuid)
//                    n.setName((fieldReference.variables.first() as VariableDeclarator).name)
//                } catch (ex : NullPointerException) {
//                    if (debug) {
//                        println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${fieldDecl!!.parentNode.get()::class.java}---")
//                    }
//                }
//            }
//        } catch (ex: UnsolvedSymbolException) {
//            if (debug) {
//                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//            }
//        }
//        super.visit(n, arg)
//    }
//
//    override fun visit(n: NameExpr, arg: Project) {
//        val solver = arg.getSolver()
//        val baseNameExpr = baseNode.findFirst(NameExpr::class.java) { n == it }.get()
//        try {
//            val jpf = JavaParserFacade.get(solver).solve(baseNameExpr)
//            if (jpf.isSolved) {
//                val decl = jpf.correspondingDeclaration
//                val typeDecl = when (decl) {
//                    is JavaParserClassDeclaration -> decl.wrappedNode
//                    is JavaParserInterfaceDeclaration -> decl.wrappedNode
//                    is JavaParserEnumDeclaration -> decl.wrappedNode
//                    else -> null
//                }
//                if (typeDecl != null) {
//                    try{
//                        n.setName(arg.getTypeByUUID(typeDecl.uuid).name)
//                    } catch (ex : NullPointerException) {
//                        if (debug) {
//                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${typeDecl.parentNode.get()::class.java}---")
//                        } else {}
//                    }
//                } else {
//                    val fieldDecl = (jpf.correspondingDeclaration as? JavaParserFieldDeclaration)?.wrappedNode
//                    fieldDecl?.let {
//                        try {
//                            val fieldReference = arg.getFieldByUUID(fieldDecl.uuid)
//                            n.setName((fieldReference.variables.first() as VariableDeclarator).name)
//                        } catch (ex : NullPointerException) {
//                            if (debug) {
//                                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${fieldDecl.parentNode.get()::class.java}---")
//                            } else {}
//                        }
//                    }
//                }
//            } else {
//                val resolvedType = baseNameExpr.calculateResolvedType()
//                if(resolvedType.isReferenceType) {
//                    val decl = resolvedType.asReferenceType().typeDeclaration.get()
//                    val typeDecl = when (decl) {
//                        is JavaParserClassDeclaration -> decl.wrappedNode
//                        is JavaParserInterfaceDeclaration -> decl.wrappedNode
//                        is JavaParserEnumDeclaration -> decl.wrappedNode
//                        else -> null
//                    }
//                    typeDecl?.let {
//                        try {
//                            n.setName(arg.getTypeByUUID(typeDecl.uuid).name)
//                        } catch (ex : NullPointerException) {
//                            if (debug) {
//                                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${typeDecl.parentNode.get()::class.java}---")
//                            }  else {}
//                        }
//                    }
//                }
//            }
//        } catch (ex: UnsolvedSymbolException) {
//            if (debug) {
//                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//            }
//        }
//        super.visit(n, arg)
//    }
//
//    override fun visit(n: MethodCallExpr, arg: Project) {
//        val solver = arg.getSolver()
//        val baseMethodCallExpr = baseNode.findFirst(MethodCallExpr::class.java) { n == it }.get()
//        try {
//            val jpf = JavaParserFacade.get(solver).solve(baseMethodCallExpr)
//            if (jpf.isSolved) {
//                val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
//                methodDecl?.let {
//                    try {
//                        n.setName(arg.getMethodByUUID(methodDecl.uuid).name)
//                    } catch (ex : NullPointerException) {
//                        if (debug) {
//                            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message} ---Parent ${methodDecl.parentNode.get()::class.java}---")
//                        } else {}
//                    }
//                }
//            }
//        } catch (ex: UnsolvedSymbolException) {
//            if (debug) {
//                println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//            }
//        }
//        super.visit(n, arg)
//    }
//}