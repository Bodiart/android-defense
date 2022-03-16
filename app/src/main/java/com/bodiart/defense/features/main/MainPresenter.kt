package com.bodiart.defense.features.main

import androidx.annotation.StringRes
import com.bodiart.attack.Attacker
import com.bodiart.attack.model.entity.data.RequestResponse
import com.bodiart.attack.model.entity.data.website.Website
import com.bodiart.defense.R
import com.bodiart.defense.model.entity.AttacksStatistic
import com.bodiart.defense.model.settings.AttackMode
import com.bodiart.defense.model.usecase.AttackSettingsGetUseCase
import com.bodiart.defense.model.usecase.AttackSettingsSetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainPresenter {

    private var view: View? = null

    private var isAttackerEnabled = false
    private val jobComposite = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + jobComposite)

    private val attackSettingsGetUseCase = AttackSettingsGetUseCase()
    private val attackSettingsSetUseCase = AttackSettingsSetUseCase()
    private var attackMode = attackSettingsGetUseCase.perform()
    private var statistic: AttacksStatistic? = null


    fun attachView(view: View) {
        this.view = view

        setupAttackMode()

        subscribeToAttacker()

        showStatistics()
        view.setLoading(false)

    }

    fun detachView() {
        view = null
        unsubscribeFromAttacker()
        jobComposite.cancel()
    }

    fun attackBtnClicked(website: String) {
        if (isAttackerEnabled) {
            Attacker.stopAttack()
            showStatistics()
        } else {
            resetStatistic()
            statistic = AttacksStatistic(website)
            showStatistics()
            Attacker.startAttack(
                uiScope,
                Website(website),
                attackMode.asyncRequestCount,
                attackMode.delayBetweenAllThreadsExecutedMillis
            )
        }
    }

    fun changeSettingsClicked() {
        view?.showChangeSettingsDialog(
            AttackMode.values().toList(),
            AttackMode.values().indexOf(attackMode)
        )
    }

    fun settingsModeSelected(selectedMode: AttackMode) {
        attackSettingsSetUseCase.perform(selectedMode)
        setupAttackMode()
        Attacker.stopAttack()
    }

    private fun showStatistics() {
        view?.showStatistic(statistic)
    }

    private fun attackerEnableStatusChanged(enabled: Boolean) {
        uiScope.launch { // go to main thread for sure
            isAttackerEnabled = enabled
            view?.setAttackBtnText(
                if (enabled) R.string.main_stop_attack else R.string.main_start_attack
            )
            view?.setLoading(enabled)
        }
    }

    private fun attackerProxiesEmpty() {
        uiScope.launch { // go to main thread for sure
            view?.showError(R.string.main_proxies_empty)
        }
    }

    private fun attackerProxiesGetSuccess(proxiesCount: Int) {
        uiScope.launch {
            statistic?.proxiesGot(proxiesCount)
        }
    }

    private fun attackerProxiesGetFailed() {
        uiScope.launch { // go to main thread for sure
            view?.showError(R.string.main_proxies_get_failed)
        }
    }

    private fun attackerHandleRequestResponse(response: RequestResponse) {
        uiScope.launch { // go to main thread for sure
            statistic?.websiteAttacked(response, null)
        }
    }

    private fun attackerHandleRequestError(url: String, throwable: Throwable) {
        uiScope.launch { // go to main thread for sure
            statistic?.websiteAttacked(null, throwable)
        }
    }

    private fun attackerAllThreadsForWebsiteExecuted(url: String) {
        uiScope.launch { // go to main thread for sure
            showStatistics()
        }
    }

    private fun attackerNoValidProxies() {
        uiScope.launch {
            view?.showError(R.string.main_no_valid_proxies)
        }
    }

    private fun subscribeToAttacker(){
        Attacker.attackEnableStatusCallback = { attackerEnableStatusChanged(it) }
        Attacker.proxiesEmptyCallback = { attackerProxiesEmpty() }
        Attacker.proxiesGetSuccessCallback = { attackerProxiesGetSuccess(it) }
        Attacker.proxiesGetFailedCallback = { attackerProxiesGetFailed() }
        Attacker.handleRequestResponseCallback = { attackerHandleRequestResponse(it) }
        Attacker.handleRequestErrorCallback = { url, throwable -> attackerHandleRequestError(url, throwable) }
        Attacker.allThreadExecutedCallback = { attackerAllThreadsForWebsiteExecuted(it) }
        Attacker.noValidProxiesCallback = { attackerNoValidProxies() }
    }

    private fun unsubscribeFromAttacker() {
        Attacker.attackEnableStatusCallback = null
        Attacker.proxiesEmptyCallback = null
        Attacker.proxiesGetFailedCallback = null
        Attacker.handleRequestResponseCallback = null
        Attacker.handleRequestErrorCallback = null
        Attacker.allThreadExecutedCallback = null
        Attacker.noValidProxiesCallback = null
    }

    private fun resetStatistic() {
        statistic = null
    }

    private fun setupAttackMode() {
        attackMode = attackSettingsGetUseCase.perform()
        view?.setSettingsModeName(attackMode.nameResId)
    }

    interface View {
        fun setAttackBtnText(@StringRes textResId: Int)
        fun showError(@StringRes errorResId: Int)
        fun setLoading(isLoadingVisible: Boolean)
        fun showStatistic(statistic: AttacksStatistic?)
        fun setSettingsModeName(@StringRes nameResId: Int)
        fun showChangeSettingsDialog(items: List<AttackMode>, checkedItemIndex: Int)
    }
}