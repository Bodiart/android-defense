package com.bodiart.defense.features.main

import androidx.annotation.StringRes
import com.bodiart.attack.Attacker
import com.bodiart.attack.model.entity.data.RequestResponse
import com.bodiart.defense.R
import com.bodiart.defense.features.main.adapter.StatisticItem
import com.bodiart.defense.model.entity.AttackerStatistic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainPresenter {

    private var view: View? = null

    private var isAttackerEnabled = false
    private val jobComposite = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + jobComposite)

    private var attackMode = AttackMode.NORMAL

    private val statistics = AttackerStatistic()


    fun attachView(view: View) {
        this.view = view
        subscribeToAttacker()

        view.setLoading(false)
    }

    fun detachView() {
        view = null
        unsubscribeFromAttacker()
        jobComposite.cancel()
    }

    fun attackBtnClicked() {
        if (isAttackerEnabled) {
            Attacker.stopAttack()
            showStatistics()
        } else {
            Attacker.startAttack(
                uiScope,
                attackMode.asyncRequestCount,
                attackMode.delayBetweenAllThreadsExecutedMillis
            )
        }
    }

    private fun showStatistics() {
        statistics.getStatistics().map {
            StatisticItem(
                it.key,
                it.value.size
            )
        }.let { view?.showStatistics(it) }
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

    private fun attackerWebsitesEmpty() {
        uiScope.launch { // go to main thread for sure
            view?.showError(R.string.main_websites_empty)
        }
    }

    private fun attackerWebsitesGetFailed() {
        uiScope.launch { // go to main thread for sure
            view?.showError(R.string.main_websites_get_failed)
        }
    }

    private fun attackerHandleRequestResponse(response: RequestResponse) {
        uiScope.launch { // go to main thread for sure
            statistics.websiteAttacked(response.url, response)
        }
    }

    private fun attackerHandleRequestError(url: String, throwable: Throwable) {
        uiScope.launch { // go to main thread for sure
            statistics.websiteAttacked(url, null)
        }
    }

    private fun attackerAllThreadsForWebsiteExecuted(url: String) {
        uiScope.launch { // go to main thread for sure
            showStatistics()
        }
    }

    private fun subscribeToAttacker(){
        Attacker.attackEnableStatusCallback = { attackerEnableStatusChanged(it) }
        Attacker.websitesEmptyCallback = { attackerWebsitesEmpty() }
        Attacker.websitesGetFailedCallback = { attackerWebsitesGetFailed() }
        Attacker.handleRequestResponseCallback = { attackerHandleRequestResponse(it) }
        Attacker.handleRequestErrorCallback = { url, throwable -> attackerHandleRequestError(url, throwable) }
        Attacker.allThreadExecutedCallback = { attackerAllThreadsForWebsiteExecuted(it) }
    }

    private fun unsubscribeFromAttacker() {
        Attacker.attackEnableStatusCallback = {}
        Attacker.websitesEmptyCallback = {}
        Attacker.websitesGetFailedCallback = {}
        Attacker.handleRequestResponseCallback = {}
        Attacker.handleRequestErrorCallback = { _, _ -> }
        Attacker.allThreadExecutedCallback = {}
    }

    interface View {
        fun setAttackBtnText(@StringRes textResId: Int)
        fun showError(@StringRes errorResId: Int)
        fun setLoading(isLoadingVisible: Boolean)
        fun showStatistics(statistics: List<StatisticItem>)
    }

    enum class AttackMode(
        val asyncRequestCount: Int,
        val delayBetweenAllThreadsExecutedMillis: Long
    ) {
        EASY(1, 3000),
        NORMAL(3, 3000),
        MEDIUM(5, 3000),
        HARD(10, 3000),
        MAX(10, 1000);
    }
}