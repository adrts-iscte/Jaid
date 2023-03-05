package model

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.utils.SourceRoot
import model.visitors.ClassConstructorCallsVisitor
import model.visitors.FieldUsesVisitor
import model.visitors.MethodCallExprVisitor
import model.visitors.SetupProjectVisitor
import java.io.File
import kotlin.io.path.Path

class Project {

    private val setOfCompilationUnit = mutableSetOf<CompilationUnit>()

    private val indexOfUUIDs = mutableMapOf<UUID, Node>()
    private val indexOfUUIDsClassOrInterfaceDeclaration = mutableMapOf<UUID, ClassOrInterfaceDeclaration>()
    private val indexOfUUIDsConstructor = mutableMapOf<UUID, ConstructorDeclaration>()
    private val indexOfUUIDsMethod = mutableMapOf<UUID, MethodDeclaration>()
    private val indexOfUUIDsField = mutableMapOf<UUID, FieldDeclaration>()

    private val indexOfMethodCallExpr = mutableMapOf<MethodDeclaration, MutableList<MethodCallExpr>>()
    private val listOfFieldUses = mutableListOf<Node>()
    private val listOfClassConstructorCalls = mutableListOf<Node>()

    private val solver : CombinedTypeSolver

    constructor(path : String) {
        this.solver = CombinedTypeSolver()
        solver.add(ReflectionTypeSolver(false))
        if (File(path).isFile) {
            val javaParser = JavaParser(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parseResult = javaParser.parse(File(path))
            if (parseResult.isSuccessful) setOfCompilationUnit.add(parseResult.result.get())
        } else {
            solver.add(JavaParserTypeSolver(File(path)))
            val sourceRoot = SourceRoot(Path(path)).setParserConfiguration(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parse = sourceRoot.tryToParse()
            setOfCompilationUnit.addAll(parse.filter { it.isSuccessful }.map { it.result.get() })
        }
        loadProject()
    }

    constructor(givenListOfCompilationUnit : MutableList<CompilationUnit>, solver : CombinedTypeSolver) {
        this.solver = solver
        setOfCompilationUnit.addAll(givenListOfCompilationUnit)
        loadProject()
    }

//    private fun initializeSolver()

    fun clone() : Project {
        val clonedListOfCompilationUnit = setOfCompilationUnit.toMutableList().map { it.clone() }.toMutableList()
        return Project(clonedListOfCompilationUnit, solver)
    }

    private fun loadProject() {

        val setupProjectVisitor = SetupProjectVisitor()
        setOfCompilationUnit.forEach { it.accept(setupProjectVisitor, indexOfUUIDs) }

        val methodCallExprVisitor = MethodCallExprVisitor()
        setOfCompilationUnit.forEach { it.accept(methodCallExprVisitor, indexOfMethodCallExpr) }

        val fieldUsesVisitor = FieldUsesVisitor()
        setOfCompilationUnit.forEach { it.accept(fieldUsesVisitor, listOfFieldUses) }

        val classConstructorCallsVisitor = ClassConstructorCallsVisitor()
        setOfCompilationUnit.forEach { it.accept(classConstructorCallsVisitor, listOfClassConstructorCalls) }

        initializeAllIndexes()
    }

    private fun initializeAllIndexes() {
        indexOfUUIDsClassOrInterfaceDeclaration.putAll(indexOfUUIDs.filterValues { it is ClassOrInterfaceDeclaration } as Map<UUID, ClassOrInterfaceDeclaration>)
        indexOfUUIDsConstructor.putAll(indexOfUUIDs.filterValues { it is ConstructorDeclaration } as Map<UUID, ConstructorDeclaration>)
        indexOfUUIDsMethod.putAll(indexOfUUIDs.filterValues { it is MethodDeclaration } as Map<UUID, MethodDeclaration>)
        indexOfUUIDsField.putAll(indexOfUUIDs.filterValues { it is FieldDeclaration } as Map<UUID, FieldDeclaration>)
        assert(indexOfUUIDsClassOrInterfaceDeclaration.size + indexOfUUIDsConstructor.size +
                indexOfUUIDsMethod.size + indexOfUUIDsField.size == indexOfUUIDs.size)
    }

    fun getSetOfCompilationUnit() = setOfCompilationUnit

    fun getIndexes(): MutableMap<UUID, Node> = indexOfUUIDs

    fun getElementByUUID(uuid: UUID) : Node? = indexOfUUIDs[uuid]

    fun getClassOrInterfaceByUUID(uuid: UUID) : ClassOrInterfaceDeclaration? = indexOfUUIDsClassOrInterfaceDeclaration[uuid]

    fun getConstructorByUUID(uuid: UUID) : ConstructorDeclaration? = indexOfUUIDsConstructor[uuid]

    fun getMethodByUUID(uuid: UUID) : MethodDeclaration? = indexOfUUIDsMethod[uuid]

    fun getFieldByUUID(uuid: UUID) : FieldDeclaration? = indexOfUUIDsField[uuid]

    fun getCompilationUnitByPath(path : String) : CompilationUnit? {
        return setOfCompilationUnit.find {
            it.correctPath == path
        }
    }

    fun getListOfMethods() = indexOfUUIDsMethod.values

    fun getMapOfMethodCallExpr() = indexOfMethodCallExpr

    fun getListOfFieldUses() = listOfFieldUses

    fun getListOfClassConstructorCalls() = listOfClassConstructorCalls

    fun renameAllFieldUses(fieldUuidToRename: UUID, newName: String) {
        val fieldUsesVisitor = FieldUsesVisitor()
        listOfFieldUses.clear()
        setOfCompilationUnit.forEach { it.accept(fieldUsesVisitor, listOfFieldUses) }

        solveFieldAccessExpr(fieldUuidToRename, newName)
        solveNameExpr(fieldUuidToRename, newName)
    }

    private fun solveNameExpr(fieldUuidToRename: UUID, newName: String) {
        listOfFieldUses.filterIsInstance<NameExpr>().forEach {
            val jpf = JavaParserFacade.get(solver).solve(it)
            if (jpf.isSolved) {
                when (jpf.correspondingDeclaration) {
                    is JavaParserFieldDeclaration -> {
                        if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldUuidToRename) {
                            it.setName(newName)
                        }
                    }
                }
            }
        }
    }

    private fun solveFieldAccessExpr(fieldUuidToRename: UUID, newName: String) {
        println(setOfCompilationUnit.elementAt(0))
        listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
            val jpf = JavaParserFacade.get(solver).solve(it)
            if (jpf.isSolved) {
                when (jpf.correspondingDeclaration) {
                    is JavaParserFieldDeclaration -> {
                        if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldUuidToRename) {
                            it.setName(newName)
                        }
                    }
                }
            }
        }
    }

    fun renameAllMethodCalls(methodUuidToRename: UUID, newName: String) {
        val methodCallExprVisitor = MethodCallExprVisitor()
        indexOfMethodCallExpr.clear()
        setOfCompilationUnit.forEach { it.accept(methodCallExprVisitor, indexOfMethodCallExpr) }

        indexOfMethodCallExpr.filterKeys { it.uuid == methodUuidToRename }.forEach { entry ->
            entry.value.forEach {
                it.setName(newName)
            }
        }
    }

    fun renameAllConstructorCalls(classUuidToRename: UUID, newName: String) {
        val newClassToRename = getClassOrInterfaceByUUID(classUuidToRename)

        newClassToRename?.let {
            val classConstructorCallsVisitor = ClassConstructorCallsVisitor()
            listOfClassConstructorCalls.clear()
            setOfCompilationUnit.forEach { it.accept(classConstructorCallsVisitor, listOfClassConstructorCalls) }

            val allUses = mutableListOf<Node>()
            solveObjectCreationExpr(allUses, classUuidToRename, newName)
            solveClassOrInterfaceType(allUses, classUuidToRename, newName)
            allUses.filterIsInstance<ObjectCreationExpr>().forEach { it.type.setName(newName) }
            allUses.filterIsInstance<ClassOrInterfaceType>().forEach { it.setName(newName) }

            newClassToRename.constructors.forEach {
                it.setName(newName)
            }
        }
    }

    private fun solveObjectCreationExpr(allUses : MutableList<Node>, classUuidToRename: UUID, newName: String) {
        listOfClassConstructorCalls.filterIsInstance<ObjectCreationExpr>().forEach {
            val jpf = JavaParserFacade.get(solver).solve(it)
            if (jpf.isSolved) {
                val constructorDecl = (jpf.correspondingDeclaration as JavaParserConstructorDeclaration<*>).wrappedNode
                if (constructorDecl.parentNode.orElse(null)?.uuid == classUuidToRename) {
                    allUses.add(it)
                }
            }
        }
    }

    private fun solveClassOrInterfaceType(allUses : MutableList<Node>, classUuidToRename: UUID, newName: String) {
        listOfClassConstructorCalls.filterIsInstance<ClassOrInterfaceType>().forEach {
            val jpf = JavaParserFacade.get(solver).convertToUsage(it)
            if (jpf.isReferenceType) {
                val decl = jpf.asReferenceType().typeDeclaration.get()
                val classDecl = when (decl) {
                    is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                    is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                    else -> null
                }
                if (classDecl?.uuid == classUuidToRename) {
                    allUses.add(it)
                }
            }
        }
    }
}