package com.bodiart.attack.model.entity.api.website


import com.google.gson.annotations.SerializedName

data class WebsiteApi(
    @SerializedName("page")
    val page: String?
)