//package com.defense.attack.model.usecase
//
//import com.defense.attack.model.api.RetrofitManager
//import com.defense.attack.model.entity.data.website.Website
//
//
//class WebsitesForAttackGetUseCase {
//
//    suspend fun perform(): Result<List<Website>> = runCatching {
//        RetrofitManager.attackApi.getWebsitesForAttack().mapNotNull {
//            if (it.page != null) {
//                Website(it.page)
//            } else {
//                null
//            }
//        }
//    }
//
//}