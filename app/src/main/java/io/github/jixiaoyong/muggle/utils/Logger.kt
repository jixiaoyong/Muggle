package io.github.jixiaoyong.muggle.utils

import android.util.Log

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: www.jixiaoyong.github.io
 * date: 2019/1/19
 * description: loggger
 */

object Logger {

    @JvmStatic
    var isLog = true
        private set

    @JvmStatic
    fun generateTag(): String {
        val stack = Thread.currentThread().stackTrace[4]
        return "${stack.className}.${stack.methodName}(Line:${stack.lineNumber})"
    }

    @JvmStatic
    @JvmOverloads
    fun d(msg: Any, tag: String? = null) {
        if (isLog) {
            Log.d(tag ?: generateTag(), msg.toString())
        }
    }

//    @JvmStatic
//    @JvmOverloads
//    fun e(msg: Any, e: Throwable? = null) {
//        if (isLog) {
//            Log.i(generateTag(), msg.toString())
//        }
//    }

    @JvmStatic
    @JvmOverloads
    fun e(msg: Any, e: Throwable? = null) {
        if (isLog) {
            Log.i(generateTag(), msg.toString(), e)
        }
    }

    @JvmStatic
    fun i(tag: String? = null, msg: Any) {
        if (isLog) {
            Log.i(tag ?: generateTag(), msg.toString())
        }
    }

}