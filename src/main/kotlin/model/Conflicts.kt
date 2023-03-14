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




interface EntityConverter<A, B> {
    fun convert(a: A): B
}

fun <A, B> EntityConverter<A, B>.convert(list: List<A>): List<B> = list.map { convert(it) }

//fun <A : Transformation, B: Transformation> ConflictType<A, B>.applicable(transformation: Transformation) = transformation is A || transformation is B



fun main() {
    val addCallableDeclarationTrans = AddCallableDeclaration(ClassOrInterfaceDeclaration(), MethodDeclaration())
    val addFieldTrans = AddField(ClassOrInterfaceDeclaration(), FieldDeclaration())

//    val applicableConflicts = applicableConflicts(addFieldTrans)
//    applicableConflicts.forEach{
//        println(it)
//    }
}

