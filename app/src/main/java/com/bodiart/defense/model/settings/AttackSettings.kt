package com.bodiart.defense.model.settings

import androidx.annotation.StringRes
import com.bodiart.defense.R
import com.bodiart.defense.util.Preference

private const val PREFERENCE_ATTACK_SETTINGS = "PREFERENCE_ATTACK_SETTINGS"

object AttackSettings {

    private var attackMode: AttackMode by Preference(PREFERENCE_ATTACK_SETTINGS, AttackMode.NORMAL)

    fun getMode(): AttackMode {
        return attackMode
    }

    fun setMode(mode: AttackMode) {
        attackMode = mode
    }
}

enum class AttackMode(
    val asyncRequestCount: Int,
    val delayBetweenAllThreadsExecutedMillis: Long,
    @StringRes val nameResId: Int
) {
    EASY(10, 3000, R.string.main_settings_easy),
    NORMAL(20, 3000, R.string.main_settings_normal),
    MEDIUM(30, 3000, R.string.main_settings_medium),
    HARD(40, 3000, R.string.main_settings_hard),
    MAX(50, 3000, R.string.main_settings_max);
}