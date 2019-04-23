package io.github.jixiaoyong.muggle.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.jixiaoyong.muggle.OauthToken
import io.github.jixiaoyong.muggle.api.bean.Repo
import io.github.jixiaoyong.muggle.api.bean.UserInfo

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-21
 * description: todo
 */
class LoginViewModel : ViewModel() {
    var repos: MutableLiveData<List<Repo>> = MutableLiveData()
    var token: MutableLiveData<OauthToken> = MutableLiveData()
    var userInfo: MutableLiveData<UserInfo> = MutableLiveData()
}