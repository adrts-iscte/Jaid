package model.conflictDetection

import model.*
import model.transformations.*
import model.visitors.EqualsUuidVisitor
import kotlin.reflect.KClass

interface Conflict {
    val first : Transformation
    val second : Transformation
    fun getConflictType() : ConflictType
    val message : String
}

fun createConflict(first: Transformation, second: Transformation, message: String, conflictType: ConflictType) : Conflict {
    return object : Conflict {
        override val first: Transformation get() = first
        override val second: Transformation get() = second
        override fun getConflictType(): ConflictType = conflictType
        override val message: String
            get() = message
    }
}

interface ConflictType {
    fun getFirst() : KClass<out Transformation>
    fun getSecond() : KClass<out Transformation>

    fun verifyIfExistsConflict(a: Transformation, b: Transformation,
                               commonAncestor: Project, listOfConflicts: MutableSet<Conflict>) {
        require(applicable(a, b))
        if (a::class != getFirst() || b::class != getSecond()) {
            return verifyIfExistsConflict(b, a, commonAncestor, listOfConflicts)
        }
        check(a, b, commonAncestor, listOfConflicts)
    }

    fun check(a: Transformation, b: Transformation, commonAncestor: Project, listOfConflicts: MutableSet<Conflict>)

    fun applicable(a: Transformation, b : Transformation) : Boolean {
        return (a::class == getFirst() && b::class == getSecond()) || (b::class == getFirst() && a::class == getSecond())
    }
}

object ConflictTypeLibrary {

    fun applicableConflict(a: Transformation, b : Transformation) : ConflictType? {
        return allConflictTypes.find { it.applicable(a, b) }
    }

    fun getConflictTypeByKClasses(a: KClass<out Transformation>, b : KClass<out Transformation>) : ConflictType {
        return allConflictTypes.find { (a == it.getFirst() && b == it.getSecond()) ||
                (b == it.getFirst() && a == it.getSecond())  }!!
    }

    private val allConflictTypes =
        allCallableCallableConflictTypes + allCallableFieldConflictTypes +
        allCallableFileConflictTypes + allCallableJavadocConflictTypes +
        allFieldFileConflictTypes + allFieldFieldConflictTypes + allFieldJavadocConflictTypes +
        allFileFileConflictTypes + allFileJavadocConflictTypes + allJavadocJavadocConflictTypes

}
