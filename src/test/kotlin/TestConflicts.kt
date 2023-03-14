import model.*
import model.transformations.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestConflicts {

    @Test
    fun CallableCallable() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/callableCallable/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/callableCallable/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/callableCallable/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

        val setOfConflicts = getConflicts(commonAncestor, listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)

        setOfConflicts.forEach {
            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallableDeclaration::class, AddCallableDeclaration::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallableDeclaration::class, ParametersAndOrNameChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallableDeclaration::class, MoveCallableInterClasses::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallableDeclaration::class, ParametersAndOrNameChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallableDeclaration::class, BodyChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallableDeclaration::class, ModifiersChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallableDeclaration::class, ReturnTypeChangedMethod::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallableDeclaration::class, MoveCallableInterClasses::class))
        assertEquals(6, setOfConflicts.getNumberOfConflictsOfType(ParametersAndOrNameChangedCallable::class, ParametersAndOrNameChangedCallable::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(ParametersAndOrNameChangedCallable::class, MoveCallableInterClasses::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(BodyChangedCallable::class, BodyChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedCallable::class, ModifiersChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ReturnTypeChangedMethod::class, ReturnTypeChangedMethod::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveCallableInterClasses::class, MoveCallableInterClasses::class))
    }

    @Test
    fun renameAndBodyChangedInAnyOrder() {
        val mergedBranch = Project("src/main/kotlin/scenarios/threeWayMerge/renameAndBodyChangedInAnyOrder/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/threeWayMerge/renameAndBodyChangedInAnyOrder/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/threeWayMerge/renameAndBodyChangedInAnyOrder/branchToBeMerged")
        val finalMergedVersion = Project("src/main/kotlin/scenarios/threeWayMerge/renameAndBodyChangedInAnyOrder/finalMergedVersion")
        val newCommonAncestor = commonAncestor.clone()

        // Body Changed
        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        // RenameMethod
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)

        // BodyChanged -> RenameMethod
        applyTransformationsTo(commonAncestor, factoryOfTransformationsMergedBranch, true)
        applyTransformationsTo(commonAncestor, factoryOfTransformationsBranchToBeMerged, true)

//        val setOfConflicts = getConflicts(commonAncestor, listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)
//        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()}")
//        }

//        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
//        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
//        allFactoryOfTransformations.forEach { println(it) }
//        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
//        assertEquals(listOfTransformations.size, 7)



        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach {
//            println(it.first)
            assertEquals(it.second.types[0], it.first.types[0])
        }

        // RenameMethod -> BodyChanged
        applyTransformationsTo(newCommonAncestor, factoryOfTransformationsBranchToBeMerged, true)
        applyTransformationsTo(newCommonAncestor, factoryOfTransformationsMergedBranch, true)

        // Tens que clonar os inicializares e todos os pedaços que são aplicados diretamente
        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(newCommonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
        newCorrespondingCompilationUnits.forEach {
//            println(it.first)
            assertEquals(it.second.types[0], it.first.types[0])
        }
    }

    @Test
    fun correctReferences() {
        val mergedBranch = Project("src/main/kotlin/scenarios/threeWayMerge/correctReferences/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/threeWayMerge/correctReferences/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/threeWayMerge/correctReferences/branchToBeMerged")
        val finalMergedVersion = Project("src/main/kotlin/scenarios/threeWayMerge/correctReferences/finalMergedVersion")
        val newCommonAncestor = commonAncestor.clone()

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)

        applyTransformationsTo(commonAncestor, factoryOfTransformationsMergedBranch, true)
        applyTransformationsTo(commonAncestor, factoryOfTransformationsBranchToBeMerged, true)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach {
//            println(it.first)
            assertEquals(it.second.types[0], it.first.types[0])
        }

        applyTransformationsTo(newCommonAncestor, factoryOfTransformationsBranchToBeMerged, true)
        applyTransformationsTo(newCommonAncestor, factoryOfTransformationsMergedBranch, true)

        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(newCommonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
        newCorrespondingCompilationUnits.forEach {
//            println(it.first)
            assertEquals(it.second.types[0], it.first.types[0])
        }
    }
}