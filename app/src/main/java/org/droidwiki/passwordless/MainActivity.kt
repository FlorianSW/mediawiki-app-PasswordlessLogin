package org.droidwiki.passwordless

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var accountListFragment: AccountListFragment
    private var screenLockRequiredDialog: AlertDialog? = null

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        screenLockRequiredDialog?.dismiss()
        ensureDeviceSecure()
    }

    private fun onNavigation(itemId: Int) {
        var newFragment: androidx.fragment.app.Fragment? = null
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

    private fun ensureDeviceSecure() {
        val keyGuard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyGuard.isDeviceSecure) {
            screenLockRequiredDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.screen_lock_required_title))
                .setPositiveButton(getString(R.string.screen_lock_settings_button)) { _, _ -> run {} }
                .setView(R.layout.screen_lock_required_dialog)
                .setCancelable(false)
                .create()

            screenLockRequiredDialog?.show()
            screenLockRequiredDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
            }
        }
    }
}
