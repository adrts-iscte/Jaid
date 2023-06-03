import model.*
import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations
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

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallable::class, AddCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallable::class, SignatureChanged::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallable::class, MoveCallableInterTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallable::class, SignatureChanged::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallable::class, BodyChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallable::class, ModifiersChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallable::class, ReturnTypeChangedMethod::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveCallable::class, MoveCallableInterTypes::class))
        assertEquals(6, setOfConflicts.getNumberOfConflictsOfType(SignatureChanged::class, SignatureChanged::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(SignatureChanged::class, MoveCallableInterTypes::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(BodyChangedCallable::class, BodyChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedCallable::class, ModifiersChangedCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ReturnTypeChangedMethod::class, ReturnTypeChangedMethod::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveCallableInterTypes::class, MoveCallableInterTypes::class))
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

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveFieldInterTypes::class, AddField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, MoveFieldInterTypes::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameField::class, MoveFieldInterTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveFieldInterTypes::class, MoveFieldInterTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddField::class, AddField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, InitializerChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, ModifiersChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, TypeChangedField::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameField::class, RenameField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(TypeChangedField::class, InitializerChangedField::class))
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

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(6, setOfConflicts.getNumberOfConflictsOfType(RemoveFile::class, Transformation::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeEnumConstantArguments::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeEnumConstantArguments::class, RemoveEnumConstant::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeEnumConstantArguments::class, ChangeEnumConstantArguments::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RenameEnumConstant::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RenameEnumConstant::class, AddEnumConstant::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameEnumConstant::class, RenameEnumConstant::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveEnumConstant::class, AddType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveType::class, AddEnumConstant::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddEnumConstant::class, AddEnumConstant::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, AddType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, RemoveType::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, RenameType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveTypeInterFiles::class, MoveTypeInterFiles::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangePackage::class, ChangePackage::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeImports::class, ChangeImports::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddType::class, RenameType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddType::class, AddType::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RemoveType::class, ChangeExtendedTypes::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RemoveType::class, ChangeImplementsTypes::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RemoveType::class, ModifiersChangedType::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RemoveType::class, RenameType::class))
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(RenameType::class, RenameType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedType::class, ModifiersChangedType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeImplementsTypes::class, ChangeImplementsTypes::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ChangeExtendedTypes::class, ChangeExtendedTypes::class))
    }

    @Test
    fun allJavadocConflicts() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/allJavadocConflicts/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/allJavadocConflicts/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/allJavadocConflicts/branchToBeMerged")
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        /* JavadocJavadoc */
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, SetJavaDoc::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, RemoveJavaDoc::class))

        /* CallableJavadoc */
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, RemoveCallable::class))

        /* FieldJavadoc */
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, RemoveField::class))

        /* FileJavadoc */
        assertEquals(2, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(SetJavaDoc::class, RemoveEnumConstant::class))
    }

    @Test
    fun CallableFile() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/callableFile/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/callableFile/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/callableFile/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddCallable::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(SignatureChanged::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(BodyChangedCallable::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedCallable::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ReturnTypeChangedMethod::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveCallableInterTypes::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveEnumConstant::class, AddCallable::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveEnumConstant::class, BodyChangedCallable::class))
    }

    @Test
    fun FieldFile() {
        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/fieldFile/mergedBranch")
        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/fieldFile/commonAncestor")
        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/fieldFile/branchToBeMerged")

        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

        val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsMergedBranch, factoryOfTransformationsBranchToBeMerged)
        val setOfConflicts = getConflicts(commonAncestor, redundancyFreeSetOfTransformations)

        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
        }

        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(AddField::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RenameField::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(InitializerChangedField::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(ModifiersChangedField::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(TypeChangedField::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(MoveFieldInterTypes::class, RemoveType::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveEnumConstant::class, InitializerChangedField::class))
        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveEnumConstant::class, AddField::class))
    }

//    @Test
//    fun CallableField() {
//        val mergedBranch = Project("src/main/kotlin/scenarios/conflicts/callableField/mergedBranch")
//        val commonAncestor = Project("src/main/kotlin/scenarios/conflicts/callableField/commonAncestor")
//        val branchToBeMerged = Project("src/main/kotlin/scenarios/conflicts/callableField/branchToBeMerged")
//
//        val factoryOfTransformationsMergedBranch = FactoryOfTransformations(commonAncestor, mergedBranch)
//        val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
//        val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(commonAncestor, branchToBeMerged)
//        val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()
//
//        val setOfConflicts = getConflicts(commonAncestor, listOfTransformationsMergedBranch, listOfTransformationsBranchToBeMerged)
//
//        setOfConflicts.forEach {
//            println("Conflict between ${it.first.getText()} and ${it.second.getText()} with message: ${it.message}")
//        }
//
//        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(RemoveField::class, BodyChangedCallable::class))
//        assertEquals(1, setOfConflicts.getNumberOfConflictsOfType(TypeChangedField::class, BodyChangedCallable::class))
//    }
}