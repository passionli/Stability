package com.example.stability

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

/**
 * 数据模型类，包含文本和图片 URL
 */
data class ListItem(val text: String, val imageUrl: String?)

/**
 * 包含 ListView 控件的 Activity，用于演示异步加载大量数据和图片
 */
class ListViewActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private lateinit var adapter: DataAdapter
    private val dataList = mutableListOf<ListItem>()
    private var isLoading = false
    
    // 10 个图片 URL
    private val imageUrls = listOf(
        "https://picsum.photos/200/200?random=1",
        "https://picsum.photos/200/200?random=2",
        "https://picsum.photos/200/200?random=3",
        "https://picsum.photos/200/200?random=4",
        "https://picsum.photos/200/200?random=5",
        "https://picsum.photos/200/200?random=6",
        "https://picsum.photos/200/200?random=7",
        "https://picsum.photos/200/200?random=8",
        "https://picsum.photos/200/200?random=9",
        "https://picsum.photos/200/200?random=10"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        
        // 初始化 ListView
        listView = findViewById(R.id.listView)
        
        // 初始化适配器
        adapter = DataAdapter(dataList)
        listView.adapter = adapter
        
        // 异步加载数据
        loadDataAsync()
    }
    
    /**
     * 异步加载数据
     */
    private fun loadDataAsync() {
        isLoading = true
        
        // 使用 AsyncTask 进行异步操作
        object : AsyncTask<Void, String, List<ListItem>>() {
            
            override fun onPreExecute() {
                super.onPreExecute()
                Log.d("ListViewActivity", "开始加载数据...")
            }
            
            override fun doInBackground(vararg params: Void?): List<ListItem> {
                // 模拟网络请求或数据库查询
                val newData = mutableListOf<ListItem>()
                
                for (i in 1..1000) {
                    // 模拟耗时操作
                    Thread.sleep(10) // 模拟每条数据加载需要 10 毫秒
                    
                    // 每 100 条数据中添加一个带图片的数据
                    val imageUrl = if (i % 100 == 0) {
                        // 循环使用 10 个图片 URL
                        imageUrls[(i / 100 - 1) % imageUrls.size]
                    } else {
                        null
                    }
                    
                    val item = ListItem("数据项 $i", imageUrl)
                    newData.add(item)
                    
                    // 每加载 100 条数据，发布一次进度
                    if (i % 100 == 0) {
                        publishProgress("已加载 $i 条数据")
                    }
                }
                
                return newData
            }
            
            override fun onProgressUpdate(vararg values: String?) {
                super.onProgressUpdate(*values)
                // 显示加载进度
                Log.d("ListViewActivity", values[0] ?: "")
            }
            
            override fun onPostExecute(result: List<ListItem>?) {
                super.onPostExecute(result)
                
                if (result != null) {
                    // 将加载的数据添加到列表中
                    dataList.addAll(result)
                    // 通知适配器数据发生变化
                    adapter.notifyDataSetChanged()
                    Log.d("ListViewActivity", "数据加载完成，共 ${result.size} 条数据")
                    Log.d("ListViewActivity", "其中包含 ${result.count { it.imageUrl != null }} 条带图片的数据")
                }
                
                isLoading = false
            }
        }.execute()
    }
    
    /**
     * 异步加载图片的任务
     */
    inner class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        
        override fun doInBackground(vararg params: String?): Bitmap? {
            val urlString = params[0]
            return try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("ListViewActivity", "加载图片失败: ${e.message}")
                null
            }
        }
        
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result != null) {
                imageView.setImageBitmap(result)
            }
        }
    }
    
    /**
     * ListView 的适配器
     */
    inner class DataAdapter(private val data: List<ListItem>) : BaseAdapter() {
        
        override fun getCount(): Int {
            return data.size
        }
        
        override fun getItem(position: Int): Any {
            return data[position]
        }
        
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            // 复用视图，提高性能
            val viewHolder: ViewHolder
            var view: View
            
            if (convertView == null) {
                // 如果没有可复用的视图，创建一个新的
                view = layoutInflater.inflate(android.R.layout.activity_list_item, parent, false)
                viewHolder = ViewHolder()
                viewHolder.textView = view.findViewById(android.R.id.text1)
                viewHolder.imageView = ImageView(this@ListViewActivity)
                viewHolder.imageView.layoutParams = ViewGroup.LayoutParams(200, 200)
                (view as ViewGroup).addView(viewHolder.imageView)
                view.tag = viewHolder
            } else {
                // 复用已有的视图
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            
            // 设置数据
            val item = data[position]
            viewHolder.textView.text = item.text
            
            // 处理图片
            if (item.imageUrl != null) {
                viewHolder.imageView.visibility = View.VISIBLE
                // 异步加载图片
                LoadImageTask(viewHolder.imageView).execute(item.imageUrl)
            } else {
                viewHolder.imageView.visibility = View.GONE
            }
            
            return view
        }
        
        /**
         * 视图持有者，用于缓存视图引用
         */
        inner class ViewHolder {
            lateinit var textView: TextView
            lateinit var imageView: ImageView
        }
    }
}