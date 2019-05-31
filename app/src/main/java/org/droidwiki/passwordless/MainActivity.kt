package org.droidwiki.passwordless

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var accountListFragment: AccountListFragment

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

    private fun ensureDeviceSecure() {
        val keyGuard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyGuard.isDeviceSecure) {
            val lockScreenInfo = LinearLayout(this)
            lockScreenInfo.orientation = LinearLayout.VERTICAL

            val infoText = TextView(this)
            infoText.text = getString(R.string.screen_lock_required)
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
}
