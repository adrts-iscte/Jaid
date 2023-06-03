import model.*
import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestDetachRedundantTransformations {

    @Test
    fun detachCallableRedundantTransformations() {
        val mergedBranch = Project("src/main/kotlin/scenarios/detachRedundantTransformations/callableRedundantTransformations/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/detachRedundantTransformations/callableRedundantTransformations/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/detachRedundantTransformations/callableRedundantTransformations/branchToBeMerged")
        val finalMergedVersion = Project("src/main/kotlin/scenarios/detachRedundantTransformations/callableRedundantTransformations/finalMergedVersion")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
        val listOfTransformations = listOfTransformationsBranchToBeMerged + listOfTransformationsMergedBranch
        assertEquals(listOfTransformations.size, 24)

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)

        assertEquals((redundancyFreeSetOfTransformations.getRightSetOfTransformations() + redundancyFreeSetOfTransformations.getLeftSetOfTransformations()).size, 10)
        assertEquals(redundancyFreeSetOfTransformations.getSharedSetOfTransformations().size, 7)

//        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)
//
//        merge(commonAncestor, redundancyFreeSetOfTransformations, true)
//        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.rootPath, commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
//        newCorrespondingCompilationUnits.forEach {
//            assertEquals(it.second.types[0], it.first.types[0])
//        }
    }

    @Test
    fun detachFieldRedundantTransformations() {
        val mergedBranch = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fieldRedundantTransformations/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fieldRedundantTransformations/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fieldRedundantTransformations/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
        val listOfTransformations = listOfTransformationsBranchToBeMerged + listOfTransformationsMergedBranch
        assertEquals(listOfTransformations.size, 24)

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)

        assertEquals((redundancyFreeSetOfTransformations.getRightSetOfTransformations() + redundancyFreeSetOfTransformations.getLeftSetOfTransformations()).size, 10)
        assertEquals(redundancyFreeSetOfTransformations.getSharedSetOfTransformations().size, 7)

//        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)
//
//        merge(commonAncestor, redundancyFreeSetOfTransformations, true)
//        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.rootPath, commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
//        newCorrespondingCompilationUnits.forEach {
//            assertEquals(it.second.types[0], it.first.types[0])
//        }
    }

    @Test
    fun detachFileRedundantTransformations() {
        val mergedBranch = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fileRedundantTransformations/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fileRedundantTransformations/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/detachRedundantTransformations/fileRedundantTransformations/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
        val listOfTransformations = listOfTransformationsBranchToBeMerged + listOfTransformationsMergedBranch
        assertEquals(listOfTransformations.size, 34)

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)

        assertEquals((redundancyFreeSetOfTransformations.getRightSetOfTransformations() + redundancyFreeSetOfTransformations.getLeftSetOfTransformations()).size, 4)
        assertEquals(redundancyFreeSetOfTransformations.getSharedSetOfTransformations().size, 15)

//        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)
//
//        merge(commonAncestor, redundancyFreeSetOfTransformations, true)
//        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.rootPath, commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
//        newCorrespondingCompilationUnits.forEach {
//            assertEquals(it.second.types[0], it.first.types[0])
//        }
    }

    @Test
    fun detachJavaDocRedundantTransformations() {
        val mergedBranch = Project("src/main/kotlin/scenarios/detachRedundantTransformations/javaDocRedundantTransformations/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/detachRedundantTransformations/javaDocRedundantTransformations/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/detachRedundantTransformations/javaDocRedundantTransformations/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
        val listOfTransformations = listOfTransformationsBranchToBeMerged + listOfTransformationsMergedBranch
        assertEquals(listOfTransformations.size, 8)

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)

        assertEquals((redundancyFreeSetOfTransformations.getRightSetOfTransformations() + redundancyFreeSetOfTransformations.getLeftSetOfTransformations()).size, 2)
        assertEquals(redundancyFreeSetOfTransformations.getSharedSetOfTransformations().size, 3)

//        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)
//
//        merge(commonAncestor, redundancyFreeSetOfTransformations, true)
//        val newCorrespondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(commonAncestor.rootPath, commonAncestor.getSetOfCompilationUnit(), finalMergedVersion.getSetOfCompilationUnit())
//        newCorrespondingCompilationUnits.forEach {
//            assertEquals(it.second.types[0], it.first.types[0])
//        }
    }
}