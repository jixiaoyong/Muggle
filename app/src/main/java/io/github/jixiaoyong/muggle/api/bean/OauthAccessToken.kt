package io.github.jixiaoyong.muggle.api.bean


import com.google.gson.annotations.SerializedName

data class OauthAccessToken(@SerializedName("access_token")
                            val accessToken: String = "",
                            @SerializedName("scope")
                            val scope: String = "",
                            @SerializedName("token_type")
                            val tokenType: String = "")


