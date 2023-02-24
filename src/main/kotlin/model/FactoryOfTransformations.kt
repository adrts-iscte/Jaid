package model

import model.transformations.Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import model.transformations.*
import model.visitors.DiffVisitor
import model.visitors.EqualsVisitor
import model.visitors.FileVisitor
import tests.longestIncreasingSubsequence.transform


class FactoryOfTransformations(baseProj: Project, branchProj: Project) {

    private val clonedBaseProj = baseProj.clone()
    private val clonedBranchProj = branchProj.clone()

    private val pairsOfCompilationUnit = mutableMapOf<CompilationUnit, CompilationUnit>()

    private val listOfFactoryOfCompilationUnit = mutableListOf<FactoryOfCompilationUnitTransformations>()

    init {
        getListOfTransformationsProject()
    }

    private fun getListOfTransformationsProject() {
        val listOfCompilationUnitBase = clonedBaseProj.getSetOfCompilationUnit()
        val listOfCompilationUnitBranch = clonedBranchProj.getSetOfCompilationUnit()

        pairsOfCompilationUnit.putAll(getPairsOfCorrespondingCompilationUnits(listOfCompilationUnitBase, listOfCompilationUnitBranch))

        pairsOfCompilationUnit.forEach{
            listOfFactoryOfCompilationUnit.add(FactoryOfCompilationUnitTransformations(it.key, it.value))
        }

        checkGlobalMovements()

    }

    private fun checkGlobalMovements() {
//        checkGlobalTypeMovements()
        checkGlobalMemberMovements()
    }

    private fun checkGlobalTypeMovements() {
        val mapOfAllTypeInsertions = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations>()
        val mapOfAllTypeRemovals = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations>()
//        val mapOfAllCompilationUnitInsertions = mutableMapOf<FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations, FactoryOfCompilationUnitTransformations>()
//        val mapOfAllCompilationUnitRemovals = mutableMapOf<FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations, FactoryOfCompilationUnitTransformations>()
//        listOfFactoryOfCompilationUnit.forEach {
//            listOfAllInsertions.addAll(it.getInsertionTransformationsList())
//            listOfAllRemovals.addAll(it.getRemovalTransformationsList())
//        }
//
//        val mapOfAllInsertionsNodeTransformation = listOfAllInsertions.associateBy { it.getNode().uuid }
//        val mapOfAllRemovalsNodeTransformation = listOfAllRemovals.associateBy { it.getNode().uuid }
//
//        val intersectionUUIDs = mapOfAllInsertionsNodeTransformation.keys.intersect(
//            mapOfAllRemovalsNodeTransformation.keys.toSet())
//
//        intersectionUUIDs.forEach{
//            val insertionTransformation = mapOfAllInsertionsNodeTransformation[it] as AddCallableDeclaration
//            val removalTransformation = mapOfAllRemovalsNodeTransformation[it] as RemoveCallableDeclaration
//            when(clonedBranchProj.getElementByUUID(it)){
//                is CallableDeclaration<*> -> {
//                    MoveCallableInterClasses(insertionTransformation, removalTransformation)
//                }
//                else -> {}
//            }
//
//        }

    }

    private fun checkGlobalMemberMovements() {
        val mapOfAllClassInsertions = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations>()
        val mapOfAllClassRemovals = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations>()
        val mapOfAllCompilationUnitFactoryToClassFactory = mutableMapOf<FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations, FactoryOfCompilationUnitTransformations>()
        listOfFactoryOfCompilationUnit.forEach { fCuT ->
            fCuT.getSetOfClassesOrInterfaces().forEach { fClT ->
                val insert = fClT.getInsertionTransformationsList()
                val removal = fClT.getRemovalTransformationsList()
                mapOfAllClassInsertions.putAll(fClT.getInsertionTransformationsList().associateWith { fClT })
                mapOfAllClassRemovals.putAll(fClT.getRemovalTransformationsList().associateWith { fClT })
                mapOfAllCompilationUnitFactoryToClassFactory[fClT] = fCuT
            }
        }

        val setOfAllClassInsertionTransformations = mapOfAllClassInsertions.keys
        val setOfAllClassRemovalTransformations = mapOfAllClassRemovals.keys

        val mapOfAllInsertionsNodeTransformation = setOfAllClassInsertionTransformations.associateBy { it.getNode().uuid }
        val mapOfAllRemovalsNodeTransformation = setOfAllClassRemovalTransformations.associateBy { it.getNode().uuid }

        val intersectionUUIDs = mapOfAllInsertionsNodeTransformation.keys.intersect(
            mapOfAllRemovalsNodeTransformation.keys.toSet())

        intersectionUUIDs.forEach{
            val insertionTransformation = mapOfAllInsertionsNodeTransformation[it] as AddCallableDeclaration
            val removalTransformation = mapOfAllRemovalsNodeTransformation[it] as RemoveCallableDeclaration
            val originClass = mapOfAllClassRemovals[removalTransformation]!!
            val destinyClass = mapOfAllClassInsertions[insertionTransformation]!!

            originClass.removeRemovalTransformation(removalTransformation)
            destinyClass.removeInsertionTransformation(insertionTransformation)

            when(clonedBranchProj.getElementByUUID(it)){
                is CallableDeclaration<*> -> {
                    originClass.addModificationTransformation(MoveCallableInterClasses(insertionTransformation, removalTransformation))
                }
                else -> {}
            }
        }
    }

    fun getListOfFactoryOfCompilationUnit() = listOfFactoryOfCompilationUnit

    fun getListOfAllTransformations() = listOfFactoryOfCompilationUnit.flatMap { it.getFinalListOfTransformations() }

    inner class FactoryOfCompilationUnitTransformations(private val baseCompilationUnit: CompilationUnit, private val branchCompilationUnit: CompilationUnit) {

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
            baseCompilationUnit.accept(diffBaseVisitor, listOfNodesBase)

            val listOfNodesBranch = mutableListOf<Node>()
            val diffBranchVisitor = FileVisitor()
            branchCompilationUnit.accept(diffBranchVisitor, listOfNodesBranch)

            val listOfInsertions =
                listOfNodesBranch.toSet().filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }.toSet()
            val listOfRemovals =
                listOfNodesBase.toSet().filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }.toSet()

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
                print += "Lista de Transformações do ficheiro ${baseCompilationUnit.storage.get().fileName.removeSuffix(".java")}: \n"

                print += "\tLista de Inserções: \n"
                insertionTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
                print += "\tLista de Remoções: \n"
                removalTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
                print += "\tLista de Modificações: \n"
                modificationTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
            } else {
                print += "O ficheiro ${baseCompilationUnit.storage.get().fileName.removeSuffix(".java")} não foi modificado!\n"
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
        fun getSetOfClassesOrInterfaces() = listOfClassesOrInterfaces.toSet()

        fun getFinalListOfTransformations() = finalListOfTransformations.toSet()

        private fun createInsertionTransformationsList(listOfInsertions: Set<Node>) {
            listOfInsertions.forEach {
                when (it) {
                    is ClassOrInterfaceDeclaration -> insertionTransformationsList.add(AddClassOrInterface(branchCompilationUnit, it))
                }
            }
        }

        private fun createRemovalTransformationsList(listOfRemovals: Set<Node>) {
            listOfRemovals.forEach {
                when (it) {
                    is ClassOrInterfaceDeclaration -> removalTransformationsList.add(RemoveClassOrInterface(branchCompilationUnit, it))
                }
            }
        }

        private fun getPackageModifications() {
            val basePackage = this.baseCompilationUnit.packageDeclaration.get()
            val branchPackage = this.branchCompilationUnit.packageDeclaration.get()
            if( basePackage != branchPackage) {
                modificationTransformationsList.add(ChangePackage(branchCompilationUnit, branchPackage.nameAsString))
            }
        }

        private fun getImportListModifications() {
            val baseImports = this.baseCompilationUnit.imports
            val branchImports = this.branchCompilationUnit.imports
            if( baseImports != branchImports) {
                modificationTransformationsList.add(ChangeImports(branchCompilationUnit, branchImports))
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

            private val listOfClassInsertions = mutableSetOf<Node>()
            private val listOfClassRemovals = mutableSetOf<Node>()

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

                listOfClassInsertions.addAll(
                    listOfNodesBranch.toSet()
                        .filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }
                        .toSet())
                listOfClassRemovals.addAll(
                    listOfNodesBase.toSet()
                        .filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }
                        .toSet())

                createInsertionClassTransformationsList(listOfClassInsertions)
                createRemovalClassTransformationsList(listOfClassRemovals)

                listOfNodesBase.removeAll(listOfClassRemovals)
                listOfNodesBranch.removeAll(listOfClassInsertions)

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

            fun getInsertionTransformationsList() = insertionClassTransformationsList.toSet()
            fun getRemovalTransformationsList() = removalClassTransformationsList.toSet()

            fun removeInsertionTransformation(t: Transformation) {
                insertionClassTransformationsList.remove(t)
                finalClassListOfTransformations.remove(t)
                finalListOfTransformations.remove(t)
            }

            fun removeRemovalTransformation(t: Transformation) {
                removalClassTransformationsList.remove(t)
                finalClassListOfTransformations.remove(t)
                finalListOfTransformations.remove(t)
            }

            fun addModificationTransformation(t: Transformation) {
                modificationClassTransformationsList.add(t)
                finalClassListOfTransformations.add(t)
                finalListOfTransformations.add(t)
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
                checkClassMembersMoved(classBase, classBranch)
            }

            private fun checkClassModifiersChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                if(classBase.modifiers != classBranch.modifiers) {
                    modificationClassTransformationsList.add(ModifiersChangedClassOrInterface(classBase, classBranch.modifiers))
                }
            }

            private fun checkClassRenamed(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseName = classBase.nameAsString
                val classBranchName = classBranch.nameAsString
                if(classBaseName != classBranchName) {
                    val renameClassOrInterfaceTransformation = RenameClassOrInterface(classBase, classBranchName)
                    modificationClassTransformationsList.add(renameClassOrInterfaceTransformation)
//                    renameClassOrInterfaceTransformation.applyTransformation(clonedBaseProj)
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

            private fun checkClassMembersMoved(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseMembers = classBase.members.filterNot { listOfClassRemovals.contains(it) }
                val classBranchMembers = classBranch.members.filterNot { listOfClassInsertions.contains(it) }
//                val classBaseMembers = classBase.members
//                val classBranchMembers = classBranch.members
                if (classBaseMembers != classBranchMembers) {
                    val mapOfMoves = transform(classBaseMembers, classBranchMembers)
                    mapOfMoves.forEach{entry ->
                        modificationClassTransformationsList.add(
                            MoveCallableIntraClass(classBase, classBranch.methods.find { it.uuid == entry.key }!!, entry.value, mapOfMoves.entries.indexOf(entry))
                        )
                    }
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
                val fieldBaseInitializer = (fieldBase.variables.first() as VariableDeclarator).initializer.orElse(null)
                val fieldBranchInitializer = (fieldBranch.variables.first() as VariableDeclarator).initializer.orElse(null)
                if (fieldBaseInitializer != null && fieldBranchInitializer != null && !EqualsVisitor.equals(fieldBaseInitializer, fieldBranchInitializer)) {
                    modificationClassTransformationsList.add(InitializerChangedField(branchClass, fieldBase, fieldBranchInitializer))
                }
            }

            private fun checkFieldRenamed(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                val fieldBaseName = (fieldBase.variables.first() as VariableDeclarator).nameAsString
                val fieldBranchName = (fieldBranch.variables.first() as VariableDeclarator).nameAsString
                if( fieldBaseName != fieldBranchName) {
                    val renameFieldTransformation = RenameField(branchClass, fieldBase, fieldBranchName)
                    modificationClassTransformationsList.add(renameFieldTransformation)
//                    renameFieldTransformation.applyTransformation(clonedBaseProj)
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
                if(!EqualsVisitor.equals(fieldBaseType, fieldBranchType)) {
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
                val callableBaseName = callableBase.name
                val callableBranchName = callableBranch.name
                if(callableBaseParameters != callableBranchParameters || !EqualsVisitor.equals(callableBaseName, callableBranchName)) {
                    val parametersAndOrNameChangedTransformation = ParametersAndOrNameChangedCallable(branchClass, callableBase, callableBranchParameters, callableBranchName.asString())
                    modificationClassTransformationsList.add(parametersAndOrNameChangedTransformation)
//                    parametersAndOrNameChangedTransformation.applyTransformation(clonedBaseProj)
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
                    if( !EqualsVisitor.equals(callableBaseBody, callableBranchBody)) {
                        modificationClassTransformationsList.add(BodyChangedCallable(branchClass, constructorBase, callableBranchBody))
                    }
                } else {
                    val methodBase = callableBase as MethodDeclaration
                    val methodBranch = callableBranch as MethodDeclaration
                    val callableBaseBody = methodBase.body.orElse(null)
                    val callableBranchBody = methodBranch.body.orElse(null)
                    if( !EqualsVisitor.equals(callableBaseBody, callableBranchBody)) {
                        modificationClassTransformationsList.add(BodyChangedCallable(branchClass, methodBase, callableBranchBody))
                    }
                }
            }
        }
    }
}