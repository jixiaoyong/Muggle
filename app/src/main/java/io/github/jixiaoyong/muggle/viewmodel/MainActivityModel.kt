package io.github.jixiaoyong.muggle.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.jixiaoyong.muggle.FileEntity
import io.github.jixiaoyong.muggle.OauthToken
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

    val token: MutableLiveData<OauthToken> = MutableLiveData()
    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    val repoContent: MutableLiveData<RepoContent> = MutableLiveData()
    val localFileList = MutableLiveData<List<FileEntity>>()
    val cloudFileList = MutableLiveData<List<RepoContent>>()

    val login = MutableLiveData<Boolean>()

    init {

    }
}