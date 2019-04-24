package io.github.jixiaoyong.muggle.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.jixiaoyong.muggle.FileEntity
import io.github.jixiaoyong.muggle.api.bean.Repo
import io.github.jixiaoyong.muggle.api.bean.RepoContent
import io.github.jixiaoyong.muggle.api.bean.UserInfo

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-21
 * description: todo
 */
class MainActivityModel : ViewModel() {

    val token: MutableLiveData<String> = MutableLiveData()
    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val selectRepo: MutableLiveData<Repo> = MutableLiveData()
    val selectRepoContent: MutableLiveData<List<RepoContent>> = MutableLiveData()

    val localFileList = MutableLiveData<List<FileEntity>>()
    val cloudFileList = MutableLiveData<List<RepoContent>>()

    val isLogin = MutableLiveData<Boolean>()

    init {
        checkLogin()
    }

    fun checkLogin() {
        isLogin.value = "" != token.value && userInfo.value != null && selectRepo.value != null
    }

}