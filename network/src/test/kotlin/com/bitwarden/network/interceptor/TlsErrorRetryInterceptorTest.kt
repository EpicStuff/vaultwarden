package com.bitwarden.network.interceptor

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import javax.net.ssl.SSLException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class TlsErrorRetryInterceptorTest {
    private val interceptor = TlsErrorRetryInterceptor()

    @Test
    fun `intercept should retry once when TLS packet header error is thrown`() {
        val request = Request.Builder()
            .url("https://example.com")
            .build()
        val expectedResponse = Response.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .build()
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns request
            every { proceed(request) } throws SSLException("Unable to parse TLS packet header") andThen expectedResponse
        }

        val result = interceptor.intercept(chain)

        assertEquals(expectedResponse, result)
        verify(exactly = 2) { chain.proceed(request) }
    }

    @Test
    fun `intercept should rethrow SSL exceptions that are not TLS packet header errors`() {
        val request = Request.Builder()
            .url("https://example.com")
            .build()
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns request
            every { proceed(request) } throws SSLException("fatal alert")
        }

        assertThrows(SSLException::class.java) {
            interceptor.intercept(chain)
        }

        verify(exactly = 1) { chain.proceed(request) }
    }
}
