package com.bodiart.attack.model.entity.data.proxy

data class ProxyInfo(
    val url: String,
    val port: Int,
    val login: String?,
    val password: String?
)