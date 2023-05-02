package evaluation

import model.FactoryOfTransformations
import model.Project
import model.UUID
import model.visitors.EqualsUuidVisitor

fun main() {

//    val leftPath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_left_0bdad\\src\\org\\bukkit\\ItemStack.java"
//    val basePath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_base_90340\\src\\org\\bukkit\\ItemStack.java"
//    val rightPath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_right_90340\\src\\org\\bukkit\\ItemStack.java"
    val leftPath = "src\\main\\resources\\repositories\\Bukkit\\rev_01a273d9_069df1b6\\identified_rev_left_01a27\\src\\main\\java\\org\\bukkit\\event\\player\\PlayerAnimationEvent.java"
    val basePath = "src\\main\\resources\\repositories\\Bukkit\\rev_0bdadf12_90340f6f\\identified_rev_base_90340\\src\\org\\bukkit\\ItemStack.java"
    val rightPath = "src\\main\\resources\\repositories\\Bukkit\\rev_01a273d9_069df1b6\\identified_rev_right_069df\\src\\main\\java\\org\\bukkit\\event\\player\\PlayerAnimationEvent.java"

    val projLeft = Project(leftPath)
//    val projBase = Project(basePath)
    val projRight = Project(rightPath)


    val leftMethod = projLeft.getConstructorByUUID(UUID("8ee24531-5cdb-44ce-b2be-08a217c681d8"))!!
    val rightMethod = projRight.getConstructorByUUID(UUID("8ee24531-5cdb-44ce-b2be-08a217c681d8"))!!

    val leftBody = leftMethod.body
    val rightBody = rightMethod.body
    println(EqualsUuidVisitor(projLeft, projRight).equals(projLeft.getSetOfCompilationUnit().elementAt(0), projRight.getSetOfCompilationUnit().elementAt(0)))
    println(projLeft.getSetOfCompilationUnit().elementAt(0) == projRight.getSetOfCompilationUnit().elementAt(0))

//    val factoryOfTransformations = FactoryOfTransformations(projBase, projLeft)

}