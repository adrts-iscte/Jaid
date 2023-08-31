package model

import model.transformations.Transformation
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import model.transformations.*
import model.visitors.EqualsUuidVisitor
import tests.longestIncreasingSubsequence.transform


class FactoryOfTransformations(private val baseProj: Project, private val branchProj: Project) {

    private val projectTransformations = mutableSetOf<Transformation>()

    private val listOfFactoryOfCompilationUnit = mutableListOf<FactoryOfCompilationUnitTransformations>()

    init {
        getListOfTransformationsProject()
    }

    private fun getListOfTransformationsProject() {
        val listOfCompilationUnitBase = baseProj.getSetOfCompilationUnit().toMutableSet()
        val listOfCompilationUnitBranch = branchProj.getSetOfCompilationUnit().toMutableSet()

        val listOfCompilationUnitInsertions = listOfCompilationUnitBranch.toSet().filterNot { l2element -> listOfCompilationUnitBase.toSet().any { l2element.uuid == it.uuid } }.toSet()
        val listOfCompilationUnitRemovals = listOfCompilationUnitBase.toSet().filterNot { l1element -> listOfCompilationUnitBranch.toSet().any { l1element.uuid == it.uuid } }.toSet()

        listOfCompilationUnitRemovals.forEach { projectTransformations.add(RemoveFile(it)) }

        listOfCompilationUnitInsertions.forEach { projectTransformations.add(AddFile(it)) }

        listOfCompilationUnitBase.removeAll(listOfCompilationUnitRemovals)
        listOfCompilationUnitBranch.removeAll(listOfCompilationUnitInsertions)

        val listOfCompilationUnitBaseIterator = listOfCompilationUnitBase.iterator()
        while (listOfCompilationUnitBaseIterator.hasNext()) {
            val compilationUnitBase = listOfCompilationUnitBaseIterator.next()
            val compilationUnitBranch = listOfCompilationUnitBranch.find { it.uuid == compilationUnitBase.uuid }!!

            listOfFactoryOfCompilationUnit.add(FactoryOfCompilationUnitTransformations(compilationUnitBase, compilationUnitBranch))

            listOfCompilationUnitBranch.remove(compilationUnitBranch)
            listOfCompilationUnitBaseIterator.remove()
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
            val insertionTransformation = mapOfAllInsertionsNodeTransformation[it] as AddType
            val removalTransformation = mapOfAllRemovalsNodeTransformation[it] as RemoveType

            val originCompilationUnit = mapOfAllCompilationUnitRemovals[removalTransformation]!!
            val destinyCompilationUnit = mapOfAllCompilationUnitInsertions[insertionTransformation]!!

            originCompilationUnit.removeRemovalCompilationUnitTransformation(removalTransformation)
            destinyCompilationUnit.removeInsertionCompilationUnitTransformation(insertionTransformation)

            originCompilationUnit.addModificationCompilationUnitTransformation(
                MoveTypeInterFiles(insertionTransformation, removalTransformation)
            )

            originCompilationUnit.addFactoryTypeTransformations(
                originCompilationUnit.FactoryOfTypeTransformations(
                    removalTransformation.getNode(), insertionTransformation.getNode()
                )
            )
        }
    }

    private fun checkGlobalMemberMovements() {
        val mapOfAllTypeInsertions = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfTypeTransformations>()
        val mapOfAllTypeRemovals = mutableMapOf<Transformation, FactoryOfCompilationUnitTransformations.FactoryOfTypeTransformations>()
        val mapOfAllCompilationUnitFactoryToTypeFactory = mutableMapOf<FactoryOfCompilationUnitTransformations.FactoryOfTypeTransformations, FactoryOfCompilationUnitTransformations>()
        listOfFactoryOfCompilationUnit.forEach { fCuT ->
            fCuT.getSetOfTypes().forEach { fClT ->
                mapOfAllTypeInsertions.putAll(fClT.getInsertionTransformationsList().associateWith { fClT })
                mapOfAllTypeRemovals.putAll(fClT.getRemovalTransformationsList().associateWith { fClT })
                mapOfAllCompilationUnitFactoryToTypeFactory[fClT] = fCuT
            }
        }

        val setOfAllTypeInsertionTransformations = mapOfAllTypeInsertions.keys
        val setOfAllTypeRemovalTransformations = mapOfAllTypeRemovals.keys

        val mapOfAllInsertionsNodeTransformation = setOfAllTypeInsertionTransformations.associateBy { it.getNode().uuid }
        val mapOfAllRemovalsNodeTransformation = setOfAllTypeRemovalTransformations.associateBy { it.getNode().uuid }

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
                val originType = mapOfAllTypeRemovals[removalTransformation]!!
                val destinyType = mapOfAllTypeInsertions[insertionTransformation]!!

                originType.removeRemovalTransformation(removalTransformation)
                destinyType.removeInsertionTransformation(insertionTransformation)

                val movedMember = branchProj.getElementByUUID(it)
                if (((movedMember as? CallableDeclaration<*>) ?: movedMember as FieldDeclaration).isStatic) {
                    when (movedMember) {
                        is CallableDeclaration<*> -> {
                                originType.addModificationTransformation(
                                    MoveCallableInterTypes(
                                        insertionTransformation as AddCallable, removalTransformation as RemoveCallable
                                    )
                                )
                                originType.checkCallableTransformations(
                                    removalTransformation.getNode(), insertionTransformation.getNode()
                                )
                            }
                        is FieldDeclaration -> {
                            originType.addModificationTransformation(
                                MoveFieldInterTypes(insertionTransformation as AddField, removalTransformation as RemoveField)
                            )
                            originType.checkFieldTransformations(removalTransformation.getNode(), insertionTransformation.getNode())
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun getListOfFactoryOfCompilationUnit() = listOfFactoryOfCompilationUnit

    fun getListOfAllTransformations() = projectTransformations + listOfFactoryOfCompilationUnit.flatMap { it.getFinalListOfTransformations() }

    override fun toString(): String {
        var print = ""
        listOfFactoryOfCompilationUnit.forEach { print += it }
        return print
    }

    inner class FactoryOfCompilationUnitTransformations(private val baseCompilationUnit: CompilationUnit, private val branchCompilationUnit: CompilationUnit) {

        private val listOfCompilationUnitInsertions = mutableSetOf<Node>()
        private val listOfCompilationUnitRemovals = mutableSetOf<Node>()

        private val insertionTransformationsList = mutableSetOf<Transformation>()
        private val removalTransformationsList = mutableSetOf<Transformation>()
        private val modificationTransformationsList = mutableSetOf<Transformation>()

        private val listOfTypes = mutableSetOf<FactoryOfTypeTransformations>()

        private val finalListOfTransformations = mutableSetOf<Transformation>()

        init {
            getListOfTransformationsOfFile()
        }

        private fun getListOfTransformationsOfFile() {
            val listOfNodesBase = baseCompilationUnit.types.toMutableList<Node>()
            val listOfNodesBranch = branchCompilationUnit.types.toMutableList<Node>()

            listOfCompilationUnitInsertions.addAll(listOfNodesBranch.toSet().filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }.toSet())
            listOfCompilationUnitRemovals.addAll(listOfNodesBase.toSet().filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }.toSet())

            createInsertionTransformationsList(listOfCompilationUnitInsertions)
            createRemovalTransformationsList(listOfCompilationUnitRemovals)

            listOfNodesBase.removeAll(listOfCompilationUnitRemovals)
            listOfNodesBranch.removeAll(listOfCompilationUnitInsertions)

            getPackageModifications()
            getImportListModifications()
            checkCompilationUnitTypesMoved(baseCompilationUnit, branchCompilationUnit, listOfNodesBase, listOfNodesBranch)
            getTypeDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)

            finalListOfTransformations.addAll(insertionTransformationsList)
            finalListOfTransformations.addAll(removalTransformationsList)
            finalListOfTransformations.addAll(modificationTransformationsList)
        }

        override fun toString(): String {
            var print = ""
            if(finalListOfTransformations.isNotEmpty()) {
                print += "Lista de Transformações do ficheiro ${baseCompilationUnit.storage.get().fileName.removeSuffix(".java")}: \n"
                if (insertionTransformationsList.isNotEmpty()) {
                    print += "\tLista de Inserções: \n"
                    insertionTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
                }
                if (removalTransformationsList.isNotEmpty()) {
                    print += "\tLista de Remoções: \n"
                    removalTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
                }
                if (modificationTransformationsList.isNotEmpty()) {
                    print += "\tLista de Modificações: \n"
                    modificationTransformationsList.forEach { print += "\t".repeat(2) + "- ${it.getText()}\n" }
                }
            } else {
                print += "O ficheiro ${baseCompilationUnit.storage.get().fileName.removeSuffix(".java")} não foi modificado!\n"
            }
            print += "\n"
            listOfTypes.forEach {
                print += it
            }
            return print
        }

        fun getInsertionTransformationsList() = insertionTransformationsList.toSet()
        fun getRemovalTransformationsList() = removalTransformationsList.toSet()
        fun getModificationTransformationsList() = modificationTransformationsList.toSet()
        fun getSetOfTypes() = listOfTypes.toSet()

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

        fun addFactoryTypeTransformations(fct: FactoryOfTypeTransformations) {
            listOfTypes.add(fct)
        }

        private fun createInsertionTransformationsList(listOfInsertions: Set<Node>) {
            listOfInsertions.forEach {
                when (it) {
                    is TypeDeclaration<*> -> insertionTransformationsList.add(AddType(branchProj, branchCompilationUnit, it))
                }
            }
        }

        private fun createRemovalTransformationsList(listOfRemovals: Set<Node>) {
            listOfRemovals.forEach {
                when (it) {
                    is TypeDeclaration<*> -> removalTransformationsList.add(RemoveType(branchCompilationUnit, it))
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

        private fun getTypeDeclarationModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
            val listOfTypeDeclarationBase = listOfNodesBase.filterIsInstance<TypeDeclaration<*>>().toMutableList()
            val listOfTypeDeclarationBranch = listOfNodesBranch.filterIsInstance<TypeDeclaration<*>>().toMutableList()
            listOfNodesBase.removeAll(listOfTypeDeclarationBase)
            listOfNodesBranch.removeAll(listOfTypeDeclarationBranch)

            val listOfTypeDeclarationBaseIterator = listOfTypeDeclarationBase.iterator()
            while (listOfTypeDeclarationBaseIterator.hasNext()) {
                val typeBase = listOfTypeDeclarationBaseIterator.next()
                val typeBranch = listOfTypeDeclarationBranch.find { it.uuid == typeBase.uuid }!!

                listOfTypes.add(FactoryOfTypeTransformations(typeBase, typeBranch))

                listOfTypeDeclarationBranch.remove(typeBranch)
                listOfTypeDeclarationBaseIterator.remove()
            }
        }

        inner class FactoryOfTypeTransformations(private val baseType: TypeDeclaration<*>, private val branchType: TypeDeclaration<*>) {

            private val listOfTypeInsertions = mutableSetOf<Node>()
            private val listOfTypeRemovals = mutableSetOf<Node>()

            private val insertionTypeTransformationsList = mutableSetOf<Transformation>()
            private val removalTypeTransformationsList = mutableSetOf<Transformation>()
            private val modificationTypeTransformationsList = mutableSetOf<Transformation>()

            private val listOfTypes = mutableSetOf<FactoryOfTypeTransformations>()

            private val finalTypesListOfTransformations = mutableSetOf<Transformation>()

            init {
                getListOfTransformationsOfType()
            }

            private fun getListOfTransformationsOfType() {
                val listOfNodesBase = mutableListOf<Node>()
//                val diffBaseVisitor = DiffVisitor(false)
//                baseClass.accept(diffBaseVisitor, listOfNodesBase)
                if (baseType.isEnumDeclaration)
                    listOfNodesBase.addAll((baseType as EnumDeclaration).entries)
                listOfNodesBase.addAll(baseType.members)

                val listOfNodesBranch = mutableListOf<Node>()
//                val diffBranchVisitor = DiffVisitor(false)
//                branchClass.accept(diffBranchVisitor, listOfNodesBranch)
                if (branchType.isEnumDeclaration)
                    listOfNodesBranch.addAll((branchType as EnumDeclaration).entries)
                listOfNodesBranch.addAll(branchType.members)

                listOfTypeInsertions.addAll(listOfNodesBranch.toSet()
                        .filterNot { l2element -> listOfNodesBase.toSet().any { l2element.uuid == it.uuid } }
                        .toSet())
                listOfTypeRemovals.addAll(listOfNodesBase.toSet()
                        .filterNot { l1element -> listOfNodesBranch.toSet().any { l1element.uuid == it.uuid } }
                        .toSet())

                createInsertionTypeTransformationsList(listOfTypeInsertions)
                createRemovalTypeTransformationsList(listOfTypeRemovals)

                listOfNodesBase.removeAll(listOfTypeRemovals)
                listOfNodesBranch.removeAll(listOfTypeInsertions)

                if (baseType.isEnumDeclaration) {
                    getEnumTransformationsList(baseType as EnumDeclaration, branchType as EnumDeclaration,
                        listOfNodesBase, listOfNodesBranch)
                    getEnumEntriesModificationsList(listOfNodesBase, listOfNodesBranch)
                } else {
                    getTypeTransformationsList(baseType as ClassOrInterfaceDeclaration, branchType as ClassOrInterfaceDeclaration,
                        listOfNodesBase, listOfNodesBranch)
                }
                getNestedTypeDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)
                getFieldDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)
                getCallableDeclarationModificationsList(listOfNodesBase, listOfNodesBranch)

                finalTypesListOfTransformations.addAll(insertionTypeTransformationsList)
                finalTypesListOfTransformations.addAll(removalTypeTransformationsList)
                finalTypesListOfTransformations.addAll(modificationTypeTransformationsList)

                finalListOfTransformations.addAll(finalTypesListOfTransformations)
            }

            override fun toString(): String {
                var print = ""
                if (finalTypesListOfTransformations.isNotEmpty()) {
                    print += "\t".repeat(3) + "Lista de Transformações da ${baseType.asString} ${baseType.nameAsString}: \n"
                    if (insertionTypeTransformationsList.isNotEmpty()) {
                        print += "\t".repeat(4) + "Lista de Inserções: \n"
                        insertionTypeTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
                    }
                    if (removalTypeTransformationsList.isNotEmpty()) {
                        print += "\t".repeat(4) + "Lista de Remoções: \n"
                        removalTypeTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
                    }
                    if (modificationTypeTransformationsList.isNotEmpty()) {
                        print += "\t".repeat(4) + "Lista de Modificações: \n"
                        modificationTypeTransformationsList.forEach { print += "\t".repeat(5) + "- ${it.getText()}\n" }
                    }
                } else {
                    print += "\t".repeat(3) + "A ${baseType.asString} ${baseType.nameAsString} não foi modificada!\n"
                }
                print += "\n"
                listOfTypes.forEach {
                    print += "\t".repeat(3) + it.toString().replace("\n",  "\n" + "\t".repeat(3))
                }
                return print
            }

            fun getInsertionTransformationsList() = insertionTypeTransformationsList.toSet()
            fun getRemovalTransformationsList() = removalTypeTransformationsList.toSet()

            fun removeInsertionTransformation(t: Transformation) {
                insertionTypeTransformationsList.remove(t)
                finalTypesListOfTransformations.remove(t)
                finalListOfTransformations.remove(t)
            }

            fun removeRemovalTransformation(t: Transformation) {
                removalTypeTransformationsList.remove(t)
                finalTypesListOfTransformations.remove(t)
                finalListOfTransformations.remove(t)
            }

            fun addModificationTransformation(t: Transformation) {
                if (!modificationTypeTransformationsList.any {
                    it.getNode() == t.getNode() && it.javaClass == t.javaClass
                    }) {
                    modificationTypeTransformationsList.add(t)
                    finalTypesListOfTransformations.add(t)
                    finalListOfTransformations.add(t)
                }
            }

            private fun createInsertionTypeTransformationsList(listOfInsertions: Set<Node>) {
                listOfInsertions.forEach {
                    when (it) {
                        is TypeDeclaration<*> -> insertionTypeTransformationsList.add(AddType(branchProj, branchType, it))
                        is FieldDeclaration -> insertionTypeTransformationsList.add(AddField(branchProj, branchType, it))
                        is CallableDeclaration<*> -> insertionTypeTransformationsList.add(AddCallable(branchProj, branchType, it))
                        is EnumConstantDeclaration -> insertionTypeTransformationsList.add(AddEnumConstant(branchType as EnumDeclaration, it))
                    }
                }
            }

            private fun createRemovalTypeTransformationsList(listOfRemovals: Set<Node>) {
                listOfRemovals.forEach {
                    when (it) {
                        is TypeDeclaration<*> -> removalTypeTransformationsList.add(RemoveType(branchType, it))
                        is FieldDeclaration -> removalTypeTransformationsList.add(RemoveField(branchType, it))
                        is CallableDeclaration<*> -> removalTypeTransformationsList.add(RemoveCallable(branchType, it))
                        is EnumConstantDeclaration -> removalTypeTransformationsList.add(RemoveEnumConstant(branchType as EnumDeclaration, it))
                    }
                }
            }

            /*
             ENUM
             */

            private fun getEnumTransformationsList(enumBase: EnumDeclaration, enumBranch: EnumDeclaration, listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                checkTypeModifiersChanged(enumBase, enumBranch)
                checkTypeRenamed(enumBase, enumBranch)
                checkTypeJavadocModifications(enumBase, enumBranch)
                checkTypeImplementsTypesChanged(enumBase, enumBranch)
                checkTypeMembersMoved(enumBase, enumBranch, listOfNodesBase, listOfNodesBranch)
                checkEnumEntriesMoved(enumBase, enumBranch, listOfNodesBase, listOfNodesBranch)
            }

            private fun getEnumEntriesModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                val listOfEnumConstantDeclarationBase = listOfNodesBase.filterIsInstance<EnumConstantDeclaration>().toMutableList()
                val listOfEnumConstantDeclarationBranch = listOfNodesBranch.filterIsInstance<EnumConstantDeclaration>().toMutableList()

                val listOfEnumConstantDeclarationBaseIterator = listOfEnumConstantDeclarationBase.iterator()
                while (listOfEnumConstantDeclarationBaseIterator.hasNext()) {
                    val enumConstantBase = listOfEnumConstantDeclarationBaseIterator.next()
                    val enumConstantBranch = listOfEnumConstantDeclarationBranch.find { it.uuid == enumConstantBase.uuid }!!

                    checkEnumConstantRenamed(enumConstantBase, enumConstantBranch)
                    checkEnumConstantJavadocChanged(enumConstantBase, enumConstantBranch)
                    checkEnumConstantArgumentsChanged(enumConstantBase, enumConstantBranch)

                    listOfEnumConstantDeclarationBranch.remove(enumConstantBranch)
                    listOfEnumConstantDeclarationBaseIterator.remove()
                }
            }

            private fun checkEnumEntriesMoved(enumBase: EnumDeclaration, enumBranch: EnumDeclaration, listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                val enumBaseEntries = enumBase.entries.filterNot { listOfTypeRemovals.contains(it) || !listOfNodesBase.contains(it) }
                val enumBranchEntries = enumBranch.entries.filterNot { listOfTypeInsertions.contains(it) || !listOfNodesBranch.contains(it) }
                if (enumBaseEntries != enumBranchEntries) {
                    val mapOfMoves = transform(enumBaseEntries, enumBranchEntries)
                    mapOfMoves.forEach{entry ->
                        val enumEntry = enumBranch.entries.find { it.uuid == entry.key }!!
                        addModificationTransformation(
                            MoveEnumConstantIntraEnum(enumBranchEntries, enumEntry, entry.value, mapOfMoves.entries.indexOf(entry)))
                    }
                }
            }

            private fun checkEnumConstantRenamed(enumConstantBase: EnumConstantDeclaration, enumConstantBranch: EnumConstantDeclaration) {
                val enumConstantBaseName = enumConstantBase.name
                val enumConstantBranchName = enumConstantBranch.name
                if(enumConstantBaseName != enumConstantBranchName) {
                    val renameEnumConstantTransformation = RenameEnumConstant(enumConstantBase, enumConstantBranchName)
                    addModificationTransformation(renameEnumConstantTransformation)
                }
            }

            private fun checkEnumConstantJavadocChanged(enumConstantBase: EnumConstantDeclaration, enumConstantBranch: EnumConstantDeclaration) {
                val enumConstantBaseJavaDoc = enumConstantBase.javadocComment.orElse(null)
                val enumConstantBranchJavaDoc = enumConstantBranch.javadocComment.orElse(null)
                if( enumConstantBaseJavaDoc != enumConstantBranchJavaDoc) {
                    if (enumConstantBaseJavaDoc == null) {
                        addModificationTransformation(SetJavaDoc(enumConstantBranch, enumConstantBranchJavaDoc, "ADD"))
                    } else if (enumConstantBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(enumConstantBranch))
                    } else {
                        addModificationTransformation(SetJavaDoc(enumConstantBranch, enumConstantBranchJavaDoc, "CHANGE"))
                    }
                }
            }

            private fun checkEnumConstantArgumentsChanged(enumConstantBase: EnumConstantDeclaration, enumConstantBranch: EnumConstantDeclaration) {
                val enumConstantBaseParameters = enumConstantBase.arguments
                val enumConstantBranchParameters = enumConstantBranch.arguments
                if (enumConstantBaseParameters != enumConstantBranchParameters) {
                    val enumConstantArgumentsTransformation = ChangeEnumConstantArguments(enumConstantBranch, enumConstantBranchParameters)
                    addModificationTransformation(enumConstantArgumentsTransformation)
                }
            }

            /*
             WHOLE TYPE
             */

            private fun getTypeTransformationsList(typeBase: ClassOrInterfaceDeclaration, typeBranch: ClassOrInterfaceDeclaration, listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                checkTypeModifiersChanged(typeBase, typeBranch)
                checkTypeRenamed(typeBase, typeBranch)
                checkTypeJavadocModifications(typeBase, typeBranch)
                checkTypeImplementsTypesChanged(typeBase, typeBranch)
                checkClassExtendedTypesChanged(typeBase, typeBranch)
                checkTypeMembersMoved(typeBase, typeBranch, listOfNodesBase, listOfNodesBranch)
            }

            private fun checkTypeModifiersChanged(typeBase: TypeDeclaration<*>, typeBranch: TypeDeclaration<*>) {
                if(typeBase.modifiers != typeBranch.modifiers) {
                    addModificationTransformation(ModifiersChangedType(typeBase, typeBranch.modifiers))
                }
            }

            private fun checkTypeRenamed(typeBase: TypeDeclaration<*>, typeBranch: TypeDeclaration<*>) {
                val typeBaseName = typeBase.name
                val typeBranchName = typeBranch.name
                if(typeBaseName != typeBranchName) {
                    val renameTypeTransformation = RenameType(typeBase, typeBranchName)
                    addModificationTransformation(renameTypeTransformation)
                }
            }

            private fun checkTypeJavadocModifications(typeBase: TypeDeclaration<*>, typeBranch: TypeDeclaration<*>) {
                val typeBaseJavaDoc = typeBase.javadocComment.orElse(null)
                val typeBranchJavaDoc = typeBranch.javadocComment.orElse(null)
                if( typeBaseJavaDoc != typeBranchJavaDoc) {
                    if (typeBaseJavaDoc == null) {
                        addModificationTransformation(SetJavaDoc(branchType, typeBranchJavaDoc, "ADD"))
                    } else if (typeBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(branchType))
                    } else {
                        addModificationTransformation(SetJavaDoc(branchType, typeBranchJavaDoc, "CHANGE"))
                    }
                }
            }

            private fun checkTypeImplementsTypesChanged(typeBase: TypeDeclaration<*>, typeBranch: TypeDeclaration<*>) {
                val typeBaseImplementsTypes = ((typeBase as? EnumDeclaration) ?: typeBase as ClassOrInterfaceDeclaration).implementedTypes
                val typeBranchImplementsTypes = ((typeBranch as? EnumDeclaration) ?: typeBranch as ClassOrInterfaceDeclaration).implementedTypes
                if (typeBaseImplementsTypes != typeBranchImplementsTypes) {
                    addModificationTransformation(ChangeImplementsTypes(branchProj, branchType, typeBranchImplementsTypes))
                }
            }

            private fun checkClassExtendedTypesChanged(typeBase: ClassOrInterfaceDeclaration, typeBranch: ClassOrInterfaceDeclaration) {
                val typeBaseExtendedTypes = typeBase.extendedTypes
                val typeBranchExtendedTypes = typeBranch.extendedTypes
                if (typeBaseExtendedTypes != typeBranchExtendedTypes) {
                    addModificationTransformation(ChangeExtendedTypes(branchProj, typeBranch, typeBranchExtendedTypes))
                }
            }

            private fun checkTypeMembersMoved(typeBase: TypeDeclaration<*>, typeBranch: TypeDeclaration<*>,
                                              listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                val typeBaseMembers = typeBase.members.filterNot { listOfTypeRemovals.contains(it) || !listOfNodesBase.contains(it) }
                val typeBranchMembers = typeBranch.members.filterNot { listOfTypeInsertions.contains(it) || !listOfNodesBranch.contains(it) }
                if (typeBaseMembers != typeBranchMembers) {
                    val mapOfMoves = transform(typeBaseMembers, typeBranchMembers)
                    mapOfMoves.forEach{entry ->
                        val member = typeBranch.members.find { it.uuid == entry.key }!!
                        when (member) {
                            is CallableDeclaration<*> -> addModificationTransformation(
                                MoveCallableIntraType(typeBranchMembers, member, entry.value, mapOfMoves.entries.indexOf(entry)))
                            is FieldDeclaration -> addModificationTransformation(
                                MoveFieldIntraType(typeBranchMembers, member, entry.value, mapOfMoves.entries.indexOf(entry)))
                        }
                    }
                }
            }

            /*
             NESTED CLASSES
             */

            private fun getNestedTypeDeclarationModificationsList(listOfNodesBase: MutableList<Node>, listOfNodesBranch: MutableList<Node>) {
                val listOfTypeDeclarationBase = listOfNodesBase.filterIsInstance<TypeDeclaration<*>>().toMutableList()
                val listOfTypeDeclarationBranch = listOfNodesBranch.filterIsInstance<TypeDeclaration<*>>().toMutableList()
                listOfNodesBase.removeAll(listOfTypeDeclarationBase)
                listOfNodesBranch.removeAll(listOfTypeDeclarationBranch)

                val listOfTypeDeclarationBaseIterator = listOfTypeDeclarationBase.iterator()
                while (listOfTypeDeclarationBaseIterator.hasNext()) {
                    val typeBase = listOfTypeDeclarationBaseIterator.next()
                    val typeBranch = listOfTypeDeclarationBranch.find { it.uuid == typeBase.uuid }!!

                    listOfTypes.add(FactoryOfTypeTransformations(typeBase, typeBranch))

                    listOfTypeDeclarationBranch.remove(typeBranch)
                    listOfTypeDeclarationBaseIterator.remove()
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
                        addModificationTransformation(SetJavaDoc(fieldBranch, fieldBranchJavaDoc, "ADD"))
                    } else if (fieldBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(fieldBranch))
                    } else {
                        addModificationTransformation(SetJavaDoc(fieldBranch, fieldBranchJavaDoc, "CHANGE"))
                    }

                }
            }

            private fun checkFieldInitializationChanged(fieldBase: FieldDeclaration, fieldBranch: FieldDeclaration) {
                val fieldBaseInitializer = (fieldBase.variables.first() as VariableDeclarator).initializer.orElse(null)
                val fieldBranchInitializer = (fieldBranch.variables.first() as VariableDeclarator).initializer.orElse(null)
                if (!EqualsUuidVisitor(baseProj,branchProj).equals(fieldBaseInitializer, fieldBranchInitializer)) {
                    addModificationTransformation(InitializerChangedField(branchProj, fieldBase, fieldBranchInitializer))
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
                if(!EqualsUuidVisitor(baseProj,branchProj).equals(fieldBaseType, fieldBranchType)) {
                    addModificationTransformation(TypeChangedField(branchProj, fieldBase, fieldBranchType))
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
                        branchProj, callableBase,
                        callableBranchParameters,
                        callableBranchName
                    )
                    addModificationTransformation(parametersAndOrNameChangedTransformation)
//                    parametersAndOrNameChangedTransformation.applyTransformation(baseProj)
                }
            }

            private fun checkMethodReturnTypeChanged(methodBase: MethodDeclaration, methodBranch: MethodDeclaration) {
                if( methodBase.type != methodBranch.type) {
                    addModificationTransformation(ReturnTypeChangedMethod(branchProj, methodBase, methodBranch.type))
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
                if(callableBaseJavaDoc != callableBranchJavaDoc) {
                    if (callableBaseJavaDoc == null) {
                        addModificationTransformation(SetJavaDoc(callableBranch,  callableBranchJavaDoc, "ADD"))
                    } else if (callableBranchJavaDoc == null) {
                        addModificationTransformation(RemoveJavaDoc(callableBranch))
                    } else {
                        addModificationTransformation(SetJavaDoc(callableBranch, callableBranchJavaDoc, "CHANGE"))
                    }

                }
            }

            fun checkCallableBodyChanged(callableBase: CallableDeclaration<*>, callableBranch: CallableDeclaration<*>) {
                if(callableBase.isConstructorDeclaration && callableBranch.isConstructorDeclaration) {
                    val constructorBase = callableBase as ConstructorDeclaration
                    val constructorBranch = callableBranch as ConstructorDeclaration
                    val callableBaseBody = constructorBase.body
                    val callableBranchBody = constructorBranch.body
                    if( !EqualsUuidVisitor(baseProj,branchProj).equals(callableBaseBody, callableBranchBody)) {
                        addModificationTransformation(BodyChangedCallable(branchProj, constructorBase, callableBranchBody))
                    }
                } else {
                    val methodBase = callableBase as MethodDeclaration
                    val methodBranch = callableBranch as MethodDeclaration
                    val callableBaseBody = methodBase.body.orElse(null)
                    val callableBranchBody = methodBranch.body.orElse(null)
                    if( !EqualsUuidVisitor(baseProj,branchProj).equals(callableBaseBody, callableBranchBody)) {
                        addModificationTransformation(BodyChangedCallable(branchProj, methodBase, callableBranchBody))
                    }
                }
            }
        }
    }
}