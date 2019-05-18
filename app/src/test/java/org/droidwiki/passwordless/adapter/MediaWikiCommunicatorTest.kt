package org.droidwiki.passwordless.adapter

import io.mockk.every
import io.mockk.mockk
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.awaitility.Awaitility.await
import org.droidwiki.passwordless.AccountsProvider
import org.droidwiki.passwordless.Registration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.security.PublicKey


class MediaWikiCommunicatorTest {
    private lateinit var accountsProvider: AccountsProvider
    private lateinit var server: MockWebServer
    private lateinit var apiUrl: HttpUrl

    @Before
    fun setUp() {
        server = MockWebServer()
        apiUrl = server.url("/w/api.php")

        accountsProvider = mockk()
        every { accountsProvider.create("A_NAME", apiUrl.url()) } returns FakePublicKey()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun register_validResponse_callsSuccessCallback() {
        server.enqueue(MockResponse().setBody("{\"register\": {\"result\": \"Success\"}}"))
        val communicator = MediaWikiCommunicator(accountsProvider)
        val callback = FakeCallback()

        communicator.register("A_NAME", apiUrl.url(), "A_TOKEN", "AN_INSTANCE_ID", callback)

        await().untilAsserted {
            assertEquals(true, callback.successCalled)
            assertEquals(false, callback.failureCalled)
        }
    }

    @Test
    fun register_invalidResponse_callsFailureCallback() {
        server.enqueue(MockResponse().setBody("{\"register\": {\"result\": \"Failed\"}}"))
        val communicator = MediaWikiCommunicator(accountsProvider)
        val callback = FakeCallback()

        communicator.register("A_NAME", apiUrl.url(), "A_TOKEN", "AN_INSTANCE_ID", callback)

        await().untilAsserted {
            assertEquals(false, callback.successCalled)
            assertEquals(true, callback.failureCalled)
        }
    }

    class FakeCallback : Registration.Callback {
        var successCalled = false
        var failureCalled = false

        override fun onSuccess() {
            successCalled = true
        }

        override fun onFailure(e: Exception) {
            failureCalled = true
        }

    }

    class FakePublicKey : PublicKey {
        override fun getAlgorithm(): String {
            return "RSA"
        }

        override fun getEncoded(): ByteArray {
            return byteArrayOf()
        }

        override fun getFormat(): String {
            return ""
        }
    }
}