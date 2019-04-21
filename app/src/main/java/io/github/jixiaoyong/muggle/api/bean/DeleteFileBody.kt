package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class DeleteFileBody @JvmOverloads constructor(
        @SerializedName("message")
        val message: String = "",
        @SerializedName("sha")
        val sha: String = "",
        @SerializedName("committer")
        val committer: Committer? = null)


