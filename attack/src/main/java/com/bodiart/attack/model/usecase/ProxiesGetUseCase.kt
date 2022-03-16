package com.bodiart.attack.model.usecase

import com.bodiart.attack.model.api.RetrofitManager
import com.bodiart.attack.model.entity.data.proxy.ProxyInfo


private const val IP_SPLIT_SYMBOL = ":"
private const val CREDENTIALS_SPLIT_SYMBOL = ":"

class ProxiesGetUseCase {

    suspend fun perform(): Result<List<ProxyInfo>> = runCatching{
        RetrofitManager.attackApi.getProxies().mapNotNull { proxyApi ->
            val ipData = proxyApi.ip?.split(IP_SPLIT_SYMBOL)
            val url = ipData?.getOrNull(0)
            val port = ipData?.getOrNull(1)

            val credentials = proxyApi.auth?.split(CREDENTIALS_SPLIT_SYMBOL)
            val login = credentials?.getOrNull(0)
            val password = credentials?.getOrNull(1)

            if (listOf(url, port, login, password).all { it != null }) {
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
    }
}