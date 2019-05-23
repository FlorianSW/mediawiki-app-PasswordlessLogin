package org.droidwiki.passwordless.adapter

import me.dm7.barcodescanner.zbar.Result
import org.droidwiki.passwordless.model.AccountRegistrationRequest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

class QRCodeToRegistrationRequestTest {
    @Test
    fun qrCodeToRegistrationRequest_validFormat_returnsCompleteRequest() {
        val qrResult = Result()
        qrResult.contents = "A_NAME;http://localhost;A_PAIR_TOKEN"

        val request = qrCodeToRegistrationRequest(qrResult)

        assertEquals(
            AccountRegistrationRequest(
                "A_NAME",
                URL("http://localhost"),
                "A_PAIR_TOKEN",
                null,
                null
            ), request
        )
    }
}