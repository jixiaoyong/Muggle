package io.github.jixiaoyong.muggle.utils

import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-20
 * description: sp helper
 */
object SPUtils {

    private var sharedPreferences: SharedPreferences? = null

    fun init(_sharedPreferences: SharedPreferences) {
        sharedPreferences = _sharedPreferences
    }

    @JvmStatic
    fun putString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    @JvmStatic
    fun getString(key: String, defaultVar: String = ""): String {
        return sharedPreferences?.getString(key, defaultVar) ?: defaultVar
    }

    @JvmStatic
    fun putAsJson(key: String, value: Any) {
        putString(key, Gson().toJson(value))
    }

    @JvmStatic
    @JvmOverloads
    fun <T> getFromJson(key: String, defaultVar: T? = null, clazz: Class<T>): T? {
        val value = getString(key)
        return if ("" == value) {
            defaultVar
        } else {
            Gson().fromJson<T>(value, clazz)
        }
    }
}