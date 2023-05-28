package mains

import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.TreeGenerators
import com.github.gumtreediff.gen.javaparser.JavaParserGenerator
import com.github.gumtreediff.io.LineReader
import com.github.gumtreediff.matchers.MappingStore
import com.github.gumtreediff.matchers.Matcher
import com.github.gumtreediff.matchers.Matchers
import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.Type
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import model.*
import model.visitors.EqualsUuidVisitor
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader


fun main() {
//    val leftFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_left_72812\\src\\org\\Stack.java"
//    val baseFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_base_0b29a\\src\\org\\Stack.java"
//    val rightFile = "C:\\Users\\André\\Desktop\\MEI\\Tese\\Merge Scenarios\\Shared Folder Scenarios\\sample\\temDeDarSemiStructuredConflict\\rev_right_e96b6\\src\\org\\Stack.java"
//
//    val leftLineReader = LineReader(FileReader(leftFile))
//    val baseLineReader = LineReader(FileReader(baseFile))
//    val rightLineReader = LineReader(FileReader(rightFile))
//
//    leftLineReader.readLines()
//    baseLineReader.readLines()
//    rightLineReader.readLines()
//
//    val leftProject = Project(leftFile, setupProject = false)
//    val baseProject = Project(baseFile, setupProject = false)
//    val rightProject = Project(rightFile, setupProject = false)
//
//    val leftCU = leftProject.getSetOfCompilationUnit().elementAt(0)
//    val baseCU = baseProject.getSetOfCompilationUnit().elementAt(0)
//    val rightCU = rightProject.getSetOfCompilationUnit().elementAt(0)
//
//    val leftGumTree = JavaParserGenerator().generateFrom().file(leftFile).root
//    val baseGumTree = JavaParserGenerator().generateFrom().file(baseFile).root
//    val rightGumTree = JavaParserGenerator().generateFrom().file(rightFile).root
//
//    val defaultMatcher = Matchers.getInstance().matcher // retrieves the default matcher
//    val mappingsLeftBase = defaultMatcher.match(leftGumTree, baseGumTree) // computes the mappings between the trees
//    val mappingsRightBase = defaultMatcher.match(rightGumTree, baseGumTree) // computes the mappings between the trees
//
//    val mapOfTypeToJavaClass = mutableMapOf<String, Class<out Node>>()
//    mapOfTypeToJavaClass["ClassOrInterfaceDeclaration"] = ClassOrInterfaceDeclaration::class.java
//    mapOfTypeToJavaClass["MethodDeclaration"] = MethodDeclaration::class.java
//    mapOfTypeToJavaClass["ConstructorDeclaration"] = ConstructorDeclaration::class.java
//    mapOfTypeToJavaClass["FieldDeclaration"] = FieldDeclaration::class.java
//
//    val allTypeNamesInMap = mapOfTypeToJavaClass.keys
//
//    val filteredLeftBaseSet = mappingsLeftBase.asSet().filter {
//        allTypeNamesInMap.any { key -> key ==  it.first.type.name}
//    }
//
//    val filteredRightBaseSet = mappingsRightBase.asSet().filter {
//        allTypeNamesInMap.any { key -> key ==  it.first.type.name}
//    }
//
//    filteredLeftBaseSet.forEach { mapping ->
//        val srcMappingNode = mapping.first
//        val dstMappingNode = mapping.second
//
//        val srcClassType = mapOfTypeToJavaClass[srcMappingNode.type.name]!!
//        val dstClassType = mapOfTypeToJavaClass[dstMappingNode.type.name]!!
//
//        val srcProjectNode = leftCU.findFirst(srcClassType) {
//            val begin = it.range.get().begin
//            leftLineReader.positionFor(begin.line, begin.column) == srcMappingNode.pos
//        }.get()
//
//        val dstProjectNode = baseCU.findFirst(dstClassType) {
//            val begin = it.range.get().begin
//            baseLineReader.positionFor(begin.line, begin.column) == dstMappingNode.pos
//        }.get()
//
//        srcProjectNode.setUUIDTo(dstProjectNode.uuid)
//    }
//
//    filteredRightBaseSet.forEach { mapping ->
//        val srcMappingNode = mapping.first
//        val dstMappingNode = mapping.second
//
//        val srcClassType = mapOfTypeToJavaClass[srcMappingNode.type.name]!!
//        val dstClassType = mapOfTypeToJavaClass[dstMappingNode.type.name]!!
//
//        val srcProjectNode = rightCU.findFirst(srcClassType) {
//            val begin = it.range.get().begin
//            rightLineReader.positionFor(begin.line, begin.column) == srcMappingNode.pos
//        }.get()
//
//        val dstProjectNode = baseCU.findFirst(dstClassType) {
//            val begin = it.range.get().begin
//            baseLineReader.positionFor(begin.line, begin.column) == dstMappingNode.pos
//        }.get()
//
//        srcProjectNode.setUUIDTo(dstProjectNode.uuid)
//    }
//
//    println(leftCU)
//    println(baseCU)
//    println(rightCU)
//
//    val factoryOfTransformationsMergedBranch = FactoryOfTransformations(baseProject, rightProject)
//    val factoryOfTransformationsBranchToBeMerged = FactoryOfTransformations(baseProject, leftProject)
//
//    applyTransformationsTo(baseProject, factoryOfTransformationsMergedBranch, true)
//    applyTransformationsTo(baseProject, factoryOfTransformationsBranchToBeMerged, true)
//
//    println(baseCU)
}

