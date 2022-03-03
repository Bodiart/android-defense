package com.defense.attack.model.entity.api.proxy


import com.google.gson.annotations.SerializedName

data class ProxyApi(
    @SerializedName("auth")
    val auth: String?,
    @SerializedName("ip")
    val ip: String?
)