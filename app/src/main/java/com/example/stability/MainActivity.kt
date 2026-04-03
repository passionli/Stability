package com.example.stability

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nativelib.NativeLib
import com.example.nativelib2.NativeLib2

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



//        Handler().postDelayed({
//            throw IllegalStateException()
//        }, 5000)


        Thread {
            //先启动监控
            val str = NativeLib().stringFromJNI()
            println("stringFromJNI $str")

            val count2 = NativeLib2().createPthreadKeyLeak()
            println("Created $count2 pthread keys before failure")

        }.start()

        Thread {
            //后触发
//            val count = NativeLib().createPthreadKeyLeak()
//            println("Created $count pthread keys before failure")

        }.start()

    }
}