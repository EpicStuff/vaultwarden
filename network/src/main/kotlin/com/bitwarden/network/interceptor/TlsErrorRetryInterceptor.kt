package com.bitwarden.network.interceptor

import com.bitwarden.network.util.isTlsPacketHeaderError
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Retries a network call once when a TLS packet header error occurs.
 */
class TlsErrorRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (exception: IOException) {
            if (!exception.isTlsPacketHeaderError()) throw exception

            chain.proceed(request)
        }
    }
}
