package model.visitors

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class FileVisitor : VoidVisitorAdapter<MutableList<Node>>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableList<Node>) {
        super.visit(n, arg)
        arg.add(n)
//        val classClone = n.clone()
//        arg.add(classClone)
//        //Limpar a cópia da lista
//        //Ou dar override no equals do ClassOrInterfaceDeclaration
//        classClone.members.clear()
    }

}