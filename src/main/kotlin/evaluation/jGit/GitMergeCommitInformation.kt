package evaluation.jGit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.util.FS
import java.io.File

object GitMergeCommitInformation {

    fun getCommonAncestor(mergeCommit : CSVMergeCommit): RevCommit {
        val repository = mergeCommit.repository
        val walk = RevWalk(repository)
        walk.revFilter = RevFilter.MERGE_BASE
        walk.markStart(mergeCommit.leftCommit)
        walk.markStart(mergeCommit.rightCommit)
        val mergeBase = walk.next()
        return mergeBase
    }

    fun cloneRepository(repositoryUrl : String, localPath : String, specificCommit : ObjectId? = null) {
        val file = File(localPath)
        if (!file.exists()) {
            file.createNewFile()
        }
        val git = if (!RepositoryCache.FileKey.isGitRepository(file, FS.DETECTED)) {
            Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(file)
//            .setCredentialsProvider(UsernamePasswordCredentialsProvider("***", "***"))
                .call()
        } else {
            Git.open(file)
        }
        specificCommit?.let {
            git.checkout().setName(specificCommit.name).call();
        }
    }
}