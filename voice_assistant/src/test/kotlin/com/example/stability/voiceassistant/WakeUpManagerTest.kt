package com.example.stability.voiceassistant

import com.example.stability.voiceassistant.manager.WakeUpManager
import org.junit.Test
import org.junit.Assert.*

class WakeUpManagerTest {

    @Test
    fun testWakeWordDetection_ExactMatch_ShouldReturnTrue() {
        val wakeUpManager = WakeUpManager()
        
        assertTrue("'你好，小迪' should be detected as wake word", 
            wakeUpManager.isWakeWord("你好，小迪"))
    }

    @Test
    fun testWakeWordDetection_AlternativeMatch_ShouldReturnTrue() {
        val wakeUpManager = WakeUpManager()
        
        assertTrue("'你好小迪' should be detected as wake word", 
            wakeUpManager.isWakeWord("你好小迪"))
    }

    @Test
    fun testWakeWordDetection_NotMatch_ShouldReturnFalse() {
        val wakeUpManager = WakeUpManager()
        
        assertFalse("'你好' should not be detected as wake word", 
            wakeUpManager.isWakeWord("你好"))
        assertFalse("'小迪' should not be detected as wake word", 
            wakeUpManager.isWakeWord("小迪"))
        assertFalse("'hello' should not be detected as wake word", 
            wakeUpManager.isWakeWord("hello"))
        assertFalse("'打开空调' should not be detected as wake word", 
            wakeUpManager.isWakeWord("打开空调"))
    }

    @Test
    fun testWakeWordDetection_WithSpaces_ShouldReturnTrue() {
        val wakeUpManager = WakeUpManager()
        
        assertTrue("' 你好，小迪 ' should be detected as wake word", 
            wakeUpManager.isWakeWord(" 你好，小迪 "))
    }
}
