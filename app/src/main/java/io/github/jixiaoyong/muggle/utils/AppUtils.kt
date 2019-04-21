package io.github.jixiaoyong.muggle.utils

import android.app.Activity
import android.os.Build
import android.view.View

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-21
 * description: todo
 */
object AppUtils {

    @JvmStatic
    fun setLightMode(activity: Activity, isLightMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //切换到浅色状态栏模式，黑字
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            //切换到深色模式，白字
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}