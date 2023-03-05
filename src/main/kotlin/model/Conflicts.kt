package model

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import model.transformations.AddCallableDeclaration
import model.transformations.AddField
import model.transformations.MoveCallableInterClasses
import model.transformations.Transformation
import kotlin.reflect.*

//interface ConflictType<A : Transformation, B : Transformation> {
//    fun check(a: A, b: B) : List<Conflict>
//    fun check(b: B, a: A) = check(a, b)
//    fun applicable(transformation: Transformation) = transformation is A || transformation is B
//}
//
//abstract class ConflictType2<A : Transformation, B : Transformation> {
//    abstract fun check(a: A, b: B) : List<Conflict>
//    open fun check(b: B, a: A) = check(a, b)
////    fun <A, B> applicable(transformation: Transformation) = transformation is A || transformation is B
//}

interface ConflictType {
    fun getFirst() : KClass<out Transformation>
    fun getSecond() : KClass<out Transformation>

    fun verifyIfExistsConflict(a: Transformation, b: Transformation, listOfConflicts : MutableSet<Conflict>) {
        if (a::class != getFirst() || a::class != getSecond()) {
            verifyIfExistsConflict(b, a, listOfConflicts)
        }
        check(a,b, listOfConflicts)
    }

    fun check(a: Transformation, b: Transformation, listOfConflicts : MutableSet<Conflict>)

    fun applicable(a: Transformation, b : Transformation) : Boolean {
        return (a::class == getFirst() && b::class == getSecond()) || (b::class == getFirst() && a::class == getSecond())
    }
}


interface EntityConverter<A, B> {
    fun convert(a: A): B
}

fun <A, B> EntityConverter<A, B>.convert(list: List<A>): List<B> = list.map { convert(it) }

//fun <A : Transformation, B: Transformation> ConflictType<A, B>.applicable(transformation: Transformation) = transformation is A || transformation is B


val conflicts = listOf(
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = AddCallableDeclaration::class
        override fun getSecond(): KClass<out Transformation> = AddCallableDeclaration::class

        override fun check(a: Transformation, b: Transformation, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = a as AddCallableDeclaration
            val secondTransformation = b as AddCallableDeclaration
            if(firstTransformation.getNewNode().signature == secondTransformation.getNewNode().signature) {
                listOfConflicts.add(createConflict(a, b, "The two added callables have the same signature"))
            }
        }
    },
    object : ConflictType {
        override fun getFirst(): KClass<out Transformation> = AddCallableDeclaration::class
        override fun getSecond(): KClass<out Transformation> = MoveCallableInterClasses::class

        override fun check(a: Transformation, b: Transformation, listOfConflicts: MutableSet<Conflict>) {
            val firstTransformation = a as AddCallableDeclaration
            val secondTransformation = b as MoveCallableInterClasses
            if(firstTransformation.getNewNode().signature == secondTransformation.getNode().signature) {
                listOfConflicts.add(createConflict(a, b, "The two added callables have the same signature"))
            }
        }
    }

)

fun applicableConflict(a: Transformation, b : Transformation) : ConflictType? {
    return conflicts.find { it.applicable(a, b) }
}

fun main() {
    val addCallableDeclarationTrans = AddCallableDeclaration(ClassOrInterfaceDeclaration(), MethodDeclaration())
    val addFieldTrans = AddField(ClassOrInterfaceDeclaration(), FieldDeclaration())

//    val applicableConflicts = applicableConflicts(addFieldTrans)
//    applicableConflicts.forEach{
//        println(it)
//    }
}

