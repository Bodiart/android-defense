package com.bodiart.attack.model.usecase

import com.bodiart.attack.model.api.RetrofitManager
import com.bodiart.attack.model.entity.data.WebsiteAndProxies
import io.github.bodiart.utils.util.extensions.process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

class WebsiteAndProxiesGetUseCase {
    private val api = RetrofitManager.attackApi
    private val websiteAndProxiesGetFromHostUseCase = WebsiteAndProxiesGetFromHostUseCase()

    suspend fun perform(): Result<WebsiteAndProxies> = runCatching {
        val hosts = api.getHosts()

        if (hosts.isEmpty()) {
            throw RuntimeException("Hosts is empty")
        }

        getWebsiteAndProxiesFromHosts(hosts) ?: throw RuntimeException("Couldn't find website for attack")
    }

    private suspend fun getWebsiteAndProxiesFromHosts(hosts: List<String>): WebsiteAndProxies? = withContext(Dispatchers.IO) {

        hosts.forEach { host ->
            // get website from host
            val websiteAndProxies = websiteAndProxiesGetFromHostUseCase.perform(host).getOrNull()
            if (websiteAndProxies != null && websiteAndProxies.proxies.isNotEmpty()) {
                return@withContext websiteAndProxies
            }
        }

        return@withContext null
    }
}