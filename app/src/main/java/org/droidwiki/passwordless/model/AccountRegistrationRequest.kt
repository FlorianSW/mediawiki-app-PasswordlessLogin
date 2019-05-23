package org.droidwiki.passwordless.model

import java.net.URL

data class AccountRegistrationRequest(
    val accountName: String? = null,
    val apiUrl: URL? = null,
    val pairToken: String? = null,
    var instanceId: String? = null,
    var secret: String? = null
) {
    fun isComplete(): Boolean {
        return !accountName.isNullOrEmpty() &&
                apiUrl != null &&
                !pairToken.isNullOrEmpty() &&
                !instanceId.isNullOrEmpty() &&
                !secret.isNullOrEmpty()
    }
}
