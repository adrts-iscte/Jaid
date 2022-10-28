import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.uuid
import java.io.File

fun main() {
    StaticJavaParser.setConfiguration(ParserConfiguration().setDoNotAssignCommentsPrecedingEmptyLines(false))
    /*val src = StaticJavaParser.parse(File("src/main/kotlin/scenarios/methodTransformations/base/MethodTransformationsBaseClass.java"))
    val dec : ClassOrInterfaceDeclaration = src.getClassByName(src.primaryTypeName.get()).get()

    val field = src.findFirst(NameExpr::class.java).get()
    println(field)
    val solver = CombinedTypeSolver()
    val jpf = JavaParserFacade.get(solver).solve(field)
    if (jpf.isSolved){
        val methodDecl = jpf.correspondingDeclaration
        println("  -> $methodDecl")
    }

    val method = src.findFirst(MethodDeclaration::class.java).get()
    println("Método " + method)

    val callsMap = mutableMapOf<MethodDeclaration, MutableList<MethodCallExpr>>()

    src.findAll(MethodCallExpr::class.java).stream().forEach { methodCall ->
        val solver = CombinedTypeSolver()
        val jpf = JavaParserFacade.get(solver).solve(methodCall)
        if (jpf.isSolved) {
            val methodDecl = (jpf.correspondingDeclaration as JavaParserMethodDeclaration).wrappedNode
//            println("  -> $methodDecl")
            callsMap.getOrPut(methodDecl) { mutableListOf() }.add(methodCall)
        }
    }

    println("Mapa: $callsMap")

    renameMethod(SimpleName("method"), "renamedMethod", callsMap)

//    val codigo = dec.toString()

    val method = src.findFirst(MethodDeclaration::class.java).get()
    println(method.uuid)*/
//    println(codigo)
}

fun renameMethod (methodName: SimpleName,
                  newMethodName: String,
                  callsMap: MutableMap<MethodDeclaration, MutableList<MethodCallExpr>>) {
    val methodToRename = callsMap.keys.find { it.name == methodName }
    methodToRename?.let {
        callsMap[methodToRename]!!.forEach {
            it.setName(newMethodName)
        }
        methodToRename.setName(newMethodName)
    }
}

