package com.example.stability.voiceassistant

import com.example.stability.voiceassistant.data.model.IntentType
import com.example.stability.voiceassistant.manager.IntentParser
import org.junit.Test
import org.junit.Assert.*

class IntentParserTest {

    private val intentParser = IntentParser()

    @Test
    fun testNavigationIntentDetection() {
        val command = intentParser.parse("导航到天安门")
        assertEquals(IntentType.NAVIGATION, command.intentType)
        assertEquals("天安门", command.parameters["destination"])
    }

    @Test
    fun testNavigationIntent_WithRouteType() {
        val command = intentParser.parse("导航到故宫走最快路线")
        assertEquals(IntentType.NAVIGATION, command.intentType)
        assertEquals("故宫走最快路线", command.parameters["destination"])
        assertEquals("fastest", command.parameters["route_type"])
    }

    @Test
    fun testMediaIntent_Play() {
        val command = intentParser.parse("播放周杰伦的歌")
        assertEquals(IntentType.MEDIA, command.intentType)
        assertEquals("play", command.parameters["action"])
        assertEquals("周杰伦的歌", command.parameters["content"])
    }

    @Test
    fun testMediaIntent_Pause() {
        val command = intentParser.parse("暂停播放")
        assertEquals(IntentType.MEDIA, command.intentType)
        assertEquals("pause", command.parameters["action"])
    }

    @Test
    fun testMediaIntent_VolumeUp() {
        val command = intentParser.parse("调高音量")
        assertEquals(IntentType.MEDIA, command.intentType)
        assertEquals("volume", command.parameters["action"])
        assertEquals("up", command.parameters["value"])
    }

    @Test
    fun testMediaIntent_VolumeDown() {
        val command = intentParser.parse("调低音量")
        assertEquals(IntentType.MEDIA, command.intentType)
        assertEquals("volume", command.parameters["action"])
        assertEquals("down", command.parameters["value"])
    }

    @Test
    fun testAirConditionIntent_TemperatureUp() {
        val command = intentParser.parse("调高温度")
        assertEquals(IntentType.AIR_CONDITION, command.intentType)
        assertEquals("temperature", command.parameters["target"])
        assertEquals("up", command.parameters["value"])
    }

    @Test
    fun testAirConditionIntent_SetTemperature() {
        val command = intentParser.parse("温度调到26度")
        assertEquals(IntentType.AIR_CONDITION, command.intentType)
        assertEquals("temperature", command.parameters["target"])
        assertEquals("26", command.parameters["value"])
    }

    @Test
    fun testAirConditionIntent_ModeCool() {
        val command = intentParser.parse("空调切换到制冷模式")
        assertEquals(IntentType.AIR_CONDITION, command.intentType)
        assertEquals("mode", command.parameters["target"])
        assertEquals("cool", command.parameters["value"])
    }

    @Test
    fun testWindowIntent_OpenDriver() {
        val command = intentParser.parse("打开主驾车窗")
        assertEquals(IntentType.WINDOW, command.intentType)
        assertEquals("open", command.parameters["action"])
        assertEquals("driver", command.parameters["position"])
    }

    @Test
    fun testWindowIntent_CloseAll() {
        val command = intentParser.parse("关闭所有车窗")
        assertEquals(IntentType.WINDOW, command.intentType)
        assertEquals("close", command.parameters["action"])
        assertEquals("all", command.parameters["position"])
    }

    @Test
    fun testUnknownIntent() {
        val command = intentParser.parse("今天天气怎么样")
        assertEquals(IntentType.UNKNOWN, command.intentType)
    }

    @Test
    fun testWakeUpIntent() {
        val command = intentParser.parse("你好，小迪")
        assertEquals(IntentType.WAKE_UP, command.intentType)
    }
}
