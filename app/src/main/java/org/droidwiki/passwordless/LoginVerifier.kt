package org.droidwiki.passwordless

import java.net.URL

interface LoginVerifier {
    fun verify(apiUrl: URL, challenge: String, response: String, cb: Callback)

    interface Callback {
        fun onSuccess()
        fun onFailure(e: Exception)
    }
}