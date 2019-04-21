package io.github.jixiaoyong.muggle.api

import io.github.jixiaoyong.muggle.Constants
import io.github.jixiaoyong.muggle.OauthToken
import io.github.jixiaoyong.muggle.api.bean.UserInfo
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by jixiaoyong on 2018/3/2.
 */

interface GithubOauthService {

    companion object {
        const val BASE_URL = Constants.GITHUB_BASE_URL
    }

    @GET("user")
    fun getUser(): Observable<UserInfo>

    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    @FormUrlEncoded //for @Field parameters can only be used with form encoding.
    fun loginByToken(@Field("client_id") client_id: String,
                     @Field("client_secret") client_secret: String,
                     @Field("code") code: String,
                     @Field("state") state: String): Observable<OauthToken>

}