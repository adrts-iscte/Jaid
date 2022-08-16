import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import model.visitors.FieldUsesVisitor
import java.io.File

fun main() {
    val base = StaticJavaParser.parse(File("src/main/kotlin/scenarios.sequenceTransformations/base/SequenceTransformationsBaseClass.java"))

    val listOfFieldUses = mutableListOf<Node>()
    val fieldUsesVisitor = FieldUsesVisitor("attribute")
    base.accept(fieldUsesVisitor, listOfFieldUses)

    val solver = CombinedTypeSolver()
    val jpf = JavaParserFacade.get(solver).solve(listOfFieldUses[1] as NameExpr)
    println(listOfFieldUses)
}