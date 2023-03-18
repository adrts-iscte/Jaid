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
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
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
    fun FieldField() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/fieldField/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/fieldField/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/fieldField/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

        val setOfConflicts = getConflicts(commonAncestor, listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveFieldInterClasses::class, AddField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, MoveFieldInterClasses::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameField::class, MoveFieldInterClasses::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveFieldInterClasses::class, MoveFieldInterClasses::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddField::class, AddField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, InitializerChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, ModifiersChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, TypeChangedField::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(TypeChangedField::class, TypeChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedField::class, ModifiersChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(InitializerChangedField::class, InitializerChangedField::class))
    }

    @Test
    fun FileFile() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/fileFile/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/fileFile/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/fileFile/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

        val setOfConflicts = getConflicts(commonAncestor, listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)

        setOfConflicts.forEach {
            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, AddClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, RemoveClassOrInterface::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, RenameClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, MoveTypeInterFiles::class))
//        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangePackage::class, ChangePackage::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeImports::class, ChangeImports::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddClassOrInterface::class, RenameClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddClassOrInterface::class, AddClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveClassOrInterface::class, ChangeExtendedTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveClassOrInterface::class, ChangeImplementsTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveClassOrInterface::class, ModifiersChangedClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveClassOrInterface::class, RenameClassOrInterface::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameClassOrInterface::class, RenameClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedClassOrInterface::class, ModifiersChangedClassOrInterface::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeImplementsTypes::class, ChangeImplementsTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeExtendedTypes::class, ChangeExtendedTypes::class))
    }
}