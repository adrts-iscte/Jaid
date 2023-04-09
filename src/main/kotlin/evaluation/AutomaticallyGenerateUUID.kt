package evaluation

import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator
import com.github.gumtreediff.io.LineReader
import com.github.gumtreediff.matchers.Mapping
import com.github.gumtreediff.matchers.Matchers
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import model.path
import model.setUUIDTo
import model.uuid
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

class AutomaticallyGenerateUUID(private val leftCU : CompilationUnit,
                                private val baseCU: CompilationUnit,
                                private val rightCU : CompilationUnit) {

    private val leftPath = leftCU.path
    private val basePath = baseCU.path
    private val rightPath = rightCU.path

    private val leftLineReader = LineReader(FileReader(leftPath))
    private val baseLineReader = LineReader(FileReader(basePath))
    private val rightLineReader = LineReader(FileReader(rightPath))

    private val leftGumTree = JavaParserGenerator().generateFrom().file(leftPath).root
    private val baseGumTree = JavaParserGenerator().generateFrom().file(basePath).root
    private val rightGumTree = JavaParserGenerator().generateFrom().file(rightPath).root

    init {
        automaticallyGenerateFilesWithUUID()
    }

    companion object {
        private val mapOfTypeToJavaClass = mapOf<String, Class<out Node>>(
            "ClassOrInterfaceDeclaration" to ClassOrInterfaceDeclaration::class.java,
            "MethodDeclaration" to MethodDeclaration::class.java,
            "ConstructorDeclaration" to ConstructorDeclaration::class.java,
            "FieldDeclaration" to FieldDeclaration::class.java
        )
    }

    fun getLeftCompilationUnit() = leftCU

    fun getBaseCompilationUnit() = baseCU

    fun getRightCompilationUnit() = rightCU

    private fun automaticallyGenerateFilesWithUUID() {
        Run.initGenerators()

        leftLineReader.readLines()
        baseLineReader.readLines()
        rightLineReader.readLines()

        val defaultMatcher = Matchers.getInstance().matcher // retrieves the default matcher
        val mappingsLeftBase = defaultMatcher.match(leftGumTree, baseGumTree) // computes the mappings between the trees
        val mappingsRightBase = defaultMatcher.match(rightGumTree, baseGumTree) // computes the mappings between the trees

        val allTypeNamesInMap = mapOfTypeToJavaClass.keys

        val filteredLeftBaseSet = mappingsLeftBase.asSet().filter {
            allTypeNamesInMap.any { key -> key ==  it.first.type.name}
        }

        val filteredRightBaseSet = mappingsRightBase.asSet().filter {
            allTypeNamesInMap.any { key -> key ==  it.first.type.name}
        }

        setUUIDsToSimilarNodes(filteredLeftBaseSet, leftCU, baseCU, leftLineReader, baseLineReader)
        setUUIDsToSimilarNodes(filteredRightBaseSet, rightCU, baseCU, rightLineReader, baseLineReader)
    }

    private fun setUUIDsToSimilarNodes(filteredSet : List<Mapping>,
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

            srcProjectNode.setUUIDTo(dstProjectNode.uuid)
        }
    }

    fun writeFiles() {
        val filename = File(leftPath).nameWithoutExtension
        writeCompilationUnitInFile(File(leftPath).parent + "\\${filename}_generated.java", leftCU)
        writeCompilationUnitInFile(File(basePath).parent + "\\${filename}_generated.java", baseCU)
        writeCompilationUnitInFile(File(rightPath).parent + "\\${filename}_generated.java", rightCU)
    }

    private fun writeCompilationUnitInFile(filename : String, compilationUnit: CompilationUnit) {
        val file = File(filename)
        if (!file.exists()) file.createNewFile()
        file.writeText(compilationUnit.toString(), Charset.defaultCharset())
    }
}

