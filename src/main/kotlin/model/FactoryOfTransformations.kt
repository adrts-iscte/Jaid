package model

import model.transformations.Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import model.transformations.*
import model.visitors.DiffVisitor
import model.visitors.EqualsUuidVisitor
import tests.longestIncreasingSubsequence.transform


class FactoryOfTransformations(private val baseProj: Project, private val branchProj: Project) {

//    private val baseProj = baseProj.clone()
//    private val branchProj = branchProj.clone()

    private val pairsOfCompilationUnit = mutableMapOf<CompilationUnit, CompilationUnit>()

    private val listOfFactoryOfCompilationUnit = mutableListOf<FactoryOfCompilationUnitTransformations>()

    init {
        getListOfTransformationsProject()
    }

    private fun getListOfTransformationsProject() {
        val listOfCompilationUnitBase = baseProj.getSetOfCompilationUnit()
        val listOfCompilationUnitBranch = branchProj.getSetOfCompilationUnit()

        pairsOfCompilationUnit.putAll(getPairsOfCorrespondingCompilationUnits(listOfCompilationUnitBase, listOfCompilationUnitBranch))

        pairsOfCompilationUnit.forEach{
            listOfFactoryOfCompilationUnit.add(FactoryOfCompilationUnitTransformations(it.key, it.value))
        }

        checkGlobalMovements()

    }

    private fun checkGlobalMovements() {
        checkGlobalTypeMovements()
        checkGlobalMemberMovements()
    }

    private fun checkGlobalTypeMovements() {
        val mapOfAllCompilationUnitInsertions = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations>()
        val mapOfAllCompilationUnitRemovals = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations>()
        listOfFactoryOfCompilationUnit.forEach { fCuT ->
            mapOfAllCompilationUnitInsertions.putAll(fCuT.getInsertionTransformationsList().associateWith { fCuT })
            mapOfAllCompilationUnitRemovals.putAll(fCuT.getRemovalTransformationsList().associateWith { fCuT })
        }

        val setOfAllCompilationUnitInsertionTransformations = mapOfAllCompilationUnitInsertions.keys
        val setOfAllCompilationUnitRemovalTransformations = mapOfAllCompilationUnitRemovals.keys

        val mapOfAllInsertionsNodeTransformation = setOfAllCompilationUnitInsertionTransformations.associateBy { it.getNode().uuid }
        val mapOfAllRemovalsNodeTransformation = setOfAllCompilationUnitRemovalTransformations.associateBy { it.getNode().uuid }

        val intersectionUUIDs = mapOfAllInsertionsNodeTransformation.keys.intersect(
            mapOfAllRemovalsNodeTransformation.keys.toSet())

        intersectionUUIDs.forEach{
            val insertionTransformation = mapOfAllInsertionsNodeTransformation[it] as AddClassOrInterface
            val removalTransformation = mapOfAllRemovalsNodeTransformation[it] as RemoveClassOrInterface

            val originCompilationUnit = mapOfAllCompilationUnitRemovals[removalTransformation]!!
            val destinyCompilationUnit = mapOfAllCompilationUnitInsertions[insertionTransformation]!!

            originCompilationUnit.removeRemovalCompilationUnitTransformation(removalTransformation)
            destinyCompilationUnit.removeInsertionCompilationUnitTransformation(insertionTransformation)

            originCompilationUnit.addModificationCompilationUnitTransformation(
                MoveTypeInterFiles(insertionTransformation, removalTransformation))

            originCompilationUnit.addFactoryClassTransformations(
                originCompilationUnit.FactoryOfClassTransformations(removalTransformation.getNode(), insertionTransformation.getNode())
            )
        }
    }

    private fun checkGlobalMemberMovements() {
        val mapOfAllClassInsertions = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations>()
        val mapOfAllClassRemovals = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations>()
        val mapOfAllCompilationUnitFactoryToClassFactory = mutableMapOf<FactoryOfCompilationUnitTransformations.FactoryOfClassTransformations, FactoryOfCompilationUnitTransformations>()
        listOfFactoryOfCompilationUnit.forEach { fCuT ->
            fCuT.getSetOfClassesOrInterfaces().forEach { fClT ->
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
            val insertionTransformation = when(mapOfAllInsertionsNodeTransformation[it]) {
                is AddCallable -> {
                    mapOfAllInsertionsNodeTransformation[it] as AddCallable
                }
                is AddField -> {
                    mapOfAllInsertionsNodeTransformation[it] as AddField
                } else -> null
            }

            val removalTransformation = when(mapOfAllRemovalsNodeTransformation[it]) {
                is RemoveCallable -> {
                    mapOfAllRemovalsNodeTransformation[it] as RemoveCallable
                }
                is RemoveField -> {
                    mapOfAllRemovalsNodeTransformation[it] as RemoveField
                } else -> null
            }

            if (insertionTransformation != null && removalTransformation != null) {
                val originClass = mapOfAllClassRemovals[removalTransformation]!!
                val destinyClass = mapOfAllClassInsertions[insertionTransformation]!!

                originClass.removeRemovalTransformation(removalTransformation)
                destinyClass.removeInsertionTransformation(insertionTransformation)

                when (branchProj.getElementByUUID(it)) {
                    is CallableDeclaration<*> -> {
                        originClass.addModificationTransformation(MoveCallableInterClasses(
                            insertionTransformation as AddCallable,
                            removalTransformation as RemoveCallable
                        ))
                        originClass.checkCallableTransformations(removalTransformation.getNode(),insertionTransformation.getNode())
                        originClass.checkCallableBodyChanged(removalTransformation.getNode(),insertionTransformation.getNode())
                    }
                    is FieldDeclaration -> {
                        originClass.addModificationTransformation(MoveFieldInterClasses(
                            insertionTransformation as AddField,
                            removalTransformation as RemoveField
                        ))
                        originClass.checkFieldTransformations(removalTransformation.getNode(),insertionTransformation.getNode())
                    }
                    else -> {}
                }
            }
        }
    }

    fun getListOfFactoryOfCompilationUnit() = listOfFactoryOfCompilationUnit

    fun getListOfAllTransformations() = listOfFactoryOfCompilationUnit.flatMap { it.getFinalListOfTransformations() }

    inner class FactoryOfCompilationUnitTransformations(private val baseCompilationUnit: CompilationUnit, private val branchCompilationUnit: CompilationUnit) {

        private val listOfCompilationUnitInsertions = mutableSetOf<Node>()
        private val listOfCompilationUnitRemovals = mutableSetOf<Node>()

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
            val diffBaseVisitor = DiffVisitor(true)
            baseCompilationUnit.accept(diffBaseVisitor, listOfNodesBase)

            val listOfNodesBranch = mutableListOf<Node>()
            val diffBranchVisitor = DiffVisitor(true)
            branchCompilationUnit.accept(diffBranchVisitor, listOfNodesBranch)

            listOfCompilationUnitInsertions.addAll(listOfNodesBranch.toSet().filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }.toSet())
            listOfCompilationUnitRemovals.addAll(listOfNodesBase.toSet().filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }.toSet())

            createInsertionTransformationsList(listOfCompilationUnitInsertions)
            createRemovalTransformationsList(listOfCompilationUnitRemovals)

            listOfNodesBase.removeAll(listOfCompilationUnitRemovals)
            listOfNodesBranch.removeAll(listOfCompilationUnitInsertions)

            getPackageModifications()
            getImportListModifications()
            checkCompilationUnitTypesMoved(baseCompilationUnit, branchCompilationUnit, listOfNodesBase, listOfNodesBranch)
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

        fun removeInsertionCompilationUnitTransformation(t: Transformation) {
            insertionTransformationsList.remove(t)
            finalListOfTransformations.remove(t)
        }

        fun removeRemovalCompilationUnitTransformation(t: Transformation) {
            removalTransformationsList.remove(t)
            finalListOfTransformations.remove(t)
        }

        fun addModificationCompilationUnitTransformation(t: Transformation) {
            if (!modificationTransformationsList.any {
                    it.getNode() == t.getNode() && it.javaClass == t.javaClass
                }) {
                modificationTransformationsList.add(t)
                finalListOfTransformations.add(t)
            }
        }

        fun addFactoryClassTransformations(fct: FactoryOfClassTransformations) {
            listOfClassesOrInterfaces.add(fct)
        }

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
            val basePackage = this.baseCompilationUnit.packageDeclaration.orElse(null)
            val branchPackage = this.branchCompilationUnit.packageDeclaration.orElse(null)
            if( basePackage != branchPackage) {
                addModificationCompilationUnitTransformation(ChangePackage(branchCompilationUnit, branchPackage.name))
            }
        }

        private fun getImportListModifications() {
            val baseImports = this.baseCompilationUnit.imports
            val branchImports = this.branchCompilationUnit.imports
            if( baseImports != branchImports) {
                addModificationCompilationUnitTransformation(ChangeImports(baseCompilationUnit, branchImports))
            }
        }

        private fun checkCompilationUnitTypesMoved(baseCompilationUnit: CompilationUnit, branchCompilationUnit: CompilationUnit,
                                                   listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
            val compilationUnitBaseTypes = baseCompilationUnit.types.filterNot { listOfCompilationUnitRemovals.contains(it) || !listOfNodesBase.contains(it) }
            val compilationUnitBranchTypes = branchCompilationUnit.types.filterNot { listOfCompilationUnitInsertions.contains(it) || !listOfNodesBranch.contains(it) }
            if (compilationUnitBaseTypes != compilationUnitBranchTypes) {
                val mapOfMoves = transform(compilationUnitBaseTypes, compilationUnitBranchTypes)
                mapOfMoves.forEach{entry ->
                    val type = branchCompilationUnit.types.find { it.uuid == entry.key }!!
                    addModificationCompilationUnitTransformation(MoveTypeIntraFile(compilationUnitBaseTypes, type, entry.value, mapOfMoves.entries.indexOf(entry)))
                }
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
                val diffBaseVisitor = DiffVisitor(false)
                baseClass.accept(diffBaseVisitor, listOfNodesBase)

                val listOfNodesBranch = mutableListOf<Node>()
                val diffBranchVisitor = DiffVisitor(false)
                branchClass.accept(diffBranchVisitor, listOfNodesBranch)

                listOfClassInsertions.addAll(listOfNodesBranch.toSet()
                        .filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }
                        .toSet())
                listOfClassRemovals.addAll(listOfNodesBase.toSet()
                        .filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }
                        .toSet())

                createInsertionClassTransformationsList(listOfClassInsertions)
                createRemovalClassTransformationsList(listOfClassRemovals)

                listOfNodesBase.removeAll(listOfClassRemovals)
                listOfNodesBranch.removeAll(listOfClassInsertions)

                getClassTransformationsList(baseClass, branchClass, listOfNodesBase, listOfNodesBranch)
                getFieldDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)
                getCallableDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)

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
                if (!modificationClassTransformationsList.any {
                    it.getNode() == t.getNode() && it.javaClass == t.javaClass
                    }) {
                    modificationClassTransformationsList.add(t)
                    finalClassListOfTransformations.add(t)
                    finalListOfTransformations.add(t)
                }
            }

            private fun createInsertionClassTransformationsList(listOfInsertions: Set<Node>) {
                listOfInsertions.forEach {
                    when (it) {
                        is FieldDeclaration -> insertionClassTransformationsList.add(AddField(branchClass, it))
                        is CallableDeclaration<*> -> insertionClassTransformationsList.add(
                            AddCallable(
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
                        is CallableDeclaration<*> -> removalClassTransformationsList.add(RemoveCallable(branchClass, it))
                    }
                }
            }

            /*
             WHOLE CLASS
             */

            fun getClassTransformationsList(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration, listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                checkClassModifiersChanged(classBase, classBranch)
                checkClassRenamed(classBase, classBranch)
                checkClassJavadocModifications(classBase, classBranch)
                checkClassImplementsTypesChanged(classBase, classBranch)
                checkClassExtendedTypesChanged(classBase, classBranch)
                checkClassMembersMoved(classBase, classBranch, listOfNodesBase, listOfNodesBranch)
            }

            private fun checkClassModifiersChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                if(classBase.modifiers != classBranch.modifiers) {
                    addModificationTransformation(ModifiersChangedClassOrInterface(classBase, classBranch.modifiers))
                }
            }

            private fun checkClassRenamed(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseName = classBase.name
                val classBranchName = classBranch.name
                if(classBaseName != classBranchName) {
                    val renameClassOrInterfaceTransformation = RenameClassOrInterface(classBase, classBranchName)
                    addModificationTransformation(renameClassOrInterfaceTransformation)
//                    renameClassOrInterfaceTransformation.applyTransformation(baseProj)
                }
            }

            private fun checkClassJavadocModifications(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseJavaDoc = classBase.javadocComment.orElse(null)
                val classBranchJavaDoc = classBranch.javadocComment.orElse(null)
                if( classBaseJavaDoc != classBranchJavaDoc) {
                    if (classBaseJavaDoc == null) {
                        addModificationTransformation(SetJavaDoc(baseClass, null, null, classBranchJavaDoc, "ADD"))
                    } else if (classBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(baseClass, null, null))
                    } else {
                        addModificationTransformation(SetJavaDoc(baseClass, null, null, classBranchJavaDoc, "CHANGE"))
                    }

                }
            }

            private fun checkClassImplementsTypesChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseImplementsTypes = classBase.implementedTypes
                val classBranchImplementsTypes = classBranch.implementedTypes
                if (classBaseImplementsTypes != classBranchImplementsTypes) {
                    addModificationTransformation(ChangeImplementsTypes(branchClass, classBranchImplementsTypes))
                }
            }

            private fun checkClassExtendedTypesChanged(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration) {
                val classBaseExtendedTypes = classBase.extendedTypes
                val classBranchExtendedTypes = classBranch.extendedTypes
                if (classBaseExtendedTypes != classBranchExtendedTypes) {
                    addModificationTransformation(ChangeExtendedTypes(branchClass, classBranchExtendedTypes))
                }
            }

            private fun checkClassMembersMoved(classBase: ClassOrInterfaceDeclaration, classBranch: ClassOrInterfaceDeclaration,
                                               listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                val classBaseMembers = classBase.members.filterNot { listOfClassRemovals.contains(it) || !listOfNodesBase.contains(it) }
                val classBranchMembers = classBranch.members.filterNot { listOfClassInsertions.contains(it) || !listOfNodesBranch.contains(it) }
                if (classBaseMembers != classBranchMembers) {
                    val mapOfMoves = transform(classBaseMembers, classBranchMembers)
                    mapOfMoves.forEach{entry ->
                        val member = classBranch.members.find { it.uuid == entry.key }!!
                        when (member) {
                            is CallableDeclaration<*> -> addModificationTransformation(
                                MoveCallableIntraClass(classBranchMembers, member, entry.value, mapOfMoves.entries.indexOf(entry)))
                            is FieldDeclaration -> addModificationTransformation(
                                MoveFieldIntraClass(classBranchMembers, member, entry.value, mapOfMoves.entries.indexOf(entry)))
                        }
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

            fun checkFieldTransformations(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
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
                        addModificationTransformation(SetJavaDoc(branchClass,  null, fieldBranch, fieldBranchJavaDoc, "ADD"))
                    } else if (fieldBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(branchClass, null, fieldBranch))
                    } else {
                        addModificationTransformation(SetJavaDoc(branchClass, null, fieldBranch, fieldBranchJavaDoc, "CHANGE"))
                    }

                }
            }

            private fun checkFieldInitializationChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                val fieldBaseInitializer = (fieldBase.variables.first() as VariableDeclarator).initializer.orElse(null)
                val fieldBranchInitializer = (fieldBranch.variables.first() as VariableDeclarator).initializer.orElse(null)
                if (!EqualsUuidVisitor(baseProj, branchProj).equals(fieldBaseInitializer, fieldBranchInitializer)) {
                    addModificationTransformation(InitializerChangedField(baseProj, fieldBase, fieldBranchInitializer))
                }
            }

            private fun checkFieldRenamed(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                val fieldBaseName = (fieldBase.variables.first() as VariableDeclarator).name
                val fieldBranchName = (fieldBranch.variables.first() as VariableDeclarator).name
                if( fieldBaseName != fieldBranchName) {
                    val renameFieldTransformation = RenameField(fieldBase, fieldBranchName)
                    addModificationTransformation(renameFieldTransformation)
//                    renameFieldTransformation.applyTransformation(baseProj)
                }
            }

            private fun checkFieldModifiersChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                if(fieldBase.modifiers != fieldBranch.modifiers) {
                    addModificationTransformation(ModifiersChangedField(fieldBase, fieldBranch.modifiers))
                }
            }

            private fun checkFieldTypeChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                val fieldBaseType = (fieldBase.variables.first() as VariableDeclarator).type
                val fieldBranchType = (fieldBranch.variables.first() as VariableDeclarator).type
                if(!EqualsUuidVisitor(baseProj, branchProj).equals(fieldBaseType, fieldBranchType)) {
                    addModificationTransformation(TypeChangedField(fieldBase, fieldBranchType))
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

            fun checkCallableTransformations(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                if (callableBase.isMethodDeclaration && callableBranch.isMethodDeclaration) {
                    val methodBase = callableBase as MethodDeclaration
                    val methodBranch = callableBranch as MethodDeclaration
                    checkMethodReturnTypeChanged(methodBase, methodBranch)
                }
                checkCallableModifiersChanged(callableBase, callableBranch)
                checkCallableParametersAndOrNameChanged(callableBase, callableBranch)
                checkCallableJavadocModifications(callableBase, callableBranch)
                checkCallableBodyChanged(callableBase, callableBranch)
            }

            private fun checkCallableParametersAndOrNameChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                val callableBaseParameters = callableBase.parameters
                val callableBranchParameters = callableBranch.parameters
                val callableBaseName = callableBase.name
                val callableBranchName = callableBranch.name
                if(callableBaseParameters != callableBranchParameters ||
                    (callableBase.isMethodDeclaration && callableBranch.isMethodDeclaration && callableBaseName != callableBranchName)) {
                    val parametersAndOrNameChangedTransformation = SignatureChanged(
                        callableBase,
                        callableBranchParameters,
                        callableBranchName
                    )
                    addModificationTransformation(parametersAndOrNameChangedTransformation)
//                    parametersAndOrNameChangedTransformation.applyTransformation(baseProj)
                }
            }

            private fun checkMethodReturnTypeChanged(methodBase: MethodDeclaration, methodBranch: MethodDeclaration) {
                if( methodBase.type != methodBranch.type) {
                    addModificationTransformation(ReturnTypeChangedMethod(methodBase, methodBranch.type))
                }
            }

            private fun checkCallableModifiersChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                if(callableBase.modifiers != callableBranch.modifiers) {
                    addModificationTransformation(ModifiersChangedCallable(
                        callableBase,
                        callableBranch.modifiers
                    ))
                }
            }

            private fun checkCallableJavadocModifications(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                val callableBaseJavaDoc = callableBase.javadocComment.orElse(null)
                val callableBranchJavaDoc = callableBranch.javadocComment.orElse(null)
                if( callableBaseJavaDoc != callableBranchJavaDoc) {
                    if (callableBaseJavaDoc == null) {
                        addModificationTransformation(SetJavaDoc(branchClass, callableBranch,  null, callableBranchJavaDoc, "ADD"))
                    } else if (callableBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(branchClass, callableBranch, null))
                    } else {
                        addModificationTransformation(SetJavaDoc(branchClass, callableBranch, null, callableBranchJavaDoc, "CHANGE"))
                    }

                }
            }

            fun checkCallableBodyChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                if(callableBase.isConstructorDeclaration && callableBranch.isConstructorDeclaration) {
                    val constructorBase = callableBase as ConstructorDeclaration
                    val constructorBranch = callableBranch as ConstructorDeclaration
                    val callableBaseBody = constructorBase.body
                    val callableBranchBody = constructorBranch.body
                    if( !EqualsUuidVisitor(baseProj, branchProj).equals(callableBaseBody, callableBranchBody)) {
                        addModificationTransformation(BodyChangedCallable(baseProj, constructorBase, callableBranchBody))
                    }
                } else {
                    val methodBase = callableBase as MethodDeclaration
                    val methodBranch = callableBranch as MethodDeclaration
                    val callableBaseBody = methodBase.body.orElse(null)
                    val callableBranchBody = methodBranch.body.orElse(null)
                    if( !EqualsUuidVisitor(baseProj, branchProj).equals(callableBaseBody, callableBranchBody)) {
                        addModificationTransformation(BodyChangedCallable(baseProj, methodBase, callableBranchBody))
                    }
                }
            }
        }
    }
}