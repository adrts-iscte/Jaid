package model

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import model.transformations.AddCallableDeclaration
import model.transformations.Transformation
import model.visitors.*
import java.util.*
import kotlin.reflect.KClass

val CompilationUnit.correctPath : String
    get() = this.storage.get().path.toString().replace(Regex("([bB]ase)+"), "").replace(Regex("([lL]eft)+"), "")

val CallableDeclaration<*>.parameterTypes : List<Type>
    get() = this.parameters.map { it.type }

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

fun getPairsOfCorrespondingCompilationUnits(listOfCompilationUnitBase : Set<CompilationUnit>, listOfCompilationUnitBranch : Set<CompilationUnit>): List<Pair<CompilationUnit, CompilationUnit>> {
    val removeBranchRepresentativeNamesRegex = Regex("([bB]ase)*([lL]eft)*(branchToBeMerged)*(mergedBranch)*(commonAncestor)*(finalMergedVersion)*")
    return listOfCompilationUnitBase.zip(listOfCompilationUnitBranch).filter {
        it.first.storage.get().path.toString().replace(removeBranchRepresentativeNamesRegex, "") ==
                it.second.storage.get().path.toString().replace(removeBranchRepresentativeNamesRegex, "")
    }
}

//fun renameAllFieldUses(proj: Project, fieldToRename: FieldDeclaration, oldName: String, newName: String) {
//    val listOfFieldUses = proj.getListOfFieldUses()
//
//    val solver = CombinedTypeSolver()
//    solveNameExpr(listOfFieldUses, solver, fieldToRename, newName)
//    solveFieldAccessExpr(listOfFieldUses, solver, fieldToRename, newName)
////    model.solveVariableDeclarationExpr(listOfFieldUses, solver, fieldToRename, newName)
//
//}
//
//fun solveNameExpr(listOfFieldUses: MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration, newName: String) {
//    listOfFieldUses.filterIsInstance<NameExpr>().forEach {
//        val jpf = JavaParserFacade.get(solver).solve(it)
//        if (jpf.isSolved) {
//            when (jpf.correspondingDeclaration) {
//                is JavaParserFieldDeclaration -> {
//                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
//                        it.setName(newName)
//                    }
//                }
////                is JavaParserVariableDeclaration -> {
////                    if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
////                        it.setName(newName)
////                    }
////                }
//            }
//        }
//    }
//}
//
//fun solveFieldAccessExpr(listOfFieldUses :  MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration , newName: String) {
//    listOfFieldUses.filterIsInstance<FieldAccessExpr>().forEach {
//        val jpf = JavaParserFacade.get(solver).solve(it)
//        if (jpf.isSolved) {
//            when (jpf.correspondingDeclaration) {
//                is JavaParserFieldDeclaration -> {
//                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
//                        it.setName(newName)
//                    }
//                }
////                is JavaParserVariableDeclaration -> {
////                    if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
////                        it.setName(newName)
////                    }
////                }
//            }
//        }
//    }
//}
//
//fun solveVariableDeclarationExpr(listOfFieldUses :  MutableList<Node>, solver: CombinedTypeSolver, fieldToRename: FieldDeclaration , newName: String) {
//    listOfFieldUses.filterIsInstance<VariableDeclarationExpr>().forEach {
//        val jpf = JavaParserFacade.get(solver).solve(it)
//        if (jpf.isSolved) {
//            when (jpf.correspondingDeclaration) {
//                is JavaParserFieldDeclaration -> {
//                    if ((jpf.correspondingDeclaration as JavaParserFieldDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
//                        it.variables.first().setName(newName)
//                    }
//                }
//                is JavaParserVariableDeclaration -> if ((jpf.correspondingDeclaration as JavaParserVariableDeclaration).wrappedNode.uuid == fieldToRename.uuid) {
//                    it.variables.first().setName(newName)
//                }
//            }
//        }
//    }
//}
//
//fun renameAllMethodCalls(proj: Project, methodToRename: MethodDeclaration, newName: String) {
//    proj.getMapOfMethodCallExpr().filterValues { it.uuid == methodToRename.uuid }.forEach {
//        it.key.setName(newName)
//    }
//}

//fun renameAllConstructorCalls(proj: Project, classToRename: ClassOrInterfaceDeclaration, newName: String) {
//    val newClassToRename = proj.getClassOrInterfaceByUUID(classToRename.uuid)
//
//    newClassToRename?.let {
//        newClassToRename.constructors.forEach {
//            it.setName(newName)
//        }
//
//        val solver = CombinedTypeSolver()
//        solver.add(ReflectionTypeSolver())
//        val listOfClassConstructorCalls = proj.getListOfClassConstructorCalls()
////        solveObjectCreationExpr(listOfClassConstructorCalls, solver, classToRename, newName)
////        solveClassOrInterfaceType(listOfClassConstructorCalls, classToRename, newName)
//        /*val listOfConstructorCalls = mutableListOf<ObjectCreationExpr>()
//        val constructorCallsVisitor = ObjectCreationVisitor(newClassToRename)
//        cu.accept(constructorCallsVisitor, listOfConstructorCalls)
//
//        listOfConstructorCalls.forEach {
//            it.type.setName(newName)
//        }
//
//        val listOfClassTypes = mutableListOf<ClassOrInterfaceType>()
//        val classTypesVisitor = ClassOrInterfaceTypeVisitor(newClassToRename)
//        cu.accept(classTypesVisitor, listOfClassTypes)
//
//        listOfClassTypes.forEach {
//            it.setName(newName)
//        }*/
//    }
//}
//
//fun solveObjectCreationExpr(listOfClassConstructorCalls: MutableList<Node>, solver: CombinedTypeSolver, classToRename: ClassOrInterfaceDeclaration, newName: String) {
//    listOfClassConstructorCalls.filterIsInstance<ObjectCreationExpr>().forEach {
////        val jpf = JavaParserFacade.get(solver).solve(it)
////        if (jpf.isSolved) {
////            val constructorDecl = (jpf.correspondingDeclaration as JavaParserConstructorDeclaration<*>).wrappedNode
////            if (constructorDecl.parentNode.orElse(null)?.uuid == classToRename.uuid) {
////                it.type.setName(newName)
////            }
////        }
//        if (classToRename.name == it.type.name) {
//            it.type.setName(newName)
//        }
//    }
//}
//
//fun solveClassOrInterfaceType(listOfClassConstructorCalls: MutableList<Node>, classToRename: ClassOrInterfaceDeclaration, newName: String) {
//    listOfClassConstructorCalls.filterIsInstance<ClassOrInterfaceType>().forEach {
//        if (classToRename.name == it.name) {
//            it.setName(newName)
//        }
//    }
//}

//fun areClassesEqual(c1 : ClassOrInterfaceDeclaration, c2 : ClassOrInterfaceDeclaration) : Boolean {
//    if (c1.isInterface != c2.isInterface) return false
//    if (c1.isInnerClass != c2.isInnerClass) return false
//    if (c1.name != c2.name) return false
//    if (c1.comment != c2.comment) return false
//    if (c1.typeParameters.toSet() != c2.typeParameters.toSet()) return false
//    if (c1.implementedTypes.toSet() != c2.implementedTypes.toSet()) return false
//    if (c1.extendedTypes.toSet() != c2.extendedTypes.toSet()) return false
//    if (c1.members != c2.members) return false
//    if (c1.modifiers.toSet() != c2.modifiers.toSet()) return false
//    return true
//
//}
//
//fun areFilesEqual(f1 : CompilationUnit, f2 : CompilationUnit) : Boolean {
//    if (f1.imports != f2.imports) return false
//    if (f1.packageDeclaration != f2.packageDeclaration) return false
//    if (f1.types != f2.types) return false
//    f1.types.filterIsInstance<ClassOrInterfaceDeclaration>().forEach {
//            itf2 -> if (!f2.types.filterIsInstance<ClassOrInterfaceDeclaration>().any { areClassesEqual(it, itf2)}) {
//                return false
//            }
//    }
//    return true
//
//}

fun calculateIndexOfMemberToAdd(clazz : ClassOrInterfaceDeclaration, classToHaveCallableAdded : ClassOrInterfaceDeclaration, newMemberUUID : UUID) : Int {
    val clazzMembers = clazz.members
    val clazzMembersUUID = clazzMembers.map { it.uuid }
    val classToHaveCallableAddedMembers = classToHaveCallableAdded.members
    val classToHaveCallableAddedMembersUUID = classToHaveCallableAddedMembers.map { it.uuid }

    for (i in clazzMembersUUID.indexOf(newMemberUUID) downTo  -1) {
        val similarMember = classToHaveCallableAddedMembers.find { it.uuid == clazzMembersUUID[i] }
        similarMember?.let {
//                val index = if (clazzMembersUUID == classToHaveCallableAddedMembersUUID) {
                return classToHaveCallableAddedMembers.indexOf(similarMember) + 1
//                } else {
//                    clazzMembers.indexOf(clazzMembers.find { it.uuid == newMemberUUID}!!)
//                }
//                return index
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