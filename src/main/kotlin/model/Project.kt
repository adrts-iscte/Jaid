package model

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
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
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.*
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.ProjectRoot
import com.github.javaparser.utils.SourceRoot
import model.transformations.Transformation
import model.visitors.ClassUsageCallsVisitor
import model.visitors.FieldUsesVisitor
import model.visitors.MethodCallExprVisitor
import model.visitors.SetupProjectVisitor
import java.io.File
import kotlin.io.path.Path

class Project {

    private val path : String

    private val setOfCompilationUnit = mutableSetOf<CompilationUnit>()

    private val indexOfUUIDs = mutableMapOf<UUID, Node>()
    private val indexOfUUIDsClassOrInterfaceDeclaration = mutableMapOf<UUID, ClassOrInterfaceDeclaration>()
    private val indexOfUUIDsConstructor = mutableMapOf<UUID, ConstructorDeclaration>()
    private val indexOfUUIDsMethod = mutableMapOf<UUID, MethodDeclaration>()
    private val indexOfUUIDsField = mutableMapOf<UUID, FieldDeclaration>()

    private val indexOfMethodCallExpr = mutableMapOf<MethodDeclaration, MutableList<MethodCallExpr>>()
    private val listOfFieldUses = mutableListOf<Node>()
    private val listOfClassUsageCalls = mutableListOf<Node>()

    private val solver : CombinedTypeSolver

    private val setupProject : Boolean
    private val onlyGeneratedFiles : Boolean

    constructor(path : String, setupProject : Boolean = true, onlyGeneratedFiles : Boolean = false) {
        this.setupProject = setupProject
        this.onlyGeneratedFiles = onlyGeneratedFiles
        this.path = path
        this.solver = CombinedTypeSolver()
        solver.add(ReflectionTypeSolver(false))
        solver.add(JavaParserTypeSolver(File(path)))
        if (File(path).isFile) {
            val javaParser = JavaParser(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parseResult = javaParser.parse(File(path))
            if (parseResult.isSuccessful) setOfCompilationUnit.add(parseResult.result.get())
        } else {
            val sourceRoot = SourceRoot(Path(path)).setParserConfiguration(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parse = sourceRoot.tryToParse()
            val results = if (onlyGeneratedFiles) {
                parse.filter { it.isSuccessful && it.result.get().storage.get().fileName.endsWith("_generated.java")}.map { it.result.get() }
            } else {
                parse.filter { it.isSuccessful }.map { it.result.get() }
            }
            setOfCompilationUnit.addAll(results)
        }
        loadProject()
    }

    constructor(path : String, givenListOfCompilationUnit : MutableList<CompilationUnit>,
                solver : CombinedTypeSolver, setupProject : Boolean = true, onlyGeneratedFiles : Boolean = false) {
        this.setupProject = setupProject
        this.onlyGeneratedFiles = onlyGeneratedFiles
        this.path = path
        this.solver = solver
        setOfCompilationUnit.addAll(givenListOfCompilationUnit)
        loadProject()
    }

//    private fun initializeSolver()

    fun clone() : Project {
        val clonedListOfCompilationUnit = setOfCompilationUnit.toMutableList().map { it.clone() }.toMutableList()
        return Project(this.path, clonedListOfCompilationUnit, solver)
    }

    fun isCorrectASTafterApplyingBothTransformations(a: Transformation, b: Transformation) : Boolean {
        val clonedProject = this.clone()
        a.applyTransformation(clonedProject)
        b.applyTransformation(clonedProject)
        val javaParser = JavaParser(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
        val parsedCompilationunits = clonedProject.getSetOfCompilationUnit().map { javaParser.parse(it.toString()) }
        return parsedCompilationunits.all { it.isSuccessful }
    }

    fun getPath() = path

    private fun loadProject() {
        if (setupProject) {
            val setupProjectVisitor = SetupProjectVisitor()
            setOfCompilationUnit.forEach { it.accept(setupProjectVisitor, indexOfUUIDs) }
        }

        createIndexOfMethodCalls()

        val fieldUsesVisitor = FieldUsesVisitor()
        setOfCompilationUnit.forEach { it.accept(fieldUsesVisitor, listOfFieldUses) }

        val classUsageCallsVisitor = ClassUsageCallsVisitor()
        setOfCompilationUnit.forEach { it.accept(classUsageCallsVisitor, listOfClassUsageCalls) }

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
        createIndexOfMethodCalls()

        indexOfMethodCallExpr.filterKeys { it.uuid == methodUuidToRename }.forEach { entry ->
            entry.value.forEach {
                it.setName(newName)
            }
        }
    }

    private fun createIndexOfMethodCalls() {
        indexOfMethodCallExpr.clear()
        val listOfMethodCallExpr = mutableListOf<MethodCallExpr>()
        val methodCallExprVisitor = MethodCallExprVisitor()
        setOfCompilationUnit.forEach { it.accept(methodCallExprVisitor, listOfMethodCallExpr) }

        listOfMethodCallExpr.forEach {methodCallExpr ->
            try {
                val jpf = JavaParserFacade.get(solver).solve(methodCallExpr)
                if (jpf.isSolved) {
                    val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
                    methodDecl?.let {
                        indexOfMethodCallExpr.getOrPut(methodDecl) { mutableListOf() }.add(methodCallExpr)
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                println("Foi encontrada uma exceção: ${ex.message}")
            }
        }
    }

    fun renameAllClassUsageCalls(classUuidToRename: UUID, newName: String) {
        val newClassToRename = getClassOrInterfaceByUUID(classUuidToRename)

        newClassToRename?.let {
            val classUsageCallsVisitor = ClassUsageCallsVisitor()
            listOfClassUsageCalls.clear()
            setOfCompilationUnit.forEach { it.accept(classUsageCallsVisitor, listOfClassUsageCalls) }

            val allUses = mutableListOf<Node>()
            solveObjectCreationExpr(allUses, classUuidToRename)
            solveClassOrInterfaceType(allUses, classUuidToRename)
            solveClassNameExpr(allUses, classUuidToRename)
            allUses.filterIsInstance<ObjectCreationExpr>().forEach { it.type.setName(newName) }
            allUses.filterIsInstance<ClassOrInterfaceType>().forEach { it.setName(newName) }
            allUses.filterIsInstance<NameExpr>().forEach { it.setName(newName) }

            newClassToRename.constructors.forEach {
                it.setName(newName)
            }
        }
    }

    private fun solveObjectCreationExpr(allUses : MutableList<Node>, classUuidToRename: UUID) {
        listOfClassUsageCalls.filterIsInstance<ObjectCreationExpr>().forEach {
            val jpf = JavaParserFacade.get(solver).solve(it)
            if (jpf.isSolved) {
                val constructorDecl = (jpf.correspondingDeclaration as JavaParserConstructorDeclaration<*>).wrappedNode
                if (constructorDecl.parentNode.orElse(null)?.uuid == classUuidToRename) {
                    allUses.add(it)
                }
            }
        }
    }

    private fun solveClassOrInterfaceType(allUses : MutableList<Node>, classUuidToRename: UUID) {
        listOfClassUsageCalls.filterIsInstance<ClassOrInterfaceType>().forEach {
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

    private fun solveClassNameExpr(allUses : MutableList<Node>, typeUuidToRename: UUID) {
        listOfClassUsageCalls.filterIsInstance<NameExpr>().forEach { nameExpr ->
            val jpf = JavaParserFacade.get(solver).solve(nameExpr)
            if (jpf.isSolved) {
                val decl = jpf.correspondingDeclaration
                val classOrInterfaceDecl = when (decl) {
                    is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                    is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                    else -> null
                }
                classOrInterfaceDecl?.let {
                    if (classOrInterfaceDecl.uuid == typeUuidToRename) {
                        allUses.add(nameExpr)
                    }
                }
            } else {
                val resolvedType = nameExpr.calculateResolvedType()
                if(resolvedType.isReferenceType) {
                    val decl = resolvedType.asReferenceType().typeDeclaration.get()
                    val classOrInterfaceDecl = when (decl) {
                        is JavaParserClassDeclaration -> decl.wrappedNode
                        is JavaParserInterfaceDeclaration -> decl.wrappedNode
                        else -> null
                    }
                    classOrInterfaceDecl?.let {
                        if (classOrInterfaceDecl.uuid == typeUuidToRename) {
                            allUses.add(nameExpr)
                        }
                    }
                }
            }
        }
    }
}