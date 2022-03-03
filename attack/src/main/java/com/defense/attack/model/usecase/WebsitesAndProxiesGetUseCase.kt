package com.defense.attack.model.usecase

import com.defense.attack.model.api.RetrofitManager
import com.defense.attack.model.entity.data.WebsiteAndProxies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

class WebsitesAndProxiesGetUseCase {
    private val api = RetrofitManager.attackApi
    private val websiteAndProxiesGetFromHostUseCase = WebsiteAndProxiesGetFromHostUseCase()

    suspend fun perform(): Result<List<WebsiteAndProxies>> = runCatching {
        val hosts = api.getHosts()

        if (hosts.isEmpty()) {
            throw RuntimeException("Hosts is empty")
        }

        getWebsitesAndProxiesFromHosts(hosts)
    }

    private suspend fun getWebsitesAndProxiesFromHosts(hosts: List<String>): List<WebsiteAndProxies> = withContext(Dispatchers.IO) {
        hosts.map { host ->
            // get all websites from all hosts in async
            async {  websiteAndProxiesGetFromHostUseCase.perform(host) }
        }.mapNotNull { deferred ->
            // wait for all requests
            val result = deferred.await()
            // return result
            result.getOrNull()
        }
    }
}