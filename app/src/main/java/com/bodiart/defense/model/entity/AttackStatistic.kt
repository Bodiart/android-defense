package com.bodiart.defense.model.entity

import com.bodiart.attack.model.entity.data.RequestResponse

class AttacksStatistic(
    val url: String
) {

    private val attacks = arrayListOf<AttackStat>()
    private var proxiesCount = 0

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

    fun proxiesGot(proxiesCount: Int) {
        this.proxiesCount = proxiesCount
    }

    fun getProxiesSize(): Int = proxiesCount
}

data class AttackStat(
    val isSuccess: Boolean,
    val respCode: Int?,
    val error: Throwable?
)