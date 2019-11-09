package org.droidwiki.passwordless

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.droidwiki.passwordless.adapter.MediaWikiCommunicator
import org.droidwiki.passwordless.adapter.NOTIFICATION_ID
import org.droidwiki.passwordless.adapter.SQLiteHelper
import org.droidwiki.passwordless.adapter.SecretAccountProvider
import org.droidwiki.passwordless.model.Account
import java.net.URL

class LoginConfirmationActivity : AppCompatActivity() {
    private val accountsProvider: AccountsProvider = SecretAccountProvider(SQLiteHelper(this))
    private val loginVerifier: LoginVerifier = MediaWikiCommunicator()

    private var disabledColor: Int = 0
    private var constructiveColor: Int = 0
    private var destructiveColor: Int = 0
    private lateinit var account: Account
    private lateinit var challenge: String
    private lateinit var confirmLogin: Button
    private lateinit var declineLogin: Button
    private lateinit var loading: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_confirmation)

        val apiUrl = intent.getStringExtra("apiUrl")
        challenge = intent.getStringExtra("challenge")

        val result = accountsProvider.findByApiUrl(URL(apiUrl))
        if (!result.isPresent) {
            throw RuntimeException("Not a known account.")
        }
        account = result.get()

        disabledColor = resources.getColor(R.color.colorSecondaryText, null)
        constructiveColor = resources.getColor(R.color.colorConstructive, null)
        destructiveColor = resources.getColor(R.color.colorDesctructive, null)

        findViewById<TextView>(R.id.account_name).text = account.name
        declineLogin = findViewById(R.id.button_no)
        confirmLogin = findViewById(R.id.button_yes)
        loading = findViewById(R.id.loading)

        declineLogin.setOnClickListener {
            onButtonPressed()
            finish()
        }
        confirmLogin.setOnClickListener {
            onButtonPressed()
            loading.visibility = View.VISIBLE
            val response = account.sign(challenge.toByteArray())
            loginVerifier.verify(URL(account.apiUrl), challenge, response, VerifyLoginCallback())
        }
    }

    private fun onButtonPressed() {
        declineLogin.setTextColor(disabledColor)
        declineLogin.isEnabled = false
        confirmLogin.setTextColor(disabledColor)
        confirmLogin.isEnabled = false
    }

    inner class VerifyLoginCallback : LoginVerifier.Callback {
        override fun onSuccess() {
            finish()
        }

        override fun onFailure(e: Exception) {
            runOnUiThread {
                loading.visibility = View.GONE
                declineLogin.setTextColor(destructiveColor)
                declineLogin.isEnabled = true
                confirmLogin.setTextColor(constructiveColor)
                confirmLogin.isEnabled = true
                Toast.makeText(this@LoginConfirmationActivity, "Login verification failed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun finish() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        super.finish()
    }
}
