package org.droidwiki.passwordless

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.droidwiki.passwordless.adapter.MediaWikiCommunicator
import org.droidwiki.passwordless.adapter.SQLiteHelper
import org.droidwiki.passwordless.adapter.SecretAccountProvider
import org.droidwiki.passwordless.model.Account
import java.net.URL

class LoginConfirmationActivity : AppCompatActivity() {
    private val accountsProvider: AccountsProvider = SecretAccountProvider(SQLiteHelper(this))
    private val loginVerifier: LoginVerifier = MediaWikiCommunicator()

    private lateinit var account: Account
    private lateinit var challenge: String

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

        findViewById<TextView>(R.id.account_name).text = account.name
        findViewById<Button>(R.id.button_no).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.button_yes).setOnClickListener {
            val response = account.sign(challenge.toByteArray())
            loginVerifier.verify(URL(account.apiUrl), challenge, response, VerifyLoginCallback())
        }
    }

    inner class VerifyLoginCallback: LoginVerifier.Callback {
        override fun onSuccess() {
            finish()
        }

        override fun onFailure(e: Exception) {
            runOnUiThread {
                Toast.makeText(this@LoginConfirmationActivity, "Login verification failed.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
