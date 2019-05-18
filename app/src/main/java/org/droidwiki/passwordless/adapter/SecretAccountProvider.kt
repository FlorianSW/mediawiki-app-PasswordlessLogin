package org.droidwiki.passwordless.adapter

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.droidwiki.passwordless.AccountsProvider
import org.droidwiki.passwordless.adapter.SQLiteHelper.Companion.ACCOUNT_TABLE_NAME
import org.droidwiki.passwordless.adapter.SQLiteHelper.Companion.COLUMN_API_URL
import org.droidwiki.passwordless.adapter.SQLiteHelper.Companion.COLUMN_NAME
import org.droidwiki.passwordless.model.Account
import java.lang.RuntimeException
import java.net.URL
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class SecretAccountProvider(private val sqLiteHelper: SQLiteHelper) : AccountsProvider {

    override fun create(name: String, apiUrl: URL): PublicKey {
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_API_URL, apiUrl.toString())
        val writableDatabase = sqLiteHelper.writableDatabase
        val result = writableDatabase.insert(ACCOUNT_TABLE_NAME, null, values)
        writableDatabase.close()
        if (result == -1L) {
            throw RuntimeException("Database entry could not be created.")
        }

        return createSecretKey(name).public
    }

    private fun createSecretKey(name: String): KeyPair {
        val keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        keyGenerator.initialize(
            KeyGenParameterSpec.Builder(name, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_SIGN)
            .setKeySize(2048)
            .build()
        )

        return keyGenerator.genKeyPair()
    }

    override fun list(): List<Account> {
        val readableDatabase = sqLiteHelper.readableDatabase
        val resultSet = readableDatabase.query(
            ACCOUNT_TABLE_NAME, arrayOf(COLUMN_NAME, COLUMN_API_URL), null,
            arrayOf<String>(), null, null, null
        )

        if (resultSet.count == 0) {
            resultSet.close()
            return listOf()
        }

        resultSet.moveToFirst()
        val elements = mutableListOf<Account>()
        while (!resultSet.isAfterLast) {
            elements.add(Account(resultSet.getString(resultSet.getColumnIndex(COLUMN_NAME)),
                resultSet.getString(resultSet.getColumnIndex(COLUMN_API_URL))))
            resultSet.moveToNext()
        }
        resultSet.close()

        return elements
    }

    override fun remove(name: String) {
        sqLiteHelper.writableDatabase.delete(ACCOUNT_TABLE_NAME, "$COLUMN_NAME = '$name'", arrayOf())

        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(name)
    }
}

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(
            "CREATE TABLE " + ACCOUNT_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_API_URL + " TEXT)"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $ACCOUNT_TABLE_NAME")
        onCreate(sqLiteDatabase)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "database"
        const val ACCOUNT_TABLE_NAME = "accounts"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_API_URL = "api_url"
    }
}