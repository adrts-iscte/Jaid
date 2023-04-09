package evaluation

import model.FactoryOfTransformations
import model.Project

fun main() {
    val leftPath = "src\\main\\resources\\repositories\\Bukkit\\rev_d2656750_6255d179\\rev_left_d2656\\src"
    val basePath = "src\\main\\resources\\repositories\\Bukkit\\rev_d2656750_6255d179\\rev_base_6255d\\src"
    val rightPath = "src\\main\\resources\\repositories\\Bukkit\\rev_d2656750_6255d179\\rev_right_6255d\\src"

    val left = Project(leftPath, setupProject = false, onlyGeneratedFiles = false)
    val base = Project(basePath, setupProject = false, onlyGeneratedFiles = false)
    val right = Project(rightPath, setupProject = false, onlyGeneratedFiles = false)

    val factoryOfTransformationsMergedBranch = FactoryOfTransformations(base, right)
    val listOfTransformationsMergedBranch = factoryOfTransformationsMergedBranch.getListOfAllTransformations().toSet()
    val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(base, left)
    val listOfTransformationsBranchToBeMerged = factoryOfTransformationsBranchToBeMerged.getListOfAllTransformations().toSet()

    println()
}