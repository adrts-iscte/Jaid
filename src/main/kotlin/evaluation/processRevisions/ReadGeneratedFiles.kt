package evaluation.processRevisions

import evaluation.attachUUIDs.FilesManager
import model.*
import model.transformations.BodyChangedCallable
import model.transformations.Transformation
import model.visitors.EqualsUuidVisitor
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

fun main() {
//    val leftPath = "src\\main\\resources\\repositories\\Bukkit\\rev_01a273d9_069df1b6\\identified_rev_left_01a27\\src"
//    val basePath = "src\\main\\resources\\repositories\\Bukkit\\rev_01a273d9_069df1b6\\identified_rev_base_069df\\src"
//    val rightPath = "src\\main\\resources\\repositories\\Bukkit\\rev_01a273d9_069df1b6\\identified_rev_right_069df\\src"
    val projectName = "jsoup"
    val saveMergedFiles = false

    val dir = "src/main/resources/repositories/${projectName}".replace("\\","/")

    val listOfAllFiles = FilesManager.listOfAllFiles(dir)

    var csvContent = "Revision;NumberOfTransformationsLeftBase;NumberOfBodyChangedCallableTransLeft;NumberOfTransformationsRightBase;NumberOfBodyChangedCallableTransRight;NumberOfConflicts;WholeProcessExecutionTime;MergeProcessOnlyExecutionTime\n"
    val listOfAllRevisionFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".identified_revisions") }

    if (saveMergedFiles) File("$dir/MergedRevisions/").deleteRecursively()
    listOfAllRevisionFiles.forEach { revisionFile ->
        println("A ver revision file: ${revisionFile.name}")
        if (revisionFile.nameWithoutExtension.contains("c1bd")){

            val revisionFileFolder : String
            val listOfTransformationsRight : Set<Transformation>
            val listOfTransformationsLeft : Set<Transformation>

            val numberOfBodyChangedCallableRight : Int
            val numberOfBodyChangedCallableLeft : Int

            val setOfConflicts : Set<Conflict>
            var mergeProcessExecutionTime : Long = 0

            val wholeProcessExecutionTime = measureTimeMillis {
            val revisionFilePath = revisionFile.path
            val reader = Files.newBufferedReader(Paths.get(revisionFilePath, *arrayOfNulls(0)))
            val listRevisions = reader.lines().toList().toMutableList()
            require(listRevisions.size == 3) { "Invalid .revisions file!" }

            revisionFileFolder = File(revisionFilePath).parent

            val leftPath = revisionFileFolder + File.separator + listRevisions[0]
            val basePath = revisionFileFolder + File.separator + listRevisions[1]
            val rightPath = revisionFileFolder + File.separator + listRevisions[2]

            val left = Project(leftPath, setupProject = true)
            val base = Project(basePath, setupProject = true)
            val right = Project(rightPath, setupProject = true)

            val factoryOfTransformationsRight = FactoryOfTransformations(base, right)
            listOfTransformationsRight = factoryOfTransformationsRight.getListOfAllTransformations().toSet()
            val factoryOfTransformationsLeft = FactoryOfTransformations(base, left)
            listOfTransformationsLeft = factoryOfTransformationsLeft.getListOfAllTransformations().toSet()

            numberOfBodyChangedCallableRight = listOfTransformationsRight.filterIsInstance<BodyChangedCallable>().size
            numberOfBodyChangedCallableLeft = listOfTransformationsLeft.filterIsInstance<BodyChangedCallable>().size

            setOfConflicts = getConflicts(base, listOfTransformationsRight, listOfTransformationsLeft)

            if (setOfConflicts.isNotEmpty()) {
                println("Number of Conflicts: ${setOfConflicts.size}")
            } else {
                println("No conflicts!")
            }

//            val numberOfBodyChangedBodyChangedConflicts = setOfConflicts.filter { it.getConflictType().getFirst() == BodyChangedCallable::class && it.getConflictType().getSecond() == BodyChangedCallable::class}.size
//            val bodyChangedBodyChangedConflicts = setOfConflicts.filter { it.getConflictType().getFirst() == BodyChangedCallable::class && it.getConflictType().getSecond() == BodyChangedCallable::class}

//            bodyChangedBodyChangedConflicts.forEach {
//                val firstTrans = it.first as BodyChangedCallable
//                val secondTrans = it.second as BodyChangedCallable
//
//                println(EqualsUuidVisitor(left,right).equals(firstTrans.getNewBody(), secondTrans.getNewBody()))
//            }

            if (saveMergedFiles && setOfConflicts.isEmpty() &&
                (listOfTransformationsLeft.isNotEmpty() || listOfTransformationsRight.isNotEmpty())) {

                mergeProcessExecutionTime = measureTimeMillis {
                    val mergedProject = merge(base, factoryOfTransformationsLeft, factoryOfTransformationsRight)

                    val destinyPath = Paths.get("$dir/MergedRevisions/${revisionFile.nameWithoutExtension}/")
                    Files.createDirectories(destinyPath)
                    mergedProject.saveProjectTo(destinyPath)
                }
            }
            }
//            csvContent += "${File(revisionFileFolder).name};${listOfTransformationsLeft.size};$numberOfBodyChangedCallableLeft;${listOfTransformationsRight.size};$numberOfBodyChangedCallableRight;${setOfConflicts.size};$numberOfBodyChangedBodyChangedConflicts\n"
                 csvContent += "${File(revisionFileFolder).name};${listOfTransformationsLeft.size};$numberOfBodyChangedCallableLeft;${listOfTransformationsRight.size};$numberOfBodyChangedCallableRight;${setOfConflicts.size};$wholeProcessExecutionTime;$mergeProcessExecutionTime\n"
//            csvContent += "${File(revisionFileFolder).name};${listOfTransformationsLeft.size};$numberOfBodyChangedCallableLeft;${listOfTransformationsRight.size};$numberOfBodyChangedCallableRight;${setOfConflicts.size};\n"
        }
    }
    writeFile(csvContent, "${projectName}Transformation&Conflicts.csv")
}

private fun writeFile(content : String, filename : String) {
    val file = File(filename)
    if (!file.exists()) file.createNewFile()
    file.writeText(content, Charset.defaultCharset())
}