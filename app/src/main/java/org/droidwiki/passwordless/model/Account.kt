package org.droidwiki.passwordless.model

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
        return bytesToHex(macData)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars).toLowerCase()
    }
}
