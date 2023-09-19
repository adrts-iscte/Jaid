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

    private val debug = false

    override fun visit(n: ObjectCreationExpr, arg: Project) {
        val baseObjectCreationExpr = originalNode.findFirst(ObjectCreationExpr::class.java) { n == it }.get()
        val constructorUuid = originalProject.getReferenceOfNode(baseObjectCreationExpr)
        constructorUuid?.let {
            val constructorToCorrect = arg.getConstructorByUUID(constructorUuid)
            if (constructorToCorrect != null) {
                n.type.setName(constructorToCorrect.name)
            } else {
                if (debug) {
                    println("Constructor with UUID $constructorUuid not found")
                } else { }
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: ClassOrInterfaceType, arg: Project) {
        val allBaseClassOrInterfaceType = originalNode.findAll(ClassOrInterfaceType::class.java) {it.name == n.name}
        allBaseClassOrInterfaceType.stream().forEach { foundClassOrInterfaceType ->
            val typeUuid = originalProject.getReferenceOfNode(foundClassOrInterfaceType)
            typeUuid?.let {
                val typeToCorrect = arg.getTypeByUUID(typeUuid)
                if (typeToCorrect != null) {
                    n.name = typeToCorrect.name
                } else {
                    if (debug) {
                        println("Type with UUID $typeUuid not found")
                    } else { }
                }
//                try {
//                    n.name = arg.getTypeByUUID(typeUuid).name
//                } catch (e : NullPointerException) {
//                    originalProject.debug()
//                    arg.debug()
//                    println("NullPointer")
//                }
            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: FieldAccessExpr, arg: Project) {
        val baseFieldAccessExpr = originalNode.findFirst(FieldAccessExpr::class.java) { n == it }.get()
        val fieldOrEnumConstantUuid = originalProject.getReferenceOfNode(baseFieldAccessExpr)
        fieldOrEnumConstantUuid?.let {
            val referencedNode = arg.getElementByUUID(fieldOrEnumConstantUuid)
            when (referencedNode) {
                is FieldDeclaration -> {
                    val fieldToCorrect = arg.getFieldByUUID(referencedNode.uuid)
                    if (fieldToCorrect != null) {
                        n.setName((fieldToCorrect.variables.first() as VariableDeclarator).name)
                    } else {
                        if (debug) {
                            println("Field with UUID $fieldOrEnumConstantUuid not found")
                        } else { }
                    }
                }
                is EnumConstantDeclaration -> {
                    val enumConstantToCorrect = arg.getEnumConstantByUUID(referencedNode.uuid)
                    if (enumConstantToCorrect != null) {
                        n.setName(enumConstantToCorrect.name)
                    } else {
                        if(debug) {
                            println("Enum constant with UUID $fieldOrEnumConstantUuid not found")
                        } else { }
                    }
                }
                else -> {
                    if (debug) {
                        println("Neither field nor enum constant was found")
                    } else { }
                }
            }
//            referenceName?.let {
//                n.setName(referenceName)
//            }
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
            when (referencedNode) {
                is FieldDeclaration -> {
                    val fieldToCorrect = arg.getFieldByUUID(referencedNode.uuid)
                    if (fieldToCorrect != null) {
                        n.setName((fieldToCorrect.variables.first() as VariableDeclarator).name)
                    } else {
                        if (debug) {
                            println("Field with UUID $fieldOrEnumConstantOrTypeUuid not found")
                        } else { }
                    }
                }
                is EnumConstantDeclaration -> {
                    val enumConstantToCorrect = arg.getEnumConstantByUUID(referencedNode.uuid)
                    if (enumConstantToCorrect != null) {
                        n.setName(enumConstantToCorrect.name)
                    } else {
                        if (debug) {
                            println("Enum constant with UUID $fieldOrEnumConstantOrTypeUuid not found")
                        } else { }
                    }
                }
                is TypeDeclaration<*> -> {
                    val typeToCorrect = arg.getTypeByUUID(referencedNode.uuid)
                    if (typeToCorrect != null) {
                        n.setName(typeToCorrect.name)
                    } else {
                        if (debug) {
                            println("Type with UUID $fieldOrEnumConstantOrTypeUuid not found")
                        } else { }
                    }
                }
                else -> {
                    if (debug) {
                        println("Neither field nor enum constant nor type was found")
                    } else { }
                }
            }
//            when (referencedNode) {
//                is FieldDeclaration -> {
//                    val fieldReference = arg.getFieldByUUID(referencedNode.uuid)
//                    (fieldReference.variables.first() as VariableDeclarator).name
//                }
//                is EnumConstantDeclaration -> arg.getEnumConstantByUUID(referencedNode.uuid).name
//                is TypeDeclaration<*> -> arg.getTypeByUUID(referencedNode.uuid).name
//                else -> null
//            }
//            referenceName?.let {
//                n.setName(referenceName)
//            }
        }
        super.visit(n, arg)
    }

    override fun visit(n: MethodCallExpr, arg: Project) {
        val baseMethodCallExpr = originalNode.findFirst(MethodCallExpr::class.java) { n == it }.get()
        val methodUuid = originalProject.getReferenceOfNode(baseMethodCallExpr)
        methodUuid?.let {
            val methodToCorrect = arg.getMethodByUUID(methodUuid)
            if (methodToCorrect != null) {
                n.setName(methodToCorrect.name)
            } else {
                if (debug) {
                    println("Method with UUID $methodUuid not found")
                } else { }
            }
//            try {
//                n.setName(arg.getMethodByUUID(methodUuid).name)
//            } catch (e : NullPointerException) {
////                originalProject.debug()
////                arg.debug()
//                println("NullPointer")
//            }
        }
        super.visit(n, arg)
    }
}