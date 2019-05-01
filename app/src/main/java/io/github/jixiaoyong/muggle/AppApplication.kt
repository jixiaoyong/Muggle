package io.github.jixiaoyong.muggle

import android.app.Application
import android.content.Context
import com.google.gson.GsonBuilder
import com.tencent.bugly.Bugly
import io.github.jixiaoyong.muggle.api.GithubApiService
import io.github.jixiaoyong.muggle.api.GithubOauthService
import io.github.jixiaoyong.muggle.utils.Logger
import io.github.jixiaoyong.muggle.utils.SPUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-20
 * description: todo
 */
class AppApplication : Application() {
    companion object {
        private var application: AppApplication? = null
        lateinit var okHttpClient: OkHttpClient
        lateinit var githubOauthService: GithubOauthService
        lateinit var githubApiService: GithubApiService

        @JvmStatic
        fun get(): Context? {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this

        SPUtils.init(applicationContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE))

        Constants.token = SPUtils.getString(Constants.KEY_OAUTH2_TOKEN)

        Bugly.init(applicationContext, BuildConfig.BUGGLE_APP_IP, BuildConfig.DEBUG)

        okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder = originalRequest.newBuilder()
                    builder.addHeader("Authorization", "token ${Constants.token}")

                    Logger.d(originalRequest.url().url().toString())

                    val requestBuilder = builder.method(originalRequest.method(), originalRequest.body())
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .build()

        githubOauthService = Retrofit.Builder()
                .baseUrl(GithubOauthService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(GithubOauthService::class.java)

        githubApiService = Retrofit.Builder()
                .baseUrl(GithubApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(GithubApiService::class.java)
    }

}