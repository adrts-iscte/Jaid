import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.type.Type
import com.github.javaparser.printer.configuration.ConfigurationOption
import com.github.javaparser.printer.configuration.DefaultConfigurationOption
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration

interface Transformation {
    fun applyTransformation(cu: CompilationUnit)
    fun getNode() : Node
    fun getText(): String
}