package org.droidwiki.passwordless.adapter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.droidwiki.passwordless.LoginConfirmationActivity
import org.droidwiki.passwordless.R

const val NOTIFICATION_CHANNEL_ID = "login_request"
const val NOTIFICATION_ID = 0

class MessageService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val loginConfirmationIntent = Intent(this, LoginConfirmationActivity::class.java)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        loginConfirmationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        loginConfirmationIntent.putExtra("challenge", message.data.getValue("challenge"))
        loginConfirmationIntent.putExtra("apiUrl", message.data.getValue("apiUrl"))

        val notificationManager = createNotificationManager()
        notificationManager.notify(NOTIFICATION_ID, fromIntent(loginConfirmationIntent))

        startActivity(loginConfirmationIntent)
    }

    private fun fromIntent(loginConfirmationIntent: Intent): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, loginConfirmationIntent, PendingIntent.FLAG_ONE_SHOT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_key_black_24dp)
            .setAutoCancel(true)
            .setContentTitle(getString(R.string.login_notification_title))
            .setContentText(getString(R.string.login_notification_body))
            .setFullScreenIntent(pendingIntent, true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()
    }

    private fun createNotificationManager(): NotificationManager {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_login_requests), NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = getString(R.string.notification_channel_login_requests_description)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return notificationManager
    }
}
