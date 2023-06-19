package evaluation

import model.FactoryOfTransformations
import model.Project
import model.UUID
import model.visitors.EqualsUuidVisitor

fun main() {

//    val leftPath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_left_0bdad\\src\\org\\bukkit\\ItemStack.java"
//    val basePath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_base_90340\\src\\org\\bukkit\\ItemStack.java"
//    val rightPath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_right_90340\\src\\org\\bukkit\\ItemStack.java"
    val leftPath = "src\\main\\resources\\repositories\\clojure\\rev_04e634ea_5d55a7ed\\identified_rev_left_04e63"
    val basePath = "src\\main\\resources\\repositories\\clojure\\rev_04e634ea_5d55a7ed\\identified_rev_base_fba9b"
    val rightPath = "src\\main\\resources\\repositories\\clojure\\rev_04e634ea_5d55a7ed\\identified_rev_right_5d55a"

    val projLeft = Project(leftPath)
    val projBase = Project(basePath)
//    val projRight = Project(rightPath)

    val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)
    println()
}