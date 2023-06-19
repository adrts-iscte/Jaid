package model

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment

class UUID(private val value : String) {

    init{
        require(value.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))) { "The UUID must be valid!" }
    }

    override fun toString() : String  = value

    override fun equals(other: Any?) = (other is UUID) && value == other.value

    override fun hashCode(): Int = value.hashCode()

}

val Node.uuid: UUID
    get() {
        val comment = this.comment.orElse(null) ?: return this.generateUUID()
        when (comment) {
            is LineComment -> {
                if (comment.content.trim().isValidUUID) {
                    return UUID(comment.content.trim())
                }
            }
            else -> {
                val content = comment.content.replace(Regex("(\\n){0,}(\\r){0,}(\\t){0,}"),"").trim().takeLast(36)
                if (content.isValidUUID) {
                    return UUID(content)
                }
            }
        }
        return this.generateUUID()
    }

fun Node.generateUUID() : UUID {
    val uuid = java.util.UUID.randomUUID().toString()
    val comment = this.comment.orElse(null)
    if (comment == null) {
        this.setLineComment(uuid)
    } else {
        when (comment) {
            is LineComment, is BlockComment -> {
                this.setComment(BlockComment(comment.content + "\n\t " + uuid))
            }
            else -> {
                this.setComment(JavadocComment(comment.content + "\n\t * " + uuid))
            }
        }
    }
    return UUID(uuid)
}

fun Node.setUUIDTo(uuid : UUID) {
    val comment = this.comment.orElse(null)
    if (comment == null) {
        this.setComment(LineComment(uuid.toString()))
    } else {
        val commentContent = comment.content
        if (commentContent.contains(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))) {
            comment.content = commentContent.replace(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), uuid.toString())
        } else {
            when (comment) {
                is LineComment, is BlockComment -> {
                    this.setComment(BlockComment("$commentContent\n $uuid"))
                }
                else -> {
                    this.setComment(JavadocComment("$commentContent\n * $uuid"))
                }
            }
        }
    }
}

val String.isValidUUID: Boolean
    get() = this.matches(Regex("\\s?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))