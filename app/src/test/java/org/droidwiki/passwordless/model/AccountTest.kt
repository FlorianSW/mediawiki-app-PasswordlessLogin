package org.droidwiki.passwordless.model

import org.junit.Assert.*
import org.junit.Test

const val EXPECTED_SIGNATURE = "583d2613cbf746290c752e4e07db95423c8538397f2bdddb176ab30b2a0aea877176e647d3319b7f85c706211551e362a91dd2c83866bf1fbdcead1f9f1b2ec9"

class AccountTest {

    @Test
    internal fun sign_returnsSignedHexString() {
        val account = Account(1, "A_NAME", "AN_URL", "A_SECRET")

        val result = account.sign("A_CHALLENGE".toByteArray())

        assertEquals(EXPECTED_SIGNATURE, result)
    }
}