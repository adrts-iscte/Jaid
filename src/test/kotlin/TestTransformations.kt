import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.FactoryOfTransformations
import model.areClassesEqual
import model.areFilesEqual
import model.transformations.AddField
import model.transformations.BodyChangedCallable
import model.transformations.ParametersAndOrNameChangedCallable
import model.visitors.loadFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTransformations {

    @Test
    fun constructorTransformations() {
        val base = loadFile("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java")
        val left = loadFile("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 7)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun methodTransformations() {
        val base = loadFile("src/main/kotlin/scenarios/methodTransformations/base/MethodTransformationsBaseClass.java")
        val left = loadFile("src/main/kotlin/scenarios/methodTransformations/left/MethodTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 17)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()

        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun fileTransformations() {
        val base = loadFile("src/main/kotlin/scenarios/fileTransformations/base/FileTransformationsBaseClass.java")
        val left = loadFile("src/main/kotlin/scenarios/fileTransformations/left/FileTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 16)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(base) }

        println(base)
        println(left)
        assertTrue(areFilesEqual(base, left))
    }

    @Test
    fun fieldTransformations() {
        val base = loadFile("src/main/kotlin/scenarios/fieldTransformations/base/FieldTransformationsBaseClass.java")
        val left = loadFile("src/main/kotlin/scenarios/fieldTransformations/left/FieldTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 8)
//        listOfTransformations.shuffle()
//        listOfTransformations.forEach { it.applyTransformation(base) }


        listOfTransformations.filterIsInstance<BodyChangedCallable>()[0].applyTransformation(base)
        listOfTransformations.filterIsInstance<ParametersAndOrNameChangedCallable>()[0].applyTransformation(base)
//        listOfTransformations.filterIsInstance<AddField>()[0].applyTransformation(base)
//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
//        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun javaDocTransformations() {
        val base = loadFile("src/main/kotlin/scenarios/javaDocTransformations/base/JavaDocTransformationsBaseClass.java")
        val left = loadFile("src/main/kotlin/scenarios/javaDocTransformations/left/JavaDocTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations().toMutableList()
        assertEquals(listOfTransformations.size, 13)
        listOfTransformations.shuffle()
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }
}