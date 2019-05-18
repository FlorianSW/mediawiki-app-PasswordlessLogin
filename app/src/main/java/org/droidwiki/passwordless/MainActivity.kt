package org.droidwiki.passwordless

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.droidwiki.passwordless.adapter.MediaWikiCommunicator
import org.droidwiki.passwordless.adapter.SQLiteHelper
import org.droidwiki.passwordless.adapter.SecretAccountProvider
import java.io.IOException
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val accountsProvider: AccountsProvider = SecretAccountProvider(SQLiteHelper(this))
    private val registrationService: Registration = MediaWikiCommunicator(accountsProvider)

    private lateinit var accountListContent: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureDeviceSecure()

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val accountList = findViewById<ListView>(R.id.account_list)
        accountListContent = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        accountList.adapter = accountListContent
        accountList.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                if (view is AppCompatTextView) {
                    accountsProvider.remove(view.text as String)
                    reloadList()
                    return true
                }
                return false
            }
        }

        reloadList()
    }

    private fun ensureDeviceSecure() {
        val keyGuard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyGuard.isDeviceSecure) {
            val lockScreenInfo = LinearLayout(this)
            lockScreenInfo.orientation = LinearLayout.VERTICAL

            val infoText = TextView(this)
            infoText.text = "You need to setup a screen lock in order to use this app."
            lockScreenInfo.addView(infoText)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Screen lock required")
                .setPositiveButton("Setup") { _, _ -> run {} }
                .setView(lockScreenInfo)
                .setCancelable(false)
                .create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add a new account")

        val form = LinearLayout(this)
        form.orientation = LinearLayout.VERTICAL

        val accountNameLabel = TextView(this)
        accountNameLabel.text = "Account name"
        form.addView(accountNameLabel)
        val accountName = EditText(this)
        accountName.inputType = InputType.TYPE_CLASS_TEXT
        form.addView(accountName)

        val apiUrlLabel = TextView(this)
        apiUrlLabel.text = "API URL"
        form.addView(apiUrlLabel)
        val apiUrl = EditText(this)
        apiUrl.inputType = InputType.TYPE_CLASS_TEXT
        apiUrl.text = SpannableStringBuilder("http://10.0.2.2:8080/w/api.php")
        form.addView(apiUrl)

        val tokenLabel = TextView(this)
        tokenLabel.text = "Token"
        form.addView(tokenLabel)
        val token = EditText(this)
        token.inputType = InputType.TYPE_CLASS_TEXT
        token.text = SpannableStringBuilder("123")
        form.addView(token)

        builder.setView(form)

        builder.setPositiveButton("OK") { _, _ -> run {} }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isComplete) {
                    runOnUiThread {
                        Toast.makeText(this, "Could not get Instance ID", Toast.LENGTH_LONG).show()
                    }
                    return@OnCompleteListener
                }
                val instanceId = task.result!!.token

                val apiURL = URL(apiUrl.text.toString())
                val name = accountName.text.toString()
                val accountToken = token.text.toString()

                registrationService.register(
                    name,
                    apiURL,
                    accountToken,
                    instanceId,
                    RegisterCallback(alertDialog, name)
                )
            })
        }

        return true
    }

    private fun reloadList() {
        accountListContent.clear()

        accountsProvider.list().forEach {
            accountListContent.add(it.name)
        }
    }

    inner class RegisterCallback(private val alertDialog: AlertDialog, private val accountName: String) : Registration.Callback {
        override fun onFailure(e: Exception) {
            accountsProvider.remove(accountName)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onSuccess() {
            runOnUiThread {
                reloadList()
                alertDialog.dismiss()
            }
        }
    }
}

class RegisterResponse {
    lateinit var register: Register
}

class Register {
    lateinit var result: RegisterResult
}

enum class RegisterResult {
    Success, Failed
}
