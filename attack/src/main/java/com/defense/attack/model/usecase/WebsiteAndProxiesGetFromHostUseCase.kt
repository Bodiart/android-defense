package com.defense.attack.model.usecase

import com.defense.attack.model.api.RetrofitManager
import com.defense.attack.model.entity.data.WebsiteAndProxies
import com.defense.attack.model.mapper.toData
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