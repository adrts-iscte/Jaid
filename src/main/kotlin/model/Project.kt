package model

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.*
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.utils.SourceRoot
import model.transformations.Transformation
import model.visitors.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path


class Project {
    private val debug = false
    private val path : String

    private val sourceRoot : SourceRoot?
    private val setOfCompilationUnit = mutableSetOf<CompilationUnit>()

    public val indexOfUUIDs = mutableMapOf<UUID, Node>()
    private val indexOfUUIDsClassOrInterfaceDeclaration = mutableMapOf<UUID, ClassOrInterfaceDeclaration>()
    public val indexOfUUIDsEnumDeclaration = mutableMapOf<UUID, EnumDeclaration>()
    private val indexOfUUIDsConstructor = mutableMapOf<UUID, ConstructorDeclaration>()
    private val indexOfUUIDsMethod = mutableMapOf<UUID, MethodDeclaration>()
    private val indexOfUUIDsField = mutableMapOf<UUID, FieldDeclaration>()
    private val indexOfUUIDsEnumConstant = mutableMapOf<UUID, EnumConstantDeclaration>()

    public val indexOfMethodCallExpr = mutableMapOf<UUID, MutableList<MethodCallExpr>>()
    public val indexOfFieldUses = mutableMapOf<UUID, MutableList<Expression>>()
    public val indexOfTypeUses = mutableMapOf<UUID, MutableList<Node>>()
    private val indexOfEnumConstantUses = mutableMapOf<UUID, MutableList<Expression>>()

    private val solver : CombinedTypeSolver
    private val memoryTypeSolver : MemoryTypeSolver

    private val setupProject : Boolean
    private val javaParserFacade : JavaParserFacade
    private val setupProjectVisitor = SetupProjectVisitor()

    constructor(path : String, setupProject : Boolean = true) {
        this.setupProject = setupProject
        this.path = path
        this.memoryTypeSolver = MemoryTypeSolver()
        this.solver = CombinedTypeSolver(ReflectionTypeSolver(false), memoryTypeSolver)
        var solverPath = "$path\\main\\java\\"
        if (!File(solverPath).exists())
            solverPath = path
        if (File(path).isFile) {
//            this.solver.add(JavaParserTypeSolver(path))
            val javaParser = JavaParser(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parseResult = javaParser.parse(File(path))
            if (parseResult.isSuccessful) {
                setOfCompilationUnit.add(parseResult.result.get())
            } else {
                println("Não deu parse corretamente! ${File(path).name}")
            }
            sourceRoot = null
        } else {
            this.solver.add(JavaParserTypeSolver(File(solverPath)))
            sourceRoot = SourceRoot(Path(path)).setParserConfiguration(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
            val parse = sourceRoot.tryToParseParallelized()
            parse.filter { !it.isSuccessful }.forEach {
                println("Não deu parse corretamente! $it")
            }
            val results = parse.filter { it.isSuccessful }.map { it.result.get() }
            setOfCompilationUnit.addAll(results)
        }
        javaParserFacade = JavaParserFacade.get(this.solver)
        loadProject()
    }

    constructor(path : String, sourceRoot: SourceRoot?, givenListOfCompilationUnit : MutableList<CompilationUnit>,
                solver : CombinedTypeSolver, memoryTypeSolver : MemoryTypeSolver, setupProject : Boolean = true) {
        this.setupProject = setupProject
        this.path = path
        this.solver = solver
        this.memoryTypeSolver = memoryTypeSolver
        this.sourceRoot = sourceRoot
        javaParserFacade = JavaParserFacade.get(this.solver)
        setOfCompilationUnit.addAll(givenListOfCompilationUnit)
        loadProject()
    }

    fun getSolver() = solver

    fun getMemorySolver() = memoryTypeSolver

    fun clone() : Project {
        val clonedListOfCompilationUnit = setOfCompilationUnit.toMutableList().map { it.clone() }.toMutableList()
        return Project(this.path, this.sourceRoot, clonedListOfCompilationUnit, this.solver, this.memoryTypeSolver)
    }

    fun saveProjectTo(path : Path){
        requireNotNull(sourceRoot)
        sourceRoot.saveAll(path)
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

    fun loadProject() {
        if (setOfCompilationUnit.any { it.hasEnum })
            println("There is a file with an enum! This project has ${
                setOfCompilationUnit.sumOf { it.findAll(EnumDeclaration::class.java).size }
            } enums!")
        else
            println("This project doesn't have any enums!")

        if (setOfCompilationUnit.any { it.hasClassOrInterfaceInsideAnotherClass })
            println("There is a file with a nested type! This project has ${setOfCompilationUnit.sumOf { it.findAll(ClassOrInterfaceDeclaration::class.java) { it.isClassOrInterfaceInsideAnotherClass }.size }} nested types!")
        else
            println("This project doesn't have any inner classes!")

        setOfCompilationUnit.forEach { cu ->
            cu.findAll(ClassOrInterfaceDeclaration::class.java).forEach {
                memoryTypeSolver.addDeclaration(it.nameAsString, it.resolve())
            }
            cu.findAll(EnumDeclaration::class.java).forEach {
                memoryTypeSolver.addDeclaration(it.nameAsString, it.resolve())
            }
        }

        initializeAllIndexes()
        println()
    }

//    fun indexNode(node: Node) {
//        val nodesIndexOfUUIDs = mutableMapOf<UUID, Node>()
//        node.accept(setupProjectVisitor, nodesIndexOfUUIDs)
//        indexOfUUIDs.setAll(nodesIndexOfUUIDs)
//
//        initializeAllOtherIndexes(nodesIndexOfUUIDs)
//        initializeReferencesIndexes(node)
//    }
//
//    private fun initializeAllOtherIndexes(nodesIndexOfUUIDs : MutableMap<UUID, Node>) {
//        indexOfUUIDsClassOrInterfaceDeclaration.setAll(nodesIndexOfUUIDs.filterValues { it is ClassOrInterfaceDeclaration } as Map<UUID, ClassOrInterfaceDeclaration>)
//        indexOfUUIDsEnumDeclaration.setAll(nodesIndexOfUUIDs.filterValues { it is EnumDeclaration } as Map<UUID, EnumDeclaration>)
//        indexOfUUIDsConstructor.setAll(nodesIndexOfUUIDs.filterValues { it is ConstructorDeclaration } as Map<UUID, ConstructorDeclaration>)
//        indexOfUUIDsMethod.setAll(nodesIndexOfUUIDs.filterValues { it is MethodDeclaration } as Map<UUID, MethodDeclaration>)
//        indexOfUUIDsField.setAll(nodesIndexOfUUIDs.filterValues { it is FieldDeclaration } as Map<UUID, FieldDeclaration>)
//        indexOfUUIDsEnumConstant.setAll(nodesIndexOfUUIDs.filterValues { it is EnumConstantDeclaration } as Map<UUID, EnumConstantDeclaration>)
//    }
//
//    private fun initializeReferencesIndexes(node: Node) {
//        createIndexOfMethodCalls(node)
//        createIndexOfFieldUses(node)
//        createIndexOfTypesUses(node)
//        createIndexOfEnumConstantUses(node)
//    }

    fun initializeAllIndexes() {
        println("Entrou no initializeIndexes!")
        if (setupProject) {
            setOfCompilationUnit.forEach { it.accept(setupProjectVisitor, indexOfUUIDs) }
        }

        indexOfUUIDsClassOrInterfaceDeclaration.clear()
        indexOfUUIDsEnumDeclaration.clear()
        indexOfUUIDsConstructor.clear()
        indexOfUUIDsMethod.clear()
        indexOfUUIDsField.clear()
        indexOfUUIDsEnumConstant.clear()

        indexOfUUIDsClassOrInterfaceDeclaration.setAll(indexOfUUIDs.filterValues { it is ClassOrInterfaceDeclaration } as Map<UUID, ClassOrInterfaceDeclaration>)
        indexOfUUIDsEnumDeclaration.setAll(indexOfUUIDs.filterValues { it is EnumDeclaration } as Map<UUID, EnumDeclaration>)
        indexOfUUIDsConstructor.setAll(indexOfUUIDs.filterValues { it is ConstructorDeclaration } as Map<UUID, ConstructorDeclaration>)
        indexOfUUIDsMethod.setAll(indexOfUUIDs.filterValues { it is MethodDeclaration } as Map<UUID, MethodDeclaration>)
        indexOfUUIDsField.setAll(indexOfUUIDs.filterValues { it is FieldDeclaration } as Map<UUID, FieldDeclaration>)
        indexOfUUIDsEnumConstant.setAll(indexOfUUIDs.filterValues { it is EnumConstantDeclaration } as Map<UUID, EnumConstantDeclaration>)

        createIndexOfMethodCalls()
        createIndexOfFieldUses()
        createIndexOfTypesUses()
        createIndexOfEnumConstantUses()
    }

    fun debug(){
        if (setOfCompilationUnit.any { it.correctPath.contains("JavaPluginLoader") }) {
            println("Tem sound!")
        }
        if (indexOfUUIDs.values.any { it is TypeDeclaration<*> && it.nameAsString.contains("Sound") }) {
            println("Tem sound!")
        }
        if (indexOfUUIDsClassOrInterfaceDeclaration.values.any { it.nameAsString.contains("Sound") }) {
            println("Tem sound na lista de types!")
        }
        if (indexOfUUIDsEnumDeclaration.values.any { it.nameAsString.contains("Sound") }) {
            println("Tem sound na lista de enum!")
            indexOfUUIDsEnumDeclaration.filterValues { it.nameAsString.contains("Sound") }.forEach {
                println("ENUM COM SOUND: $it")
            }
        }
    }

    fun getSetOfCompilationUnit() = setOfCompilationUnit

    fun getElementByUUID(uuid: UUID) : Node? = indexOfUUIDs[uuid]

    fun getTypeByUUID(uuid: UUID) : TypeDeclaration<*> = indexOfUUIDsClassOrInterfaceDeclaration[uuid] ?: getEnumByUUID(uuid)

    fun getClassOrInterfaceByUUID(uuid: UUID) : ClassOrInterfaceDeclaration = indexOfUUIDsClassOrInterfaceDeclaration[uuid]!!

    fun getEnumByUUID(uuid: UUID) : EnumDeclaration = indexOfUUIDsEnumDeclaration[uuid]!!

    fun getConstructorByUUID(uuid: UUID) : ConstructorDeclaration = indexOfUUIDsConstructor[uuid]!!

    fun getMethodByUUID(uuid: UUID) : MethodDeclaration = indexOfUUIDsMethod[uuid]!!

    fun getFieldByUUID(uuid: UUID) : FieldDeclaration = indexOfUUIDsField[uuid]!!

    fun getEnumConstantByUUID(uuid: UUID) : EnumConstantDeclaration = indexOfUUIDsEnumConstant[uuid]!!

    fun getCompilationUnitByPath(path : String) : CompilationUnit? {
        return setOfCompilationUnit.find {
            it.correctPath == path
        }
    }

    fun getReferenceOfNode(node : Node) : UUID? {
        return when(node) {
            is MethodCallExpr -> {
                indexOfMethodCallExpr.getKey(node)
            }
            is NameExpr -> {
                indexOfFieldUses.getKey(node) ?: (indexOfEnumConstantUses.getKey(node) ?: indexOfTypeUses.getKey(node))
            }
            is FieldAccessExpr -> {
                indexOfFieldUses.getKey(node) ?: indexOfEnumConstantUses.getKey(node)
            }
            else -> { //ObjectCreationExpr & ClassOrInterfaceType
                indexOfTypeUses.getKey(node)
            }
        }
    }

    fun hasUsesIn(removedNode : Node, otherNode : Node) : Boolean {
        val listOfNodesUses = when(removedNode) {
            is FieldDeclaration -> indexOfFieldUses[removedNode.uuid]
            is TypeDeclaration<*>, is ConstructorDeclaration -> indexOfTypeUses[removedNode.uuid]
            is MethodDeclaration -> indexOfMethodCallExpr[removedNode.uuid]
            is EnumConstantDeclaration -> indexOfEnumConstantUses[removedNode.uuid]
            else -> throw IllegalArgumentException("The removed node is not from a supported type!")
        }

        return listOfNodesUses?.any {
            otherNode == it || otherNode.isAncestorOf(it)
        } ?: false
    }

    private fun createIndexOfMethodCalls() {
        indexOfMethodCallExpr.clear()
        val listOfMethodCallExpr = mutableListOf<MethodCallExpr>()
        val methodCallExprVisitor = MethodCallExprVisitor()
//        node.accept(methodCallExprVisitor, listOfMethodCallExpr)
        setOfCompilationUnit.forEach { it.accept(methodCallExprVisitor, listOfMethodCallExpr) }

        listOfMethodCallExpr.forEach { methodCallExpr ->
            try {
                val jpf = javaParserFacade.solve(methodCallExpr)
                if (jpf.isSolved) {
                    val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
                    methodDecl?.let {
                        indexOfMethodCallExpr.getOrPut(methodDecl.uuid) { mutableListOf() }.add(methodCallExpr)
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun createIndexOfFieldUses() {
        indexOfFieldUses.clear()
        val listOfFieldUses = mutableListOf<Expression>()
        val fieldUsesVisitor = FieldAndEnumConstantsUsesVisitor()
//        node.accept(fieldUsesVisitor, listOfFieldUses)
        setOfCompilationUnit.forEach { it.accept(fieldUsesVisitor, listOfFieldUses) }

        listOfFieldUses.forEach {fieldUse ->
            try {
                val jpf = when(fieldUse) {
                    is FieldAccessExpr -> javaParserFacade.solve(fieldUse)
                    else -> javaParserFacade.solve(fieldUse as NameExpr)
                }
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserFieldDeclaration -> {
                            val fieldDecl = (jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode
                            indexOfFieldUses.getOrPut(fieldDecl.uuid) { mutableListOf() }.add(fieldUse)
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun createIndexOfTypesUses() {
        indexOfTypeUses.clear()
        val listOfTypeUses = mutableListOf<Node>()
        val typeUsesVisitor = TypeUsesVisitor()
//        node.accept(typeUsesVisitor, listOfTypeUses)
        setOfCompilationUnit.forEach { it.accept(typeUsesVisitor, listOfTypeUses) }

        listOfTypeUses.forEach {typeUse ->
            try {
                when(typeUse) {
                    is ObjectCreationExpr -> solveObjectCreationExpr(typeUse)
                    is ClassOrInterfaceType -> solveClassOrInterfaceType(typeUse)
                    else -> solveNameExpr(typeUse as NameExpr)
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun solveObjectCreationExpr(typeUse : ObjectCreationExpr) {
        val jpf = javaParserFacade.solve(typeUse)
        if (jpf.isSolved) {
            val constructorDecl = (jpf.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            constructorDecl?.let {
                indexOfTypeUses.getOrPut(constructorDecl.uuid) { mutableListOf() }.add(typeUse)
            }
        }
    }

    private fun solveClassOrInterfaceType(typeUse : ClassOrInterfaceType) {
        val jpf = javaParserFacade.convertToUsage(typeUse)
        if (jpf.isReferenceType) {
            val decl = jpf.asReferenceType().typeDeclaration.get()
            val typeDecl = when (decl) {
                is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                is JavaParserEnumDeclaration -> (decl as? JavaParserEnumDeclaration)?.wrappedNode
                else -> null
            }
            typeDecl?.let {
                indexOfTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
            }
        }
    }

    private fun createIndexOfEnumConstantUses() {
        indexOfEnumConstantUses.clear()
        val listOfEnumConstantUses = mutableListOf<Expression>()
        val enumConstantUsesVisitor = FieldAndEnumConstantsUsesVisitor()
        setOfCompilationUnit.forEach { it.accept(enumConstantUsesVisitor, listOfEnumConstantUses) }
//        node.accept(enumConstantUsesVisitor, listOfEnumConstantUses)

        listOfEnumConstantUses.forEach {enumConstantUse ->
            try {
                val jpf = when(enumConstantUse) {
                    is FieldAccessExpr -> javaParserFacade.solve(enumConstantUse)
                    else -> javaParserFacade.solve(enumConstantUse as NameExpr)
                }
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserEnumConstantDeclaration -> {
                            val enumConstantDecl = (jpf.correspondingDeclaration as JavaParserEnumConstantDeclaration).wrappedNode
                            indexOfEnumConstantUses.getOrPut(enumConstantDecl.uuid) { mutableListOf() }.add(enumConstantUse)
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun solveNameExpr(typeUse : NameExpr) {
        val jpf = javaParserFacade.solve(typeUse)
        if (jpf.isSolved) {
            val decl = jpf.correspondingDeclaration
            val typeDecl = when (decl) {
                is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
                is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
                is JavaParserEnumDeclaration -> (decl as? JavaParserEnumDeclaration)?.wrappedNode
                else -> null
            }
            typeDecl?.let {
                indexOfTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
            }
        } else {
            val resolvedType = typeUse.calculateResolvedType()
            if(resolvedType.isReferenceType) {
                val decl = resolvedType.asReferenceType().typeDeclaration.get()
                val typeDecl = when (decl) {
                    is JavaParserClassDeclaration -> decl.wrappedNode
                    is JavaParserInterfaceDeclaration -> decl.wrappedNode
                    is JavaParserEnumDeclaration -> decl.wrappedNode
                    else -> null
                }
                typeDecl?.let {
                    indexOfTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
                }
            }
        }
    }

    fun renameAllMethodCalls(methodUuidToRename: UUID, newName: String) {
        createIndexOfMethodCalls()

        indexOfMethodCallExpr[methodUuidToRename]?.forEach {
            it.setName(newName)
        }
    }

    fun renameAllFieldUses(fieldUuidToRename: UUID, newName: String) {
        createIndexOfFieldUses()

        indexOfFieldUses[fieldUuidToRename]?.forEach {
            when (it) {
                is NameExpr -> it.setName(newName)
                is FieldAccessExpr -> it.setName(newName)
            }
        }
    }

    fun renameAllTypeUsageCalls(typeUuidToRename: UUID, newName: String) {
        createIndexOfTypesUses()

        indexOfTypeUses[typeUuidToRename]?.forEach {
            when (it) {
                is ObjectCreationExpr -> it.type.setName(newName)
                is ClassOrInterfaceType -> it.setName(newName)
                is NameExpr -> it.setName(newName)
            }
        }

        val typeToRename = getTypeByUUID(typeUuidToRename)

        if (typeToRename.isClassOrInterfaceDeclaration) {
            typeToRename.constructors.forEach {
                it.setName(newName)
            }
        }
    }

    fun renameAllEnumConstantUses(enumConstantUuidToRename: UUID, newName: String) {
        createIndexOfEnumConstantUses()

        indexOfEnumConstantUses[enumConstantUuidToRename]?.forEach {
            when (it) {
                is NameExpr -> it.setName(newName)
                is FieldAccessExpr -> it.setName(newName)
            }
        }
    }

    fun addFile(compilationUnitToBeAdded: CompilationUnit) {
//        requireNotNull(sourceRoot)
        sourceRoot?.let {
            sourceRoot.add(compilationUnitToBeAdded)
//            (sourceRoot.parserConfiguration.symbolResolver.get() as JavaSymbolSolver).inject(compilationUnitToBeAdded)
            val coids = compilationUnitToBeAdded.findAll(ClassOrInterfaceDeclaration::class.java)
            coids.forEach {
                memoryTypeSolver.addDeclaration(it.nameAsString, it.resolve())
            }
        }
        setOfCompilationUnit.add(compilationUnitToBeAdded)
//        initializeAllIndexes()
    }

    fun removeFile(compilationUnitToBeRemoved: CompilationUnit) {
//        requireNotNull(sourceRoot)
        sourceRoot?.let {
            sourceRoot.compilationUnits.removeIf { it.correctPath == compilationUnitToBeRemoved.correctPath }
        }
        setOfCompilationUnit.removeIf { it.correctPath == compilationUnitToBeRemoved.correctPath }
//        initializeAllIndexes()
    }


//    fun updateIndexes(newNode: Node) {
//        val setupProjectVisitor = SetupProjectVisitor()
//        newNode.accept(setupProjectVisitor, indexOfUUIDs)
//
//        when(newNode) {
//            is EnumDeclaration -> {}
//            is ClassOrInterfaceDeclaration -> {}
//            is ConstructorDeclaration -> {}
//            is MethodDeclaration -> {}
//            is FieldDeclaration -> {}
//            is EnumConstantDeclaration -> {}
//        }
//    }

/*
    private fun solveNameExpr(fieldUuidToRename: UUID, newName: String) {
        listOfFieldUses.filterIsInstance<NameExpr>().forEach {
            try {
                val jpf = javaParserFacade.solve(it)
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserFieldDeclaration -> {
                            if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldUuidToRename) {
                                it.setName(newName)
                            }
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun solveFieldAccessExpr(fieldUuidToRename: UUID, newName: String) {
        listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
            try {
                val jpf = javaParserFacade.solve(it)
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserFieldDeclaration -> {
                            if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldUuidToRename) {
                                it.setName(newName)
                            }
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }
*/

//    fun renameAllClassUsageCalls(classUuidToRename: UUID, newName: String) {
//        val newClassToRename = getClassOrInterfaceByUUID(classUuidToRename)
//
//        newClassToRename?.let {
//            val typeUsesVisitor = TypeUsesVisitor()
//            listOfClassUsageCalls.clear()
//            setOfCompilationUnit.forEach { it.accept(typeUsesVisitor, listOfClassUsageCalls) }
//
//            val allUses = mutableListOf<Node>()
//            solveObjectCreationExpr(allUses, classUuidToRename)
//            solveClassOrInterfaceType(allUses, classUuidToRename)
//            solveClassNameExpr(allUses, classUuidToRename)
//            allUses.filterIsInstance<ObjectCreationExpr>().forEach { it.type.setName(newName) }
//            allUses.filterIsInstance<ClassOrInterfaceType>().forEach { it.setName(newName) }
//            allUses.filterIsInstance<NameExpr>().forEach { it.setName(newName) }
//
//            newClassToRename.constructors.forEach {
//                it.setName(newName)
//            }
//        }
//    }

//    private fun solveObjectCreationExpr(allUses : MutableList<Node>, classUuidToRename: UUID) {
//        listOfClassUsageCalls.filterIsInstance<ObjectCreationExpr>().forEach {
//            val jpf = javaParserFacade.solve(it)
//            if (jpf.isSolved) {
//                val constructorDecl = (jpf.correspondingDeclaration as JavaParserConstructorDeclaration<*>).wrappedNode
//                if (constructorDecl.parentNode.orElse(null)?.uuid == classUuidToRename) {
//                    allUses.add(it)
//                }
//            }
//        }
//    }
//
//    private fun solveClassOrInterfaceType(allUses : MutableList<Node>, classUuidToRename: UUID) {
//        listOfClassUsageCalls.filterIsInstance<ClassOrInterfaceType>().forEach {
//            val jpf = javaParserFacade.convertToUsage(it)
//            if (jpf.isReferenceType) {
//                val decl = jpf.asReferenceType().typeDeclaration.get()
//                val classDecl = when (decl) {
//                    is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
//                    is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
//                    else -> null
//                }
//                if (classDecl?.uuid == classUuidToRename) {
//                    allUses.add(it)
//                }
//            }
//        }
//    }
//
//    private fun solveClassNameExpr(allUses : MutableList<Node>, typeUuidToRename: UUID) {
//        listOfClassUsageCalls.filterIsInstance<NameExpr>().forEach { nameExpr ->
//            val jpf = javaParserFacade.solve(nameExpr)
//            if (jpf.isSolved) {
//                val decl = jpf.correspondingDeclaration
//                val classOrInterfaceDecl = when (decl) {
//                    is JavaParserClassDeclaration -> (decl as? JavaParserClassDeclaration)?.wrappedNode
//                    is JavaParserInterfaceDeclaration -> (decl as? JavaParserInterfaceDeclaration)?.wrappedNode
//                    else -> null
//                }
//                classOrInterfaceDecl?.let {
//                    if (classOrInterfaceDecl.uuid == typeUuidToRename) {
//                        allUses.add(nameExpr)
//                    }
//                }
//            } else {
//                val resolvedType = nameExpr.calculateResolvedType()
//                if(resolvedType.isReferenceType) {
//                    val decl = resolvedType.asReferenceType().typeDeclaration.get()
//                    val classOrInterfaceDecl = when (decl) {
//                        is JavaParserClassDeclaration -> decl.wrappedNode
//                        is JavaParserInterfaceDeclaration -> decl.wrappedNode
//                        else -> null
//                    }
//                    classOrInterfaceDecl?.let {
//                        if (classOrInterfaceDecl.uuid == typeUuidToRename) {
//                            allUses.add(nameExpr)
//                        }
//                    }
//                }
//            }
//        }
//    }

}


//        listOfFieldUses.filterIsInstance<NameExpr>().forEach {nameExpr ->
//            try {
//                val jpf = javaParserFacade.solve(nameExpr)
//                if (jpf.isSolved) {
//                    when (jpf.correspondingDeclaration) {
//                        is JavaParserFieldDeclaration -> {
//                            val fieldDecl = (jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode
//                            fieldDecl?.let {
//                                indexOfFieldUses.getOrPut(fieldDecl) { mutableListOf() }.add(nameExpr)
//                            }
//                        }
//                    }
//                }
//            } catch (ex: UnsolvedSymbolException) {
//                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
//                    println("Foi encontrada uma exceção: ${ex.message}")
//                }
//            }
//        }
//
//        listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach { fieldAccessExpr ->
//            try {
//                val jpf = javaParserFacade.solve(fieldAccessExpr)
//                if (jpf.isSolved) {
//                    when (jpf.correspondingDeclaration) {
//                        is JavaParserFieldDeclaration -> {
//                            val fieldDecl = (jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode
//                            fieldDecl?.let {
//                                indexOfFieldUses.getOrPut(fieldDecl) { mutableListOf() }.add(fieldAccessExpr)
//                            }
//                        }
//                    }
//                }
//            } catch (ex: UnsolvedSymbolException) {
//                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains("TextUtil")) {
//                    println("Foi encontrada uma exceção: ${ex.message}")
//                }
//            }
//        }