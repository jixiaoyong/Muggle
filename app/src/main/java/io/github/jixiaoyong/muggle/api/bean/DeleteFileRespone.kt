package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class DeleteFileRespone(@SerializedName("commit")
                             val commit: Commit,
                             @SerializedName("content")
                             val content: String? = null)


