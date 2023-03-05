import model.FactoryOfTransformations
import model.Project
import model.applyTransformationsTo
import model.getPairsOfCorrespondingCompilationUnits
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestThreeWayMerge {

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