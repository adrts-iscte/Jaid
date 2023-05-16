package evaluation.attachUUIDs

import com.github.javaparser.ast.CompilationUnit
import model.path
import java.io.*
import java.util.*

object FilesManager{

    fun fillFilesTuples(leftDirectory: String, leftCUs: MutableSet<CompilationUnit>,
                        baseDirectory: String, baseCUs: MutableSet<CompilationUnit>,
                        rightDirectory: String, rightCUs: MutableSet<CompilationUnit>, visitedPaths: MutableList<String?>): List<FilesTuple> {

        val visitedPath = leftDirectory + baseDirectory + rightDirectory
        visitedPaths.add(visitedPath)

        val tuples = mutableListOf<FilesTuple>()

        val filesFromLeft = LinkedList(listFiles(leftDirectory))
        val filesFromBase = LinkedList(listFiles(baseDirectory))
        val filesFromRight = LinkedList(listFiles(rightDirectory))
        for (l in filesFromLeft) {
            val leftFile = File(l)
            var baseFile: File? = File(baseDirectory + File.separator + leftFile.name)
            var rightFile: File? = File(rightDirectory + File.separator + leftFile.name)

            if (!baseFile!!.exists()) baseFile = null
            if (!rightFile!!.exists()) rightFile = null

            val leftCompilationUnit = leftCUs.find { it.path == leftFile.path }
            val baseCompilationunit = if (baseFile != null) baseCUs.find { it.path == baseFile!!.path } else null
            val rightCompilationunit = if (rightFile != null) rightCUs.find { it.path == rightFile!!.path } else null

            val tp = FilesTuple(leftCompilationUnit, baseCompilationunit, rightCompilationunit)

            if (!tuples.contains(tp))
                tuples.add(tp)
        }

        for (b in filesFromBase) {
            val baseFile = File(b)
            var leftFile: File? = File(leftDirectory + File.separator + baseFile.name)
            var rightFile: File? = File(rightDirectory + File.separator + baseFile.name)

            if (!leftFile!!.exists()) leftFile = null
            if (!rightFile!!.exists()) rightFile = null

            val leftCompilationUnit = if (leftFile != null) leftCUs.find { it.path == leftFile!!.path } else null
            val baseCompilationunit = baseCUs.find { it.path == baseFile.path }
            val rightCompilationunit = if (rightFile != null) rightCUs.find { it.path == rightFile!!.path } else null

            val tp = FilesTuple(leftCompilationUnit, baseCompilationunit, rightCompilationunit)

            if (!tuples.contains(tp)) tuples.add(tp)
        }
        for (r in filesFromRight) {
            val rightFile = File(r)
            var baseFile: File? = File(baseDirectory + File.separator + rightFile.name)
            var leftFile: File? = File(leftDirectory + File.separator + rightFile.name)

            if (!baseFile!!.exists()) baseFile = null
            if (!leftFile!!.exists()) leftFile = null
            val leftCompilationUnit = if (leftFile != null) leftCUs.find { it.path == leftFile.path } else null
            val baseCompilationunit = if (baseFile != null) baseCUs.find { it.path == baseFile.path } else null
            val rightCompilationunit = rightCUs.find { it.path == rightFile.path }

            val tp = FilesTuple(leftCompilationUnit, baseCompilationunit, rightCompilationunit)

            if (!tuples.contains(tp)) tuples.add(tp)
        }

        val subdirectoriesFromLeft = LinkedList(listDirectories(leftDirectory))
        val subdirectoriesFromBase = LinkedList(listDirectories(baseDirectory))
        val subdirectoriesFromRight = LinkedList(listDirectories(rightDirectory))
        for (sl in subdirectoriesFromLeft) {

            val foldername = File(sl).name

            if (!visitedPaths.contains(sl + baseDirectory + File.separator + foldername + rightDirectory + File.separator + foldername)) {
                val tps: List<FilesTuple> = fillFilesTuples(sl, leftCUs,
                    baseDirectory + File.separator + foldername, baseCUs,
                    rightDirectory + File.separator + foldername, rightCUs,
                    visitedPaths
                )
                tuples.removeAll(tps)
                tuples.addAll(tps)
            }
        }
        for (sb in subdirectoriesFromBase) {
            val foldername = File(sb).name

            if (!visitedPaths.contains(leftDirectory + File.separator + foldername + sb + rightDirectory + File.separator + foldername)) {
                val tps: List<FilesTuple> = fillFilesTuples(leftDirectory + File.separator + foldername, leftCUs,
                    sb, baseCUs,
                    rightDirectory + File.separator + foldername, rightCUs,
                    visitedPaths
                )
                tuples.removeAll(tps)
                tuples.addAll(tps)
            }
        }
        for (sr in subdirectoriesFromRight) {
            val foldername = File(sr).name

            if (!visitedPaths.contains(leftDirectory + File.separator + foldername + baseDirectory + File.separator + foldername + sr)) {
                val tps: List<FilesTuple> = fillFilesTuples(leftDirectory + File.separator + foldername, leftCUs,
                    baseDirectory + File.separator + foldername, baseCUs,
                    sr, rightCUs,
                    visitedPaths
                )
                tuples.removeAll(tps)
                tuples.addAll(tps)
            }
        }
        return tuples

    }

    fun listFiles(directory: String): List<String> {
        val allFiles: MutableList<String> = ArrayList()
        val fList = File(directory).listFiles()
        if (fList != null) {
            var b: Byte
            val i: Int
            var arrayOfFile: Array<File>
            i = fList.also { arrayOfFile = it }.size
            b = 0
            while (b < i) {
                val file = arrayOfFile[b.toInt()]
                if (file.isFile && file.name.lowercase(Locale.getDefault()).contains(".java"))
                    allFiles.add(file.absolutePath)
                b++
            }
        }
        return allFiles
    }

    fun listDirectories(directory: String): List<String> {
        val allFiles: MutableList<String> = ArrayList()
        val fList = File(directory).listFiles()
        if (fList != null) {
            var b: Byte
            val i: Int
            var arrayOfFile: Array<File>
            i = fList.also { arrayOfFile = it }.size
            b = 0
            while (b < i) {
                val file = arrayOfFile[b.toInt()]
                if (file.isDirectory)
                    allFiles.add(file.absolutePath)
                b++
            }
        }
        return allFiles
    }

    fun listOfAllFiles(path : String): MutableList<File> {
        val listOfAllFiles = mutableListOf<File>()
        File(path).walk().forEach { listOfAllFiles.add(it) }
        return listOfAllFiles
    }
}