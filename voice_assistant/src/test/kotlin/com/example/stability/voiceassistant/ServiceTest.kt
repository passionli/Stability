package com.example.stability.voiceassistant

import com.example.stability.voiceassistant.data.model.VoiceCommand
import com.example.stability.voiceassistant.data.model.IntentType
import com.example.stability.voiceassistant.service.NavigationService
import com.example.stability.voiceassistant.service.MediaService
import com.example.stability.voiceassistant.service.AirConditionService
import com.example.stability.voiceassistant.service.WindowService
import org.junit.Test
import org.junit.Assert.*

class ServiceTest {

    @Test
    fun testNavigationService_HandleCommand() {
        val service = NavigationService()
        val command = VoiceCommand("导航到天安门", IntentType.NAVIGATION, mapOf("destination" to "天安门"))
        
        val response = service.handleCommand(command)
        
        assertTrue("Response should contain destination", response.contains("天安门"))
        assertTrue("Response should indicate navigation start", response.contains("导航"))
    }

    @Test
    fun testNavigationService_CancelNavigation() {
        val service = NavigationService()
        val command = VoiceCommand("取消导航", IntentType.NAVIGATION, mapOf("action" to "cancel"))
        
        val response = service.handleCommand(command)
        
        assertEquals("已取消导航", response)
    }

    @Test
    fun testMediaService_Play() {
        val service = MediaService()
        val command = VoiceCommand("播放音乐", IntentType.MEDIA, mapOf("action" to "play", "content" to "测试音乐"))
        
        val response = service.handleCommand(command)
        
        assertTrue("Response should contain song name", response.contains("测试音乐"))
        assertTrue(service.isPlaying())
    }

    @Test
    fun testMediaService_Pause() {
        val service = MediaService()
        service.handleCommand(VoiceCommand("播放音乐", IntentType.MEDIA, mapOf("action" to "play")))
        
        assertTrue("Service should be playing", service.isPlaying())
        
        val response = service.handleCommand(VoiceCommand("暂停", IntentType.MEDIA, mapOf("action" to "pause")))
        
        assertEquals("已暂停播放", response)
        assertFalse(service.isPlaying())
    }

    @Test
    fun testMediaService_VolumeControl() {
        val service = MediaService()
        
        assertEquals(50, service.getVolume())
        
        service.handleCommand(VoiceCommand("调高音量", IntentType.MEDIA, mapOf("action" to "volume", "value" to "up")))
        assertEquals(60, service.getVolume())
        
        service.handleCommand(VoiceCommand("调低音量", IntentType.MEDIA, mapOf("action" to "volume", "value" to "down")))
        assertEquals(50, service.getVolume())
    }

    @Test
    fun testAirConditionService_TurnOn() {
        val service = AirConditionService()
        
        assertFalse(service.isOn())
        
        val response = service.handleCommand(VoiceCommand("打开空调", IntentType.AIR_CONDITION, mapOf("action" to "on")))
        
        assertEquals("空调已开启", response)
        assertTrue(service.isOn())
    }

    @Test
    fun testAirConditionService_TemperatureControl() {
        val service = AirConditionService()
        service.handleCommand(VoiceCommand("打开空调", IntentType.AIR_CONDITION, mapOf("action" to "on")))
        
        assertEquals(24, service.getTemperature())
        
        service.handleCommand(VoiceCommand("调高温度", IntentType.AIR_CONDITION, mapOf("target" to "temperature", "value" to "up")))
        assertEquals(25, service.getTemperature())
        
        service.handleCommand(VoiceCommand("调低温度", IntentType.AIR_CONDITION, mapOf("target" to "temperature", "value" to "down")))
        assertEquals(24, service.getTemperature())
    }

    @Test
    fun testAirConditionService_SetSpecificTemperature() {
        val service = AirConditionService()
        service.handleCommand(VoiceCommand("打开空调", IntentType.AIR_CONDITION, mapOf("action" to "on")))
        
        val response = service.handleCommand(VoiceCommand("温度调到26度", IntentType.AIR_CONDITION, mapOf("target" to "temperature", "value" to "26")))
        
        assertTrue("Response should contain temperature", response.contains("26"))
        assertEquals(26, service.getTemperature())
    }

    @Test
    fun testWindowService_OpenDriverWindow() {
        val service = WindowService()
        
        assertFalse(service.getWindowState("driver"))
        
        val response = service.handleCommand(VoiceCommand("打开主驾车窗", IntentType.WINDOW, mapOf("action" to "open", "position" to "driver")))
        
        assertTrue("Response should indicate window opened", response.contains("打开"))
        assertTrue("Driver window should be open", service.getWindowState("driver"))
    }

    @Test
    fun testWindowService_CloseAllWindows() {
        val service = WindowService()
        service.handleCommand(VoiceCommand("打开所有车窗", IntentType.WINDOW, mapOf("action" to "open", "position" to "all")))
        
        assertTrue(service.getWindowState("driver"))
        assertTrue(service.getWindowState("passenger"))
        
        val response = service.handleCommand(VoiceCommand("关闭所有车窗", IntentType.WINDOW, mapOf("action" to "close", "position" to "all")))
        
        assertTrue("Response should indicate windows closed", response.contains("关闭"))
        assertFalse(service.getWindowState("driver"))
        assertFalse(service.getWindowState("passenger"))
    }
}
