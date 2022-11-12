import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import model.FactoryOfTransformations
import model.areClassesEqual
import model.visitors.loadProject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTransformations {

    @Test
    fun constructorTransformations() {
        val base = loadProject("src/main/kotlin/scenarios/constructorTransformations/base/ConstructorTransformationsBaseClass.java")
        val left = loadProject("src/main/kotlin/scenarios/constructorTransformations/left/ConstructorTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
        assertEquals(listOfTransformations.size, 7)
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun methodTransformations() {
        val base = loadProject("src/main/kotlin/scenarios/methodTransformations/base/MethodTransformationsBaseClass.java")
        val left = loadProject("src/main/kotlin/scenarios/methodTransformations/left/MethodTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
        assertEquals(listOfTransformations.size, 17)
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun fileTransformations() {
        val base = loadProject("src/main/kotlin/scenarios/fileTransformations/base/FileTransformationsBaseClass.java")
        val left = loadProject("src/main/kotlin/scenarios/fileTransformations/left/FileTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
        assertEquals(listOfTransformations.size, 16)
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun fieldTransformations() {
        val base = loadProject("src/main/kotlin/scenarios/fieldTransformations/base/FieldTransformationsBaseClass.java")
        val left = loadProject("src/main/kotlin/scenarios/fieldTransformations/left/FieldTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
        assertEquals(listOfTransformations.size, 8)
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }

    @Test
    fun javaDocTransformations() {
        val base = loadProject("src/main/kotlin/scenarios/javaDocTransformations/base/JavaDocTransformationsBaseClass.java")
        val left = loadProject("src/main/kotlin/scenarios/javaDocTransformations/left/JavaDocTransformationsLeftClass.java")

        val factoryOfTransformations = FactoryOfTransformations(base, left)
//        println(factoryOfTransformations)
        val listOfTransformations = factoryOfTransformations.getFinalListOfTransformations()
        assertEquals(listOfTransformations.size, 13)
        listOfTransformations.forEach { it.applyTransformation(base) }

//        println(base)
        val baseClass = base.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        val leftClass = left.findFirst(ClassOrInterfaceDeclaration::class.java).get()
        assertTrue(areClassesEqual(baseClass, leftClass))
    }
}