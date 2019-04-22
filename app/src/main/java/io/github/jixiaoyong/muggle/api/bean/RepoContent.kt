package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class Links(@SerializedName("git")
                 val git: String = "",
                 @SerializedName("self")
                 val self: String = "",
                 @SerializedName("html")
                 val html: String = "")


data class RepoContent(@SerializedName("path")
                       val path: String = "",
                       @SerializedName("size")
                       val size: Int = 0,
                       @SerializedName("_links")
                       val Links: Links,
                       @SerializedName("html_url")
                       val htmlUrl: String = "",
                       @SerializedName("name")
                       val name: String = "",
                       @SerializedName("download_url")
                       val downloadUrl: String = "",
                       @SerializedName("git_url")
                       val gitUrl: String = "",
                       @SerializedName("type")//file,dir,symlink,submodule
                       val type: String = "",
                       @SerializedName("sha")
                       var sha: String = "",
                       @SerializedName("url")
                       val url: String = "") {
}


