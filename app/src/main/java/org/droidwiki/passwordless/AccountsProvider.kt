package org.droidwiki.passwordless

import org.droidwiki.passwordless.model.Account
import java.net.URL
import java.util.*

interface AccountsProvider {
    fun list(): List<Account>
    fun create(name: String, apiUrl: URL): String
    fun remove(id: Int)
    fun remove(name: String)
    fun findByApiUrl(apiUrl: URL): Optional<Account>
}