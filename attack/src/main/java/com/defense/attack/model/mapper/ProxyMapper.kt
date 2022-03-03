package com.defense.attack.model.mapper

import com.defense.attack.model.entity.api.proxy.ProxyApi
import com.defense.attack.model.entity.data.proxy.ProxyInfo

private const val IP_SPLIT_SYMBOL = ":"
private const val CREDENTIALS_SPLIT_SYMBOL = ":"

fun ProxyApi.toData(): ProxyInfo? {
    val ipData = this.ip?.split(IP_SPLIT_SYMBOL)
    val url = ipData?.getOrNull(0)
    val port = ipData?.getOrNull(1)

    val credentials = this.auth?.split(CREDENTIALS_SPLIT_SYMBOL)
    val login = credentials?.getOrNull(0)
    val password = credentials?.getOrNull(1)

    return if (listOf(url, port, login, password).all { it != null }) {
        ProxyInfo(
            requireNotNull(url),
            requireNotNull(port).toInt(),
            requireNotNull(login),
            requireNotNull(password)
        )
    } else {
        null
    }
}

fun List<ProxyApi>.toData(): List<ProxyInfo?> = this.map { it.toData() }