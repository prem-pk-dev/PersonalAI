package com.prempk.personalai

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule

class HindiModelDirectoryCommitTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test fun promotesStagedModelAndReplacesPreviousDirectory() {
        val root = temporaryFolder.newFolder("models")
        val staging = File(root, "hindi.installing").apply { mkdirs() }
        val destination = File(root, "hindi").apply { mkdirs() }
        File(staging, "model.int8.onnx").writeText("new-model")
        File(destination, "model.int8.onnx").writeText("old-model")

        assertTrue(HindiModelDirectoryCommit.promote(staging, destination))
        assertEquals("new-model", File(destination, "model.int8.onnx").readText())
        assertFalse(staging.exists())
        assertFalse(File(root, "hindi.previous").exists())
    }

    @Test fun missingStagingDirectoryDoesNotTouchExistingModel() {
        val root = temporaryFolder.newFolder("models")
        val staging = File(root, "missing.installing")
        val destination = File(root, "hindi").apply { mkdirs() }
        File(destination, "model.int8.onnx").writeText("known-good-model")

        assertFalse(HindiModelDirectoryCommit.promote(staging, destination))
        assertEquals("known-good-model", File(destination, "model.int8.onnx").readText())
    }

    @Test fun installsIntoAnEmptyDestination() {
        val root = temporaryFolder.newFolder("models")
        val staging = File(root, "hindi.installing").apply { mkdirs() }
        val destination = File(root, "hindi")
        File(staging, "tokens.txt").writeText("tokens")

        assertTrue(HindiModelDirectoryCommit.promote(staging, destination))
        assertEquals("tokens", File(destination, "tokens.txt").readText())
    }
}
