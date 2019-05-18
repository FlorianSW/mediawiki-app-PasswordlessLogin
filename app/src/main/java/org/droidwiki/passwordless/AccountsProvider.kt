package org.droidwiki.passwordless

import org.droidwiki.passwordless.model.Account
import java.net.URL
import java.security.PublicKey

interface AccountsProvider {
    fun list(): List<Account>
    fun create(name: String, apiUrl: URL): PublicKey
    fun remove(name: String)
}