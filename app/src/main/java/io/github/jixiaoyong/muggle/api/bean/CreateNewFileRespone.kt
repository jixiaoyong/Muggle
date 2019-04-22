package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class CreateNewFileRespone(@SerializedName("commit")
                                val commit: Commit,
                                @SerializedName("content")
                                val content: RepoContent)






