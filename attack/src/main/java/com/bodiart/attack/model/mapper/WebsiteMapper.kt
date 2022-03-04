package com.bodiart.attack.model.mapper

import com.bodiart.attack.model.entity.api.website.WebsiteApi
import com.bodiart.attack.model.entity.data.website.Website

fun WebsiteApi.toData(): Website? {
    return if (this.page != null) {
        Website(
            this.page
        )
    } else {
        null
    }
}