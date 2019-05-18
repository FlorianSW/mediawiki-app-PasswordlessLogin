package org.droidwiki.passwordless.adapter

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.droidwiki.passwordless.LoginConfirmationActivity

class MessageService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val loginConfirmationIntent = Intent(this, LoginConfirmationActivity::class.java)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(loginConfirmationIntent)
    }
}