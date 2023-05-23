package evaluation.attachUUIDs

import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator
import com.github.gumtreediff.io.LineReader
import com.github.gumtreediff.matchers.Mapping
import com.github.gumtreediff.matchers.Matchers
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
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

    init {
        automaticallyGenerateFilesWithUUID()
        writeFiles()
    }

    companion object {
        private val mapOfTypeToJavaClass = mapOf<String, Class<out Node>>(
            "ClassOrInterfaceDeclaration" to ClassOrInterfaceDeclaration::class.java,
            "MethodDeclaration" to MethodDeclaration::class.java,
            "ConstructorDeclaration" to ConstructorDeclaration::class.java,
            "FieldDeclaration" to FieldDeclaration::class.java,
            "EnumDeclaration" to EnumDeclaration::class.java,
            "EnumConstantDeclaration" to EnumConstantDeclaration::class.java,
        )
    }

    private fun automaticallyGenerateFilesWithUUID() {
        Run.initGenerators()

        val defaultMatcher = Matchers.getInstance().matcher

        val allTypeNamesInMap = mapOfTypeToJavaClass.keys
        basePath?.let {
            val baseGumTree = JavaParserGenerator().generateFrom().file(basePath).root

            leftPath?.let {
                val leftGumTree = JavaParserGenerator().generateFrom().file(leftPath).root

                val mappingsLeftBase = defaultMatcher.match(leftGumTree, baseGumTree)

                val filteredLeftBaseSet = mappingsLeftBase.asSet().filter {
                    allTypeNamesInMap.any { key -> key == it.first.type.name }
                }.toSet()

                setUUIDsToSimilarNodes(filteredLeftBaseSet, leftCU!!, baseCU!!, leftLineReader!!, baseLineReader!!)
            }

            rightPath?.let {
                val rightGumTree = JavaParserGenerator().generateFrom().file(rightPath).root

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

            val srcClassType = mapOfTypeToJavaClass[srcMappingNode.type.name]!!
            val dstClassType = mapOfTypeToJavaClass[dstMappingNode.type.name]!!

            val srcProjectNode = srcCU.findFirst(srcClassType) {
                val begin = it.range.get().begin
                srcLineReader.positionFor(begin.line, begin.column) == srcMappingNode.pos
            }.get()

            val dstProjectNode = dstCU.findFirst(dstClassType) {
                val begin = it.range.get().begin
                dstLineReader.positionFor(begin.line, begin.column) == dstMappingNode.pos
            }.get()

            val srcProjectNodeParent = srcProjectNode.parentNode.orElse(null)

            if (!(srcProjectNode is MethodDeclaration && srcProjectNodeParent!= null && srcProjectNodeParent is ObjectCreationExpr)) {
                srcProjectNode.setUUIDTo(dstProjectNode.uuid)
            }
        }
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

