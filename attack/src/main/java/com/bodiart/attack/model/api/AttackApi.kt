package com.bodiart.attack.model.api

import com.bodiart.attack.model.entity.api.proxy.ProxyApi
import com.bodiart.attack.model.entity.api.website.WebsiteAndProxiesGetFromHostResponse
import retrofit2.http.GET
import retrofit2.http.Url

//private const val WEBSITES_JSON_URL = "https://raw.githubusercontent.com/opengs/uashieldtargets/master/sites.json"
private const val PROXIES_JSON_URL = "https://raw.githubusercontent.com/opengs/uashieldtargets/master/proxy.json"
//private const val HOSTS_JSON_URL = "https://gitlab.com/katolyk.severyn/android-dd-attack/-/raw/main/dd-attack-url.json"

interface AttackApi {

//    @GET(WEBSITES_JSON_URL)
//    suspend fun getWebsitesForAttack(): List<WebsiteApi>
//
    @GET(PROXIES_JSON_URL)
    suspend fun getProxies(): List<ProxyApi>

//    @GET(HOSTS_JSON_URL)
//    suspend fun getHosts(): String

//    @GET
//    suspend fun getWebsiteAndProxiesFromHost(@Url host: String): WebsiteAndProxiesGetFromHostResponse
}