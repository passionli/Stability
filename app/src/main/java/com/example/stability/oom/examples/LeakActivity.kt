package com.example.stability.oom.examples

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stability.R
import com.example.stability.oom.detection.LeakCanaryManager
import com.example.stability.oom.utils.OomLog

/**
 * 内存泄漏示例 Activity
 * 提供 UI 界面用于测试和演示各种内存泄漏场景
 */
class LeakActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * 日志标签
     */
    private val TAG = "LeakActivity"

    /**
     * 泄漏示例列表视图
     */
    private lateinit var leakExamplesListView: ListView

    /**
     * 状态显示文本
     */
    private lateinit var statusTextView: TextView

    /**
     * 触发泄漏按钮
     */
    private lateinit var triggerLeakButton: Button

    /**
     * 修复泄漏按钮
     */
    private lateinit var fixLeakButton: Button

    /**
     * 获取状态按钮
     */
    private lateinit var getStatusButton: Button

    /**
     * 选中的泄漏示例名称
     */
    private var selectedLeakExample: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leak)

        // 初始化视图
        initViews()

        // 加载泄漏示例列表
        loadLeakExamples()

        OomLog.i(TAG, "LeakActivity created")
    }

    /**
     * 初始化视图组件
     */
    private fun initViews() {
        leakExamplesListView = findViewById(R.id.leak_examples_list)
        statusTextView = findViewById(R.id.status_text)
        triggerLeakButton = findViewById(R.id.trigger_leak_button)
        fixLeakButton = findViewById(R.id.fix_leak_button)
        getStatusButton = findViewById(R.id.get_status_button)

        // 设置点击事件
        triggerLeakButton.setOnClickListener(this)
        fixLeakButton.setOnClickListener(this)
        getStatusButton.setOnClickListener(this)

        // 设置列表点击事件
        leakExamplesListView.setOnItemClickListener { _, _, position, _ ->
            val examples = LeakExamples.getLeakExampleNames()
            selectedLeakExample = examples[position]
            statusTextView.text = "已选择: $selectedLeakExample"
            OomLog.d(TAG, "Selected leak example: $selectedLeakExample")
        }
    }

    /**
     * 加载泄漏示例列表
     */
    private fun loadLeakExamples() {
        val examples = LeakExamples.getLeakExampleNames()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            examples
        )
        leakExamplesListView.adapter = adapter

        // 默认选中第一个
        if (examples.isNotEmpty()) {
            selectedLeakExample = examples[0]
            leakExamplesListView.setItemChecked(0, true)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.trigger_leak_button -> {
                triggerSelectedLeak()
            }
            R.id.fix_leak_button -> {
                fixLeaks()
            }
            R.id.get_status_button -> {
                showStatus()
            }
        }
    }

    /**
     * 触发选中的泄漏示例
     */
    private fun triggerSelectedLeak() {
        if (selectedLeakExample.isNullOrEmpty()) {
            statusTextView.text = "请先选择一个泄漏示例"
            return
        }

        val success = LeakExamples.runLeakExample(selectedLeakExample!!, this)
        
        if (success) {
            statusTextView.text = "已触发泄漏: $selectedLeakExample\n\n" +
                    "提示：关闭此 Activity 后，LeakCanary 会检测到内存泄漏并显示通知"
            OomLog.w(TAG, "Leak triggered: $selectedLeakExample")
        } else {
            statusTextView.text = "触发泄漏失败: $selectedLeakExample"
            OomLog.e(TAG, "Failed to trigger leak: $selectedLeakExample")
        }
    }

    /**
     * 修复所有泄漏
     */
    private fun fixLeaks() {
        LeakExamples.fixAllLeaks()
        statusTextView.text = "已尝试修复所有泄漏\n\n" +
                "提示：修复后需要等待 GC 回收才能完全释放内存"
        OomLog.i(TAG, "All leaks fixed")
    }

    /**
     * 显示 LeakCanary 状态
     */
    private fun showStatus() {
        val status = LeakCanaryManager.getStatus()
        val configInfo = LeakCanaryManager.getConfigInfo()
        
        statusTextView.text = """
            LeakCanary 状态:
            
            $status
            
            配置信息:
            
            $configInfo
        """.trimIndent()
        
        OomLog.d(TAG, "LeakCanary status displayed")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 在 Activity 销毁时尝试清理
        // 注意：如果泄漏已经发生，这里的清理可能无法完全修复
        OomLog.i(TAG, "LeakActivity destroyed")
    }
}
