/*
package nl.stekkinger.nizi

import android.content.Context
import android.util.Log
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.result.Credentials

object CredentialsManager {

    private val TAG = SecureCredentialsManager::class.java.simpleName;
    private val PREFERENCES_NAME = "auth0"
    private val ACCESS_TOKEN = "access_token"

    fun saveCredentials(context: Context, credentials: Credentials) {
        val sp = context.getSharedPreferences(
            PREFERENCES_NAME, Context.MODE_PRIVATE)

        sp!!.edit().putString(ACCESS_TOKEN, credentials.accessToken)
            .apply()
    }

    fun getAccessToken(context: Context): String? {
        val sp = context.getSharedPreferences(
            PREFERENCES_NAME, Context.MODE_PRIVATE)

        return sp!!.getString(ACCESS_TOKEN, null)
    }

    fun clearCredentials(context: Context) {
        val sp = context.getSharedPreferences(
            PREFERENCES_NAME, Context.MODE_PRIVATE
        )

        sp!!.edit().clear().apply()
    }
}
*/
