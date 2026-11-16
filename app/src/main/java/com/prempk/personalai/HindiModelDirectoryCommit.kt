package com.prempk.personalai

import java.io.File

/**
 * Promotes a fully downloaded and verified model directory without deleting the
 * previous installation first. Staging and destination must share a filesystem.
 */
internal object HindiModelDirectoryCommit {
    fun promote(staging: File, destination: File): Boolean {
        val backup = File(destination.parentFile, "${destination.name}.previous")

        // Recover an interrupted prior promotion before attempting another install.
        if (backup.exists()) {
            if (!destination.exists()) {
                if (!backup.renameTo(destination)) return false
            } else if (!backup.deleteRecursively()) {
                return false
            }
        }

        if (!staging.isDirectory) return false

        val hadDestination = destination.exists()
        if (hadDestination && !destination.renameTo(backup)) return false

        if (!staging.renameTo(destination)) {
            if (hadDestination) backup.renameTo(destination)
            return false
        }

        // The new directory is active; a stale backup can be cleaned up later.
        if (backup.exists()) backup.deleteRecursively()
        return true
    }
}
