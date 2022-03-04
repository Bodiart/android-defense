package com.bodiart.attack.model.usecase

import com.bodiart.attack.model.api.RetrofitManager
import com.bodiart.attack.model.entity.data.WebsiteAndProxies
import com.bodiart.attack.model.mapper.toData
import java.lang.RuntimeException

class WebsiteAndProxiesGetFromHostUseCase {

    suspend fun perform(host: String): Result<WebsiteAndProxies> = runCatching {
        val websitesAndProxy = RetrofitManager.attackApi.getWebsiteAndProxiesFromHost(host)

        val website = websitesAndProxy.website?.toData()
        val proxies = websitesAndProxy.proxies?.toData()?.filterNotNull()

        if (website != null && !proxies.isNullOrEmpty()) {
            WebsiteAndProxies(website, proxies)
        } else {
            throw RuntimeException("Get website and proxies from $host failed")
        }
    }
}