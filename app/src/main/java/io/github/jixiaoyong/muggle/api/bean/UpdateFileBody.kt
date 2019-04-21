package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class UpdateFileBody @JvmOverloads constructor(
        @SerializedName("message")
        val message: String = "",
        @SerializedName("content")
        val content: String = "",
        @SerializedName("sha")
        val sha: String? = null,
        @SerializedName("committer")
        val committer: Committer? = null)


data class CreateFileBody @JvmOverloads constructor(
        @SerializedName("message")
        val message: String = "",
        @SerializedName("content")
        val content: String = "",
        @SerializedName("committer")
        val committer: Committer? = null)
