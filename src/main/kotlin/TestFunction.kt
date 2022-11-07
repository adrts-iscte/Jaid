import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.visitor.EqualsVisitor
import model.transformations.RemoveField
import model.transformations.RenameField
import model.uuid
import model.visitors.DiffVisitor

fun testFunction(c1: ClassOrInterfaceDeclaration, c2: ClassOrInterfaceDeclaration) {

    println("$c1\n")
    println("$c2\n")

//    val equalsVisitor = object : EqualsVisitor() {
//        override fun visit(n: ClassOrInterfaceDeclaration, arg: Visitable): Boolean {
//            val n2 = arg as ClassOrInterfaceDeclaration
//            if (!nodesEquals(n.members, n2.members))
//                return true
//            return super.visit(n, arg)
//        }
//    }
    val equalsVisitor = EqualsVisitor.equals(c1,c2)
    println("Are both classes equal? $equalsVisitor\n")

    val listOfDifferencesClassWithAttribute = mutableListOf<Node>()
    val diffWithAttribute2 = DiffVisitor()
    c1.accept(diffWithAttribute2, listOfDifferencesClassWithAttribute)
    println(listOfDifferencesClassWithAttribute)

    val listOfDifferencesClassWithoutAttribute = mutableListOf<Node>()
    val diffWithoutAttribute2 = DiffVisitor()
    c2.accept(diffWithoutAttribute2, listOfDifferencesClassWithoutAttribute)
    println(listOfDifferencesClassWithoutAttribute)

    println("Lista de Diferenças: ")
    val diffList =
        getDifferencesBetweenTwoLists(listOfDifferencesClassWithAttribute, listOfDifferencesClassWithoutAttribute)
    println(diffList)

    val leftDiffList = diffList.first
    val rightDiffList = diffList.second
/*
    val listOfTransformations = mutableListOf<model.transformations.Transformation>()

    val leftDiffListIterator = leftDiffList.iterator()
    while (leftDiffListIterator.hasNext()) {
        when (val diff = leftDiffListIterator.next()) {
            is FieldDeclaration -> {
                checkFieldRename(diff,rightDiffList, listOfTransformations)
            }
            is MethodDeclaration -> {
                checkMethodRename(diff,rightDiffList, listOfTransformations)
            }
        }
        leftDiffListIterator.remove()
    }

    for (diff in rightDiffList) {
        when (diff) {
            is FieldDeclaration -> listOfTransformations.add(model.transformations.AddField(diff))
            is MethodDeclaration -> listOfTransformations.add(AddMethod(diff))
        }
    }

    println()
    println("Lista de Transformações: ")
    listOfTransformations.forEach { println("- ${it.getText()}") }*/
}
/*
fun checkFieldRename(leftDiff: FieldDeclaration,
                     rightDiffList: MutableSet<Node>,
                     listOfTransformations: MutableList<model.transformations.Transformation>) {

    val rightDiffListFiltered = rightDiffList.filterIsInstance<FieldDeclaration>()

    val find = rightDiffListFiltered.find { it.modifiers == leftDiff.modifiers &&
            it.variables[0].type == leftDiff.variables[0].type }
//  println(find)
    if (find != null) {
        val newName = (find.childNodes.first() as VariableDeclarator).nameAsString
        listOfTransformations.add(RenameField(clazz, leftDiff, newName))
        rightDiffList.remove(find)
    } else {
//        listOfTransformations.add(RemoveField(leftDiff))
    }
}
*/
/*fun checkMethodRename(leftDiff: MethodDeclaration,
                      rightDiffList: MutableSet<Node>,
                      listOfTransformations: MutableList<model.transformations.Transformation>) {

    val rightDiffListFiltered = rightDiffList.filterIsInstance<MethodDeclaration>()

    val find = rightDiffListFiltered.find { it.modifiers == leftDiff.modifiers &&
            it.parameters == leftDiff.parameters &&
            it.type == leftDiff.type &&
            it.body == leftDiff.body}
//  println(find)
    if (find != null) {
        val newName = find.name.asString()
        listOfTransformations.add(RenameMethod(leftDiff, newName))
        rightDiffList.remove(find)
    } else {
        listOfTransformations.add(RemoveMethod(leftDiff))
    }
}*/

fun getDifferencesBetweenTwoLists(l1: List<Node>, l2: List<Node>) : Pair<MutableSet<Node>, MutableSet<Node>> {
//    val first = l1.toSet().minus(l2.toSet()).toMutableSet()
    val first = l1.toSet().filterNot { l1element -> l2.toSet().any{ l1element.uuid == it.uuid}}.toMutableSet()
//    val second = l2.toSet().minus(l1.toSet()).toMutableSet()
    val second = l2.toSet().filterNot { l2element -> l1.toSet().any{ l2element.uuid == it.uuid}}.toMutableSet()
    return Pair(first, second)
}