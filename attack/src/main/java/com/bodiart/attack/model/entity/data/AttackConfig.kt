package com.bodiart.attack.model.entity.data

import com.bodiart.attack.model.entity.data.proxy.ProxyInfo
import okhttp3.MediaType

data class AttackConfig(
    val url: String,
    val method: RequestMethod,
    val headers: Map<String, String>,
    val contentType: MediaType?,
    val payload: String?,
    val delayBetweenAllThreadsExecuted: Long,
    val proxies: List<ProxyInfo>,
)

enum class RequestMethod(val methodName: String) {
    GET("GET"),
    POST("POST")
}