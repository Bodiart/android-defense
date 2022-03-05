package com.bodiart.defense.model.entity

import com.bodiart.attack.model.entity.data.RequestResponse

class AttacksStatistic(
    val url: String
) {

    private val attacks = arrayListOf<AttackStat>()

    fun websiteAttacked(requestResponse: RequestResponse?, error: Throwable?) {
        attacks.add(
            AttackStat(
                requestResponse != null,
                requestResponse?.code,
                error
            )
        )
    }

    fun getStatisticsAttacks(): List<AttackStat> = attacks
}

data class AttackStat(
    val isSuccess: Boolean,
    val respCode: Int?,
    val error: Throwable?
)