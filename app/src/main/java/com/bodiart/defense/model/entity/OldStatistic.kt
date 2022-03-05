package com.bodiart.defense.model.entity

import com.bodiart.attack.model.entity.data.RequestResponse

class AttackerStatistic {

    private val websiteAttackStatistic: HashMap<String, List<AttackResult>> = hashMapOf()

    fun websiteAttacked(url: String, requestResponse: RequestResponse?) {
        val resultsList = websiteAttackStatistic[url]?.toMutableList() ?: mutableListOf()

        resultsList.add(AttackResult(requestResponse != null, requestResponse?.code))

        websiteAttackStatistic[url] = resultsList
    }

    fun getStatistics() = websiteAttackStatistic

}

data class AttackResult(
    val isSuccess: Boolean,
    val respCode: Int?
)