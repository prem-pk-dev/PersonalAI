package com.prempk.personalai

import java.io.File

/**
 * Promotes a fully downloaded and verified model directory without deleting the
 * previous installation first. Staging and destination must share a filesystem.
 */
internal object HindiModelDirectoryCommit {
    fun promote(staging: File, destination: File): Boolean {
        if (!staging.isDirectory) return false
        val backup = File(destination.parentFile, "${destination.name}.previous")
        if (backup.exists() && !backup.deleteRecursively()) return false

        val hadDestination = destination.exists()
        if (hadDestination && !destination.renameTo(backup)) return false

        if (!staging.renameTo(destination)) {
            if (hadDestination) backup.renameTo(destination)
            return false
        }

        if (backup.exists() && !backup.deleteRecursively()) {
            // The new install is valid and active; stale backup cleanup can be retried.
            return true
        }
        return true
    }
}
