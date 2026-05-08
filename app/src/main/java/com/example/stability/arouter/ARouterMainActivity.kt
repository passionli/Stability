package com.example.stability.arouter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.stability.R

@Route(path = "/arouter/main")
class ARouterMainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ARouterMain"
        private const val ROUTE_PATH = "/arouter/main"
    }

    @Autowired(name = "userName")
    @JvmField
    var userName: String? = null

    @Autowired(name = "userAge")
    @JvmField
    var userAge: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== ARouterMainActivity.onCreate() called ===")
        Log.d(TAG, "Intent: $intent")
        Log.d(TAG, "Intent extras: ${intent.extras}")
        Log.d(TAG, "Intent data: ${intent.data}")

        super.onCreate(savedInstanceState)

        Log.d(TAG, "Before ARouter.inject()")
        ARouter.getInstance().inject(this)
        Log.d(TAG, "After ARouter.inject()")
        Log.d(TAG, "userName after inject: $userName")
        Log.d(TAG, "userAge after inject: $userAge")

        if (userName == null) {
            userName = intent.getStringExtra("userName")
            Log.d(TAG, "userName from Intent: $userName")
        }
        if (userAge == 0) {
            userAge = intent.getIntExtra("userAge", 0)
            Log.d(TAG, "userAge from Intent: $userAge")
        }

        setContentView(R.layout.activity_arouter_main)

        setupUI()
    }

    private fun setupUI() {
        Log.d(TAG, "setupUI() called")

        val container = findViewById<LinearLayout>(R.id.arouter_container)
        container.orientation = LinearLayout.VERTICAL

        val receivedData = TextView(this).apply {
            text = "接收到的参数:\nuserName=$userName\nuserAge=$userAge"
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        container.addView(receivedData)

        val buttons = listOf(
            "简单路由跳转" to "/arouter/simple",
            "带参数路由跳转" to "/arouter/withParams",
            "使用Bundle传递复杂数据" to "/arouter/bundle",
            "获取服务" to "service",
            "路由分组" to "/arouter/group/test",
            "Uri跳转" to "uri"
        )

        buttons.forEach { (text, path) ->
            Button(this).apply {
                this.text = text
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 0)
                }
                setOnClickListener {
                    Log.d(TAG, "Button clicked: $text, path: $path")
                    when (path) {
                        "service" -> navigateToService()
                        "uri" -> navigateByUri()
                        "/arouter/withParams" -> navigateWithParams()
                        "/arouter/bundle" -> navigateWithBundle()
                        else -> navigateToPath(path)
                    }
                }
            }.also { container.addView(it) }
        }
    }

    private fun navigateToPath(path: String) {
        Log.d(TAG, "navigateToPath() called with path: $path")
        try {
            Log.d(TAG, "Building route: $path")
            val postcard = ARouter.getInstance().build(path)
            Log.d(TAG, "Postcard created: $postcard")
            Log.d(TAG, "Navigating...")
            postcard.navigation()
            Log.d(TAG, "navigation() called successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed", e)
            Toast.makeText(this, "路由失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToService() {
        Log.d(TAG, "navigateToService() called")
        try {
            val helloService = ARouter.getInstance().navigation(IHelloService::class.java)
            Log.d(TAG, "HelloService: $helloService")
            if (helloService != null) {
                helloService.sayHello("ARouter")
                Log.d(TAG, "sayHello() called")
            } else {
                Toast.makeText(this, "服务未找到", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Service navigation failed", e)
            Toast.makeText(this, "服务调用失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateWithParams() {
        Log.d(TAG, "navigateWithParams() called")
        try {
            ARouter.getInstance()
                .build("/arouter/withParams")
                .withString("name", "测试用户")
                .withInt("count", 100)
                .withBoolean("isVip", true)
                .navigation()
            Log.d(TAG, "WithParams navigation() called")
        } catch (e: Exception) {
            Log.e(TAG, "WithParams navigation failed", e)
            Toast.makeText(this, "路由失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateWithBundle() {
        Log.d(TAG, "navigateWithBundle() called")
        try {
            val userInfo = BundleActivity.UserInfo(
                userId = "U123456",
                userName = "张三",
                userAge = 28,
                userEmail = "zhangsan@example.com"
            )
            ARouter.getInstance()
                .build("/arouter/bundle")
                .withParcelable("userInfo", userInfo)
                .navigation()
            Log.d(TAG, "Bundle navigation() called")
        } catch (e: Exception) {
            Log.e(TAG, "Bundle navigation failed", e)
            Toast.makeText(this, "路由失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateByUri() {
        Log.d(TAG, "navigateByUri() called")
        try {
            val uri = android.net.Uri.parse("arouter://example.com/arouter/uriTest?source=main&type=test")
            Log.d(TAG, "Uri: $uri")
            ARouter.getInstance().build(uri).navigation()
            Log.d(TAG, "Uri navigation() called")
        } catch (e: Exception) {
            Log.e(TAG, "Uri navigation failed", e)
            Toast.makeText(this, "路由失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun start(context: Context) {
        Log.d("ARouterMainActivity", "start() called, using direct Intent")
        val intent = android.content.Intent(context, ARouterMainActivity::class.java)
        context.startActivity(intent)
    }

    fun startWithParams(context: Context, userName: String, age: Int) {
        val intent = android.content.Intent(context, ARouterMainActivity::class.java)
        intent.putExtra("userName", userName)
        intent.putExtra("userAge", age)
        context.startActivity(intent)
    }

    fun startWithARouter(context: Context) {
        Log.d("ARouterMainActivity", "startWithARouter() called")
        try {
            ARouter.getInstance().build("/arouter/main").navigation(context)
            Log.d("ARouterMainActivity", "ARouter navigation successful")
        } catch (e: Exception) {
            Log.e("ARouterMainActivity", "ARouter navigation failed", e)
            start(context)
        }
    }
}