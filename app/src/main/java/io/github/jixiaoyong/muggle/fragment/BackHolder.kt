package io.github.jixiaoyong.muggle.fragment

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-20
 * description: 可以监听返回键的接口
 */
interface BackHolder {
    fun onBackPressed(): Boolean
}