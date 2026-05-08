package com.example.stability.arouter

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/arouter/bundle")
class BundleActivity : AppCompatActivity() {

    data class UserInfo(
        val userId: String,
        val userName: String,
        val userAge: Int,
        val userEmail: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(userId)
            parcel.writeString(userName)
            parcel.writeInt(userAge)
            parcel.writeString(userEmail)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UserInfo> {
            override fun createFromParcel(parcel: Parcel): UserInfo {
                return UserInfo(parcel)
            }

            override fun newArray(size: Int): Array<UserInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userInfo = intent.getParcelableExtra<UserInfo>("userInfo")
        
        val textView = TextView(this).apply {
            text = """使用Bundle传递复杂数据示例

Path: /arouter/bundle

接收到的 UserInfo 对象:
${if (userInfo != null) """
- userId: ${userInfo.userId}
- userName: ${userInfo.userName}
- userAge: ${userInfo.userAge}
- userEmail: ${userInfo.userEmail}
""" else "  (无数据)"}

ARouter支持传递Parcelable对象，使用 withParcelable() 方法。
"""
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        
        setContentView(textView)
        title = "Bundle传递"
    }
}