package evaluation.jGit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

fun main() {

    val projectsCSVFilePath = "src\\main\\resources\\repositories\\projects.csv"
    val projectsCSVFile = readProjectsCsv(projectsCSVFilePath)

    val repositoriesPath = "src/main/resources/repositories"

    //Filter Bukkit Project
    val bukkitProject = projectsCSVFile.find { it.name == "jsoup" }!!

    val bukkitCommitsCSVFile = readMergeCommitsCsv(File(projectsCSVFilePath).parent + "/commits_${bukkitProject.name}.csv", bukkitProject)

    bukkitCommitsCSVFile.forEach {

        val projectFolder = "$repositoriesPath/${it.CSVRepository.name}"
        File(projectFolder).deleteRecursively()
        Files.createDirectories(Paths.get("$projectFolder/${it.CSVRepository.name}Repository/"))
        GitMergeCommitInformation.cloneRepository(it.CSVRepository.url, "$projectFolder/${it.CSVRepository.name}Repository/")

        val revFolder = "$repositoriesPath/${bukkitProject.name}/rev_${it.parent1Id}_${it.parent2Id}"
        println(revFolder)
        Files.createDirectories(Paths.get(revFolder))

        val baseRevFolder = "$revFolder/rev_base_${it.commonAncestorCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(baseRevFolder))
        GitMergeCommitInformation.cloneRepository(bukkitProject.url, baseRevFolder, it.commonAncestorCommit)

        val leftRevFolder = "$revFolder/rev_left_${it.leftCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(leftRevFolder))
        GitMergeCommitInformation.cloneRepository(bukkitProject.url, leftRevFolder, it.leftCommit)

        val rightRevFolder = "$revFolder/rev_right_${it.rightCommit.id.abbreviate(5).name()}/"
        Files.createDirectories(Paths.get(rightRevFolder))
        GitMergeCommitInformation.cloneRepository(bukkitProject.url, rightRevFolder, it.rightCommit)

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

data class CSVRepository(
    val name: String,
    val url: String
)

data class CSVMergeCommit(
    val CSVRepository: CSVRepository,
    val mergeCommit: String,
    val parent1Id: String,
    val parent2Id: String) {

    val repository: Repository = Git.open(File("src/main/resources/repositories/${CSVRepository.name}/${CSVRepository.name}Repository/")).repository
    val leftCommit : RevCommit
    val rightCommit : RevCommit
    val commonAncestorCommit : RevCommit

    init {
        val walk = RevWalk(repository)
        walk.revFilter = RevFilter.MERGE_BASE
        val leftCommitId = repository.resolve(parent1Id)
        this.leftCommit = walk.parseCommit(leftCommitId)
        val rightCommitId = repository.resolve(parent2Id)
        this.rightCommit = walk.parseCommit(rightCommitId)
        walk.close()
        this.commonAncestorCommit = GitMergeCommitInformation.getCommonAncestor(this)
    }
}