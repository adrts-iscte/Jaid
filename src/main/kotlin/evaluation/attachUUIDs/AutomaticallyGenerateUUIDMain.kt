@file:JvmName("AutomaticallyGenerateUUIDKt")

package evaluation.attachUUIDs

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

    val dir = "src/main/resources/repositories/jsoup".replace("\\","/")

    val listOfAllFiles = FilesManager.listOfAllFiles(dir)
    val listOfAllRevisionFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".identified_revisions") }
    listOfAllRevisionFiles.forEach { revisionFile ->
//        if (revisionFile.nameWithoutExtension.contains("0c4")) {
            println("A ver revision file: ${revisionFile.name}")
            val revisionObject = Revision(revisionFile.path)
            val listOfTuples = revisionObject.listOfTuples

            listOfTuples.forEach {
                AutomaticallyGenerateUUID(
                    it.leftCompilationUnit?.path,
                    it.baseCompilationUnit?.path,
                    it.rightCompilationUnit?.path
                )
            }
//        }
    }
}