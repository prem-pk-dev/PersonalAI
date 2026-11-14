package com.prempk.personalai

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class SherpaRuntimeSmokeTest {
    @Test
    fun nativeRuntimeLoadsAndKeywordSpotterClassIsPresent() {
        System.loadLibrary("sherpa-onnx-jni")
        val spotterClass = Class.forName("com.k2fsa.sherpa.onnx.KeywordSpotter")
        assertNotNull(spotterClass)
    }
}
