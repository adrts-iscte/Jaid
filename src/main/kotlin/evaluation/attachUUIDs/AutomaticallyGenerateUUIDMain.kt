package evaluation.attachUUIDs

import model.path

fun main() {
    val projectName = "jsoup"

    val dir = "src/main/resources/repositories/$projectName".replace("\\","/")
    val listOfAllFiles = FilesManager.listOfAllFiles(dir)
    val listOfAllRevisionFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".identified_revisions") }

    listOfAllRevisionFiles.forEach { revisionFile ->
//        if (revisionFile.nameWithoutExtension.contains("rev_04e634ea_5d55a7ed")) {
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