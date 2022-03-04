package com.bodiart.attack.model.entity.data

import com.bodiart.attack.model.entity.data.proxy.ProxyInfo
import com.bodiart.attack.model.entity.data.website.Website

data class WebsiteAndProxies(
    val website: Website,
    val proxies: List<ProxyInfo>
)