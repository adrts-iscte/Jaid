package model

import model.transformations.Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import model.transformations.*
import model.visitors.DiffVisitor
import model.visitors.FileVisitor

class FactoryOfTransformations(private val base: CompilationUnit, private val branch: CompilationUnit) {
    private val clonedBase = base.clone()
    private val clonedBranch = branch.clone()

    private val insertionTransformationsList = mutableSetOf<Transformation>()
    private val removalTransformationsList = mutableSetOf<Transformation>()
    private val modificationTransformationsList = mutableSetOf<Transformation>()

    private val listOfClassesOrInterfaces = mutableSetOf<FactoryOfClassTransformations>()

    private val finalListOfTransformations = mutableSetOf<Transformation>()

    init {
        getListOfTransformationsOfFile()
    }

    private fun getListOfTransformationsOfFile() {

        val listOfNodesBase = mutableListOf<Node>()
        val diffBaseVisitor = FileVisitor()
        clonedBase.accept(diffBaseVisitor, listOfNodesBase)

        val listOfNodesBranch = mutableListOf<Node>()
        val diffBranchVisitor = FileVisitor()
        clonedBranch.accept(diffBranchVisitor, listOfNodesBranch)

        val listOfInsertions =
            listOfNodesBranch.toSet().filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }
                .toSet()
        val listOfRemovals =
            listOfNodesBase.toSet().filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }
                .toSet()

        createInsertionTransformationsList(listOfInsertions)
        createRemovalTransformationsList(listOfRemovals)

        listOfNodesBase.removeAll(listOfRemovals)
        listOfNodesBranch.removeAll(listOfInsertions)

        getPackageModifications()
        getImportListModifications()
        getClassDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)

        finalListOfTransformations.addAll(insertionTransformationsList)
        finalListOfTransformations.addAll(removalTransformationsList)
        finalListOfTransformations.addAll(modificationTransformationsList)
    }

    override fun toString(): String {
        var print = ""
        if(finalListOfTransformations.isNotEmpty()) {
            print += "Lista de Transformações do ficheiro ${clonedBase.storage.get().fileName.removeSuffix(".java")}: \n"

            print += "\tLista de Inserções: \n"
            insertionTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
            print += "\tLista de Remoções: \n"
            removalTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
            print += "\tLista de Modificações: \n"
            modificationTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
        } else {
            print += "O ficheiro ${clonedBase.storage.get().fileName.removeSuffix(".java")} não foi modificado!\n"
        }
        print += "\n"
        listOfClassesOrInterfaces.forEach {
            print += it
        }
        return print
    }

    fun getInsertionTransformationsList() = insertionTransformationsList.toSet()
    fun getRemovalTransformationsList() = removalTransformationsList.toSet()
    fun getModificationTransformationsList() = modificationTransformationsList.toSet()

    fun getFinalListOfTransformations() = finalListOfTransformations.toSet()

    private fun createInsertionTransformationsList(listOfInsertions: Set<Node>) {
        listOfInsertions.forEach {
            when (it) {
                is ClassOrInterfaceDeclaration -> insertionTransformationsList.add(AddClassOrInterface(clonedBranch, it))
            }
        }
    }

    private fun createRemovalTransformationsList(listOfRemovals: Set<Node>) {
        listOfRemovals.forEach {
            when (it) {
                is ClassOrInterfaceDeclaration -> removalTransformationsList.add(RemoveClassOrInterface(it))
            }
        }
    }

    private fun getPackageModifications() {
        val basePackage = base.packageDeclaration.get()
        val branchPackage = branch.packageDeclaration.get()
        if( basePackage != branchPackage) {
            modificationTransformationsList.add(ChangePackage(branchPackage.nameAsString))
        }
    }

    private fun getImportListModifications() {
        val baseImports = base.imports
        val branchImports = branch.imports
        if( baseImports != branchImports) {
            modificationTransformationsList.add(ChangeImports(branchImports))
        }
    }

    private fun getClassDeclarationModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
        val listOfClassDeclarationBase = listOfNodesBase.filterIsInstance<ClassOrInterfaceDeclaration>().toMutableList()
        val listOfClassDeclarationBranch = listOfNodesBranch.filterIsInstance<ClassOrInterfaceDeclaration>().toMutableList()
        listOfNodesBase.removeAll(listOfClassDeclarationBase)
        listOfNodesBranch.removeAll(listOfClassDeclarationBranch)

        val listOfClassDeclarationBaseIterator = listOfClassDeclarationBase.iterator()
        while (listOfClassDeclarationBaseIterator.hasNext()) {
            val classBase = listOfClassDeclarationBaseIterator.next()
            val classBranch = listOfClassDeclarationBranch.find { it.uuid == classBase.uuid }!!

            listOfClassesOrInterfaces.add(FactoryOfClassTransformations(classBase, classBranch))

            listOfClassDeclarationBranch.remove(classBranch)
            listOfClassDeclarationBaseIterator.remove()
        }
    }



    inner class FactoryOfClassTransformations(private val baseClass: ClassOrInterfaceDeclaration, private val branchClass: ClassOrInterfaceDeclaration) {

        private val insertionClassTransformationsList = mutableSetOf<Transformation>()
        private val removalClassTransformationsList = mutableSetOf<Transformation>()
        private val modificationClassTransformationsList = mutableSetOf<Transformation>()

        private val finalClassListOfTransformations = mutableSetOf<Transformation>()

        init {
            getListOfTransformationsOfClass()
        }

        private fun getListOfTransformationsOfClass() {
            val listOfNodesBase = mutableListOf<Node>()
            val diffBaseVisitor = DiffVisitor()
            baseClass.accept(diffBaseVisitor, listOfNodesBase)

            val listOfNodesBranch = mutableListOf<Node>()
            val diffBranchVisitor = DiffVisitor()
            branchClass.accept(diffBranchVisitor, listOfNodesBranch)

            val listOfInsertions =
                listOfNodesBranch.toSet()
                    .filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }
                    .toSet()
            val listOfRemovals =
                listOfNodesBase.toSet()
                    .filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }
                    .toSet()

            createInsertionClassTransformationsList(listOfInsertions)
            createRemovalClassTransformationsList(listOfRemovals)

            listOfNodesBase.removeAll(listOfRemovals)
            listOfNodesBranch.removeAll(listOfInsertions)

            getClassTransformationsList(baseClass, branchClass)
            getFieldDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)
            getCallableDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)
            getCallableBodyModificationsList(listOfNodesBase, listOfNodesBranch)

            finalClassListOfTransformations.addAll(insertionClassTransformationsList)
            finalClassListOfTransformations.addAll(removalClassTransformationsList)
            finalClassListOfTransformations.addAll(modificationClassTransformationsList)

            finalListOfTransformations.addAll(finalClassListOfTransformations)
            print("")
        }

        override fun toString(): String {
            var print = ""
            if (finalClassListOfTransformations.isNotEmpty()) {
                print += "\t".repeat(3) + "Lista de Transformações da Classe ${baseClass.nameAsString}: \n"

                print += "\t".repeat(4) + "Lista de Inserções: \n"
                insertionClassTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
                print += "\t".repeat(4) + "Lista de Remoções: \n"
                removalClassTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
                print += "\t".repeat(4) + "Lista de Modificações: \n"
                modificationClassTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
            } else {
                print += "\t".repeat(3) + "A Classe ${baseClass.nameAsString} não foi modificada!\n"
            }
            return print
        }

        private fun createInsertionClassTransformationsList(listOfInsertions: Set<Node>) {
            listOfInsertions.forEach {
                when (it) {
                    is FieldDeclaration -> insertionClassTransformationsList.add(AddField(branchClass, it))
                    is CallableDeclaration<*> -> insertionClassTransformationsList.add(
                        AddCallableDeclaration(
                            branchClass,
                            it
                        )
                    )
                }
            }
        }

        private fun createRemovalClassTransformationsList(listOfRemovals: Set<Node>) {
            listOfRemovals.forEach {
                when (it) {
                    is FieldDeclaration -> removalClassTransformationsList.add(RemoveField(branchClass, it))
                    is CallableDeclaration<*> -> removalClassTransformationsList.add(
                        RemoveCallableDeclaration(
                            branchClass,
                            it
                        )
                    )
                }
            }
        }

        /*
         WHOLE CLASS
         */

        private fun getClassTransformationsList(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            checkClassModifiersChanged(classBase, classBranch)
            checkClassRenamed(classBase, classBranch)
            checkClassJavadocModifications(classBase, classBranch)
            checkClassImplementsTypesChanged(classBase, classBranch)
            checkClassExtendedTypesChanged(classBase, classBranch)
        }

        private fun checkClassModifiersChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            if(classBase.modifiers != classBranch.modifiers) {
                modificationClassTransformationsList.add(ModifiersChangedClass(classBase, classBranch.modifiers))
            }
        }

        private fun checkClassRenamed(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            val classBaseName = classBase.nameAsString
            val classBranchName = classBranch.nameAsString
            if(classBaseName != classBranchName) {
                val renameClassTransformation = RenameClass(classBase, classBranchName)
                modificationClassTransformationsList.add(renameClassTransformation)
                renameClassTransformation.applyTransformation(clonedBase)
            }
        }

        private fun checkClassJavadocModifications(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            val classBaseJavaDoc = classBase.javadocComment.orElse(null)
            val classBranchJavaDoc = classBranch.javadocComment.orElse(null)
            if( classBaseJavaDoc != classBranchJavaDoc) {
                if (classBaseJavaDoc == null) {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass, null, null, classBranchJavaDoc, "ADD"))
                } else if (classBranchJavaDoc == null) {
                    modificationClassTransformationsList.add(RemoveJavaDoc(branchClass, null, null))
                } else {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass, null, null, classBranchJavaDoc, "CHANGE"))
                }

            }
        }

        private fun checkClassImplementsTypesChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            val classBaseImplementsTypes = classBase.implementedTypes
            val classBranchImplementsTypes = classBranch.implementedTypes
            if (classBaseImplementsTypes != classBranchImplementsTypes) {
                modificationClassTransformationsList.add(ChangeImplementsTypes(branchClass, classBranchImplementsTypes))
            }
        }

        private fun checkClassExtendedTypesChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
            val classBaseExtendedTypes = classBase.extendedTypes
            val classBranchExtendedTypes = classBranch.extendedTypes
            if (classBaseExtendedTypes != classBranchExtendedTypes) {
                modificationClassTransformationsList.add(ChangeExtendedTypes(branchClass, classBranchExtendedTypes))
            }
        }

        /*
         FIELDS
         */

        private fun getFieldDeclarationModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
            val listOfFieldDeclarationBase = listOfNodesBase.filterIsInstance<FieldDeclaration>().toMutableList()
            val listOfFieldDeclarationBranch = listOfNodesBranch.filterIsInstance<FieldDeclaration>().toMutableList()
            listOfNodesBase.removeAll(listOfFieldDeclarationBase)
            listOfNodesBranch.removeAll(listOfFieldDeclarationBranch)

            val listOfFieldDeclarationBaseIterator = listOfFieldDeclarationBase.iterator()
            while (listOfFieldDeclarationBaseIterator.hasNext()) {
                val fieldBase = listOfFieldDeclarationBaseIterator.next()
                val fieldBranch = listOfFieldDeclarationBranch.find { it.uuid == fieldBase.uuid }!!

                checkFieldTransformations(fieldBase, fieldBranch)

                listOfFieldDeclarationBranch.remove(fieldBranch)
                listOfFieldDeclarationBaseIterator.remove()
            }
        }

        private fun checkFieldTransformations(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            checkFieldRenamed(fieldBase, fieldBranch)
            checkFieldModifiersChanged(fieldBase, fieldBranch)
            checkFieldTypeChanged(fieldBase, fieldBranch)
            checkFieldInitializationChanged(fieldBase, fieldBranch)
            checkFieldJavadocModifications(fieldBase, fieldBranch)
        }

        private fun checkFieldJavadocModifications(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            val fieldBaseJavaDoc = fieldBase.javadocComment.orElse(null)
            val fieldBranchJavaDoc = fieldBranch.javadocComment.orElse(null)
            if( fieldBaseJavaDoc != fieldBranchJavaDoc) {
                if (fieldBaseJavaDoc == null) {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass,  null, fieldBranch, fieldBranchJavaDoc, "ADD"))
                } else if (fieldBranchJavaDoc == null) {
                    modificationClassTransformationsList.add(RemoveJavaDoc(branchClass, null, fieldBranch))
                } else {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass, null, fieldBranch, fieldBranchJavaDoc, "CHANGE"))
                }

            }
        }

        private fun checkFieldInitializationChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            val fieldBaseInitializer = (fieldBase.variables.first() as VariableDeclarator).initializer
            val fieldBranchInitializer = (fieldBranch.variables.first() as VariableDeclarator).initializer
            if (fieldBaseInitializer != fieldBranchInitializer) {
                modificationClassTransformationsList.add(InitializerChangedField(branchClass, fieldBase, fieldBranchInitializer.get()))
            }
        }

        private fun checkFieldRenamed(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            val fieldBaseName = (fieldBase.variables.first() as VariableDeclarator).nameAsString
            val fieldBranchName = (fieldBranch.variables.first() as VariableDeclarator).nameAsString
            if( fieldBaseName != fieldBranchName) {
                val renameFieldTransformation = RenameField(branchClass, fieldBase, fieldBranchName)
                modificationClassTransformationsList.add(renameFieldTransformation)
                renameFieldTransformation.applyTransformation(clonedBase)
            }
        }

        private fun checkFieldModifiersChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            if(fieldBase.modifiers != fieldBranch.modifiers) {
                modificationClassTransformationsList.add(ModifiersChangedField(branchClass, fieldBase, fieldBranch.modifiers))
            }
        }

        private fun checkFieldTypeChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
            val fieldBaseType = (fieldBase.variables.first() as VariableDeclarator).type
            val fieldBranchType = (fieldBranch.variables.first() as VariableDeclarator).type
            if(fieldBaseType != fieldBranchType) {
                modificationClassTransformationsList.add(TypeChangedField(branchClass, fieldBase, fieldBranchType))
            }
        }

        /*
         CALLABLES (METHOD AND CONSTRUCTORS)
         */

        private fun getCallableDeclarationModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
            val listOfCallableDeclarationBase = listOfNodesBase.filterIsInstance<CallableDeclaration<*>>().toMutableList()
            val listOfCallableDeclarationBranch = listOfNodesBranch.filterIsInstance<CallableDeclaration<*>>().toMutableList()

            val listOfCallableDeclarationBaseIterator = listOfCallableDeclarationBase.iterator()
            while (listOfCallableDeclarationBaseIterator.hasNext()) {
                val callableBase = listOfCallableDeclarationBaseIterator.next()
                val callableBranch = listOfCallableDeclarationBranch.find { it.uuid == callableBase.uuid }!!

                checkCallableTransformations(callableBase, callableBranch)

                listOfCallableDeclarationBranch.remove(callableBranch)
                listOfCallableDeclarationBaseIterator.remove()
            }
        }

        private fun checkCallableTransformations(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
            if (callableBase.isMethodDeclaration && callableBranch.isMethodDeclaration) {
                val methodBase = callableBase as MethodDeclaration
                val methodBranch = callableBranch as MethodDeclaration
                checkMethodReturnTypeChanged(methodBase, methodBranch)
            }
            checkCallableModifiersChanged(callableBase, callableBranch)
            checkCallableParametersAndOrNameChanged(callableBase, callableBranch)
            checkCallableJavadocModifications(callableBase, callableBranch)
        }

        private fun checkCallableParametersAndOrNameChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
            val callableBaseParameters = callableBase.parameters
            val callableBranchParameters = callableBranch.parameters
            val callableBaseName = callableBase.nameAsString
            val callableBranchName = callableBranch.nameAsString
            if(callableBaseParameters != callableBranchParameters || callableBaseName != callableBranchName) {
                val parametersAndOrNameChangedTransformation = ParametersAndOrNameChangedCallable(branchClass, callableBase, callableBranchParameters, callableBranchName)
                modificationClassTransformationsList.add(parametersAndOrNameChangedTransformation)
                parametersAndOrNameChangedTransformation.applyTransformation(clonedBase)
            }
        }

        private fun checkMethodReturnTypeChanged(methodBase: MethodDeclaration, methodBranch: MethodDeclaration) {
            if( methodBase.type != methodBranch.type) {
                modificationClassTransformationsList.add(ReturnTypeChangedMethod(branchClass, methodBase, methodBranch.type))
            }
        }

        private fun checkCallableModifiersChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
            if(callableBase.modifiers != callableBranch.modifiers) {
                modificationClassTransformationsList.add(ModifiersChangedCallable(branchClass, callableBase, callableBranch.modifiers))
            }
        }

        /*
        private fun checkMethodRename(methodBase: MethodDeclaration, methodBranch: MethodDeclaration) {
            val methodBaseName = methodBase.nameAsString
            val methodBranchName = methodBranch.nameAsString
            if(methodBaseName != methodBranchName) {
                val renameMethodTransformation = RenameMethod(branchClass, methodBase, methodBranchName)
                modificationClassTransformationsList.add(renameMethodTransformation)
                renameMethodTransformation.applyTransformation(clonedBase)
            }
        }
        */

        private fun checkCallableJavadocModifications(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
            val callableBaseJavaDoc = callableBase.javadocComment.orElse(null)
            val callableBranchJavaDoc = callableBranch.javadocComment.orElse(null)
            if( callableBaseJavaDoc != callableBranchJavaDoc) {
                if (callableBaseJavaDoc == null) {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass, callableBranch,  null, callableBranchJavaDoc, "ADD"))
                } else if (callableBranchJavaDoc == null) {
                    modificationClassTransformationsList.add(RemoveJavaDoc(branchClass, callableBranch, null))
                } else {
                    modificationClassTransformationsList.add(SetJavaDoc(branchClass, callableBranch, null, callableBranchJavaDoc, "CHANGE"))
                }

            }
        }

        private fun getCallableBodyModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
            val listOfCallableDeclarationBase = listOfNodesBase.filterIsInstance<CallableDeclaration<*>>().toMutableList()
            val listOfCallableDeclarationBranch = listOfNodesBranch.filterIsInstance<CallableDeclaration<*>>().toMutableList()
            listOfNodesBase.removeAll(listOfCallableDeclarationBase)
            listOfNodesBranch.removeAll(listOfCallableDeclarationBranch)

            val listOfCallableDeclarationBaseIterator = listOfCallableDeclarationBase.iterator()
            while (listOfCallableDeclarationBaseIterator.hasNext()) {
                val callableBase = listOfCallableDeclarationBaseIterator.next()
                val callableBranch = listOfCallableDeclarationBranch.find { it.uuid == callableBase.uuid }!!

                checkCallableBodyChanged(callableBase, callableBranch)

                listOfCallableDeclarationBranch.remove(callableBranch)
                listOfCallableDeclarationBaseIterator.remove()
            }
        }

        private fun checkCallableBodyChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
            if(callableBase.isConstructorDeclaration && callableBranch.isConstructorDeclaration) {
                val constructorBase = callableBase as ConstructorDeclaration
                val constructorBranch = callableBranch as ConstructorDeclaration
                val callableBaseBody = constructorBase.body
                val callableBranchBody = constructorBranch.body
                if( callableBaseBody != callableBranchBody) {
                    modificationClassTransformationsList.add(BodyChangedCallable(branchClass, constructorBase, callableBranchBody))
                }
            } else {
                val methodBase = callableBase as MethodDeclaration
                val methodBranch = callableBranch as MethodDeclaration
                val callableBaseBody = methodBase.body.orElse(null)
                val callableBranchBody = methodBranch.body.orElse(null)
                if( callableBaseBody != callableBranchBody) {
                    modificationClassTransformationsList.add(BodyChangedCallable(branchClass, methodBase, callableBranchBody))
                }
            }
        }
    }
}