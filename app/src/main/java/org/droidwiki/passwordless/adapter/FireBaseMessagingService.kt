package org.droidwiki.passwordless.adapter

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.droidwiki.passwordless.LoginConfirmationActivity

class MessageService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val loginConfirmationIntent = Intent(this, LoginConfirmationActivity::class.java)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        loginConfirmationIntent.putExtra("challenge", message.data.getValue("challenge"))
        loginConfirmationIntent.putExtra("apiUrl", message.data.getValue("apiUrl"))
        startActivity(loginConfirmationIntent)
    }
}