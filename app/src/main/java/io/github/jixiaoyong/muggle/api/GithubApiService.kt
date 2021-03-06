package io.github.jixiaoyong.muggle.api

import io.github.jixiaoyong.muggle.Constants
import io.github.jixiaoyong.muggle.api.bean.*
import io.reactivex.Observable
import retrofit2.http.*

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2018-03-20
 * description: 登录GitHub后的API
 */

interface GithubApiService {

    companion object {
        const val BASE_URL = Constants.GITHUB_API_BASE_URL
    }

    @GET("user")
    fun getUser(): Observable<UserInfo>

    @GET("/user/repos")
    fun getUserRepos(): Observable<ArrayList<Repo>>


    // GET /repos/:owner/:repo/contents/:path
    // Parameters: ref string The name of the commit/branch/tag.
    // Default: the repository’s default branch (usually master)
    @GET("/repos/{owner}/{repo}/contents/{path}")
    fun getUserRepoContent(@Path("owner") owner: String,
                           @Path("repo") repo: String,
                           @Path("path") path: String): Observable<Array<RepoContent>>


    // GET GET /repos/:owner/:repo/commits
    // Parameters:
    // path Only commits containing this file path will be returned.
    // since  Only commits after this date will be returned.
    //        This is a timestamp in ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ.

    @GET("/repos/{owner}/{repo}/commits")
    fun getUserRepoCommit(@Path("owner") owner: String,
                          @Path("repo") repo: String,
                          @Query("path") path: String,
                          @Query("since") since: String): Observable<Array<GetCommitRespone>>

    // PUT /repos/:owner/:repo/contents/:path
    @PUT("/repos/{owner}/{repo}/contents/{path}")
    fun createNewFile(@Path("owner") owner: String,
                      @Path("repo") repo: String,
                      @Path("path") path: String,
                      @Body body: CreateFileBody): Observable<CreateNewFileRespone>


    // PUT /repos/:owner/:repo/contents/:path
    @PUT("/repos/{owner}/{repo}/contents/{path}")
    fun updateFile(@Path("owner") owner: String,
                   @Path("repo") repo: String,
                   @Path("path") path: String,
                   @Body body: UpdateFileBody
    ): Observable<UpdateFileRespone>

    // DELETE /repos/:owner/:repo/contents/:path
    @HTTP(method = "DELETE", path = "/repos/{owner}/{repo}/contents/{path}", hasBody = true)
    fun deleteFile(@Path("owner") owner: String,
                   @Path("repo") repo: String,
                   @Path("path") path: String,
                   @Body body: DeleteFileBody): Observable<DeleteFileRespone>

}