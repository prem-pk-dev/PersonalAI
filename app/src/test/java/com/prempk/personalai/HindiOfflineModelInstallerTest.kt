package com.prempk.personalai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HindiOfflineModelInstallerTest {
    @Test fun rejectsMissingModelDirectory() {
        assertFalse(HindiOfflineModelInstaller.isModelDirectoryReady(File("missing-hindi-model")))
    }

    @Test fun rejectsIncompleteModelFiles() {
        val directory = createTempDir(prefix = "pk-hindi-incomplete")
        try {
            File(directory, "model.int8.onnx").writeBytes(byteArrayOf(1, 2, 3))
            File(directory, "tokens.txt").writeText("token")
            assertFalse(HindiOfflineModelInstaller.isModelDirectoryReady(directory))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun acceptsOnlyCompletePinnedInstallation() {
        val directory = createTempDir(prefix = "pk-hindi-ready")
        try {
            File(directory, "model.int8.onnx").writeBytes(byteArrayOf(1, 2, 3))
            File(directory, "tokens.txt").writeText("token")
            File(directory, ".install-complete").writeText(
                "${HindiOfflineModelInstaller.MODEL_REVISION}:${HindiOfflineModelInstaller.MODEL_SHA256}"
            )
            assertTrue(HindiOfflineModelInstaller.isModelDirectoryReady(directory))
            File(directory, ".install-complete").writeText("wrong-revision")
            assertFalse(HindiOfflineModelInstaller.isModelDirectoryReady(directory))
        } finally {
            directory.deleteRecursively()
        }
    }
}
