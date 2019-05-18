package org.droidwiki.passwordless

import java.net.URL

interface Registration {
    fun register(accountName: String, apiUrl: URL, accountToken: String, instanceId: String, secret: String, callback: Callback)

    interface Callback {
        fun onSuccess()
        fun onFailure(e: Exception)
    }
}