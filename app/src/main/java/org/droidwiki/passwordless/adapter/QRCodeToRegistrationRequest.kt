package org.droidwiki.passwordless.adapter

import me.dm7.barcodescanner.zbar.Result
import org.droidwiki.passwordless.model.AccountRegistrationRequest
import java.net.URL

fun qrCodeToRegistrationRequest(qrCodeResult: Result): AccountRegistrationRequest {
    val parts = qrCodeResult.contents.split(";")
    if (parts.size == 3) {
        return AccountRegistrationRequest(
            parts[0],
            URL(parts[1]),
            parts[2],
            null,
            null
        )
    }
    return AccountRegistrationRequest()
}