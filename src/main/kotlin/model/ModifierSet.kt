package model

import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList

class ModifierSet(modifiers : NodeList<Modifier>) {

    private val accessModifier = mutableSetOf<Modifier>()
    private val remainingModifiers = mutableSetOf<Modifier>()

    init {
        accessModifier.addAll(modifiers.filter { it.isAccessModifier }.toSet())
        remainingModifiers.addAll(modifiers.filterNot { it.isAccessModifier }.toSet())
    }

    fun getAccessModifiers() : MutableSet<Modifier> = accessModifier
    fun hasAbstractModifier() : Boolean = remainingModifiers.any { it.isAbstractModifier }
    fun hasAnotherModifiersOtherThanAbstract() : Boolean = remainingModifiers.any { !it.isAbstractModifier }

    fun setAccessModifiers(newAccessModifiers : MutableSet<Modifier>) {
        accessModifier.clear()
        accessModifier.addAll(newAccessModifiers)
    }

    fun setRemainingModifiers(newRemainingModifiers : Set<Modifier>) {
        remainingModifiers.clear()
        remainingModifiers.addAll(newRemainingModifiers)
    }

    fun isConflictiousWith(other : ModifierSet) : Boolean {
        if ((accessModifier + other.accessModifier).size > 1) {
            return true
        }
        if ((hasAbstractModifier() && other.hasAnotherModifiersOtherThanAbstract()) ||
            (other.hasAbstractModifier() && hasAnotherModifiersOtherThanAbstract())) {
            return true
        }

        return false
    }

    fun merge(other : ModifierSet) : NodeList<Modifier> {
        check(!isConflictiousWith(other)) { "Cannot merge two conflictuous Modifier's set" }
        val allModifiers = accessModifier + other.accessModifier +
                          remainingModifiers + other.remainingModifiers
        return NodeList(allModifiers)
    }

    fun replaceModifiersBy(other : ModifierSet) : ModifierSet {
        this.setAccessModifiers(other.accessModifier)
        this.setRemainingModifiers(other.remainingModifiers)
        return this
    }

    fun toNodeList() : NodeList<Modifier> {
        return NodeList(accessModifier + remainingModifiers)
    }
}