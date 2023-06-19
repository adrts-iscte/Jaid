package evaluation.jGit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import java.io.File

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