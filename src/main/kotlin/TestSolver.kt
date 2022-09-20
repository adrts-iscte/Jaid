import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration
import com.github.javaparser.symbolsolver.resolution.SymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.visitors.FieldUsesVisitor
import java.io.File

fun main() {
    val solver = CombinedTypeSolver()
    StaticJavaParser.setConfiguration(ParserConfiguration().setSymbolResolver(JavaSymbolSolver(CombinedTypeSolver())))
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios/sequenceTransformations/base/FieldTransformationsBaseClass.java"))

    val listOfFieldUses = mutableListOf<Node>()
    val fieldUsesVisitor = FieldUsesVisitor("attribute")
    base.accept(fieldUsesVisitor, listOfFieldUses)


    val jpf = JavaParserFacade.get(solver).solve(listOfFieldUses[1] as NameExpr)
    val asd = (listOfFieldUses[1] as NameExpr).resolve()
    println(listOfFieldUses)
}