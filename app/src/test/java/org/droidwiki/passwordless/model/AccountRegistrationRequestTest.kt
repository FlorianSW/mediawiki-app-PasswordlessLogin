package org.droidwiki.passwordless.model

import org.junit.Assert.*
import org.junit.Test
import java.net.URL

class AccountRegistrationRequestTest {
    @Test
    fun isComplete_missingField_fails() {
        val request = AccountRegistrationRequest()

        assertFalse(request.isComplete())
    }

    @Test
    fun isComplete_complete_succeeds() {
        val request = AccountRegistrationRequest(
            "A_NAME",
            URL("http:/localhost"),
            "A_PAIR_TOKEN",
            "AN_INSTANCE_ID",
            "A_SECRET"
        )

        assertTrue(request.isComplete())
    }

    @Test
    fun isComplete_emptyString_fails() {
        val request = AccountRegistrationRequest(
            "",
            URL("http:/localhost"),
            "",
            "",
            ""
        )

        assertFalse(request.isComplete())
    }
}