package model

import model.transformations.Transformation

interface Conflict {
    val first : Transformation
    val second : Transformation
    val message : String
}

fun createConflict(first: Transformation, second: Transformation, message: String) : Conflict {
    return object : Conflict {
        override val first: Transformation get() = first
        override val second: Transformation get() = second
        override val message: String
            get() = message
    }
}