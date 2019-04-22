package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class Commit(@SerializedName("committer")
                  val committer: Committer,
                  @SerializedName("author")
                  val author: Author,
                  @SerializedName("html_url")
                  val htmlUrl: String = "",
                  @SerializedName("tree")
                  val tree: Tree,
                  @SerializedName("message")
                  val message: String = "",
                  @SerializedName("sha")
                  val sha: String = "",
                  @SerializedName("url")
                  val url: String = "",
                  @SerializedName("verification")
                  val verification: Verification,
                  @SerializedName("node_id")
                  val nodeId: String = "",
                  @SerializedName("parents")
                  val parents: List<ParentsItem>?)


data class Author(@SerializedName("date")
                  val date: String = "",
                  @SerializedName("name")
                  val name: String = "",
                  @SerializedName("email")
                  val email: String = "")


data class Tree(@SerializedName("sha")
                val sha: String = "",
                @SerializedName("url")
                val url: String = "")


data class Verification(@SerializedName("reason")
                        val reason: String = "",
                        @SerializedName("signature")
                        val signature: String? = null,
                        @SerializedName("payload")
                        val payload: String? = null,
                        @SerializedName("verified")
                        val verified: Boolean = false)


data class UpdateFileRespone(@SerializedName("commit")
                             val commit: Commit,
                             @SerializedName("content")
                             val content: RepoContent)


data class Committer @JvmOverloads constructor(@SerializedName("name")
                                               val name: String = "",
                                               @SerializedName("email")
                                               val email: String = "",
                                               @SerializedName("date")
                                               val date: String? = null)


data class ParentsItem(@SerializedName("html_url")
                       val htmlUrl: String = "",
                       @SerializedName("sha")
                       val sha: String = "",
                       @SerializedName("url")
                       val url: String = "")


