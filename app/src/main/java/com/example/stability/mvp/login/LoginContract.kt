package com.example.stability.mvp.login

/**
 * 登录功能的 MVP 契约接口
 * 设计原因：将 View 和 Presenter 的接口放在一起，便于管理和理解
 * 技术目的：定义登录功能的交互协议
 */
interface LoginContract {
    
    /**
     * View 接口：定义 View 可以执行的操作
     * 设计原因：View 只负责 UI 相关操作，不包含业务逻辑
     * 技术目的：提供 Presenter 控制 View 的方法
     */
    interface View {
        /**
         * 显示加载状态
         * 技术目的：在登录过程中显示加载动画
         */
        fun showLoading()
        
        /**
         * 隐藏加载状态
         * 技术目的：登录完成后隐藏加载动画
         */
        fun hideLoading()
        
        /**
         * 显示用户名错误
         * 技术目的：提示用户用户名输入错误
         */
        fun showUsernameError(message: String)
        
        /**
         * 显示密码错误
         * 技术目的：提示用户密码输入错误
         */
        fun showPasswordError(message: String)
        
        /**
         * 显示登录成功
         * 技术目的：提示用户登录成功
         */
        fun showLoginSuccess()
        
        /**
         * 显示登录错误
         * 技术目的：提示用户登录失败的原因
         */
        fun showLoginError(message: String)
        
        /**
         * 跳转到主页
         * 技术目的：登录成功后导航到应用主页
         */
        fun navigateToHome()
    }
    
    /**
     * Presenter 接口：定义 Presenter 可以执行的操作
     * 设计原因：Presenter 负责业务逻辑，连接 View 和 Model
     * 技术目的：处理用户输入和业务逻辑
     */
    interface Presenter {
        /**
         * 绑定 View
         * 技术目的：建立 Presenter 和 View 的联系
         */
        fun attachView(view: View)
        
        /**
         * 解绑 View
         * 技术目的：防止内存泄漏，在 View 销毁时调用
         */
        fun detachView()
        
        /**
         * 执行登录操作
         * 技术目的：处理登录业务逻辑
         */
        fun login(username: String, password: String)
        
        /**
         * 检查会话状态
         * 技术目的：检查用户是否已登录
         */
        fun checkSession()
    }
    
    /**
     * Model 接口：定义数据操作
     * 设计原因：Model 负责数据处理，与 Presenter 分离
     * 技术目的：提供数据访问和处理能力
     */
    interface Model {
        /**
         * 执行登录
         * 技术目的：与服务器通信或本地验证
         */
        fun login(username: String, password: String, callback: LoginCallback)
        
        /**
         * 保存会话信息
         * 技术目的：存储登录状态
         */
        fun saveSession(token: String)
        
        /**
         * 获取会话信息
         * 技术目的：检查是否已登录
         */
        fun getSession(): String?
        
        /**
         * 清除会话信息
         * 技术目的：退出登录时使用
         */
        fun clearSession()
    }
    
    /**
     * 登录回调接口
     * 技术目的：处理登录异步操作的结果
     */
    interface LoginCallback {
        /**
         * 登录成功
         * 技术目的：通知 Presenter 登录成功
         */
        fun onSuccess(token: String)
        
        /**
         * 登录失败
         * 技术目的：通知 Presenter 登录失败的原因
         */
        fun onError(message: String)
    }
}