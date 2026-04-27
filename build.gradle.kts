// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 顶级构建文件，用于添加适用于所有子项目/模块的配置选项

plugins {
    // 插件配置
    alias(libs.plugins.android.application) apply false
    // 引入 Android 应用插件，但不应用到当前项目
    // 这样做是为了在子项目中可以使用该插件，而不需要在每个子项目中单独声明版本
    
    alias(libs.plugins.android.library) apply false
    // 引入 Android 库插件，但不应用到当前项目
    // 同样是为了在子项目中可以使用该插件，而不需要在每个子项目中单独声明版本
}