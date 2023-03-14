import model.FactoryOfTransformations
import model.Project
import model.applyTransformationsTo
import model.getPairsOfCorrespondingCompilationUnits
import model.visitors.EqualsUuidVisitor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTransformations {

    @Test
    fun constructorTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/constructorTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/constructorTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
//        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 7)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(), projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach {
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun methodTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/methodTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/methodTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
//        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 17)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(), projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach {
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun fileTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/fileTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/fileTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 16) // Tava a 17!

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(),projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach{
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun fieldTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/fieldTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/fieldTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
//        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 8)


//        listOfTransformations.filterIsInstance<BodyChangedCallable>()[0].applyTransformation(base)
//        listOfTransformations.filterIsInstance<ParametersAndOrNameChangedCallable>()[0].applyTransformation(base)
//        listOfTransformations.filterIsInstance<AddField>()[0].applyTransformation(base)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(), projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach {
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun javaDocTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/javaDocTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/javaDocTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
//        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 13)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(),projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach{
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun moveTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/transformations/moveTransformations/base/")
        val projLeft = Project("src/main/kotlin/scenarios/transformations/moveTransformations/left/")

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
        allFactoryOfTransformations.forEach { println(it) }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 16)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(),projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach{
//            println(it.first)
            assertTrue(EqualsUuidVisitor.equals(it.first, it.second))
        }
    }

    @Test
    fun renameAndBodyChangedInAnyOrder() {
        val mergedBranch = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/renameAndBodyChangedInAnyOrder/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/renameAndBodyChangedInAnyOrder/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/renameAndBodyChangedInAnyOrder/branchToBeMerged")
        val finalMergedVersion = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/renameAndBodyChangedInAnyOrder/finalMergedVersion")
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
        val mergedBranch = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/correctReferences/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/correctReferences/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/correctReferences/branchToBeMerged")
        val finalMergedVersion = Project("src/main/kotlin/scenarios/transformations/threeWayMerge/correctReferences/finalMergedVersion")
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