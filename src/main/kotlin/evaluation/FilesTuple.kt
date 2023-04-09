package evaluation

import com.github.javaparser.ast.CompilationUnit
import java.io.File
import kotlin.io.path.absolutePathString

class FilesTuple(left: CompilationUnit?, base: CompilationUnit?, right: CompilationUnit?) {
    
    val leftCompilationUnit: CompilationUnit? = left
    val baseCompilationUnit: CompilationUnit? = base
    val rightCompilationUnit: CompilationUnit? = right

    override fun toString(): String {
        return """
            LEFT: ${if (leftCompilationUnit == null) "empty" else leftCompilationUnit.storage.get().path.absolutePathString()}
            BASE: ${if (baseCompilationUnit == null) "empty" else baseCompilationUnit.storage.get().path.absolutePathString()}
            RIGHT: ${if (rightCompilationUnit == null) "empty" else rightCompilationUnit.storage.get().path.absolutePathString()}
            """.trimIndent()
        
    }
      
    override fun equals(obj: Any?): Boolean {
        if (obj is FilesTuple) {
            val tp = obj
            val thisleftid = if (leftCompilationUnit != null) leftCompilationUnit.storage.get().path.absolutePathString() else ""
            val thisbaseid = if (baseCompilationUnit != null) baseCompilationUnit.storage.get().path.absolutePathString() else ""
            val thisrightid = if (rightCompilationUnit != null) rightCompilationUnit.storage.get().path.absolutePathString() else ""

            val otherleftid = if (tp.leftCompilationUnit != null) tp.leftCompilationUnit.storage.get().path.absolutePathString() else ""
            val otherbaseid = if (tp.baseCompilationUnit != null) tp.baseCompilationUnit.storage.get().path.absolutePathString() else ""
            val otherrightid = if (tp.rightCompilationUnit != null) tp.rightCompilationUnit.storage.get().path.absolutePathString() else ""
            
            return thisleftid == otherleftid && thisbaseid == otherbaseid && thisrightid == otherrightid
            
        }
        return false
        
    }

    override fun hashCode(): Int {
        var result = leftCompilationUnit?.hashCode() ?: 0
        result = 31 * result + (baseCompilationUnit?.hashCode() ?: 0)
        result = 31 * result + (rightCompilationUnit?.hashCode() ?: 0)
        return result
    }
}