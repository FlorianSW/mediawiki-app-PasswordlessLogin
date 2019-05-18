package org.droidwiki.passwordless.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import org.droidwiki.passwordless.*
import java.io.IOException
import java.net.URL

class MediaWikiCommunicator : Registration, LoginVerifier {

    override fun register(
        accountName: String,
        apiUrl: URL,
        accountToken: String,
        instanceId: String,
        secret: String,
        callback: Registration.Callback
    ) {
        val json = MediaType.get("application/x-www-form-urlencoded")
        val client = OkHttpClient()
        val formContent = "action=passwordlesslogin&" +
                "pairToken=$accountToken&" +
                "deviceId=$instanceId&" +
                "secret=$secret&" +
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

            if (registerResponse.register.result === ResultValues.Failed) {
                callback.onFailure(InvalidToken())
                return
            }

            callback.onSuccess()
        }

        inner class InvalidToken : Exception("Invalid token")
    }
    override fun verify(apiUrl: URL, challenge: String, response: String, cb: LoginVerifier.Callback) {
        val json = MediaType.get("application/x-www-form-urlencoded")
        val client = OkHttpClient()
        val formContent = "action=passwordlesslogin-verify&" +
                "challenge=$challenge&" +
                "response=${response.toLowerCase()}&" +
                "format=json"
        val body = RequestBody.create(json, formContent)
        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        val call = client.newCall(request)

        call.enqueue(VerifyCallback(cb))
    }

    inner class VerifyCallback(private val callback: LoginVerifier.Callback) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            val verifyResponse = ObjectMapper().readValue(response.body()?.string(), VerifyResponse::class.java)

            if (verifyResponse.verify.result === ResultValues.Failed) {
                callback.onFailure(VerificationFailed())
                return
            }

            callback.onSuccess()
        }

        inner class VerificationFailed: Exception("Login verification failed")
    }
}

class RegisterResponse {
    lateinit var register: ApiResult
}

class VerifyResponse {
    lateinit var verify: ApiResult
}

class ApiResult {
    lateinit var result: ResultValues
}

enum class ResultValues {
    Success, Failed
}