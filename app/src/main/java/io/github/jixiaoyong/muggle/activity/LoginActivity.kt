package io.github.jixiaoyong.muggle.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jixiaoyong.muggle.AppApplication
import io.github.jixiaoyong.muggle.Constants
import io.github.jixiaoyong.muggle.Constants.token
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.api.bean.Repo
import io.github.jixiaoyong.muggle.utils.SPUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-20
 * description: 登录界面，选择仓库
 */
class LoginActivity : AppCompatActivity() {

    private var repos: Array<Repo> = arrayOf()

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
        setContentView(R.layout.activity_login)
        initView()

        handleOauth(intent)
    }

    private fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycler_view.adapter = MAdapter(repos, listener)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("TAG", "JSONhandleOauth")

        handleOauth(intent!!)
        setIntent(null)
    }


    fun handleOauth(intent: Intent) {
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
                    token = it.accessToken
                    SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, token)
                    getUser()
                    getRepos()
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
                    repos = it
                    recycler_view.adapter = MAdapter(it, listener)
                }, {
                    it.printStackTrace()
                })
    }

    private fun getUser() {

        AppApplication.githubApiService.getUser().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    Log.d("TAG", "userInfo:" + it.name)
                    SPUtils.putAsJson(Constants.KEY_USER_INFO, it)
                    MainActivity.userInfo = it
                }, {
                    it.printStackTrace()
                })
    }
}

class MAdapter(private val repos: Array<Repo>, private val listener: (position: Int) -> Unit) : RecyclerView.Adapter<MAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_github_repos, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return repos.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.textView.text = repos[position].name
        holder.itemView.setOnClickListener {
            listener.invoke(position)
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textview)
    }
}