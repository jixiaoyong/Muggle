package io.github.jixiaoyong.muggle.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.jixiaoyong.muggle.AppApplication
import io.github.jixiaoyong.muggle.Constants
import io.github.jixiaoyong.muggle.Constants.token
import io.github.jixiaoyong.muggle.OauthToken
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.adapter.ReposListAdapter
import io.github.jixiaoyong.muggle.api.bean.Repo
import io.github.jixiaoyong.muggle.databinding.ActivityLoginBinding
import io.github.jixiaoyong.muggle.utils.AppUtils
import io.github.jixiaoyong.muggle.utils.Logger
import io.github.jixiaoyong.muggle.utils.SPUtils
import io.github.jixiaoyong.muggle.viewmodel.LoginViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.toolbar.*

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-20
 * description: 登录界面，选择仓库
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private var repos: Array<Repo> = arrayOf()

    private lateinit var dataBinding: ActivityLoginBinding

    private val listener: (position: Int) -> Unit = object : (Int) -> Unit {
        override fun invoke(position: Int) {
            Log.d("TAG", "select repo name ${repos[position].name} ${repos[position].forksUrl}")

            SPUtils.putAsJson(Constants.KEY_SELECT_REPO_INFO, repos[position])
            MainActivity.selectRepo = repos[position]

            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.setLightMode(this, true)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val adapter = ReposListAdapter(listener)
        dataBinding.adapter = adapter
        dataBinding.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        dataBinding.onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
            getUser()
            getRepos()
        }
        dataBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        dataBinding.refreshing = true

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.repos.observe(this, Observer {
            repos = it.toTypedArray()
            Logger.d("start update recycler " + it.size)
            adapter.submitList(ArrayList(it))
            dataBinding.refreshing = false
        })
        viewModel.token.observe(this, Observer {
            token = it.accessToken
            SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, token)
            if (it != null) {
                getUser()
                getRepos()
            }
        })
        viewModel.userInfo.observe(this, Observer {
            SPUtils.putAsJson(Constants.KEY_USER_INFO, it)
            MainActivity.userInfo = it
        })

        initToolbar(getString(R.string.select_the_repo))

        //todo delete this when SyncFragment can change repo in other way
        if (token != null && "" != token) {
            val t = OauthToken()
            t.accessToken = token
            viewModel.token.value = t
        }

        handleOauth(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleOauth(intent!!)
        setIntent(null)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun handleOauth(intent: Intent) {
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            Log.d("TAG", "JSON:$code $state")
            getToken(code, state)
        }

    }

    private fun getToken(code: String, state: String) {

        AppApplication.githubOauthService
                .loginByToken(Constants.MUGGLE_CLIENT_ID, Constants.MUGGLE_CLIENT_SECRET, code, state)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    viewModel.token.value = it
                }, {
                    it.printStackTrace()
                })
    }

    private fun getRepos() {

        AppApplication.githubApiService.getUserRepos().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    it.map {
                        Log.d("TAG", "repo name ${it.name} ${it.forksUrl}")
                    }
                    viewModel.repos.value = it
                }, {
                    it.printStackTrace()
                })
    }

    private fun getUser() {

        AppApplication.githubApiService.getUser().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    Log.d("TAG", "userInfo:" + it.name)
                    viewModel.userInfo.value = it
                }, {
                    it.printStackTrace()
                })
    }

    /**
     * Init view component
     */
    private fun initToolbar(toolbarTitle: String) {
        toolbar!!.title = toolbarTitle
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}