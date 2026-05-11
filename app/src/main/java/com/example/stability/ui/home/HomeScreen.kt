package com.example.stability.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stability.arouter.ARouterMainActivity
import com.example.stability.cpp.CppMain
import com.example.stability.c.CMain
import com.example.stability.design_patterns.DesignPatternsMain
import com.example.stability.multithreading.MultithreadingMain
import com.example.stability.mvp.MVPMain
import com.example.stability.opengl.basic.OpenGLActivity
import com.example.stability.opengl.intermediate.OpenGLIntermediateActivity
import com.example.stability.opengl.intermediate.OpenGLTransformActivity
import com.example.stability.opengl.advanced.OpenGLAdvancedActivity
import com.example.stability.video_edit.VideoEditActivity
import com.example.stability.webrtc.WebRTCMain
import com.example.stability.anr.examples.AnrActivity
import com.example.stability.oom.examples.OomExamplesActivity
import com.example.stability.oom.examples.LeakActivity
import com.example.stability.ListViewActivity
import com.example.stability.ComposeActivity

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val action: FeatureAction
)

sealed class FeatureAction {
    object KotlinLearning : FeatureAction()
    object DataStructures : FeatureAction()
    object Multithreading : FeatureAction()
    object Cpp : FeatureAction()
    object CLanguage : FeatureAction()
    object OpenGL : FeatureAction()
    object VideoEdit : FeatureAction()
    object WebRTC : FeatureAction()
    object DesignPatterns : FeatureAction()
    object MVP : FeatureAction()
    object ANR : FeatureAction()
    object Memory : FeatureAction()
    object ARouter : FeatureAction()
    object ListView : FeatureAction()
    object Compose : FeatureAction()
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val showThreadDialog = mutableStateOf(false)
    val showCppDialog = mutableStateOf(false)
    val showCDialog = mutableStateOf(false)
    val showOpenGLLevelDialog = mutableStateOf(false)
    val showDesignPatternDialog = mutableStateOf(false)
    val showMemoryDialog = mutableStateOf(false)
    
    val features = listOf(
        FeatureItem("Kotlin 学习", Icons.Default.Code, FeatureAction.KotlinLearning),
        FeatureItem("数据结构", Icons.Default.SdStorage, FeatureAction.DataStructures),
        FeatureItem("多线程", Icons.Default.Computer, FeatureAction.Multithreading),
        FeatureItem("C++", Icons.Default.Computer, FeatureAction.Cpp),
        FeatureItem("C 语言", Icons.Default.Android, FeatureAction.CLanguage),
        FeatureItem("OpenGL", Icons.Default.PlayCircle, FeatureAction.OpenGL),
        FeatureItem("视频编辑", Icons.Default.VideoLibrary, FeatureAction.VideoEdit),
        FeatureItem("WebRTC", Icons.Default.NetworkCheck, FeatureAction.WebRTC),
        FeatureItem("设计模式", Icons.Default.Pattern, FeatureAction.DesignPatterns),
        FeatureItem("MVP 架构", Icons.Default.DesignServices, FeatureAction.MVP),
        FeatureItem("ANR", Icons.Default.Smartphone, FeatureAction.ANR),
        FeatureItem("内存管理", Icons.Default.Memory, FeatureAction.Memory),
        FeatureItem("ARouter", Icons.Default.NetworkCheck, FeatureAction.ARouter),
        FeatureItem("ListView", Icons.Default.ListAlt, FeatureAction.ListView),
        FeatureItem("Compose", Icons.Default.Android, FeatureAction.Compose)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "功能示例",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { item ->
                FeatureCard(
                    title = item.title,
                    icon = item.icon,
                    onClick = { 
                        handleFeatureClick(item.action, context, viewModel, 
                            showThreadDialog, showCppDialog, showCDialog, 
                            showOpenGLLevelDialog, showDesignPatternDialog, showMemoryDialog) 
                    }
                )
            }
        }
    }
    
    Dialogs(
        showThreadDialog = showThreadDialog,
        showCppDialog = showCppDialog,
        showCDialog = showCDialog,
        showOpenGLLevelDialog = showOpenGLLevelDialog,
        showDesignPatternDialog = showDesignPatternDialog,
        showMemoryDialog = showMemoryDialog,
        context = context,
        viewModel = viewModel
    )
}

fun handleFeatureClick(
    action: FeatureAction,
    context: Context,
    viewModel: HomeViewModel,
    showThreadDialog: MutableState<Boolean>,
    showCppDialog: MutableState<Boolean>,
    showCDialog: MutableState<Boolean>,
    showOpenGLLevelDialog: MutableState<Boolean>,
    showDesignPatternDialog: MutableState<Boolean>,
    showMemoryDialog: MutableState<Boolean>
) {
    when (action) {
        FeatureAction.KotlinLearning -> viewModel.runKotlinLearningExamples()
        FeatureAction.DataStructures -> viewModel.runDataStructuresExamples()
        FeatureAction.Multithreading -> showThreadDialog.value = true
        FeatureAction.Cpp -> showCppDialog.value = true
        FeatureAction.CLanguage -> showCDialog.value = true
        FeatureAction.OpenGL -> showOpenGLLevelDialog.value = true
        FeatureAction.VideoEdit -> context.startActivity(Intent(context, VideoEditActivity::class.java))
        FeatureAction.WebRTC -> context.startActivity(Intent(context, WebRTCMain::class.java))
        FeatureAction.DesignPatterns -> showDesignPatternDialog.value = true
        FeatureAction.MVP -> context.startActivity(Intent(context, MVPMain::class.java))
        FeatureAction.ANR -> context.startActivity(Intent(context, AnrActivity::class.java))
        FeatureAction.Memory -> showMemoryDialog.value = true
        FeatureAction.ARouter -> context.startActivity(Intent(context, ARouterMainActivity::class.java))
        FeatureAction.ListView -> context.startActivity(Intent(context, ListViewActivity::class.java))
        FeatureAction.Compose -> context.startActivity(Intent(context, ComposeActivity::class.java))
    }
}

@Composable
fun Dialogs(
    showThreadDialog: MutableState<Boolean>,
    showCppDialog: MutableState<Boolean>,
    showCDialog: MutableState<Boolean>,
    showOpenGLLevelDialog: MutableState<Boolean>,
    showDesignPatternDialog: MutableState<Boolean>,
    showMemoryDialog: MutableState<Boolean>,
    context: Context,
    viewModel: HomeViewModel
) {
    if (showThreadDialog.value) {
        ThreadDialog(context, viewModel, showThreadDialog)
    }
    if (showCppDialog.value) {
        CppDialog(context, viewModel, showCppDialog)
    }
    if (showCDialog.value) {
        CDialog(context, viewModel, showCDialog)
    }
    if (showOpenGLLevelDialog.value) {
        OpenGLLevelDialog(context, showOpenGLLevelDialog)
    }
    if (showDesignPatternDialog.value) {
        DesignPatternDialog(context, showDesignPatternDialog)
    }
    if (showMemoryDialog.value) {
        MemoryDialog(context, showMemoryDialog)
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ThreadDialog(context: Context, viewModel: HomeViewModel, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择多线程示例级别") },
        text = {
            Column {
                TextButton(onClick = {
                    viewModel.runBasicThreadExamples()
                    showDialog.value = false
                }) { Text("初级（基础线程）") }
                TextButton(onClick = {
                    viewModel.runIntermediateThreadExamples()
                    showDialog.value = false
                }) { Text("中级（线程同步）") }
                TextButton(onClick = {
                    viewModel.runAdvancedThreadExamples()
                    showDialog.value = false
                }) { Text("高级（并发工具）") }
                TextButton(onClick = {
                    val multithreadingMain = MultithreadingMain(context)
                    multithreadingMain.runAllExamples()
                    showDialog.value = false
                }) { Text("运行所有示例") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CppDialog(context: Context, viewModel: HomeViewModel, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择 C++ 示例级别") },
        text = {
            Column {
                TextButton(onClick = {
                    viewModel.runBasicCppExamples()
                    showDialog.value = false
                }) { Text("初级（基础语法）") }
                TextButton(onClick = {
                    viewModel.runIntermediateCppExamples()
                    showDialog.value = false
                }) { Text("中级（面向对象）") }
                TextButton(onClick = {
                    viewModel.runAdvancedCppExamples()
                    showDialog.value = false
                }) { Text("高级（模板与多线程）") }
                TextButton(onClick = {
                    val cppMain = CppMain(context)
                    cppMain.runAllExamples()
                    showDialog.value = false
                }) { Text("运行所有示例") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CDialog(context: Context, viewModel: HomeViewModel, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择 C 语言示例级别") },
        text = {
            Column {
                TextButton(onClick = {
                    viewModel.runBasicCExamples()
                    showDialog.value = false
                }) { Text("初级（基础语法）") }
                TextButton(onClick = {
                    viewModel.runIntermediateCExamples()
                    showDialog.value = false
                }) { Text("中级（指针与结构体）") }
                TextButton(onClick = {
                    viewModel.runAdvancedCExamples()
                    showDialog.value = false
                }) { Text("高级（内存管理与多线程）") }
                TextButton(onClick = {
                    val cMain = CMain(context)
                    cMain.runAllExamples()
                    showDialog.value = false
                }) { Text("运行所有示例") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun OpenGLLevelDialog(context: Context, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择 OpenGL 示例级别") },
        text = {
            Column {
                TextButton(onClick = {
                    context.startActivity(Intent(context, OpenGLActivity::class.java))
                    showDialog.value = false
                }) { Text("初级（三角形）") }
                TextButton(onClick = {
                    context.startActivity(Intent(context, OpenGLIntermediateActivity::class.java))
                    showDialog.value = false
                }) { Text("中级（彩色四边形）") }
                TextButton(onClick = {
                    context.startActivity(Intent(context, OpenGLTransformActivity::class.java))
                    showDialog.value = false
                }) { Text("中级（变换组合）") }
                TextButton(onClick = {
                    context.startActivity(Intent(context, OpenGLAdvancedActivity::class.java))
                    showDialog.value = false
                }) { Text("高级（纹理立方体）") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DesignPatternDialog(context: Context, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择设计模式") },
        text = {
            Column {
                TextButton(onClick = {
                    val creationalBasic = com.example.stability.design_patterns.creational.basic.CreationalBasicExample()
                    val creationalIntermediate = com.example.stability.design_patterns.creational.intermediate.CreationalIntermediateExample()
                    val creationalAdvanced = com.example.stability.design_patterns.creational.advanced.CreationalAdvancedExample()
                    creationalBasic.runAllExamples()
                    creationalIntermediate.runAllExamples()
                    creationalAdvanced.runAllExamples()
                    showDialog.value = false
                }) { Text("创建型") }
                TextButton(onClick = {
                    val structuralBasic = com.example.stability.design_patterns.structural.basic.StructuralBasicExample()
                    val structuralIntermediate = com.example.stability.design_patterns.structural.intermediate.StructuralIntermediateExample()
                    val structuralAdvanced = com.example.stability.design_patterns.structural.advanced.StructuralAdvancedExample()
                    structuralBasic.runAllExamples()
                    structuralIntermediate.runAllExamples()
                    structuralAdvanced.runAllExamples()
                    showDialog.value = false
                }) { Text("结构型") }
                TextButton(onClick = {
                    val behavioralBasic = com.example.stability.design_patterns.behavioral.basic.BehavioralBasicExample()
                    val behavioralIntermediate = com.example.stability.design_patterns.behavioral.intermediate.BehavioralIntermediateExample()
                    val behavioralAdvanced = com.example.stability.design_patterns.behavioral.advanced.BehavioralAdvancedExample()
                    behavioralBasic.runAllExamples()
                    behavioralIntermediate.runAllExamples()
                    behavioralAdvanced.runAllExamples()
                    showDialog.value = false
                }) { Text("行为型") }
                TextButton(onClick = {
                    val designPatternsMain = DesignPatternsMain(context)
                    designPatternsMain.runAllExamples()
                    showDialog.value = false
                }) { Text("运行所有示例") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun MemoryDialog(context: Context, showDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("选择内存相关示例") },
        text = {
            Column {
                TextButton(onClick = {
                    context.startActivity(Intent(context, OomExamplesActivity::class.java))
                    showDialog.value = false
                }) { Text("OOM 示例") }
                TextButton(onClick = {
                    context.startActivity(Intent(context, LeakActivity::class.java))
                    showDialog.value = false
                }) { Text("内存泄漏检测（LeakCanary）") }
            }
        },
        confirmButton = {}
    )
}
