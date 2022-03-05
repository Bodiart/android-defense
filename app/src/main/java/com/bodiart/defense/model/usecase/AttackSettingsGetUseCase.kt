package com.bodiart.defense.model.usecase

import com.bodiart.defense.model.settings.AttackMode
import com.bodiart.defense.model.settings.AttackSettings

class AttackSettingsGetUseCase {

    fun perform(): AttackMode {
        return AttackSettings.getMode()
    }
}