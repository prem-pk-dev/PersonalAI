package com.prempk.personalai

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SherpaRuntimeSmokeTest {
    @Test
    fun keywordSpotterClassIsPackagedWithoutLoadingDeviceNativeAbi() {
        // The upstream AAR is verified for ARM ABIs. The hosted CI emulator is x86_64,
        // so do not initialize the JNI static loader on an ABI it may not contain.
        val spotterClass = Class.forName(
            "com.k2fsa.sherpa.onnx.KeywordSpotter",
            false,
            javaClass.classLoader
        )
        assertNotNull(spotterClass)
    }
}
