package com.example.stability

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.internal.policy.PhoneWindow2
import com.example.nativelib.NativeLib
import com.example.nativelib2.NativeLib2
import com.example.stability.data_structures.DataStructuresMain
import com.example.stability.kotlin_learning.KotlinLearningMain
import com.example.stability.mvp.MVPMain
import com.example.stability.opengl.basic.OpenGLActivity
import com.example.stability.video_edit.VideoEditActivity
import com.example.stability.webrtc.WebRTCMain
import com.example.stability.arouter.ARouterMainActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        println(PhoneWindow2::class.java)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 使用配置列表驱动 UI 构建
        setupButtons()

        println("getWindow $window")

        Thread {
            // 先启动监控
            // val str = NativeLib().stringFromJNI()
            // println("stringFromJNI $str")

            // val count2 = NativeLib2().createPthreadKeyLeak()
            // println("Created $count2 pthread keys before failure")

        }.start()

        Thread {
            // 后触发
            // val count = NativeLib().createPthreadKeyLeak()
            // println("Created $count pthread keys before failure")

        }.start()
    }

    /**
     * 设置所有按钮（使用配置驱动）
     */
    private fun setupButtons() {
        // 简单按钮配置列表（直接启动 Activity 或执行操作）
        val simpleButtons = listOf(
            ButtonConfig(R.id.btnKotlinLearning, "KotlinLearning") {
                val kotlinLearningMain = KotlinLearningMain()
                kotlinLearningMain.runAllExamples()
            },
            ButtonConfig(R.id.btnListView, "MainActivity") {
                navigateTo<ListViewActivity>()
            },
            ButtonConfig(R.id.btnCompose, "MainActivity") {
                navigateTo<ComposeActivity>()
            },
            ButtonConfig(R.id.btnDataStructures, "MainActivity") {
                val dataStructuresMain = DataStructuresMain()
                dataStructuresMain.runAllExamples()
            },
            ButtonConfig(R.id.btnMVP, "MainActivity") {
                navigateTo<MVPMain>()
            },
            ButtonConfig(R.id.btnWebRTC, "MainActivity") {
                navigateTo<WebRTCMain>()
            },
            ButtonConfig(R.id.btnVideoEdit, "MainActivity") {
                navigateTo<VideoEditActivity>()
            },
            ButtonConfig(R.id.btnANR, "MainActivity") {
                navigateTo<com.example.stability.anr.examples.AnrActivity>()
            },
            ButtonConfig(R.id.btnARouter, "MainActivity") {
                navigateTo<ARouterMainActivity>()
            },
            ButtonConfig(R.id.btnVoiceAssistant, "MainActivity") {
                // Voice Assistant 功能待实现
            }
        )

        // 绑定简单按钮
        simpleButtons.forEach { bindButton(it) }

        // 绑定带选项的按钮
        bindOptionButtons()
    }

    /**
     * 绑定带选项的按钮
     */
    private fun bindOptionButtons() {
        bindOptionButton(
            buttonId = R.id.btnOpenGL,
            title = "选择 OpenGL 示例级别",
            options = listOf(
                "初级（三角形）" to { navigateTo<OpenGLActivity>() },
                "中级（彩色四边形）" to { navigateTo<com.example.stability.opengl.intermediate.OpenGLIntermediateActivity>() },
                "中级（变换组合）" to { navigateTo<com.example.stability.opengl.intermediate.OpenGLTransformActivity>() },
                "高级（纹理立方体）" to { navigateTo<com.example.stability.opengl.advanced.OpenGLAdvancedActivity>() }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnMultithreading,
            title = "选择多线程示例级别",
            options = listOf(
                "初级（基础线程）" to {
                    com.example.stability.multithreading.basic.BasicThreadExample().runAllExamples()
                },
                "中级（线程同步）" to {
                    com.example.stability.multithreading.intermediate.IntermediateThreadExample().runAllExamples()
                },
                "高级（并发工具）" to {
                    com.example.stability.multithreading.advanced.AdvancedThreadExample().runAllExamples()
                },
                "运行所有示例" to {
                    com.example.stability.multithreading.MultithreadingMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnCpp,
            title = "选择 C++ 示例级别",
            options = listOf(
                "初级（基础语法）" to {
                    com.example.stability.cpp.basic.BasicCppExample().runAllExamples()
                },
                "中级（面向对象）" to {
                    com.example.stability.cpp.intermediate.IntermediateCppExample().runAllExamples()
                },
                "高级（模板与多线程）" to {
                    com.example.stability.cpp.advanced.AdvancedCppExample().runAllExamples()
                },
                "运行所有示例" to {
                    com.example.stability.cpp.CppMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnC,
            title = "选择 C 语言示例级别",
            options = listOf(
                "初级（基础语法）" to {
                    com.example.stability.c.basic.BasicCExample().runAllExamples()
                },
                "中级（指针与结构体）" to {
                    com.example.stability.c.intermediate.IntermediateCExample().runAllExamples()
                },
                "高级（内存管理与多线程）" to {
                    com.example.stability.c.advanced.AdvancedCExample().runAllExamples()
                },
                "运行所有示例" to {
                    com.example.stability.c.CMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnCommunication,
            title = "选择通信协议",
            options = listOf(
                "BLE（蓝牙）" to {
                    runBleExamples()
                },
                "USB" to {
                    runUsbExamples()
                },
                "Wi-Fi 直连" to {
                    runWifiDirectExamples()
                },
                "运行所有示例" to {
                    com.example.stability.communication.CommunicationMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnNetwork,
            title = "选择网络协议",
            options = listOf(
                "Socket" to {
                    runSocketExamples()
                },
                "TCP" to {
                    runTcpExamples()
                },
                "UDP" to {
                    runUdpExamples()
                },
                "运行所有示例" to {
                    com.example.stability.network.NetworkMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnDesignPatterns,
            title = "选择设计模式",
            options = listOf(
                "创建型" to {
                    runCreationalPatterns()
                },
                "结构型" to {
                    runStructuralPatterns()
                },
                "行为型" to {
                    runBehavioralPatterns()
                },
                "运行所有示例" to {
                    com.example.stability.design_patterns.DesignPatternsMain(this).runAllExamples()
                }
            )
        )

        bindOptionButton(
            buttonId = R.id.btnOOM,
            title = "选择内存相关示例",
            options = listOf(
                "OOM 示例" to {
                    navigateTo<com.example.stability.oom.examples.OomExamplesActivity>()
                },
                "内存泄漏检测（LeakCanary）" to {
                    navigateTo<com.example.stability.oom.examples.LeakActivity>()
                }
            )
        )
    }

    /**
     * 按钮配置数据类
     */
    private data class ButtonConfig(
        val buttonId: Int,
        val logTag: String,
        val action: () -> Unit
    )

    /**
     * 绑定简单按钮（高阶函数）
     */
    private fun bindButton(config: ButtonConfig) {
        findViewById<Button>(config.buttonId).setOnClickListener {
            Log.d(config.logTag, "=== Button clicked: ${resources.getResourceEntryName(config.buttonId)} ===")
            config.action()
        }
    }

    /**
     * 绑定带选项的按钮（高阶函数）
     */
    private fun bindOptionButton(buttonId: Int, title: String, options: List<Pair<String, () -> Unit>>) {
        findViewById<Button>(buttonId).setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: ${resources.getResourceEntryName(buttonId)} ===")
            
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setItems(options.map { it.first }.toTypedArray()) { _, which ->
                options[which].second()
            }
            builder.show()
        }
    }

    /**
     * 导航到指定 Activity（泛型高阶函数）
     */
    private inline fun <reified T> navigateTo() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    /**
     * 运行 BLE 示例
     */
    private fun runBleExamples() {
        com.example.stability.communication.ble.basic.BleBasicExample(this).runAllExamples()
        com.example.stability.communication.ble.intermediate.BleIntermediateExample(this).runAllExamples()
        com.example.stability.communication.ble.advanced.BleAdvancedExample(this).runAllExamples()
    }

    /**
     * 运行 USB 示例
     */
    private fun runUsbExamples() {
        com.example.stability.communication.usb.basic.UsbBasicExample(this).runAllExamples()
        com.example.stability.communication.usb.intermediate.UsbIntermediateExample(this).runAllExamples()
        com.example.stability.communication.usb.advanced.UsbAdvancedExample(this).runAllExamples()
    }

    /**
     * 运行 Wi-Fi 直连示例
     */
    private fun runWifiDirectExamples() {
        com.example.stability.communication.wifi_direct.basic.WifiDirectBasicExample(this).runAllExamples()
        com.example.stability.communication.wifi_direct.intermediate.WifiDirectIntermediateExample(this).runAllExamples()
        com.example.stability.communication.wifi_direct.advanced.WifiDirectAdvancedExample(this).runAllExamples()
    }

    /**
     * 运行 Socket 示例
     */
    private fun runSocketExamples() {
        com.example.stability.network.socket.basic.SocketBasicExample().runAllExamples()
        com.example.stability.network.socket.intermediate.SocketIntermediateExample().runAllExamples()
        com.example.stability.network.socket.advanced.SocketAdvancedExample().runAllExamples()
    }

    /**
     * 运行 TCP 示例
     */
    private fun runTcpExamples() {
        com.example.stability.network.tcp.basic.TcpBasicExample().runAllExamples()
        com.example.stability.network.tcp.intermediate.TcpIntermediateExample().runAllExamples()
        com.example.stability.network.tcp.advanced.TcpAdvancedExample().runAllExamples()
    }

    /**
     * 运行 UDP 示例
     */
    private fun runUdpExamples() {
        com.example.stability.network.udp.basic.UdpBasicExample().runAllExamples()
        com.example.stability.network.udp.intermediate.UdpIntermediateExample().runAllExamples()
        com.example.stability.network.udp.advanced.UdpAdvancedExample().runAllExamples()
    }

    /**
     * 运行创建型设计模式示例
     */
    private fun runCreationalPatterns() {
        com.example.stability.design_patterns.creational.basic.CreationalBasicExample().runAllExamples()
        com.example.stability.design_patterns.creational.intermediate.CreationalIntermediateExample().runAllExamples()
        com.example.stability.design_patterns.creational.advanced.CreationalAdvancedExample().runAllExamples()
    }

    /**
     * 运行结构型设计模式示例
     */
    private fun runStructuralPatterns() {
        com.example.stability.design_patterns.structural.basic.StructuralBasicExample().runAllExamples()
        com.example.stability.design_patterns.structural.intermediate.StructuralIntermediateExample().runAllExamples()
        com.example.stability.design_patterns.structural.advanced.StructuralAdvancedExample().runAllExamples()
    }

    /**
     * 运行行为型设计模式示例
     */
    private fun runBehavioralPatterns() {
        com.example.stability.design_patterns.behavioral.basic.BehavioralBasicExample().runAllExamples()
        com.example.stability.design_patterns.behavioral.intermediate.BehavioralIntermediateExample().runAllExamples()
        com.example.stability.design_patterns.behavioral.advanced.BehavioralAdvancedExample().runAllExamples()
    }

    override fun onResume() {
        super.onResume()
        // 注释掉自动启动其他 Activity 的代码
    }
}