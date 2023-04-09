package evaluation

import model.path

fun main() {
//    val leftFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_left_72812\\src\\org\\Stack.java"
//    val baseFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_base_0b29a\\src\\org\\Stack.java"
//    val rightFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_right_e96b6\\src\\org\\Stack.java"
//
//    val automaticallyGenerateUUID = AutomaticallyGenerateUUID(leftFile, baseFile, rightFile)
//
//    val leftCU = automaticallyGenerateUUID.getLeftCompilationUnit()
//    val baseCU = automaticallyGenerateUUID.getBaseCompilationUnit()
//    val rightCU = automaticallyGenerateUUID.getRightCompilationUnit()
//
//    println(leftCU)
//    println(baseCU)
//    println(rightCU)

    val dir = "src/main/resources/repositories/Bukkit".replace("\\","/")

    val listOfAllFiles = FilesManager.listOfAllFiles(dir)
    val listOfAllRevisionFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".revisions") }
    listOfAllRevisionFiles.forEach { revisionFile ->
        println("A ver revision file: ${revisionFile.name}")
        val revisionObject = Revision(revisionFile.path)
        val listOfTuples = revisionObject.listOfTuples

        listOfTuples.forEach {
            val agUUID= AutomaticallyGenerateUUID(it.leftCompilationUnit!!, it.baseCompilationUnit!!, it.rightCompilationUnit!!)
            println(it.leftCompilationUnit.storage.get().fileName)
            agUUID.writeFiles()
        }
//        val numberOfTransformations =
//        println("Number of Transformations: ${}")
    }
}