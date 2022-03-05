package com.bodiart.defense.model.usecase

import com.bodiart.defense.model.settings.AttackMode
import com.bodiart.defense.model.settings.AttackSettings

class AttackSettingsSetUseCase {

    fun perform(mode: AttackMode) {
        AttackSettings.setMode(mode)
    }
}