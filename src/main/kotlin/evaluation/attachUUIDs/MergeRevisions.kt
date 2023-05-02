package evaluation.attachUUIDs

import com.github.javaparser.ast.CompilationUnit
import model.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

class Revision(revisionsPath: String) {

    val revisionFileFolder : String

    val leftPath : String
    val basePath : String
    val rightPath : String

    val leftProj : Project
    val baseProj : Project
    val rightProj : Project

    val listOfTuples = mutableListOf<FilesTuple>()

    init {
        val reader = Files.newBufferedReader(Paths.get(revisionsPath, *arrayOfNulls(0)))
        val listRevisions = reader.lines().toList().toMutableList()
        require(listRevisions.size == 3) { "Invalid .revisions file!" }

        this.revisionFileFolder = File(revisionsPath).parent

        this.leftPath = revisionFileFolder + File.separator + listRevisions[0]
        this.basePath = revisionFileFolder + File.separator + listRevisions[1]
        this.rightPath = revisionFileFolder + File.separator + listRevisions[2]

        this.leftProj = Project(leftPath, setupProject = false)
        this.baseProj = Project(basePath, setupProject = false)
        this.rightProj = Project(rightPath, setupProject = false)

        val mergedDirectories = mergeDirectories(leftPath, leftProj.getSetOfCompilationUnit(),
                                                basePath, baseProj.getSetOfCompilationUnit(),
                                                rightPath, rightProj.getSetOfCompilationUnit())

        listOfTuples.addAll(mergedDirectories//.filter { it.leftCompilationUnit != null &&
//                        it.baseCompilationUnit != null &&
//                        it.rightCompilationUnit != null &&
//                        (it.leftCompilationUnit != it.baseCompilationUnit ||
//                        it.baseCompilationUnit != it. rightCompilationUnit)
//                        (!EqualsUuidVisitor.equals(it.leftCompilationUnit, it.baseCompilationUnit) ||
//                        !EqualsUuidVisitor.equals(it.baseCompilationUnit, it.rightCompilationUnit))
                                )
    }

    private fun mergeDirectories(leftPath: String, leftCUs: MutableSet<CompilationUnit>,
                                 basePath: String, baseCUs: MutableSet<CompilationUnit>,
                                 rightPath: String, rightCUs: MutableSet<CompilationUnit>): List<FilesTuple> {
        return FilesManager.fillFilesTuples(leftPath, leftCUs, basePath, baseCUs, rightPath, rightCUs, mutableListOf())
    }
}

//object MergeRevisions {
//
//    fun mergeRevision(revisionsPath: String): List<FilesTuple> {
//        val reader = Files.newBufferedReader(Paths.get(revisionsPath, *arrayOfNulls(0)))
//        val listRevisions = reader.lines().toList().toMutableList()
//        if (listRevisions.size != 3) throw Exception("Invalid .revisions file!")
//
//        val revisionFileFolder = File(revisionsPath).parent
//
//        val leftPath = revisionFileFolder.toString() + File.separator + listRevisions[0]
//        val basePath = revisionFileFolder.toString() + File.separator + listRevisions[1]
//        val rightPath = revisionFileFolder.toString() + File.separator + listRevisions[2]
//
//        val leftProj = Project(leftPath, setupProject = false)
//        val baseProj = Project(basePath, setupProject = false)
//        val rightProj = Project(rightPath, setupProject = false)
//
//        return mergeDirectories(leftPath, leftProj.getSetOfCompilationUnit(),
//                                basePath, baseProj.getSetOfCompilationUnit(),
//                                rightPath, rightProj.getSetOfCompilationUnit())
//    }
//
//    private fun mergeDirectories(leftPath: String, leftCUs: MutableSet<CompilationUnit>,
//                                 basePath: String, baseCUs: MutableSet<CompilationUnit>,
//                                 rightPath: String, rightCUs: MutableSet<CompilationUnit>): List<FilesTuple> {
//        return FilesManager.fillFilesTuples(leftPath, leftCUs, basePath, baseCUs, rightPath, rightCUs, mutableListOf())
//    }
//
//}