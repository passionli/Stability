package com.example.stability

import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.collectAsState

class ComposeActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComposeExample()
                }
            }
        }
    }
}

data class TodoItem(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

class ComposeViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    private val _todoList = MutableStateFlow(
        mutableListOf(
            TodoItem(1, "学习 Kotlin", "掌握 Kotlin 基础语法"),
            TodoItem(2, "学习 Compose", "深入理解 Jetpack Compose"),
            TodoItem(3, "完成项目", "构建一个完整的应用")
        )
    )
    val todoList: StateFlow<MutableList<TodoItem>> = _todoList.asStateFlow()
    
    private var nextId = 4
    
    fun incrementCount() {
        _count.value++
    }
    
    fun resetCount() {
        _count.value = 0
    }
    
    fun addTodoItem() {
        val newItem = TodoItem(nextId++, "新任务 ${nextId}", "这是新添加的任务")
        val updatedList = _todoList.value.toMutableList()
        updatedList.add(newItem)
        _todoList.value = updatedList
    }
    
    fun removeLastTodoItem() {
        if (_todoList.value.isNotEmpty()) {
            val updatedList = _todoList.value.toMutableList()
            updatedList.removeAt(updatedList.lastIndex)
            _todoList.value = updatedList
        }
    }
    
    fun toggleTodoItem(id: Int) {
        val updatedList = _todoList.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = updatedList[index]
            updatedList[index] = item.copy(isCompleted = !item.isCompleted)
            _todoList.value = updatedList
        }
    }
    
    fun deleteTodoItem(id: Int) {
        val updatedList = _todoList.value.toMutableList()
        updatedList.removeIf { it.id == id }
        _todoList.value = updatedList
    }
}

@Composable
fun ComposeExample() {
    val viewModel: ComposeViewModel = viewModel()
    val count by viewModel.count.collectAsState()
    val todoList by viewModel.todoList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Compose Example",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Count: $count",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            println(Log.getStackTraceString(Throwable()))
            viewModel.incrementCount()
        }) {
            Text("Increment")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { viewModel.resetCount() }) {
            Text("Reset")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "待办事项列表 (${todoList.size} 项)",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                viewModel.addTodoItem()
            }) {
                Text("添加任务")
            }
            
            Button(onClick = {
                viewModel.removeLastTodoItem()
            }) {
                Text("删除最后")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todoList, key = { it.id }) { item ->
                TodoItemCard(
                    item = item,
                    onToggleComplete = {
                        viewModel.toggleTodoItem(item.id)
                    },
                    onDelete = {
                        viewModel.deleteTodoItem(item.id)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "数字列表",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyColumn(
            modifier = Modifier.height(150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(10) { index ->
                Text(
                    text = "项目 ${index + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (index % 2 == 0) Color.LightGray.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "带索引的列表",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyColumn(
            modifier = Modifier.height(100.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(
                listOf("苹果", "香蕉", "橙子", "葡萄", "西瓜")
            ) { index, fruit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = fruit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun TodoItemCard(
    item: TodoItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted)
                Color.Green.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isCompleted) Color.Gray else Color.Unspecified
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleComplete) {
                    Text(
                        text = if (item.isCompleted) "撤销" else "完成",
                        color = if (item.isCompleted) Color(0xFFFF9800) else Color.Green
                    )
                }
                
                TextButton(onClick = onDelete) {
                    Text(text = "删除", color = Color.Red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComposeExamplePreview() {
    MaterialTheme {
        ComposeExample()
    }
}
