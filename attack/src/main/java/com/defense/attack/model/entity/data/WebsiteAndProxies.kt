package com.defense.attack.model.entity.data

import com.defense.attack.model.entity.data.proxy.ProxyInfo
import com.defense.attack.model.entity.data.website.Website

data class WebsiteAndProxies(
    val website: Website,
    val proxies: List<ProxyInfo>
)