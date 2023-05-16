package evaluation.jGit

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val projectName = "Bukkit"
    val projectsCSVFilePath = "src\\main\\resources\\repositories\\projects.csv"
    val projectsCSVFile = readProjectsCsv(projectsCSVFilePath)

    val repositoriesPath = "src/main/resources/repositories"

    val project = projectsCSVFile.find { it.name == projectName }!!

    val projectFolder = "$repositoriesPath/${project.name}"
    File(projectFolder).deleteRecursively()
    Files.createDirectories(Paths.get("$projectFolder/${project.name}Repository/"))
    GitMergeCommitInformation.cloneRepository(project.url, "$projectFolder/${project.name}Repository/")


    val commitsCSVFile = readMergeCommitsCsv(File(projectsCSVFilePath).parent + "/commits_${project.name}.csv", project)

    commitsCSVFile.forEach {
        val revFolder = "$repositoriesPath/${project.name}/rev_${it.parent1Id}_${it.parent2Id}"
        println(revFolder)
        Files.createDirectories(Paths.get(revFolder))

        val baseRevFolder = "$revFolder/rev_base_${it.commonAncestorCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(baseRevFolder))
        GitMergeCommitInformation.cloneRepository(project.url, baseRevFolder, it.commonAncestorCommit)

        val leftRevFolder = "$revFolder/rev_left_${it.leftCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(leftRevFolder))
        GitMergeCommitInformation.cloneRepository(project.url, leftRevFolder, it.leftCommit)

        val rightRevFolder = "$revFolder/rev_right_${it.rightCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(rightRevFolder))
        GitMergeCommitInformation.cloneRepository(project.url, rightRevFolder, it.rightCommit)

        writeRevisionsFile(revFolder, it, identifiedRevs = false)

        val baseIdentifiedRev = baseRevFolder.replace("rev_base_", "identified_rev_base_")
        val leftIdentifiedRev = leftRevFolder.replace("rev_left_", "identified_rev_left_")
        val rightIdentifiedRev = rightRevFolder.replace("rev_right_", "identified_rev_right_")

        File(baseRevFolder).copyRecursively(File(baseIdentifiedRev), false)
        File(leftRevFolder).copyRecursively(File(leftIdentifiedRev), false)
        File(rightRevFolder).copyRecursively(File(rightIdentifiedRev), false)

        writeRevisionsFile(revFolder, it, identifiedRevs = true)
    }

//    GitMergeCommitInformation.cloneRepository(bukkitProject.url, "$repositoriesPath/${bukkitProject.name}")
    println(projectsCSVFile)
}

fun readProjectsCsv(path : String): List<CSVRepository> {
    val reader = File(path).inputStream().bufferedReader()
    val header = reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, url, date) = it.split(',', ignoreCase = false, limit = 3)
            CSVRepository(name, url)
        }.toList()
}

fun readMergeCommitsCsv(path : String, repository: CSVRepository): List<CSVMergeCommit> {
    val reader = File(path).inputStream().bufferedReader()
    val header = reader.readLine()
//    return mutableListOf(reader.readLine()).map {
//        val (mergeCommit, parent1Id, parent2Id) = it.split(',', ignoreCase = false, limit = 3)
//        CSVMergeCommit(repository, mergeCommit, parent1Id, parent2Id)
//    }
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (mergeCommit, parent1Id, parent2Id) = it.split(',', ignoreCase = false, limit = 3)
            CSVMergeCommit(repository, mergeCommit, parent1Id, parent2Id)
        }.toList()
}

fun writeRevisionsFile(revFolder : String, mergeCommit: CSVMergeCommit, identifiedRevs : Boolean) {
    val revisionExtension = if (identifiedRevs) "identified_revisions" else "revisions"
    val revisionsFile = File("$revFolder/rev_${mergeCommit.parent1Id}_${mergeCommit.parent2Id}.$revisionExtension")
    if (!revisionsFile.exists()) revisionsFile.createNewFile()
    val prefixOfIdentifiedRevs = if (identifiedRevs) "identified_" else ""
    var content = "${prefixOfIdentifiedRevs}rev_left_${mergeCommit.leftCommit.id.abbreviate(5).name()}/src\n"
    content += "${prefixOfIdentifiedRevs}rev_base_${mergeCommit.commonAncestorCommit.id.abbreviate(5).name()}/src\n"
    content += "${prefixOfIdentifiedRevs}rev_right_${mergeCommit.rightCommit.id.abbreviate(5).name()}/src\n"
    revisionsFile.writeText(content, Charset.defaultCharset())
}