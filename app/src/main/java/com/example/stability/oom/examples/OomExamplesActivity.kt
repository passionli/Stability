package com.example.stability.oom.examples

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.oom.utils.OomLog

/**
 * OOM 示例演示活动
 * 展示各种经典 OOM 场景示例，用户可以选择并运行
 */
class OomExamplesActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private lateinit var btnRun: Button
    private lateinit var tvStatus: TextView
    private var selectedExample: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oom_examples)
        
        // 初始化视图
        listView = findViewById(R.id.lv_examples)
        btnRun = findViewById(R.id.btn_run)
        tvStatus = findViewById(R.id.tv_status)
        
        // 设置列表适配器
        val examples = OomExamples.getExampleNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, examples)
        listView.adapter = adapter
        
        // 设置列表点击事件
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedExample = examples[position]
            tvStatus.text = "已选择: $selectedExample"
            btnRun.isEnabled = true
        }
        
        // 设置运行按钮点击事件
        btnRun.setOnClickListener {
            selectedExample?.let { exampleName ->
                runExample(exampleName)
            }
        }
        
        // 初始化状态
        btnRun.isEnabled = false
        tvStatus.text = "请选择一个 OOM 示例"
    }
    
    /**
     * 运行选中的示例
     */
    private fun runExample(exampleName: String) {
        tvStatus.text = "正在运行: $exampleName...\n请稍候..."
        btnRun.isEnabled = false
        
        // 在后台线程运行示例，避免阻塞主线程
        Thread {
            try {
                val triggered = OomExamples.runExampleByName(exampleName, this@OomExamplesActivity)
                
                runOnUiThread {
                    if (triggered) {
                        tvStatus.text = "✅ $exampleName\n已成功触发 OOM！"
                    } else {
                        tvStatus.text = "⚠️ $exampleName\n未触发 OOM（可能内存充足）"
                    }
                    btnRun.isEnabled = true
                }
            } catch (e: Exception) {
                OomLog.e("OomExamplesActivity", "Error running example: $exampleName", e)
                runOnUiThread {
                    tvStatus.text = "❌ $exampleName\n运行失败: ${e.message}"
                    btnRun.isEnabled = true
                }
            }
        }.start()
    }
}
