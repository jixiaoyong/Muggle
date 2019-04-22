package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class GetCommitRespone(@SerializedName("committer")
                            val committer: Committer,
                            @SerializedName("author")
                            val author: Author,
                            @SerializedName("html_url")
                            val htmlUrl: String = "",
                            @SerializedName("comments_url")
                            val commentsUrl: String = "",
                            @SerializedName("commit")
                            val commit: Commit,
                            @SerializedName("sha")
                            val sha: String = "",
                            @SerializedName("url")
                            val url: String = "",
                            @SerializedName("node_id")
                            val nodeId: String = "",
                            @SerializedName("parents")
                            val parents: List<ParentsItem>?)


