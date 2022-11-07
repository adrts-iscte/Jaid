package model

import model.transformations.Transformation

interface Conflict {
    val first : Transformation
    val second : Transformation
    val message : String
}