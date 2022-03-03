package com.defense.attack.model.entity.api.website

import com.defense.attack.model.entity.api.proxy.ProxyApi
import com.google.gson.annotations.SerializedName

data class WebsiteAndProxiesGetFromHostResponse(
    @SerializedName("site")
    val website: WebsiteApi?,
    @SerializedName("proxy")
    val proxies: List<ProxyApi>?
)