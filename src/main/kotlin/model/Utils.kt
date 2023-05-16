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
import javassist.Loader.Simple
import model.conflictDetection.Conflict
import model.conflictDetection.ConflictTypeLibrary
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

val TypeDeclaration<*>.asString : String
    get() {
        return if (this.isClassOrInterfaceDeclaration) {
            if ((this as ClassOrInterfaceDeclaration).isInterface)
                "INTERFACE"
            else
                "CLASS"
        } else "ENUM"
    }

val Node.asString : String
    get() {
        return when(this) {
            is TypeDeclaration<*> -> this.asString
            is CallableDeclaration<*> -> {
                if (this.isConstructorDeclaration) {
                    "CONSTRUCTOR"
                } else {
                    "METHOD"
                }
            }
            is FieldDeclaration -> "FIELD"
            is EnumConstantDeclaration -> "ENUM CONSTANT"
            else -> "ERROR NODE"
        }
    }

val Node.getNodesName : SimpleName
    get() {
        return when(this) {
            is FieldDeclaration -> (this.variables.first() as VariableDeclarator).name
            is TypeDeclaration<*> -> this.name
            is CallableDeclaration<*> -> this.name
            is EnumConstantDeclaration -> this.name
            else -> SimpleName("ERROR NODES NAME")
        }
    }

fun areParentNodesEqual(firstTransformationParentNode : Node, secondTransformationParentNode : Node) : Boolean {
    return if(firstTransformationParentNode::class == secondTransformationParentNode::class) {
        if (firstTransformationParentNode is CompilationUnit && secondTransformationParentNode is CompilationUnit) {
            firstTransformationParentNode.correctPath == secondTransformationParentNode.correctPath
        } else {
            firstTransformationParentNode.uuid == secondTransformationParentNode.uuid
        }
    } else {
        false
    }
}

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

fun <K, V> MutableMap<K, V>.setAll(otherMap: Map<K, V>) {
    otherMap.entries.forEach { this[it.key] = it.value }
}

fun <K, V> Map<K,  MutableList<V>>.getKey(target: V): K? {
    return this.filterValues { it.contains(target) }.keys.firstOrNull()
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

fun calculateIndexOfMemberToAdd(type : TypeDeclaration<*>, typeToHaveCallableAdded : TypeDeclaration<*>, newMemberUUID : UUID) : Int {
    val typeMembers = type.members
    val typeMembersUUID = typeMembers.map { it.uuid }
    val typeToHaveCallableAddedMembers = typeToHaveCallableAdded.members
    val typeToHaveCallableAddedMembersUUID = typeToHaveCallableAddedMembers.map { it.uuid }

    for (i in typeMembersUUID.indexOf(newMemberUUID) downTo  0) {
        val similarMember = typeToHaveCallableAddedMembers.find { it.uuid == typeMembersUUID[i] }
        similarMember?.let {
            return typeToHaveCallableAddedMembers.indexOf(similarMember) + 1
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

    val fieldUsesVisitor = FieldAndEnumConstantsUsesVisitor()
    val listOfFieldUses = mutableListOf<Expression>()
    searchReferencesIn.accept(fieldUsesVisitor, listOfFieldUses)

    listOfFieldUses.forEach {
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

    return allReferences
}

//fun getNodeReferencesToReferencedNode(project: Project, referencedNode: FieldDeclaration, searchReferencesIn: Node): MutableList<Expression> {
//    val allReferences = mutableListOf<Expression>()
//
////    val visitor = when(referencedNode) {
////        is FieldDeclaration -> FieldUsesVisitor()
////        is ClassOrInterfaceDeclaration -> ClassUsageCallsVisitor()
////        is MethodDeclaration -> MethodCallExprVisitor()
////        else -> null
////    }
//
//    val fieldUsesVisitor = FieldUsesVisitor()
//    val listOfFieldUses = mutableListOf<Node>()
//    searchReferencesIn.accept(fieldUsesVisitor, listOfFieldUses)
//
//    listOfFieldUses.filterIsInstance<NameExpr>().forEach {
//        try {
//            val jpf = JavaParserFacade.get(project.getSolver()).solve(it)
//            if (jpf.isSolved) {
//                when (jpf.correspondingDeclaration) {
//                    is JavaParserFieldDeclaration -> {
//                        if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == referencedNode.uuid) {
//                            allReferences.add(it)
//                        }
//                    }
//                }
//            }
//        } catch (ex: UnsolvedSymbolException) {
//            println("Foi encontrada uma exceção no CorrectAllReferences: ${ex.message}")
//        }
//    }
//    listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
//        val jpf = JavaParserFacade.get(project.getSolver()).solve(it)
//        if (jpf.isSolved) {
//            when (jpf.correspondingDeclaration) {
//                is JavaParserFieldDeclaration -> {
//                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == referencedNode.uuid) {
//                        allReferences.add(it)
//                    }
//                }
//            }
//        }
//    }
//
//    return allReferences
//}