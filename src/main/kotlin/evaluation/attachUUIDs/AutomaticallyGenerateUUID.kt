package evaluation.attachUUIDs

import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator
import com.github.gumtreediff.io.LineReader
import com.github.gumtreediff.matchers.Mapping
import com.github.gumtreediff.matchers.Matchers
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.ObjectCreationExpr
import model.setUUIDTo
import model.uuid
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset


class AutomaticallyGenerateUUID(private val leftPath : String?,
                                private val basePath: String?,
                                private val rightPath : String?) {

    private val leftLineReader = leftPath?.let { LineReader( FileReader(it)) }
    private val baseLineReader = basePath?.let { LineReader(FileReader(it)) }
    private val rightLineReader = rightPath?.let { LineReader(FileReader(it)) }

    private val leftCU = leftPath?.let { StaticJavaParser.parse(leftLineReader) as CompilationUnit }
    private val baseCU = basePath?.let { StaticJavaParser.parse(baseLineReader) as CompilationUnit }
    private val rightCU = rightPath?.let { StaticJavaParser.parse(rightLineReader) as CompilationUnit }

//    private val leftGumTree = JavaParserGenerator().generateFrom().file(leftPath).root
//    private val baseGumTree = JavaParserGenerator().generateFrom().file(basePath).root
//    private val rightGumTree = JavaParserGenerator().generateFrom().file(rightPath).root

    init {
//        transformInOldFiles()
        automaticallyGenerateFilesWithUUID()
        writeFiles()
    }

    companion object {
        private val mapOfTypeToJavaClass = mapOf<String, Class<out Node>>(
            "ClassOrInterfaceDeclaration" to ClassOrInterfaceDeclaration::class.java,
            "MethodDeclaration" to MethodDeclaration::class.java,
            "ConstructorDeclaration" to ConstructorDeclaration::class.java,
            "FieldDeclaration" to FieldDeclaration::class.java
        )
    }

    private fun transformInOldFiles() {
//        val leftFile = File(leftPath)
//        val baseFile = File(basePath)
//        val rightFile = File(rightPath)
//
//        val oldLeftFile = File(leftPath.replace(".java", ".old"))
//        val oldBaseFile = File(baseFile.parent, "${baseFile.nameWithoutExtension}.old")
//        val oldRightFile = File(rightFile.parent, "${rightFile.nameWithoutExtension}.old")
//
//        Files.deleteIfExists(Paths.get(oldLeftFile.path))
//        oldLeftFile.createNewFile()
//        oldLeftFile.writeText(leftFile.readText(Charset.defaultCharset()), Charset.defaultCharset())
//
//        Files.deleteIfExists(Paths.get(oldBaseFile.path))
//        oldBaseFile.createNewFile()
//        oldBaseFile.writeText(baseFile.readText(Charset.defaultCharset()), Charset.defaultCharset())
//
//        Files.deleteIfExists(Paths.get(oldRightFile.path))
//        oldRightFile.createNewFile()
//        oldRightFile.writeText(rightFile.readText(Charset.defaultCharset()), Charset.defaultCharset())
    }

    private fun automaticallyGenerateFilesWithUUID() {
        Run.initGenerators()

        val defaultMatcher = Matchers.getInstance().matcher // retrieves the default matcher

        val allTypeNamesInMap = mapOfTypeToJavaClass.keys
        basePath?.let {
            val baseGumTree = JavaParserGenerator().generateFrom().file(basePath).root

            leftPath?.let {
//                val stringLeftCU = leftCU.toString()
                val leftGumTree = JavaParserGenerator().generateFrom().file(leftPath).root
//                val leftLineReader = LineReader(StringReader(stringLeftCU))
//                leftLineReader.readLines()
//                leftCU = StaticJavaParser.parse(leftLineReader) as CompilationUnit

                val mappingsLeftBase = defaultMatcher.match(leftGumTree, baseGumTree)

                val filteredLeftBaseSet = mappingsLeftBase.asSet().filter {
                    allTypeNamesInMap.any { key -> key == it.first.type.name }
                }.toSet()

                setUUIDsToSimilarNodes(filteredLeftBaseSet, leftCU!!, baseCU!!, leftLineReader!!, baseLineReader!!)
            }

            rightPath?.let {
//                val stringRightCU = rightCU.toString()
                val rightGumTree = JavaParserGenerator().generateFrom().file(rightPath).root
//                val rightLineReader = LineReader(StringReader(stringRightCU))
//                rightLineReader.readLines()
//                rightCU = StaticJavaParser.parse(rightLineReader) as CompilationUnit

                val mappingsRightBase = defaultMatcher.match(rightGumTree, baseGumTree)

                val filteredRightBaseSet = mappingsRightBase.asSet().filter {
                    allTypeNamesInMap.any { key -> key == it.first.type.name }
                }.toSet()

                setUUIDsToSimilarNodes(filteredRightBaseSet, rightCU!!, baseCU!!, rightLineReader!!, baseLineReader!!)
            }
        }
    }

    private fun setUUIDsToSimilarNodes(filteredSet : Set<Mapping>,
                                       srcCU : CompilationUnit, dstCU : CompilationUnit,
                                       srcLineReader: LineReader, dstLineReader: LineReader) {
        filteredSet.forEach { mapping ->
            val srcMappingNode = mapping.first
            val dstMappingNode = mapping.second

//            if (srcMappingNode.parent.type.name != "EnumDeclaration" &&
//                dstMappingNode.parent.type.name != "EnumDeclaration" &&
//                srcMappingNode.parent.type.name != "ObjectCreationExpr" &&
//                dstMappingNode.parent.type.name != "ObjectCreationExpr" &&
//                !(srcMappingNode.type.name == "ClassOrInterfaceDeclaration" && srcMappingNode.parent.type.name == "ClassOrInterfaceDeclaration" ) &&
//                !(dstMappingNode.type.name == "ClassOrInterfaceDeclaration" && dstMappingNode.parent.type.name == "ClassOrInterfaceDeclaration" )) {

//            if (!(srcMappingNode.type.name == "MethodDeclaration" && srcMappingNode.parent.type.name == "MethodDeclaration" ) &&
//                !(dstMappingNode.type.name == "MethodDeclaration" && dstMappingNode.parent.type.name == "MethodDeclaration" )) {
                val srcClassType = mapOfTypeToJavaClass[srcMappingNode.type.name]!!
                val dstClassType = mapOfTypeToJavaClass[dstMappingNode.type.name]!!

//                println(srcMappingNode.toString())
//                println(dstMappingNode.toString())

//                try {
                val srcProjectNode = srcCU.findFirst(srcClassType) {
                    val begin = it.range.get().begin
                    srcLineReader.positionFor(begin.line, begin.column) == srcMappingNode.pos
                }.get()
//                    if (!srcProjectNode.isPresent) {
//                        println()
//                    }
//                } catch (e: NoSuchElementException) {
//                    println(e)
//                }

//                try {
                val dstProjectNode = dstCU.findFirst(dstClassType) {
                    val begin = it.range.get().begin
                    dstLineReader.positionFor(begin.line, begin.column) == dstMappingNode.pos
                }.get()
//                    if (!dstProjectNode.isPresent) {
//                        println()
//                    }
//                } catch (e: NoSuchElementException) {
//                    println(e)
//                }

                val srcProjectNodeParent = srcProjectNode.parentNode.orElse(null)
                val dstProjectNodeParent = dstProjectNode.parentNode.orElse(null)

                if (!(srcProjectNode is MethodDeclaration && srcProjectNodeParent!= null && srcProjectNodeParent is ObjectCreationExpr)) {
                    srcProjectNode.setUUIDTo(dstProjectNode.uuid)
                }
            }
//        }
    }

    private fun writeFiles() {
        leftPath?.let {
            writeCompilationUnitInFile(leftPath, leftCU!!)
        }
        basePath?.let {
            writeCompilationUnitInFile(basePath, baseCU!!)
        }
        rightPath?.let {
            writeCompilationUnitInFile(rightPath, rightCU!!)
        }
    }

    private fun writeCompilationUnitInFile(filename : String, compilationUnit: CompilationUnit) {
        val file = File(filename)
        if (!file.exists()) file.createNewFile()
        file.writeText(compilationUnit.toString(), Charset.defaultCharset())
    }
}

