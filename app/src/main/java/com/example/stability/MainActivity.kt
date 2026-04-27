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
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        println(PhoneWindow2::class.java)

        super.onCreate(savedInstanceState)
//        var i = 0
//        do {
//            Thread(Runnable() {
//                if (window != null) {
//                    println("getWindow $window")
//                    // 模拟子线程调用 getDecorView 偶现崩溃
//                    window.decorView.toString()
//                }
//            }).start()
//            i++
//        } while (i < 8)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 查找 Kotlin 学习按钮
        val btnKotlinLearning = findViewById<Button>(R.id.btnKotlinLearning)
        // 设置按钮点击事件
        btnKotlinLearning.setOnClickListener {
            Log.d("KotlinLearning", "=== Button clicked: Run Kotlin Learning Examples ===")
            // 创建 KotlinLearningMain 实例
            val kotlinLearningMain = KotlinLearningMain()
            // 运行所有 Kotlin 学习示例
            kotlinLearningMain.runAllExamples()
        }
        
        // 查找 ListView 示例按钮
        val btnListView = findViewById<Button>(R.id.btnListView)
        // 设置按钮点击事件
        btnListView.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: ListView Example ===")
            // 创建 Intent，启动 ListViewActivity
            val intent = Intent(this, ListViewActivity::class.java)
            startActivity(intent)
        }

        // 查找 Compose 示例按钮
        val btnCompose = findViewById<Button>(R.id.btnCompose)
        // 设置按钮点击事件
        btnCompose.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Compose Example ===")
            // 创建 Intent，启动 ComposeActivity
            val intent = Intent(this, ComposeActivity::class.java)
            startActivity(intent)
        }
        
        // 查找数据结构示例按钮
        val btnDataStructures = findViewById<Button>(R.id.btnDataStructures)
        // 设置按钮点击事件
        btnDataStructures.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Data Structures Examples ===")
            // 创建 DataStructuresMain 实例
            val dataStructuresMain = DataStructuresMain()
            // 运行所有数据结构示例
            dataStructuresMain.runAllExamples()
        }
        
        // 查找 OpenGL 示例按钮
        val btnOpenGL = findViewById<Button>(R.id.btnOpenGL)
        // 设置按钮点击事件
        btnOpenGL.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: OpenGL Examples ===")
            
            // 创建一个菜单，让用户选择要运行的 OpenGL 示例级别
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择 OpenGL 示例级别")
            builder.setItems(arrayOf("初级（三角形）", "中级（彩色四边形）", "中级（变换组合）", "高级（纹理立方体）")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 启动初级 OpenGL 示例
                        val intent = Intent(this, OpenGLActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        // 启动中级 OpenGL 示例
                        val intent = Intent(this, com.example.stability.opengl.intermediate.OpenGLIntermediateActivity::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        // 启动中级 OpenGL 变换组合示例
                        val intent = Intent(this, com.example.stability.opengl.intermediate.OpenGLTransformActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        // 启动高级 OpenGL 示例
                        val intent = Intent(this, com.example.stability.opengl.advanced.OpenGLAdvancedActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            builder.show()
        }
        
        // 查找多线程示例按钮
        val btnMultithreading = findViewById<Button>(R.id.btnMultithreading)
        // 设置按钮点击事件
        btnMultithreading.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Multithreading Examples ===")
            
            // 创建一个菜单，让用户选择要运行的多线程示例级别
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择多线程示例级别")
            builder.setItems(arrayOf("初级（基础线程）", "中级（线程同步）", "高级（并发工具）", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行初级多线程示例
                        val basicExample = com.example.stability.multithreading.basic.BasicThreadExample()
                        basicExample.runAllExamples()
                    }
                    1 -> {
                        // 运行中级多线程示例
                        val intermediateExample = com.example.stability.multithreading.intermediate.IntermediateThreadExample()
                        intermediateExample.runAllExamples()
                    }
                    2 -> {
                        // 运行高级多线程示例
                        val advancedExample = com.example.stability.multithreading.advanced.AdvancedThreadExample()
                        advancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有多线程示例
                        val multithreadingMain = com.example.stability.multithreading.MultithreadingMain(this)
                        multithreadingMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找 C++ 示例按钮
        val btnCpp = findViewById<Button>(R.id.btnCpp)
        // 设置按钮点击事件
        btnCpp.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: C++ Examples ===")
            
            // 创建一个菜单，让用户选择要运行的 C++ 示例级别
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择 C++ 示例级别")
            builder.setItems(arrayOf("初级（基础语法）", "中级（面向对象）", "高级（模板与多线程）", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行初级 C++ 示例
                        val basicExample = com.example.stability.cpp.basic.BasicCppExample()
                        basicExample.runAllExamples()
                    }
                    1 -> {
                        // 运行中级 C++ 示例
                        val intermediateExample = com.example.stability.cpp.intermediate.IntermediateCppExample()
                        intermediateExample.runAllExamples()
                    }
                    2 -> {
                        // 运行高级 C++ 示例
                        val advancedExample = com.example.stability.cpp.advanced.AdvancedCppExample()
                        advancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有 C++ 示例
                        val cppMain = com.example.stability.cpp.CppMain(this)
                        cppMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找 C 语言示例按钮
        val btnC = findViewById<Button>(R.id.btnC)
        // 设置按钮点击事件
        btnC.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: C Examples ===")
            
            // 创建一个菜单，让用户选择要运行的 C 语言示例级别
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择 C 语言示例级别")
            builder.setItems(arrayOf("初级（基础语法）", "中级（指针与结构体）", "高级（内存管理与多线程）", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行初级 C 语言示例
                        val basicExample = com.example.stability.c.basic.BasicCExample()
                        basicExample.runAllExamples()
                    }
                    1 -> {
                        // 运行中级 C 语言示例
                        val intermediateExample = com.example.stability.c.intermediate.IntermediateCExample()
                        intermediateExample.runAllExamples()
                    }
                    2 -> {
                        // 运行高级 C 语言示例
                        val advancedExample = com.example.stability.c.advanced.AdvancedCExample()
                        advancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有 C 语言示例
                        val cMain = com.example.stability.c.CMain(this)
                        cMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找通信协议示例按钮
        val btnCommunication = findViewById<Button>(R.id.btnCommunication)
        // 设置按钮点击事件
        btnCommunication.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Communication Examples ===")
            
            // 创建一个菜单，让用户选择要运行的通信协议
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择通信协议")
            builder.setItems(arrayOf("BLE（蓝牙）", "USB", "Wi-Fi 直连", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行 BLE 示例
                        val bleBasicExample = com.example.stability.communication.ble.basic.BleBasicExample(this)
                        val bleIntermediateExample = com.example.stability.communication.ble.intermediate.BleIntermediateExample(this)
                        val bleAdvancedExample = com.example.stability.communication.ble.advanced.BleAdvancedExample(this)
                        bleBasicExample.runAllExamples()
                        bleIntermediateExample.runAllExamples()
                        bleAdvancedExample.runAllExamples()
                    }
                    1 -> {
                        // 运行 USB 示例
                        val usbBasicExample = com.example.stability.communication.usb.basic.UsbBasicExample(this)
                        val usbIntermediateExample = com.example.stability.communication.usb.intermediate.UsbIntermediateExample(this)
                        val usbAdvancedExample = com.example.stability.communication.usb.advanced.UsbAdvancedExample(this)
                        usbBasicExample.runAllExamples()
                        usbIntermediateExample.runAllExamples()
                        usbAdvancedExample.runAllExamples()
                    }
                    2 -> {
                        // 运行 Wi-Fi 直连示例
                        val wifiDirectBasicExample = com.example.stability.communication.wifi_direct.basic.WifiDirectBasicExample(this)
                        val wifiDirectIntermediateExample = com.example.stability.communication.wifi_direct.intermediate.WifiDirectIntermediateExample(this)
                        val wifiDirectAdvancedExample = com.example.stability.communication.wifi_direct.advanced.WifiDirectAdvancedExample(this)
                        wifiDirectBasicExample.runAllExamples()
                        wifiDirectIntermediateExample.runAllExamples()
                        wifiDirectAdvancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有通信协议示例
                        val communicationMain = com.example.stability.communication.CommunicationMain(this)
                        communicationMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找网络协议示例按钮
        val btnNetwork = findViewById<Button>(R.id.btnNetwork)
        // 设置按钮点击事件
        btnNetwork.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Network Examples ===")
            
            // 创建一个菜单，让用户选择要运行的网络协议
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择网络协议")
            builder.setItems(arrayOf("Socket", "TCP", "UDP", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行 Socket 示例
                        val socketBasicExample = com.example.stability.network.socket.basic.SocketBasicExample()
                        val socketIntermediateExample = com.example.stability.network.socket.intermediate.SocketIntermediateExample()
                        val socketAdvancedExample = com.example.stability.network.socket.advanced.SocketAdvancedExample()
                        socketBasicExample.runAllExamples()
                        socketIntermediateExample.runAllExamples()
                        socketAdvancedExample.runAllExamples()
                    }
                    1 -> {
                        // 运行 TCP 示例
                        val tcpBasicExample = com.example.stability.network.tcp.basic.TcpBasicExample()
                        val tcpIntermediateExample = com.example.stability.network.tcp.intermediate.TcpIntermediateExample()
                        val tcpAdvancedExample = com.example.stability.network.tcp.advanced.TcpAdvancedExample()
                        tcpBasicExample.runAllExamples()
                        tcpIntermediateExample.runAllExamples()
                        tcpAdvancedExample.runAllExamples()
                    }
                    2 -> {
                        // 运行 UDP 示例
                        val udpBasicExample = com.example.stability.network.udp.basic.UdpBasicExample()
                        val udpIntermediateExample = com.example.stability.network.udp.intermediate.UdpIntermediateExample()
                        val udpAdvancedExample = com.example.stability.network.udp.advanced.UdpAdvancedExample()
                        udpBasicExample.runAllExamples()
                        udpIntermediateExample.runAllExamples()
                        udpAdvancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有网络协议示例
                        val networkMain = com.example.stability.network.NetworkMain(this)
                        networkMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找设计模式示例按钮
        val btnDesignPatterns = findViewById<Button>(R.id.btnDesignPatterns)
        // 设置按钮点击事件
        btnDesignPatterns.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Design Patterns Examples ===")
            
            // 创建一个菜单，让用户选择要运行的设计模式
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("选择设计模式")
            builder.setItems(arrayOf("创建型", "结构型", "行为型", "运行所有示例")) { dialog, which ->
                when (which) {
                    0 -> {
                        // 运行创建型设计模式示例
                        val creationalBasicExample = com.example.stability.design_patterns.creational.basic.CreationalBasicExample()
                        val creationalIntermediateExample = com.example.stability.design_patterns.creational.intermediate.CreationalIntermediateExample()
                        val creationalAdvancedExample = com.example.stability.design_patterns.creational.advanced.CreationalAdvancedExample()
                        creationalBasicExample.runAllExamples()
                        creationalIntermediateExample.runAllExamples()
                        creationalAdvancedExample.runAllExamples()
                    }
                    1 -> {
                        // 运行结构型设计模式示例
                        val structuralBasicExample = com.example.stability.design_patterns.structural.basic.StructuralBasicExample()
                        val structuralIntermediateExample = com.example.stability.design_patterns.structural.intermediate.StructuralIntermediateExample()
                        val structuralAdvancedExample = com.example.stability.design_patterns.structural.advanced.StructuralAdvancedExample()
                        structuralBasicExample.runAllExamples()
                        structuralIntermediateExample.runAllExamples()
                        structuralAdvancedExample.runAllExamples()
                    }
                    2 -> {
                        // 运行行为型设计模式示例
                        val behavioralBasicExample = com.example.stability.design_patterns.behavioral.basic.BehavioralBasicExample()
                        val behavioralIntermediateExample = com.example.stability.design_patterns.behavioral.intermediate.BehavioralIntermediateExample()
                        val behavioralAdvancedExample = com.example.stability.design_patterns.behavioral.advanced.BehavioralAdvancedExample()
                        behavioralBasicExample.runAllExamples()
                        behavioralIntermediateExample.runAllExamples()
                        behavioralAdvancedExample.runAllExamples()
                    }
                    3 -> {
                        // 运行所有设计模式示例
                        val designPatternsMain = com.example.stability.design_patterns.DesignPatternsMain(this)
                        designPatternsMain.runAllExamples()
                    }
                }
            }
            builder.show()
        }
        
        // 查找 MVP 架构示例按钮
        val btnMVP = findViewById<Button>(R.id.btnMVP)
        // 设置按钮点击事件
        btnMVP.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: MVP Architecture Examples ===")
            
            // 启动 MVP 统一管理类
            val intent = Intent(this, MVPMain::class.java)
            startActivity(intent)
        }
        
        // 查找 WebRTC 示例按钮
        val btnWebRTC = findViewById<Button>(R.id.btnWebRTC)
        // 设置按钮点击事件
        btnWebRTC.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: WebRTC Examples ===")
            
            // 启动 WebRTC 统一管理类
            val intent = Intent(this, WebRTCMain::class.java)
            startActivity(intent)
        }
        
        // 查找视频编辑示例按钮
        val btnVideoEdit = findViewById<Button>(R.id.btnVideoEdit)
        // 设置按钮点击事件
        btnVideoEdit.setOnClickListener {
            Log.d("MainActivity", "=== Button clicked: Video Edit Examples ===")
            // 创建 Intent，启动 VideoEditActivity
            val intent = Intent(this, VideoEditActivity::class.java)
            startActivity(intent)
        }

        println("getWindow $window")

//        Handler().postDelayed({
//            throw IllegalStateException()
//        }, 5000)


        Thread {
            //先启动监控
//            val str = NativeLib().stringFromJNI()
//            println("stringFromJNI $str")

//            val count2 = NativeLib2().createPthreadKeyLeak()
//            println("Created $count2 pthread keys before failure")

        }.start()

        Thread {
            //后触发
//            val count = NativeLib().createPthreadKeyLeak()
//            println("Created $count pthread keys before failure")

        }.start()

    }

    override fun onResume() {
        super.onResume()
        // 注释掉自动启动其他 Activity 的代码，这样可以看到 MainActivity 中的按钮
        // startActivity(Intent(this, JetpackActivity::class.java))
        // startActivity(Intent(this, ComposeActivity::class.java))
    }
}