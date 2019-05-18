package org.droidwiki.passwordless.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import org.droidwiki.passwordless.AccountsProvider
import org.droidwiki.passwordless.RegisterResponse
import org.droidwiki.passwordless.RegisterResult
import org.droidwiki.passwordless.Registration
import java.io.IOException
import java.net.URL
import java.util.*

class MediaWikiCommunicator(private val accountsProvider: AccountsProvider) : Registration {
    override fun register(
        accountName: String,
        apiUrl: URL,
        accountToken: String,
        instanceId: String,
        callback: Registration.Callback
    ) {
        val publicKey = accountsProvider.create(accountName, apiUrl)
        val json = MediaType.get("application/x-www-form-urlencoded")
        val client = OkHttpClient()
        val formContent = "action=passwordlesslogin&" +
                "pairToken=$accountToken&" +
                "deviceId=$instanceId&" +
                "secret=${String(Base64.getEncoder().encode(publicKey.encoded))}&" +
                "format=json"
        val body = RequestBody.create(json, formContent)
        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        val call = client.newCall(request)

        call.enqueue(RegisterCallback(callback))
    }

    inner class RegisterCallback(private val callback: Registration.Callback) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            val registerResponse = ObjectMapper().readValue(response.body()?.string(), RegisterResponse::class.java)

            if (registerResponse.register.result === RegisterResult.Failed) {
                callback.onFailure(InvalidToken())
                return
            }

            callback.onSuccess()
        }

        inner class InvalidToken : Exception("Invalid token")
    }
}