package org.droidwiki.passwordless.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.droidwiki.passwordless.adapter.SQLiteHelper.Companion.ACCOUNT_TABLE_NAME
import org.droidwiki.passwordless.model.Account
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class SecretAccountProviderTest {
    private lateinit var provider: SecretAccountProvider
    private lateinit var sqLiteHelper: SQLiteHelper

    @Before
    fun setUp() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        sqLiteHelper = SQLiteHelper(appContext)
        provider = SecretAccountProvider(sqLiteHelper)

        cleanUp()
    }

    @After
    fun cleanUp() {
        sqLiteHelper.writableDatabase.delete(ACCOUNT_TABLE_NAME, "", arrayOf())

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyStore.aliases().toList().forEach { keyStore.deleteEntry(it) }
    }

    @Test
    fun list_noItems_emptyList() {
        assertEquals(listOf<Account>(), provider.list())
    }

    @Test
    fun persists_data() {
        val apiUrl = URL("https://localhost/w/api.php")

        provider.create("A_NAME", apiUrl)

        assertEquals(1, provider.list().size)
    }

    @Test
    fun creates_unique_secret_key() {
        val apiUrl = URL("https://localhost/w/api.php")

        val secretKey = provider.create("A_NAME", apiUrl)
        val anotherSecretKey = provider.create("ANOTHER_NAME", apiUrl)

        assertNotEquals(secretKey, anotherSecretKey);
    }

    @Test
    fun removes_entry() {
        val apiUrl = URL("https://localhost/w/api.php")
        provider.create("A_NAME", apiUrl)

        provider.remove("A_NAME")

        assertEquals(0, provider.list().size)
        assertEquals(0, keyStoreSize())
    }

    @Test
    fun findsByApiUrl() {
        val apiUrl = URL("https://localhost/w/api.php")
        provider.create("A_NAME", apiUrl)

        val result = provider.findByApiUrl(apiUrl)

        assertEquals("A_NAME", result.get().name)
    }

    @Test
    fun findByApiUrl_noEntry_emptyOptional() {
        val apiUrl = URL("https://localhost/w/api.php")
        provider.create("A_NAME", apiUrl)

        val result = provider.findByApiUrl(URL("http://localhost/wiki/api.php"))

        assertFalse(result.isPresent)
    }

    private fun keyStoreSize(): Int  = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.aliases().toList().size
}