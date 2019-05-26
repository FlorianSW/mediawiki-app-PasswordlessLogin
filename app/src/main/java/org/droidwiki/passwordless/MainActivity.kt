package org.droidwiki.passwordless

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.droidwiki.passwordless.adapter.*
import org.droidwiki.passwordless.model.AccountRegistrationRequest
import java.net.URL


class MainActivity : AppCompatActivity(), AccountListFragment.AccountListListener {
    private val accountsProvider: AccountsProvider = SecretAccountProvider(SQLiteHelper(this))
    private val registrationService: Registration = MediaWikiCommunicator()
    private lateinit var accountListFragment: AccountListFragment

    private var cameraView: ZBarScannerView? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureDeviceSecure()

        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            onNavigation(it.itemId)
            true
        }
        bottomNavigationView.selectedItemId = R.id.action_accounts
    }

    private fun onNavigation(itemId: Int) {
        var newFragment: Fragment? = null
        if (itemId == R.id.action_accounts) {
            accountListFragment = AccountListFragment.newInstance()
            newFragment = accountListFragment
        }
        if (itemId == R.id.action_app_info) {
            newFragment = AboutFragment()
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, newFragment!!)
        fragmentTransaction.commit()
    }

    override fun onPause() {
        super.onPause()
        cameraView?.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        cameraView?.setResultHandler(QRResultHandler())
        cameraView?.startCamera()
    }

    override fun accountsProvider(): AccountsProvider {
        return accountsProvider
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            50 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    openAddAccountDialog()
                    onQrCodeButtonClicked(alertDialog?.findViewById(R.id.qr_scanner)!!)
                }
            }
        }
    }

    override fun openAddAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add a new MediaWiki site")
        builder.setView(R.layout.add_account_dialog)
        builder.setPositiveButton("Register") { _, _ -> run {} }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        alertDialog = builder.create()
        alertDialog?.show()

        val accountName = alertDialog?.findViewById<EditText>(R.id.account_name)
        val apiUrl = alertDialog?.findViewById<EditText>(R.id.api_url)

        val token = alertDialog?.findViewById<EditText>(R.id.pair_token)

        val qrCodeButton = alertDialog?.findViewById<Button>(R.id.scan_qr_code_button)
        qrCodeButton!!.setOnClickListener {
            onQrCodeButtonClicked(alertDialog?.findViewById(R.id.qr_scanner)!!)
        }

        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            onFirebaseInstanceId {
                val apiURL = URL(apiUrl!!.text.toString())
                val name = accountName!!.text.toString()
                val accountToken = token!!.text.toString()
                val secret = accountsProvider.create(name, apiURL)

                val request = AccountRegistrationRequest(
                    name,
                    apiURL,
                    accountToken,
                    it,
                    secret
                )
                doRegisterAccount(request)
            }
        }
    }

    private fun onFirebaseInstanceId(action: (instanceId: String) -> Unit): Unit {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isComplete) {
                runOnUiThread {
                    Toast.makeText(this, "Could not get Instance ID", Toast.LENGTH_LONG).show()
                }
                return@OnCompleteListener
            }
            val instanceId = task.result!!.token

            action(instanceId)
        })
    }

    private fun doRegisterAccount(request: AccountRegistrationRequest) {
        if (!request.isComplete()) {
            runOnUiThread {
                Toast.makeText(this, "You need to fill out all fields", Toast.LENGTH_LONG).show()
            }
            return
        }
        registrationService.register(
            request,
            RegisterCallback(alertDialog!!, request.accountName!!)
        )
    }

    private fun onQrCodeButtonClicked(scannerView: ZBarScannerView) {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)
            return
        }

        cameraView = scannerView
        scannerView.setResultHandler(QRResultHandler())
        scannerView.startCamera()
    }

    inner class RegisterCallback(private val alertDialog: AlertDialog, private val accountName: String) :
        Registration.Callback {
        override fun onFailure(e: Exception) {
            accountsProvider.remove(accountName)
            cameraView?.startCamera()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onSuccess() {
            runOnUiThread {
                accountListFragment.reloadList()
                alertDialog.dismiss()
            }
        }
    }

    inner class QRResultHandler : ZBarScannerView.ResultHandler {
        override fun handleResult(rawResult: Result) {
            onFirebaseInstanceId {
                val request = qrCodeToRegistrationRequest(rawResult)
                val intermediateSecret = "INTERMEDIATE_SECRET"
                request.secret = intermediateSecret
                request.instanceId = it
                if (request.isComplete()) {
                    cameraView?.stopCamera()
                    val secret = accountsProvider.create(request.accountName!!, request.apiUrl!!)
                    request.secret = secret
                    doRegisterAccount(request)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Not a valid Pair QR Code", Toast.LENGTH_LONG).show()
                    }
                    cameraView?.resumeCameraPreview(this)
                }
            }
        }
    }
}
