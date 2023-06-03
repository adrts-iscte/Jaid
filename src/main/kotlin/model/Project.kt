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
import java.lang.RuntimeException
import java.nio.file.Path
import java.util.IdentityHashMap
import kotlin.io.path.Path

class Project {
    private val debug = false
    private val path: String

    private val sourceRoot: SourceRoot?
    private val setOfCompilationUnit = mutableSetOf<CompilationUnit>()

    private val indexOfUUIDs = mutableMapOf<UUID, Node>()
    private val indexOfCompilationUnits = mutableMapOf<UUID, CompilationUnit>()
    private val indexOfUUIDtoClassOrInterface = mutableMapOf<UUID, ClassOrInterfaceDeclaration>()
    private val indexOfUUIDtoEnum = mutableMapOf<UUID, EnumDeclaration>()
    private val indexOfUUIDtoConstructor = mutableMapOf<UUID, ConstructorDeclaration>()
    private val indexOfUUIDtoMethod = mutableMapOf<UUID, MethodDeclaration>()
    private val indexOfUUIDtoField = mutableMapOf<UUID, FieldDeclaration>()
    private val indexOfUUIDtoEnumConstant = mutableMapOf<UUID, EnumConstantDeclaration>()

    private val indexOfUUIDtoMethodCallExpr = mutableMapOf<UUID, MutableList<MethodCallExpr>>()
    private val indexOfUUIDtoFieldUses = mutableMapOf<UUID, MutableList<Expression>>()
    private val indexOfUUIDtoTypeUses = mutableMapOf<UUID, MutableList<Node>>()
    private val indexOfUUIDtoEnumConstantUses = mutableMapOf<UUID, MutableList<Expression>>()

    private val indexOfMethodCallExprToUUID = IdentityHashMap<MethodCallExpr, UUID>()
    private val indexOfFieldUsesToUUID = IdentityHashMap<Expression, UUID>()
    private val indexOfTypeUsesToUUID = IdentityHashMap<Node, UUID>()
    private val indexOfEnumConstantUsesToUUID = IdentityHashMap<Expression, UUID>()

    private val solver: CombinedTypeSolver
    private val memoryTypeSolver: MemoryTypeSolver

    private val setupProject: Boolean
    private val initializeIndexes: Boolean
    private val javaParserFacade: JavaParserFacade
    private val setupProjectVisitor = SetupProjectVisitor()

    constructor(path: String, setupProject: Boolean = true, initializeIndexes: Boolean = true) {
        this.setupProject = setupProject
        this.initializeIndexes = initializeIndexes
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
            sourceRoot = SourceRoot(Path(path)).setParserConfiguration(
                ParserConfiguration().setSymbolResolver(
                    JavaSymbolSolver(solver)
                )
            )
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

    constructor(
        path: String,
        sourceRoot: SourceRoot?,
        givenListOfCompilationUnit: MutableList<CompilationUnit>,
        solver: CombinedTypeSolver,
        memoryTypeSolver: MemoryTypeSolver,
        setupProject: Boolean = true,
        initializeIndexes: Boolean = true
    ) {
        this.setupProject = setupProject
        this.initializeIndexes = initializeIndexes
        this.path = path
        this.solver = solver
        this.memoryTypeSolver = memoryTypeSolver
        this.sourceRoot = sourceRoot
        javaParserFacade = JavaParserFacade.get(this.solver)
        setOfCompilationUnit.addAll(givenListOfCompilationUnit)
        loadProject()
    }

    fun getSolver() = solver

    fun getSourceRoot() = sourceRoot

    fun clone(): Project {
        val clonedListOfCompilationUnit = setOfCompilationUnit.toMutableList().map { it.clone() }.toMutableList()
        return Project(
            path,
            sourceRoot,
            clonedListOfCompilationUnit,
            solver,
            memoryTypeSolver,
            setupProject,
            initializeIndexes
        )
    }

    fun saveProjectTo(path: Path) {
        requireNotNull(sourceRoot)
        sourceRoot.saveAll(path)
    }

    fun isCorrectASTafterApplyingBothTransformations(a: Transformation, b: Transformation): Boolean {
        val clonedProject = this.clone()
        a.applyTransformation(clonedProject)
        b.applyTransformation(clonedProject)
        val javaParser = JavaParser(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(solver)))
        val parsedCompilationunits = clonedProject.getSetOfCompilationUnit().map { javaParser.parse(it.toString()) }
        return parsedCompilationunits.all { it.isSuccessful }
    }

    fun loadProject() {
//        if (setOfCompilationUnit.any { it.hasEnum })
//            println("There is a file with an enum! This project has ${
//                setOfCompilationUnit.sumOf { it.findAll(EnumDeclaration::class.java).size }
//            } enums!")
//        else
//            println("This project doesn't have any enums!")
//
//        if (setOfCompilationUnit.any { it.hasClassOrInterfaceInsideAnotherClass })
//            println("There is a file with a nested type! This project has ${setOfCompilationUnit.sumOf { it.findAll(ClassOrInterfaceDeclaration::class.java) { it.isClassOrInterfaceInsideAnotherClass }.size }} nested types!")
//        else
//            println("This project doesn't have any inner classes!")

        if (setupProject) {
            setOfCompilationUnit.forEach { it.accept(setupProjectVisitor, indexOfUUIDs) }
        }

        if (initializeIndexes) {
            initializeAllIndexes()
        }
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
//        println("Entrou no initializeIndexes! no Project $path")

        setOfCompilationUnit.forEach { updateIndexesWithNode(it) }
    }

    fun debug() {
        val blockeventFile = setOfCompilationUnit.find { it.storage.get().fileName.contains("BlockBreak") }
        blockeventFile?.let {
            println("Tem BlockEvent!")
            val firstClass = blockeventFile.types[0].uuid
            println(firstClass)
//            println(indexOfUUIDs[firstClass])

        }

        val listenerEvent = setOfCompilationUnit.find { it.storage.get().fileName.contains("BlockListener") }
        listenerEvent?.let {
            println("Entrou no BlockListener!")
            val onBlockBreak = listenerEvent.types[0].methods.find { it.nameAsString == "onBlockBreak" }
            onBlockBreak?.let {
//                val resolved2 = javaParserFacade.convertToUsage(onBlockBreak.findFirst(ClassOrInterfaceType::class.java).get())
                val resolved = onBlockBreak.findFirst(ClassOrInterfaceType::class.java).get().resolve()
                println((resolved.asReferenceType().typeDeclaration.get() as JavaParserClassDeclaration).wrappedNode.uuid)
            }
        }

    }

    fun updateIndexesWithNode(node: Node?) {
        node?.let {
            val nodesIndexOfUUIDs = mutableMapOf<UUID, Node>()
            node.accept(setupProjectVisitor, nodesIndexOfUUIDs)

            indexOfUUIDs.setAll(nodesIndexOfUUIDs.filterValues { it !is CompilationUnit })
            indexOfCompilationUnits.setAll(nodesIndexOfUUIDs.filterValues { it is CompilationUnit } as Map<UUID, CompilationUnit>)
            indexOfUUIDtoClassOrInterface.setAll(nodesIndexOfUUIDs.filterValues { it is ClassOrInterfaceDeclaration } as Map<UUID, ClassOrInterfaceDeclaration>)
            indexOfUUIDtoEnum.setAll(nodesIndexOfUUIDs.filterValues { it is EnumDeclaration } as Map<UUID, EnumDeclaration>)
            indexOfUUIDtoConstructor.setAll(nodesIndexOfUUIDs.filterValues { it is ConstructorDeclaration } as Map<UUID, ConstructorDeclaration>)
            indexOfUUIDtoMethod.setAll(nodesIndexOfUUIDs.filterValues { it is MethodDeclaration } as Map<UUID, MethodDeclaration>)
            indexOfUUIDtoField.setAll(nodesIndexOfUUIDs.filterValues { it is FieldDeclaration } as Map<UUID, FieldDeclaration>)
            indexOfUUIDtoEnumConstant.setAll(nodesIndexOfUUIDs.filterValues { it is EnumConstantDeclaration } as Map<UUID, EnumConstantDeclaration>)

            createIndexOfMethodCalls(node)
            createIndexOfFieldUses(node)
            createIndexOfTypesUses(node)
            createIndexOfEnumConstantUses(node)
        }
    }

    fun getSetOfCompilationUnit() = setOfCompilationUnit

    fun getCompilationUnitByUUID(uuid: UUID): CompilationUnit? = indexOfCompilationUnits[uuid]

    fun getElementByUUID(uuid: UUID): Node? = indexOfUUIDs[uuid]

    fun getTypeByUUID(uuid: UUID): TypeDeclaration<*>? = indexOfUUIDtoClassOrInterface[uuid] ?: getEnumByUUID(uuid)

    fun getClassOrInterfaceByUUID(uuid: UUID): ClassOrInterfaceDeclaration? = indexOfUUIDtoClassOrInterface[uuid]

    fun getEnumByUUID(uuid: UUID): EnumDeclaration? = indexOfUUIDtoEnum[uuid]

    fun getConstructorByUUID(uuid: UUID): ConstructorDeclaration? = indexOfUUIDtoConstructor[uuid]

    fun getMethodByUUID(uuid: UUID): MethodDeclaration? = indexOfUUIDtoMethod[uuid]

    fun getFieldByUUID(uuid: UUID): FieldDeclaration? = indexOfUUIDtoField[uuid]

    fun getEnumConstantByUUID(uuid: UUID): EnumConstantDeclaration? = indexOfUUIDtoEnumConstant[uuid]

//    fun getCompilationUnitByPath(path : String) : CompilationUnit? {
//        return setOfCompilationUnit.find {
//            it.correctPath == path
//        }
//    }

//    fun getReferenceOfNode(node : Node) : UUID? {
//        return when(node) {
//            is MethodCallExpr -> {
//                indexOfUUIDtoMethodCallExpr.getKey(node)
//            }
//            is NameExpr -> {
//                indexOfUUIDtoFieldUses.getKey(node) ?: (indexOfUUIDtoEnumConstantUses.getKey(node) ?: indexOfUUIDtoTypeUses.getKey(node))
//            }
//            is FieldAccessExpr -> {
//                indexOfUUIDtoFieldUses.getKey(node) ?: indexOfUUIDtoEnumConstantUses.getKey(node)
//            }
//            else -> { //ObjectCreationExpr & ClassOrInterfaceType
//                indexOfUUIDtoTypeUses.getKey(node)
//            }
//        }
//    }

    fun getReferenceOfNode(node: Node): UUID? {
        return when (node) {
            is MethodCallExpr -> {
                indexOfMethodCallExprToUUID[node]
            }

            is NameExpr -> {
                indexOfFieldUsesToUUID[node] ?: (indexOfEnumConstantUsesToUUID[node] ?: indexOfTypeUsesToUUID[node])
            }

            is FieldAccessExpr -> {
                indexOfFieldUsesToUUID[node] ?: indexOfEnumConstantUsesToUUID[node]
            }

            else -> { //ObjectCreationExpr & ClassOrInterfaceType
                indexOfTypeUsesToUUID[node]
            }
        }
    }

    fun hasUsesIn(removedNode: Node, otherNode: Node): Boolean {
        val listOfNodesUses = when (removedNode) {
            is FieldDeclaration -> indexOfUUIDtoFieldUses[removedNode.uuid]
            is TypeDeclaration<*>, is ConstructorDeclaration -> indexOfUUIDtoTypeUses[removedNode.uuid]
            is MethodDeclaration -> indexOfUUIDtoMethodCallExpr[removedNode.uuid]
            is EnumConstantDeclaration -> indexOfUUIDtoEnumConstantUses[removedNode.uuid]
            else -> throw IllegalArgumentException("The removed node is not from a supported type!")
        }

        return listOfNodesUses?.any {
            otherNode == it || otherNode.isAncestorOf(it)
        } ?: false
    }

    private fun createIndexOfMethodCalls(node: Node) {
//        indexOfUUIDtoMethodCallExpr.clear()
        val listOfMethodCallExpr = mutableListOf<MethodCallExpr>()
        val methodCallExprVisitor = MethodCallExprVisitor()
//        node.accept(methodCallExprVisitor, listOfMethodCallExpr)
        node.accept(methodCallExprVisitor, listOfMethodCallExpr)

        listOfMethodCallExpr.forEach { methodCallExpr ->
            try {
                val jpf = javaParserFacade.solve(methodCallExpr)
                if (jpf.isSolved) {
                    val methodDecl = (jpf.correspondingDeclaration as? JavaParserMethodDeclaration)?.wrappedNode
                    methodDecl?.let {
                        indexOfUUIDtoMethodCallExpr.getOrPut(methodDecl.uuid) { mutableListOf() }.add(methodCallExpr)
                        indexOfMethodCallExprToUUID[methodCallExpr] = methodDecl.uuid
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains(
                        "TextUtil"
                    )
                ) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            } catch (ex: RuntimeException) {
                println()
            }
        }
    }

    private fun createIndexOfFieldUses(node: Node) {
//        indexOfUUIDtoFieldUses.clear()
        val listOfFieldUses = mutableListOf<Expression>()
        val fieldUsesVisitor = FieldAndEnumConstantsUsesVisitor()
//        node.accept(fieldUsesVisitor, listOfFieldUses)
        node.accept(fieldUsesVisitor, listOfFieldUses)

        listOfFieldUses.forEach { fieldUse ->
            try {
                val jpf = when (fieldUse) {
                    is FieldAccessExpr -> javaParserFacade.solve(fieldUse)
                    else -> javaParserFacade.solve(fieldUse as NameExpr)
                }
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserFieldDeclaration -> {
                            val fieldDecl = (jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode
                            indexOfUUIDtoFieldUses.getOrPut(fieldDecl.uuid) { mutableListOf() }.add(fieldUse)
                            indexOfFieldUsesToUUID[fieldUse] = fieldDecl.uuid
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains(
                        "TextUtil"
                    )
                ) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun createIndexOfTypesUses(node: Node) {
//        indexOfUUIDtoTypeUses.clear()
        val listOfTypeUses = mutableListOf<Node>()
        val typeUsesVisitor = TypeUsesVisitor()
//        node.accept(typeUsesVisitor, listOfTypeUses)
        node.accept(typeUsesVisitor, listOfTypeUses)

        listOfTypeUses.forEach { typeUse ->
            try {
                when (typeUse) {
                    is ObjectCreationExpr -> solveObjectCreationExpr(typeUse)
                    is ClassOrInterfaceType -> solveClassOrInterfaceType(typeUse)
                    else -> solveNameExpr(typeUse as NameExpr)
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains(
                        "TextUtil"
                    )
                ) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun solveObjectCreationExpr(typeUse: ObjectCreationExpr) {
        val jpf = javaParserFacade.solve(typeUse)
        if (jpf.isSolved) {
            val constructorDecl = (jpf.correspondingDeclaration as? JavaParserConstructorDeclaration<*>)?.wrappedNode
            constructorDecl?.let {
                indexOfUUIDtoTypeUses.getOrPut(constructorDecl.uuid) { mutableListOf() }.add(typeUse)
                indexOfTypeUsesToUUID[typeUse] = constructorDecl.uuid
            }
        }
    }

    private fun solveClassOrInterfaceType(typeUse: ClassOrInterfaceType) {
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
                indexOfUUIDtoTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
                indexOfTypeUsesToUUID[typeUse] = typeDecl.uuid
            }
        }
    }

    private fun createIndexOfEnumConstantUses(node: Node) {
//        indexOfUUIDtoEnumConstantUses.clear()
        val listOfEnumConstantUses = mutableListOf<Expression>()
        val enumConstantUsesVisitor = FieldAndEnumConstantsUsesVisitor()
        node.accept(enumConstantUsesVisitor, listOfEnumConstantUses)

        listOfEnumConstantUses.forEach { enumConstantUse ->
            try {
                val jpf = when (enumConstantUse) {
                    is FieldAccessExpr -> javaParserFacade.solve(enumConstantUse)
                    else -> javaParserFacade.solve(enumConstantUse as NameExpr)
                }
                if (jpf.isSolved) {
                    when (jpf.correspondingDeclaration) {
                        is JavaParserEnumConstantDeclaration -> {
                            val enumConstantDecl =
                                (jpf.correspondingDeclaration as JavaParserEnumConstantDeclaration).wrappedNode
                            indexOfUUIDtoEnumConstantUses.getOrPut(enumConstantDecl.uuid) { mutableListOf() }
                                .add(enumConstantUse)
                            indexOfEnumConstantUsesToUUID[enumConstantUse] = enumConstantDecl.uuid
                        }
                    }
                }
            } catch (ex: UnsolvedSymbolException) {
                if (debug && ex.message != null && ex.message != "Unsolved symbol : org.junit.Assert" && !ex.message!!.contains(
                        "TextUtil"
                    )
                ) {
                    println("Foi encontrada uma exceção: ${ex.message}")
                }
            }
        }
    }

    private fun solveNameExpr(typeUse: NameExpr) {
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
                indexOfUUIDtoTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
                indexOfTypeUsesToUUID[typeUse] = typeDecl.uuid
            }
        } else {
            val resolvedType = typeUse.calculateResolvedType()
            if (resolvedType.isReferenceType) {
                val decl = resolvedType.asReferenceType().typeDeclaration.get()
                val typeDecl = when (decl) {
                    is JavaParserClassDeclaration -> decl.wrappedNode
                    is JavaParserInterfaceDeclaration -> decl.wrappedNode
                    is JavaParserEnumDeclaration -> decl.wrappedNode
                    else -> null
                }
                typeDecl?.let {
                    indexOfUUIDtoTypeUses.getOrPut(typeDecl.uuid) { mutableListOf() }.add(typeUse)
                    indexOfTypeUsesToUUID[typeUse] = typeDecl.uuid
                }
            }
        }
    }

    fun renameAllMethodCalls(methodUuidToRename: UUID, newName: String) {
//        createIndexOfMethodCalls()

        indexOfUUIDtoMethodCallExpr[methodUuidToRename]?.forEach {
            it.setName(newName)
        }
    }

    fun renameAllFieldUses(fieldUuidToRename: UUID, newName: String) {
//        createIndexOfFieldUses()

        indexOfUUIDtoFieldUses[fieldUuidToRename]?.forEach {
            when (it) {
                is NameExpr -> it.setName(newName)
                is FieldAccessExpr -> it.setName(newName)
            }
        }
    }

    fun renameAllTypeUsageCalls(typeUuidToRename: UUID, newName: String) {
//        createIndexOfTypesUses()

        indexOfUUIDtoTypeUses[typeUuidToRename]?.forEach {
            when (it) {
                is ObjectCreationExpr -> it.type.setName(newName)
                is ClassOrInterfaceType -> it.setName(newName)
                is NameExpr -> it.setName(newName)
            }
        }

        val typeToRename = getTypeByUUID(typeUuidToRename)!!

        if (typeToRename.isClassOrInterfaceDeclaration) {
            typeToRename.constructors.forEach {
                it.setName(newName)
            }
        }
    }

    fun renameAllEnumConstantUses(enumConstantUuidToRename: UUID, newName: String) {
//        createIndexOfEnumConstantUses()

        indexOfUUIDtoEnumConstantUses[enumConstantUuidToRename]?.forEach {
            when (it) {
                is NameExpr -> it.setName(newName)
                is FieldAccessExpr -> it.setName(newName)
            }
        }
    }

    fun addFile(compilationUnitToBeAdded: CompilationUnit) {
        sourceRoot?.let {
            sourceRoot.addCompilationUnit(compilationUnitToBeAdded)
            val coids = compilationUnitToBeAdded.findAll(ClassOrInterfaceDeclaration::class.java)
//            coids.forEach {
//                memoryTypeSolver.addDeclaration(it.nameAsString, it.resolve())
//            }
        }
        setOfCompilationUnit.add(compilationUnitToBeAdded)
    }

    fun removeFile(compilationUnitToBeRemoved: CompilationUnit) {
        sourceRoot?.let {
            sourceRoot.removeCompilationUnit(compilationUnitToBeRemoved)
        }
        setOfCompilationUnit.removeIf { it.uuid == compilationUnitToBeRemoved.uuid }
//        indexOfCompilationUnits.remove(compilationUnitToBeRemoved.uuid)
    }

    fun updateUUIDOfNode(newUUID: UUID, nodeToBeUpdated : Node) {
        val oldUUID = UUID(nodeToBeUpdated.uuid.toString())
        if (newUUID == oldUUID)
            return

        nodeToBeUpdated.setUUIDTo(newUUID)
        indexOfUUIDs[newUUID] = indexOfUUIDs.remove(oldUUID)!!

        when(nodeToBeUpdated) {
            is CompilationUnit -> {
                indexOfCompilationUnits[newUUID] = indexOfCompilationUnits.remove(oldUUID)!!
            }
            is TypeDeclaration<*> -> {
                if (nodeToBeUpdated is ClassOrInterfaceDeclaration) {
                    indexOfUUIDtoClassOrInterface[newUUID] = indexOfUUIDtoClassOrInterface.remove(oldUUID)!!
                } else {
                    indexOfUUIDtoEnum[newUUID] = indexOfUUIDtoEnum.remove(oldUUID)!!
                }
                indexOfTypeUsesToUUID.entries.filter { it.value == oldUUID }.forEach { it.setValue(newUUID) }
                indexOfUUIDtoTypeUses.remove(oldUUID)?.let { indexOfUUIDtoTypeUses[newUUID] = it }
            }
            is EnumConstantDeclaration -> {
                indexOfUUIDtoEnumConstant[newUUID] = indexOfUUIDtoEnumConstant.remove(oldUUID)!!
                indexOfEnumConstantUsesToUUID.entries.filter { it.value == oldUUID }.forEach { it.setValue(newUUID) }
                indexOfUUIDtoEnumConstantUses.remove(oldUUID)?.let { indexOfUUIDtoEnumConstantUses[newUUID] = it }
            }
            is FieldDeclaration -> {
                indexOfUUIDtoField[newUUID] = indexOfUUIDtoField.remove(oldUUID)!!
                indexOfFieldUsesToUUID.entries.filter { it.value == oldUUID }.forEach { it.setValue(newUUID) }
                indexOfUUIDtoFieldUses.remove(oldUUID)?.let { indexOfUUIDtoFieldUses[newUUID] = it }
            }
            is MethodDeclaration -> {
                indexOfUUIDtoMethod[newUUID] = indexOfUUIDtoMethod.remove(oldUUID)!!
                indexOfMethodCallExprToUUID.entries.filter { it.value == oldUUID }.forEach { it.setValue(newUUID) }
                indexOfUUIDtoMethodCallExpr.remove(oldUUID)?.let { indexOfUUIDtoMethodCallExpr[newUUID] = it }
            }
            is ConstructorDeclaration -> {
                indexOfUUIDtoConstructor[newUUID] = indexOfUUIDtoConstructor.remove(oldUUID)!!
//                indexOfTypeUsesToUUID.entries.filter { it.value == oldUUID }.forEach { it.setValue(newUUID) }
//                indexOfUUIDtoTypeUses[newUUID] = indexOfUUIDtoTypeUses.remove(oldUUID)!!
            }
        }
        //Falta criar cenários com estas referências
    }
}