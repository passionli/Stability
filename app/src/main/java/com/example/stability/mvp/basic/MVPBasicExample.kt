package com.example.stability.mvp.basic

import android.util.Log

/**
 * MVP 基础概念示例
 * 本示例演示 MVP 架构的核心概念和基本组件
 */
class MVPBasicExample {
    
    companion object {
        private const val TAG = "MVP"
    }
    
    /**
     * 启动基础示例
     * 设计原因：提供一个入口点来演示 MVP 的基本概念
     * 技术目的：初始化并运行 MVP 基础示例
     */
    fun start() {
        Log.d(TAG, "=== MVP 基础概念示例启动 ===")
        
        // 1. MVP 架构概述
        demonstrateMVPOverview()
        
        // 2. MVP 三层职责
        demonstrateMVPResponsibilities()
        
        // 3. MVP vs MVC vs MVVM
        demonstrateMVPComparison()
        
        // 4. MVP 简单示例
        demonstrateSimpleMVPExample()
        
        Log.d(TAG, "=== MVP 基础概念示例完成 ===")
    }
    
    /**
     * 演示 MVP 架构概述
     * 设计原因：帮助理解 MVP 的定义和核心思想
     * 技术目的：说明 MVP 是什么以及为什么需要它
     */
    private fun demonstrateMVPOverview() {
        Log.d(TAG, "")
        Log.d(TAG, "========== MVP 架构概述 ==========")
        Log.d(TAG, "")
        Log.d(TAG, "MVP (Model-View-Presenter) 是一种 UI 架构模式")
        Log.d(TAG, "")
        Log.d(TAG, "核心思想：")
        Log.d(TAG, "  - 将 UI 逻辑和业务逻辑分离")
        Log.d(TAG, "  - View 不直接访问 Model")
        Log.d(TAG, "  - Presenter 充当 View 和 Model 之间的中介")
        Log.d(TAG, "")
        Log.d(TAG, "MVP 的三个核心组件：")
        Log.d(TAG, "")
        Log.d(TAG, "  1. Model (模型)")
        Log.d(TAG, "     - 负责数据和业务逻辑")
        Log.d(TAG, "     - 处理数据获取、存储和验证")
        Log.d(TAG, "     - 不直接与 View 通信")
        Log.d(TAG, "")
        Log.d(TAG, "  2. View (视图)")
        Log.d(TAG, "     - 负责 UI 渲染和用户交互")
        Log.d(TAG, "     - 被动的，不包含业务逻辑")
        Log.d(TAG, "     - 通过接口与 Presenter 通信")
        Log.d(TAG, "")
        Log.d(TAG, "  3. Presenter (展示器)")
        Log.d(TAG, "     - 充当 Model 和 View 之间的中介")
        Log.d(TAG, "     - 处理用户输入，更新 View")
        Log.d(TAG, "     - 从 Model 获取数据并格式化后展示")
        Log.d(TAG, "")
        Log.d(TAG, "MVP 工作流程：")
        Log.d(TAG, "  用户操作 View")
        Log.d(TAG, "    → View 调用 Presenter 的方法")
        Log.d(TAG, "    → Presenter 处理业务逻辑")
        Log.d(TAG, "    → Presenter 从 Model 获取数据")
        Log.d(TAG, "    → Presenter 更新 View（通过接口）")
        Log.d(TAG, "")
        Log.d(TAG, "==================================")
    }
    
    /**
     * 演示 MVP 三层职责
     * 设计原因：详细说明每层的具体职责
     * 技术目的：帮助理解如何在代码中划分职责
     */
    private fun demonstrateMVPResponsibilities() {
        Log.d(TAG, "")
        Log.d(TAG, "========== MVP 三层职责 ==========")
        Log.d(TAG, "")
        Log.d(TAG, "Model 层职责：")
        Log.d(TAG, "  1. 数据管理")
        Log.d(TAG, "     - 从网络、数据库或本地存储获取数据")
        Log.d(TAG, "     - 数据验证和转换")
        Log.d(TAG, "     - 缓存管理")
        Log.d(TAG, "")
        Log.d(TAG, "  2. 业务逻辑")
        Log.d(TAG, "     - 数据处理和计算")
        Log.d(TAG, "     - 业务规则验证")
        Log.d(TAG, "     - 依赖的服务调用")
        Log.d(TAG, "")
        Log.d(TAG, "  3. 数据接口示例：")
        Log.d(TAG, "     interface UserModel {")
        Log.d(TAG, "       fun getUser(id: String): User")
        Log.d(TAG, "       fun saveUser(user: User): Boolean")
        Log.d(TAG, "       fun deleteUser(id: String): Boolean")
        Log.d(TAG, "     }")
        Log.d(TAG, "")
        Log.d(TAG, "View 层职责：")
        Log.d(TAG, "  1. UI 展示")
        Log.d(TAG, "     - 显示数据")
        Log.d(TAG, "     - 渲染布局")
        Log.d(TAG, "     - 处理动画效果")
        Log.d(TAG, "")
        Log.d(TAG, "  2. 用户交互")
        Log.d(TAG, "     - 接收用户输入")
        Log.d(TAG, "     - 转发事件给 Presenter")
        Log.d(TAG, "     - 显示提示信息")
        Log.d(TAG, "")
        Log.d(TAG, "  3. View 接口示例：")
        Log.d(TAG, "     interface UserView {")
        Log.d(TAG, "       fun showUser(user: User)")
        Log.d(TAG, "       fun showLoading()")
        Log.d(TAG, "       fun hideLoading()")
        Log.d(TAG, "       fun showError(message: String)")
        Log.d(TAG, "     }")
        Log.d(TAG, "")
        Log.d(TAG, "Presenter 层职责：")
        Log.d(TAG, "  1. 业务协调")
        Log.d(TAG, "     - 响应 View 的用户操作")
        Log.d(TAG, "     - 调用 Model 处理业务")
        Log.d(TAG, "     - 更新 View 显示结果")
        Log.d(TAG, "")
        Log.d(TAG, "  2. 生命周期管理")
        Log.d(TAG, "     - 处理 View 的创建和销毁")
        Log.d(TAG, "     - 管理资源订阅")
        Log.d(TAG, "     - 防止内存泄漏")
        Log.d(TAG, "")
        Log.d(TAG, "  3. Presenter 示例：")
        Log.d(TAG, "     class UserPresenter(VAL userView: UserView) {")
        Log.d(TAG, "       private val userModel = UserModelImpl()")
        Log.d(TAG, "")
        Log.d(TAG, "       fun loadUser(id: String) {")
        Log.d(TAG, "         userView.showLoading()")
        Log.d(TAG, "         val user = userModel.getUser(id)")
        Log.d(TAG, "         userView.hideLoading()")
        Log.d(TAG, "         userView.showUser(user)")
        Log.d(TAG, "       }")
        Log.d(TAG, "     }")
        Log.d(TAG, "")
        Log.d(TAG, "=================================")
    }
    
    /**
     * 演示 MVP vs MVC vs MVVM
     * 设计原因：帮助理解 MVP 在众多架构模式中的位置
     * 技术目的：通过对比加深对 MVP 的理解
     */
    private fun demonstrateMVPComparison() {
        Log.d(TAG, "")
        Log.d(TAG, "========== MVP vs MVC vs MVVM ==========")
        Log.d(TAG, "")
        Log.d(TAG, "MVC (Model-View-Controller)：")
        Log.d(TAG, "  - Controller 处理用户输入")
        Log.d(TAG, "  - Model 和 View 直接通信")
        Log.d(TAG, "  - 适用于桌面应用")
        Log.d(TAG, "  通信：View → Controller → Model → View")
        Log.d(TAG, "")
        Log.d(TAG, "MVP (Model-View-Presenter)：")
        Log.d(TAG, "  - Presenter 充当 View 和 Model 的中介")
        Log.d(TAG, "  - View 和 Model 不直接通信")
        Log.d(TAG, "  - 适用于 Android 应用")
        Log.d(TAG, "  通信：View ↔ Presenter ↔ Model")
        Log.d(TAG, "")
        Log.d(TAG, "MVVM (Model-View-ViewModel)：")
        Log.d(TAG, "  - ViewModel 持有 UI 状态")
        Log.d(TAG, "  - 支持双向数据绑定")
        Log.d(TAG, "  - 适用于声明式 UI（如 Jetpack Compose）")
        Log.d(TAG, "  通信：View ↔ ViewModel ↔ Model")
        Log.d(TAG, "")
        Log.d(TAG, "MVP 的优势：")
        Log.d(TAG, "  1. 清晰的关注点分离")
        Log.d(TAG, "  2. 易于单元测试（Presenter 不依赖 Android）")
        Log.d(TAG, "  3. View 和 Model 完全解耦")
        Log.d(TAG, "  4. 逻辑集中在 Presenter，易于维护")
        Log.d(TAG, "")
        Log.d(TAG, "MVP 的劣势：")
        Log.d(TAG, "  1. View 和 Presenter 接口多")
        Log.d(TAG, "  2. 简单的 UI 可能过度设计")
        Log.d(TAG, "  3. Presenter 可能变得臃肿")
        Log.d(TAG, "")
        Log.d(TAG, "========================================")
    }
    
    /**
     * 演示简单的 MVP 示例
     * 设计原因：通过代码示例展示 MVP 的实际实现
     * 技术目的：提供可参考的 MVP 模式模板
     */
    private fun demonstrateSimpleMVPExample() {
        Log.d(TAG, "")
        Log.d(TAG, "========== 简单 MVP 示例 ==========")
        Log.d(TAG, "")
        Log.d(TAG, "// 1. 定义 View 接口")
        Log.d(TAG, "interface LoginView {")
        Log.d(TAG, "    fun showLoading()")
        Log.d(TAG, "    fun hideLoading()")
        Log.d(TAG, "    fun showSuccess(message: String)")
        Log.d(TAG, "    fun showError(message: String)")
        Log.d(TAG, "    fun navigateToHome()")
        Log.d(TAG, "}")
        Log.d(TAG, "")
        Log.d(TAG, "// 2. 定义 Model 接口")
        Log.d(TAG, "interface LoginModel {")
        Log.d(TAG, "    fun login(username: String, password: String,")
        Log.d(TAG, "                  callback: (Boolean, String) -> Unit)")
        Log.d(TAG, "}")
        Log.d(TAG, "")
        Log.d(TAG, "// 3. 实现 Model")
        Log.d(TAG, "class LoginModelImpl : LoginModel {")
        Log.d(TAG, "    override fun login(username: String, password: String,")
        Log.d(TAG, "                          callback: (Boolean, String) -> Unit) {")
        Log.d(TAG, "        // 模拟网络请求")
        Log.d(TAG, "        Thread {")
        Log.d(TAG, "            Thread.sleep(1000) // 模拟延迟")
        Log.d(TAG, "            if (username == \"admin\" && password == \"123456\") {")
        Log.d(TAG, "                callback(true, \"登录成功\")")
        Log.d(TAG, "            } else {")
        Log.d(TAG, "                callback(false, \"用户名或密码错误\")")
        Log.d(TAG, "            }")
        Log.d(TAG, "        }.start()")
        Log.d(TAG, "    }")
        Log.d(TAG, "}")
        Log.d(TAG, "")
        Log.d(TAG, "// 4. 实现 Presenter")
        Log.d(TAG, "class LoginPresenter(val view: LoginView) {")
        Log.d(TAG, "    private val model: LoginModel = LoginModelImpl()")
        Log.d(TAG, "")
        Log.d(TAG, "    fun login(username: String, password: String) {")
        Log.d(TAG, "        // 验证输入")
        Log.d(TAG, "        if (username.isEmpty()) {")
        Log.d(TAG, "            view.showError(\"用户名不能为空\")")
        Log.d(TAG, "            return")
        Log.d(TAG, "        }")
        Log.d(TAG, "        if (password.isEmpty()) {")
        Log.d(TAG, "            view.showError(\"密码不能为空\")")
        Log.d(TAG, "            return")
        Log.d(TAG, "        }")
        Log.d(TAG, "")
        Log.d(TAG, "        // 调用 Model")
        Log.d(TAG, "        view.showLoading()")
        Log.d(TAG, "        model.login(username, password) { success, message ->")
        Log.d(TAG, "            view.hideLoading()")
        Log.d(TAG, "            if (success) {")
        Log.d(TAG, "                view.showSuccess(message)")
        Log.d(TAG, "                view.navigateToHome()")
        Log.d(TAG, "            } else {")
        Log.d(TAG, "                view.showError(message)")
        Log.d(TAG, "            }")
        Log.d(TAG, "        }")
        Log.d(TAG, "    }")
        Log.d(TAG, "}")
        Log.d(TAG, "")
        Log.d(TAG, "// 5. Activity 实现 View 接口")
        Log.d(TAG, "class LoginActivity : AppCompatActivity(), LoginView {")
        Log.d(TAG, "    private lateinit var presenter: LoginPresenter")
        Log.d(TAG, "")
        Log.d(TAG, "    override fun onCreate(savedInstanceState: Bundle?) {")
        Log.d(TAG, "        super.onCreate(savedInstanceState)")
        Log.d(TAG, "        presenter = LoginPresenter(this)")
        Log.d(TAG, "    }")
        Log.d(TAG, "")
        Log.d(TAG, "    fun onLoginClicked() {")
        Log.d(TAG, "        val username = usernameEditText.text.toString()")
        Log.d(TAG, "        val password = passwordEditText.text.toString()")
        Log.d(TAG, "        presenter.login(username, password)")
        Log.d(TAG, "    }")
        Log.d(TAG, "")
        Log.d(TAG, "    override fun showLoading() { progressBar.visibility = View.VISIBLE }")
        Log.d(TAG, "    override fun hideLoading() { progressBar.visibility = View.GONE }")
        Log.d(TAG, "    override fun showSuccess(message: String) {")
        Log.d(TAG, "        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()")
        Log.d(TAG, "    }")
        Log.d(TAG, "    override fun showError(message: String) {")
        Log.d(TAG, "        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()")
        Log.d(TAG, "    }")
        Log.d(TAG, "    override fun navigateToHome() {")
        Log.d(TAG, "        startActivity(Intent(this, HomeActivity::class.java))")
        Log.d(TAG, "    }")
        Log.d(TAG, "}")
        Log.d(TAG, "")
        Log.d(TAG, "====================================")
    }
}
