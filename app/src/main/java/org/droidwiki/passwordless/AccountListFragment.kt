package org.droidwiki.passwordless

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.droidwiki.passwordless.adapter.*
import org.droidwiki.passwordless.model.Account
import org.droidwiki.passwordless.model.AccountRegistrationRequest
import java.net.URL

class AccountListFragment : Fragment() {
    private var noAccountsText: TextView? = null
    private var cameraView: ZBarScannerView? = null
    private var alertDialog: AlertDialog? = null

    private lateinit var accountListContent: AccountArrayAdapter
    private lateinit var accountsProvider: AccountsProvider
    private val registrationService: Registration = MediaWikiCommunicator()
    private lateinit var addAccountButton: FloatingActionButton

    fun reloadList() {
        accountListContent.clear()
        noAccountsText?.visibility = View.GONE

        accountsProvider.list().forEach {
            accountListContent.add(it)
        }

        if (accountListContent.isEmpty) {
            noAccountsText?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_list, container, false)
        noAccountsText = view.findViewById(R.id.no_accounts_text)

        val accountList = view.findViewById<ListView>(R.id.account_list)
        accountListContent = AccountArrayAdapter(context!!, R.layout.list_item)
        accountListContent.setOnDeleteListener(object : AccountArrayAdapter.OnDeleteListener {
            override fun onDelete(account: Account) {
                accountsProvider.remove(account.id)
                reloadList()
            }

        })
        accountList?.adapter = accountListContent
        addAccountButton = view.findViewById(R.id.action_add)
        addAccountButton.setOnClickListener {
            openAddAccountDialog()
        }

        reloadList()

        return view
    }

    private fun openAddAccountDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.register_title))
        builder.setView(R.layout.add_account_dialog)
        builder.setPositiveButton(getString(R.string.register_positive)) { _, _ -> run {} }
        builder.setNegativeButton(getString(R.string.register_negative)) { dialog, _ -> dialog.cancel() }
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

                doRegisterAccount(AccountRegistrationRequest(name, apiURL, accountToken, it, secret))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)
        addAccountButton.startAnimation(scaleDown)
        addAccountButton.hide()
        cameraView?.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        val scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up)
        addAccountButton.startAnimation(scaleUp)
        addAccountButton.show()
        cameraView?.setResultHandler(QRResultHandler())
        cameraView?.startCamera()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        accountsProvider = SecretAccountProvider(SQLiteHelper(context))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            50 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAddAccountDialog()
                    onQrCodeButtonClicked(alertDialog?.findViewById(R.id.qr_scanner)!!)
                }
            }
        }
    }

    private fun onFirebaseInstanceId(action: (instanceId: String) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isComplete) {
                activity?.runOnUiThread {
                    Toast.makeText(context, getString(R.string.register_firebase_error), Toast.LENGTH_LONG).show()
                }
                return@OnCompleteListener
            }
            val instanceId = task.result!!.token

            action(instanceId)
        })
    }

    private fun doRegisterAccount(request: AccountRegistrationRequest) {
        if (!request.isComplete()) {
            activity?.runOnUiThread {
                Toast.makeText(context, getString(R.string.register_mandatory_fields_missing), Toast.LENGTH_LONG).show()
            }
            return
        }
        registrationService.register(
            request,
            RegisterCallback(alertDialog!!, request.accountName!!)
        )
    }

    private fun onQrCodeButtonClicked(scannerView: ZBarScannerView) {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 50)
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
            activity?.runOnUiThread {
                Toast.makeText(context, getString(R.string.register_registration_failed, e.message), Toast.LENGTH_LONG)
                    .show()
            }
        }

        override fun onSuccess() {
            activity?.runOnUiThread {
                reloadList()
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
                    activity?.runOnUiThread {
                        Toast.makeText(context, getString(R.string.register_qr_code_format_error), Toast.LENGTH_LONG)
                            .show()
                    }
                    cameraView?.resumeCameraPreview(this)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AccountListFragment().apply {
                arguments = Bundle()
            }
    }
}
