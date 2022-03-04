package com.bodiart.attack.model.entity.data

import com.bodiart.attack.model.entity.data.proxy.ProxyInfo


data class RequestResponse(
    val code: Int,
    val message: String,
    val url: String,
    val proxy: ProxyInfo?
)