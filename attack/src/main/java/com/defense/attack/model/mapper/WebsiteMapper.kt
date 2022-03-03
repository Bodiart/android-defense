package com.defense.attack.model.mapper

import com.defense.attack.model.entity.api.website.WebsiteApi
import com.defense.attack.model.entity.data.website.Website

fun WebsiteApi.toData(): Website? {
    return if (this.page != null) {
        Website(
            this.page
        )
    } else {
        null
    }
}