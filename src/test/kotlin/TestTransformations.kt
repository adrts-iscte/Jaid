import model.*
import model.visitors.EqualsVisitor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTransformations {

    @Test
    fun constructorTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java")
        val base = projBase.getSetOfCompilationUnit().elementAt(0)
        val projLeft = Project("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java")
        val left = projLeft.getSetOfCompilationUnit().elementAt(0)

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val theOnlyFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()[0]
//        println(theOnlyFactoryOfTransformations)
        val listOfTransformations = theOnlyFactoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 7)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(projBase) }

//        println(base)
        assertTrue(areFilesEqual(base, left))
    }

    @Test
    fun methodTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/methodTransformations/base/MethodTransformationsBaseClass.java")
        val base = projBase.getSetOfCompilationUnit().elementAt(0)
        val projLeft = Project("src/main/kotlin/scenarios/methodTransformations/left/MethodTransformationsLeftClass.java")
        val left = projLeft.getSetOfCompilationUnit().elementAt(0)

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val theOnlyFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()[0]
//        println(theOnlyFactoryOfTransformations)
        val listOfTransformations = theOnlyFactoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 17)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(projBase) }

//        println(base)
        assertTrue(EqualsVisitor.equals(base, left))
    }

    @Test
    fun fileTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/fileTransformations/base/FileTransformationsBaseClass.java")
        val base = projBase.getSetOfCompilationUnit().elementAt(0)
        val projLeft = Project("src/main/kotlin/scenarios/fileTransformations/left/FileTransformationsLeftClass.java")
        val left = projLeft.getSetOfCompilationUnit().elementAt(0)

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val theOnlyFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()[0]
        println(theOnlyFactoryOfTransformations)
        val listOfTransformations = theOnlyFactoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 17)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(projBase) }

//        println(base)
        assertTrue(areFilesEqual(base, left))
    }

    @Test
    fun fieldTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/fieldTransformations/base/FieldTransformationsBaseClass.java")
        val base = projBase.getSetOfCompilationUnit().elementAt(0)
        val projLeft = Project("src/main/kotlin/scenarios/fieldTransformations/left/FieldTransformationsLeftClass.java")
        val left = projLeft.getSetOfCompilationUnit().elementAt(0)

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val theOnlyFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()[0]
//        println(theOnlyFactoryOfTransformations)
        val listOfTransformations = theOnlyFactoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 8)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(projBase) }

//        listOfTransformations.filterIsInstance<BodyChangedCallable>()[0].applyTransformation(base)
//        listOfTransformations.filterIsInstance<ParametersAndOrNameChangedCallable>()[0].applyTransformation(base)
//        listOfTransformations.filterIsInstance<AddField>()[0].applyTransformation(base)

//        println(base)
        assertTrue(areFilesEqual(base, left))
    }

    @Test
    fun javaDocTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/javaDocTransformations/base/JavaDocTransformationsBaseClass.java")
        val base = projBase.getSetOfCompilationUnit().elementAt(0)
        val projLeft = Project("src/main/kotlin/scenarios/javaDocTransformations/left/JavaDocTransformationsLeftClass.java")
        val left = projLeft.getSetOfCompilationUnit().elementAt(0)

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val theOnlyFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()[0]
//        println(theOnlyFactoryOfTransformations)
        val listOfTransformations = theOnlyFactoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 13)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(projBase) }

        println(base)
        assertTrue(areFilesEqual(base, left))
    }

    @Test
    fun moveTransformations() {
        val projBase = Project("src/main/kotlin/scenarios/moveTransformations/base/")
//        val base = projBase.getListOfCompilationUnit()[0]
        val projLeft = Project("src/main/kotlin/scenarios/moveTransformations/left/")
//        val left = projLeft.getListOfCompilationUnit()[0]

        val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
        val allFactoryOfTransformations = factoryOfTransformations.getListOfFactoryOfCompilationUnit()
        allFactoryOfTransformations.forEach {
            println(it)
        }
        val listOfTransformations = factoryOfTransformations.getListOfAllTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 6)

        applyTransformationsTo(projBase, factoryOfTransformations)

        val correspondingCompilationUnits = getPairsOfCorrespondingCompilationUnits(projBase.getSetOfCompilationUnit(),projLeft.getSetOfCompilationUnit())
        correspondingCompilationUnits.forEach{
            println(it.first)
            assertTrue(areFilesEqual(it.first, it.second))
        }
    }
}