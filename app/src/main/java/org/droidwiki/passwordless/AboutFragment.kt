package org.droidwiki.passwordless

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.LibsBuilder

private const val URL_TO_GITHUB = "https://github.com/FlorianSW/mediawiki-app-PasswordlessLogin"

class AboutFragment : MaterialAboutFragment() {

    override fun getMaterialAboutList(p0: Context?): MaterialAboutList {
        val appCard = MaterialAboutCard.Builder()
        appCard.addItem(
            MaterialAboutTitleItem.Builder()
                .text("Passwordless Login")
                .desc("© 2019 Florian Schmidt")
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Version")
                .subText(BuildConfig.VERSION_NAME)
                .icon(R.drawable.ic_update_grey_24dp)
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Changelog")
                .icon(R.drawable.ic_track_changes_grey_24dp)
                .setOnClickAction(clickRedirect("$URL_TO_GITHUB/releases"))
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Source Code")
                .icon(R.drawable.ic_github_grey_24dp)
                .setOnClickAction(clickRedirect(URL_TO_GITHUB))
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Report issue")
                .icon(R.drawable.ic_bug_report_grey_24dp)
                .setOnClickAction(clickRedirect("https://phabricator.wikimedia.org/tag/passwordless-login/"))
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Lizenz")
                .subText("MIT")
                .icon(R.drawable.ic_account_balance_grey_24dp)
                .setOnClickAction(clickRedirect("$URL_TO_GITHUB/blob/master/LICENSE"))
                .build()
        )
        appCard.addItem(
            MaterialAboutActionItem.Builder()
                .text("Used libraries")
                .icon(R.drawable.ic_code_grey_24dp)
                .setOnClickAction {
                    val f = LibsBuilder()
                        .withFields(R.string::class.java.fields)
                        .withLicenseShown(true)
                        .withAutoDetect(true)
                        .supportFragment()
                    fragmentManager!!
                        .beginTransaction()
                        .replace(R.id.content, f)
                        .setBreadCrumbTitle("Used libraries")
                        .addToBackStack(null)
                        .commit()
                }
                .build()
        )
        return MaterialAboutList.Builder()
            .addCard(appCard.build())
            .build()
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_MaterialAboutActivity_Fragment
    }

    private fun clickRedirect(url: String): () -> Unit {
        return { openInBrowser(context!!, url) }
    }

    private fun openInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No browser found", Toast.LENGTH_LONG).show()
        }

    }
}