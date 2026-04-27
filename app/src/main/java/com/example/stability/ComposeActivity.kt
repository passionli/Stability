package com.example.stability
// 包声明，指定当前文件所在的包名

import android.os.Bundle
// 导入 Bundle 类，用于在 Activity 之间传递数据
import androidx.activity.ComponentActivity
// 导入 ComponentActivity 类，这是 Compose 应用的基础 Activity 类
import androidx.activity.compose.setContent
// 导入 setContent 函数，用于设置 Compose 内容
import androidx.compose.foundation.layout.Arrangement
// 导入 Arrangement 类，用于设置布局中元素的排列方式
import androidx.compose.foundation.layout.Column
// 导入 Column 类，用于创建垂直方向的布局
import androidx.compose.foundation.layout.Spacer
// 导入 Spacer 类，用于在布局中添加空白空间
import androidx.compose.foundation.layout.fillMaxSize
// 导入 fillMaxSize 修饰符，用于让元素填充整个可用空间
import androidx.compose.foundation.layout.height
// 导入 height 修饰符，用于设置元素的高度
import androidx.compose.foundation.layout.padding
// 导入 padding 修饰符，用于为元素添加内边距
import androidx.compose.material3.Button
// 导入 Button 组件，用于创建按钮
import androidx.compose.material3.MaterialTheme
// 导入 MaterialTheme 类，用于应用 Material Design 样式
import androidx.compose.material3.Surface
// 导入 Surface 组件，用于创建一个带有背景色的表面
import androidx.compose.material3.Text
// 导入 Text 组件，用于显示文本
import androidx.compose.runtime.Composable
// 导入 Composable 注解，用于标记可组合函数
import androidx.compose.runtime.getValue
// 导入 getValue 委托，用于获取状态值
import androidx.compose.runtime.mutableIntStateOf
// 导入 mutableIntStateOf 函数，用于创建可变的整数状态
import androidx.compose.runtime.remember
// 导入 remember 函数，用于在重组之间保持状态
import androidx.compose.runtime.setValue
// 导入 setValue 委托，用于设置状态值
import androidx.compose.ui.Alignment
// 导入 Alignment 类，用于设置布局中元素的对齐方式
import androidx.compose.ui.Modifier
// 导入 Modifier 类，用于修改组件的行为和外观
import androidx.compose.ui.tooling.preview.Preview
// 导入 Preview 注解，用于在 Android Studio 中预览 Composable 函数
import androidx.compose.ui.unit.dp
// 导入 dp 单位，用于指定尺寸

class ComposeActivity : ComponentActivity() {
    // 创建一个继承自 ComponentActivity 的类，这是使用 Compose 的 Activity 基类
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 重写 onCreate 方法，这是 Activity 创建时的回调方法
        // savedInstanceState 参数用于保存和恢复 Activity 的状态
        
        super.onCreate(savedInstanceState)
        // 调用父类的 onCreate 方法，必须调用
        
        setContent {
            // 调用 setContent 函数，设置 Compose 内容
            // 这里的 lambda 表达式包含了所有的 Composable 函数调用
            
            MaterialTheme {
                // 使用 MaterialTheme 组件，应用 Material Design 样式
                // 这样可以确保应用中的组件具有一致的外观
                
                Surface(
                    // 使用 Surface 组件，创建一个带有背景色的表面
                    modifier = Modifier.fillMaxSize(),
                    // 设置 modifier，让 Surface 填充整个可用空间
                    color = MaterialTheme.colorScheme.background
                    // 设置 Surface 的背景色为 MaterialTheme 中定义的背景色
                ) {
                    ComposeExample()
                    // 调用 ComposeExample 可组合函数，显示示例内容
                }
            }
        }
    }
}

@Composable
// Composable 注解标记这是一个可组合函数，可以在其他 Composable 函数中调用
fun ComposeExample() {
    // 定义一个名为 ComposeExample 的可组合函数
    
    var count by remember { mutableIntStateOf(0) }
    // 创建一个可变的整数状态 count，初始值为 0
    // 使用 by remember 委托，确保 count 在 Composable 函数重组时保持状态
    // 这样即使屏幕旋转等操作导致组件重新绘制，count 的值也不会丢失

    Column(
        // 使用 Column 组件，创建垂直方向的布局
        modifier = Modifier
            // 设置 modifier
            .fillMaxSize()
            // 让 Column 填充整个可用空间
            .padding(16.dp),
            // 为 Column 添加 16dp 的内边距
        horizontalAlignment = Alignment.CenterHorizontally,
        // 设置水平方向的对齐方式为居中
        verticalArrangement = Arrangement.Center
        // 设置垂直方向的排列方式为居中
    ) {
        Text(
            // 使用 Text 组件，显示文本
            text = "Compose Example",
            // 设置文本内容
            style = MaterialTheme.typography.headlineMedium
            // 设置文本样式为 MaterialTheme 中定义的 headlineMedium 样式
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        // 使用 Spacer 组件，添加 32dp 高度的空白空间
        // 这样可以在标题和计数器之间创建间距
        
        Text(
            // 使用 Text 组件，显示计数器的值
            text = "Count: $count",
            // 设置文本内容，使用字符串模板显示 count 的值
            style = MaterialTheme.typography.displayLarge
            // 设置文本样式为 MaterialTheme 中定义的 displayLarge 样式
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        // 使用 Spacer 组件，添加 32dp 高度的空白空间
        // 这样可以在计数器和按钮之间创建间距
        
        Button(onClick = { count++ }) {
            // 使用 Button 组件，创建一个按钮
            // 设置点击事件监听器，当按钮被点击时，count 加 1
            Text("Increment")
            // 按钮内部显示的文本
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        // 使用 Spacer 组件，添加 16dp 高度的空白空间
        // 这样可以在两个按钮之间创建间距
        
        Button(onClick = { count = 0 }) {
            // 使用 Button 组件，创建一个按钮
            // 设置点击事件监听器，当按钮被点击时，count 重置为 0
            Text("Reset")
            // 按钮内部显示的文本
        }
    }
}

@Preview(showBackground = true)
// Preview 注解，用于在 Android Studio 中预览 Composable 函数
// showBackground = true 表示显示背景
@Composable
// Composable 注解标记这是一个可组合函数
fun ComposeExamplePreview() {
    // 定义一个名为 ComposeExamplePreview 的可组合函数，用于预览
    
    MaterialTheme {
        // 使用 MaterialTheme 组件，应用 Material Design 样式
        ComposeExample()
        // 调用 ComposeExample 可组合函数，显示预览内容
    }
}
