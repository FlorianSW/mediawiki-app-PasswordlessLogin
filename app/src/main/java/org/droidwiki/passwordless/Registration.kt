package org.droidwiki.passwordless

import org.droidwiki.passwordless.model.AccountRegistrationRequest

interface Registration {
    fun register(request: AccountRegistrationRequest, callback: Callback)

    interface Callback {
        fun onSuccess()
        fun onFailure(e: Exception)
    }
}