package evaluation.processRevisions

import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import evaluation.attachUUIDs.FilesManager
import model.*
import model.conflictDetection.Conflict
import model.detachRedundantTransformations.RedundancyFreeSetOfTransformations
import model.transformations.Transformation
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

fun main() {
    val projectName = "jsoup"
    val doMerge = true
    val saveMergedFiles = true

//    val specificRevision = true
    val specificRevision = false

    val dir = "src/main/resources/repositories/${projectName}".replace("\\","/")

    val listOfAllFiles = FilesManager.listOfAllFiles(dir)

    var csvContent = "Revision;NumberOfLOC;NumberOfNonEmptyLOC;TotalNumberOfReferences;WholeProcessExecutionTime;ParsingAndIndexingOnlyExecutionTime;ExtractingTransformationsExecutionTime;MergeProcessOnlyExecutionTime\n"
    val listOfAllRevisionFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".identified_revisions") }

    if (saveMergedFiles) File("$dir/MergedRevisions/").deleteRecursively()
    listOfAllRevisionFiles.forEach { revisionFile ->
        println("A ver revision file: ${revisionFile.name}")
        JavaParserFacade.clearInstances()
        if (!specificRevision || revisionFile.nameWithoutExtension.contains("rev_ba41f25b_0ceb400c")){
            val revisionFileFolder : String
            val listOfTransformationsRight : Set<Transformation>
            val listOfTransformationsLeft : Set<Transformation>

            val setOfConflicts : Set<Conflict>
            var mergeProcessExecutionTime : Long = 0
            val parsingAndIndexingExecutionTime : Long
            val extractingTransformationsExecutionTime : Long

            val revisionFilePath = revisionFile.path
            val reader = Files.newBufferedReader(Paths.get(revisionFilePath, *arrayOfNulls(0)))
            val listRevisions = reader.lines().toList().toMutableList()
            require(listRevisions.size == 3) { "Invalid .revisions file!" }

            revisionFileFolder = File(revisionFilePath).parent

            val leftPath = revisionFileFolder + File.separator + listRevisions[0]
            val basePath = revisionFileFolder + File.separator + listRevisions[1]
            val rightPath = revisionFileFolder + File.separator + listRevisions[2]

            val locLeft = calculateLOCofPath(leftPath)
            val numberOfLOCLeft = locLeft.size
            val numberOfNonEmptyLOCLeft = locLeft.filter { it.isNotEmpty() }.size

            val locBase = calculateLOCofPath(basePath)
            val numberOfLOCBase = locBase.size
            val numberOfNonEmptyLOCBase = locBase.filter { it.isNotEmpty() }.size

            val locRight = calculateLOCofPath(rightPath)
            val numberOfLOCRight = locRight.size
            val numberOfNonEmptyLOCRight = locRight.filter { it.isNotEmpty() }.size

            val totalNumberOfLOC = numberOfLOCLeft + numberOfLOCBase + numberOfLOCRight
            val totalNumberOfNonEmptyLOC = numberOfNonEmptyLOCLeft + numberOfNonEmptyLOCBase + numberOfNonEmptyLOCRight

            val totalNumberOfReferences : Int

            val wholeProcessExecutionTime = measureTimeMillis {
            val left : Project
            val base : Project
            val right : Project

            parsingAndIndexingExecutionTime = measureTimeMillis {
                left = Project(leftPath)
                base = Project(basePath)
                right = Project(rightPath)
            }

            totalNumberOfReferences = left.getTotalNumberOfReferences() + base.getTotalNumberOfReferences() + right.getTotalNumberOfReferences()

            val factoryOfTransformationsRight : FactoryOfTransformations
            val factoryOfTransformationsLeft : FactoryOfTransformations

            extractingTransformationsExecutionTime = measureTimeMillis {
                factoryOfTransformationsRight = FactoryOfTransformations(base, right)
                factoryOfTransformationsLeft = FactoryOfTransformations(base, left)
            }

            listOfTransformationsRight = factoryOfTransformationsRight.getListOfAllTransformations().toSet()
            listOfTransformationsLeft = factoryOfTransformationsLeft.getListOfAllTransformations().toSet()

            val redundancyFreeSetOfTransformations = RedundancyFreeSetOfTransformations(factoryOfTransformationsLeft, factoryOfTransformationsRight)
            setOfConflicts = getConflicts(base, redundancyFreeSetOfTransformations)

            if (setOfConflicts.isNotEmpty()) {
                println("Number of Conflicts: ${setOfConflicts.size}")
            } else {
                println("No conflicts!")
            }

            if (doMerge && setOfConflicts.isEmpty() && (listOfTransformationsLeft.isNotEmpty() || listOfTransformationsRight.isNotEmpty())) {
                mergeProcessExecutionTime = measureTimeMillis {
//                    val clonedBase = base.clone()
                    val mergedProject = merge(base, redundancyFreeSetOfTransformations)

                    val destinyPath = Paths.get("$dir/MergedRevisions/${revisionFile.nameWithoutExtension}/")
                    Files.createDirectories(destinyPath)
                    if (saveMergedFiles) {
                        mergedProject.saveProjectTo(destinyPath)
                    }
                }
            }
            }
        csvContent += "${File(revisionFileFolder).name};$totalNumberOfLOC;$totalNumberOfNonEmptyLOC;$totalNumberOfReferences;$wholeProcessExecutionTime;$parsingAndIndexingExecutionTime;$extractingTransformationsExecutionTime;$mergeProcessExecutionTime\n"
        }
    }
    writeFile(csvContent, "${projectName}LOC&ExecutionTimes.csv")
}

fun calculateLOCofPath(pathString : String) : MutableList<String> {
    val listOfAllFiles = FilesManager.listOfAllFiles(pathString)
    val javaFiles = listOfAllFiles.filter { it.isFile && it.name.endsWith(".java") }
    val allLinesOfCode = mutableListOf<String>()
    javaFiles.forEach {
        val reader = Files.newBufferedReader(Paths.get(it.path, *arrayOfNulls(0)))
        allLinesOfCode.addAll(reader.lines().toList())
    }
    return allLinesOfCode
}

private fun writeFile(content : String, filename : String) {
    val file = File(filename)
    if (!file.exists()) file.createNewFile()
    file.writeText(content, Charset.defaultCharset())
}