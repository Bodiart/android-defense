package com.defense.attack.model.api

import android.annotation.SuppressLint
import com.defense.attack.model.entity.data.proxy.ProxyInfo
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val BASE_URL = "https://raw.githubusercontent.com/"

private const val PROXY_AUTH_HEADER = "Proxy-Authorization"

private const val JSON_REQUESTS_TIMEOUTS = 10000L
const val DDOS_REQUESTS_TIMEOUTS = 3000L

object RetrofitManager {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getJsonRequestsOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val attackApi: AttackApi = retrofit.create(AttackApi::class.java)

    private fun getJsonRequestsOkHttpClient() = OkHttpClient.Builder().run {
        connectTimeout(JSON_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        readTimeout(JSON_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        writeTimeout(JSON_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        callTimeout(JSON_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        addInterceptor(
            HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT).apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
//        AllowHttp().allowHttp(this)
        allowHttp()
        build()
    }

    fun getDdosRequestsOkHttpClient(proxyInfo: ProxyInfo?): OkHttpClient = OkHttpClient.Builder().run {
        // timeouts
        connectTimeout(DDOS_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        readTimeout(DDOS_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        writeTimeout(DDOS_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        callTimeout(DDOS_REQUESTS_TIMEOUTS, TimeUnit.MILLISECONDS)
        // add proxy if exists
        proxyInfo?.let { proxy ->
            proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxy.url, proxy.port)))
            proxyAuthenticator { _, response ->
                response.request().newBuilder()
                    .header(PROXY_AUTH_HEADER, Credentials.basic(proxy.login, proxy.password))
                    .build()
            }
        }
//        AllowHttp().allowHttp(this)
        allowHttp()
        // build client
        build()
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun OkHttpClient.Builder.allowHttp(): OkHttpClient.Builder {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            // apply to builder
            this.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            this.hostnameVerifier { _, _ -> true }
        } catch (throwable: Throwable) {
            // failed to apply http
        }

        return this
    }
}