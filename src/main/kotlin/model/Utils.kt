package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import model.transformations.Transformation
import model.visitors.*
import kotlin.reflect.KClass

val CompilationUnit.correctPath : String
    get() = this.storage.get().path.toString().replace(Regex("([bB]ase)+"), "")
                                                .replace(Regex("([lL]eft)+"), "")
                                                .replace(Regex("(mergedBranch)+"), "")
                                                .replace(Regex("(branchToBeMerged)+"), "")
                                                .replace(Regex("(commonAncestor)+"), "")

val CompilationUnit.path : String
    get() = this.storage.get().path.toString()

val CompilationUnit.hasEnum : Boolean
    get() = this.findAll(EnumDeclaration::class.java).isNotEmpty()

val CompilationUnit.hasClassOrInterfaceInsideAnotherClass : Boolean
    get() = this.findAll(ClassOrInterfaceDeclaration::class.java) { it.isClassOrInterfaceInsideAnotherClass }.size > 0

val ClassOrInterfaceDeclaration.isClassOrInterfaceInsideAnotherClass : Boolean
    get() = this.parentNode.get() is ClassOrInterfaceDeclaration

val CallableDeclaration<*>.parameterTypes : List<Type>
    get() = this.parameters.map { it.type }

val FieldDeclaration.name : SimpleName
    get() = (this.variables.first() as VariableDeclarator).name

val NodeList<Parameter>.types : List<Type>
    get() = this.map { it.type }

val Modifier.isAccessModifier: Boolean
    get() = this.keyword.name == "PUBLIC" || this.keyword.name == "PRIVATE" || this.keyword.name == "PROTECTED"

val Modifier.isAbstractModifier: Boolean
    get() = this.keyword.name == "ABSTRACT"

fun <T> MutableList<T>.move(newIndex: Int, item: T)  {
    val currentIndex = indexOf(item)
    if (currentIndex < 0) return
    removeAt(currentIndex)
    add(newIndex, item)
}


fun Set<Conflict>.getNumberOfConflictsOfType(a: KClass<out Transformation>, b : KClass<out Transformation>) : Int {
    return this.filter { it.getConflictType() == ConflictTypeLibrary.getConflictTypeByKClasses(a, b) }.size
}
fun getProductOfTwoCollectionsOfTransformations(c1: Collection<Transformation>, c2: Collection<Transformation>): List<Pair<Transformation, Transformation>> {
    return c1.flatMap { c1Elem -> c2.map { c2Elem -> c1Elem to c2Elem } }
}

//fun <T> MutableList<T>.move(newIndex: Int, item: T)  {
//    val currentIndex = indexOf(item)
//    if (currentIndex < 0) return
//    removeAt(currentIndex)
//    if (currentIndex > newIndex)
//        add(newIndex, item)
//    else
//        add(newIndex - 1, item)
//}

//fun getPairsOfCorrespondingCompilationUnits(listOfCompilationUnitBase : Set<CompilationUnit>, listOfCompilationUnitBranch : Set<CompilationUnit>): List<Pair<CompilationUnit, CompilationUnit>> {
//    val removeBranchRepresentativeNamesRegex = Regex("([bB]ase)*([lL]eft)*(branchToBeMerged)*(mergedBranch)*(commonAncestor)*(finalMergedVersion)*")
//    return listOfCompilationUnitBase.zip(listOfCompilationUnitBranch).filter {
//        it.first.storage.get().path.toString().replace(removeBranchRepresentativeNamesRegex, "") ==
//                it.second.storage.get().path.toString().replace(removeBranchRepresentativeNamesRegex, "")
//    }
//}

fun getPairsOfCorrespondingCompilationUnits(listOfCompilationUnitBase : Set<CompilationUnit>, listOfCompilationUnitBranch : Set<CompilationUnit>): List<Pair<CompilationUnit, CompilationUnit>> {
    val removeBranchRepresentativeNamesRegex = Regex("([bB]ase)*([lL]eft)*(branchToBeMerged)*(mergedBranch)*(commonAncestor)*(finalMergedVersion)*")
    return listOfCompilationUnitBase.zip(listOfCompilationUnitBranch).filter {
        it.first.storage.get().fileName.toString().replace(removeBranchRepresentativeNamesRegex, "") ==
                it.second.storage.get().fileName.toString().replace(removeBranchRepresentativeNamesRegex, "")
    }
}

fun arePackageDeclarationEqual(p1 : Name, p2 : Name) : Boolean {
    val removeBranchRepresentativeNamesRegex = Regex("([bB]ase)*([lL]eft)*(branchToBeMerged)*(mergedBranch)*(commonAncestor)*(finalMergedVersion)*")
    return p1.asString().replace(removeBranchRepresentativeNamesRegex, "") == p2.asString().replace(removeBranchRepresentativeNamesRegex, "")
}

fun calculateIndexOfMemberToAdd(clazz : ClassOrInterfaceDeclaration, classToHaveCallableAdded : ClassOrInterfaceDeclaration, newMemberUUID : UUID) : Int {
    val clazzMembers = clazz.members
    val clazzMembersUUID = clazzMembers.map { it.uuid }
    val classToHaveCallableAddedMembers = classToHaveCallableAdded.members
    val classToHaveCallableAddedMembersUUID = classToHaveCallableAddedMembers.map { it.uuid }

    for (i in clazzMembersUUID.indexOf(newMemberUUID) downTo  0) {
        val similarMember = classToHaveCallableAddedMembers.find { it.uuid == clazzMembersUUID[i] }
        similarMember?.let {
            return classToHaveCallableAddedMembers.indexOf(similarMember) + 1
        }
    }

    return 0
}

fun calculateIndexOfTypeToAdd(compilationUnit : CompilationUnit,
                              compilationUnitToHaveCallableAdded : CompilationUnit,
                              newTypeUUID : UUID) : Int {
    val compilationUnitMembers = compilationUnit.types
    val compilationUnitMembersUUID = compilationUnitMembers.map { it.uuid }
    val compilationUnitToHaveCallableAddedMembers = compilationUnitToHaveCallableAdded.types
    val compilationUnitToHaveCallableAddedMembersUUID = compilationUnitToHaveCallableAddedMembers.map { it.uuid }

    for (i in compilationUnitMembersUUID.indexOf(newTypeUUID) downTo  0) {
        val similarType = compilationUnitToHaveCallableAddedMembers.find { it.uuid == compilationUnitMembersUUID[i]}
        similarType?.let {
            return compilationUnitToHaveCallableAddedMembers.indexOf(similarType) + 1
        }
    }

    return 0
}

fun getNodeReferencesToReferencedNode(project: Project, referencedNode: FieldDeclaration, searchReferencesIn: Node): MutableList<Expression> {
    val allReferences = mutableListOf<Expression>()

//    val visitor = when(referencedNode) {
//        is FieldDeclaration -> FieldUsesVisitor()
//        is ClassOrInterfaceDeclaration -> ClassUsageCallsVisitor()
//        is MethodDeclaration -> MethodCallExprVisitor()
//        else -> null
//    }

    val fieldUsesVisitor = FieldUsesVisitor()
    val listOfFieldUses = mutableListOf<Node>()
    searchReferencesIn.accept(fieldUsesVisitor, listOfFieldUses)

    listOfFieldUses.filterIsInstance<NameExpr>().forEach {
        try {
            val jpf = JavaParserFacade.get(project.getSolver()).solve(it)
            if (jpf.isSolved) {
                when (jpf.correspondingDeclaration) {
                    is JavaParserFieldDeclaration -> {
                        if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == referencedNode.uuid) {
                            allReferences.add(it)
                        }
                    }
                }
            }
        } catch (ex: UnsolvedSymbolException) {
            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
        }
    }
    listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
        val jpf = JavaParserFacade.get(project.getSolver()).solve(it)
        if (jpf.isSolved) {
            when (jpf.correspondingDeclaration) {
                is JavaParserFieldDeclaration -> {
                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == referencedNode.uuid) {
                        allReferences.add(it)
                    }
                }
            }
        }
    }

    return allReferences
}