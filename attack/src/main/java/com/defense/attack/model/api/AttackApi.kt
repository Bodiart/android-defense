package com.defense.attack.model.api

import com.defense.attack.model.entity.api.proxy.ProxyApi
import com.defense.attack.model.entity.api.website.WebsiteAndProxiesGetFromHostResponse
import com.defense.attack.model.entity.api.website.WebsiteApi
import retrofit2.http.GET
import retrofit2.http.Url

//private const val WEBSITES_JSON_URL = "https://raw.githubusercontent.com/opengs/uashieldtargets/master/sites.json"
//private const val PROXIES_JSON_URL = "https://raw.githubusercontent.com/opengs/uashieldtargets/master/proxy.json"
private const val HOSTS_JSON_URL = "https://gitlab.com/cto.endel/atack_hosts/-/raw/master/hosts.json"

interface AttackApi {

//    @GET(WEBSITES_JSON_URL)
//    suspend fun getWebsitesForAttack(): List<WebsiteApi>
//
//    @GET(PROXIES_JSON_URL)
//    suspend fun getProxies(): List<ProxyApi>

    @GET(HOSTS_JSON_URL)
    suspend fun getHosts(): List<String>

    @GET
    suspend fun getWebsiteAndProxiesFromHost(@Url host: String): WebsiteAndProxiesGetFromHostResponse
}