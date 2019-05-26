package org.droidwiki.passwordless.model

import org.droidwiki.passwordless.ToHex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class Account(val id: Int, val name: String, val apiUrl: String, private val secret: String) {
    fun sign(challenge: ByteArray): String {
        val byteKey = secret.toByteArray(charset("UTF-8"))
        val hmacSha256 = "HmacSHA512"

        val sha512HMAC = Mac.getInstance(hmacSha256)
        val keySpec = SecretKeySpec(byteKey, hmacSha256)
        sha512HMAC!!.init(keySpec)

        val macData = sha512HMAC.doFinal(challenge)
        return ToHex.bytesToHex(macData)
    }
}
