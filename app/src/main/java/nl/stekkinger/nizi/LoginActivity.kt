package nl.stekkinger.nizi

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var TAG = "Login"

    private var auth0: Auth0? = null
    private var credentialsManager: SecureCredentialsManager? = null

    private val CODE_DEVICE_AUTHENTICATION = 22
    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    val EXTRA_ID_TOKEN = "com.auth0.ID_TOKEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup the UI
        setContentView(R.layout.activity_login)
        activity_login_btn_login.setOnClickListener { doLogin() }

        //Setup CredentialsManager
        auth0 = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))
        auth0?.let {
            it.isOIDCConformant = true

            credentialsManager = SecureCredentialsManager(this,
                AuthenticationAPIClient(it),
                SharedPreferencesStorage(this)
            )
        }

        //Check if the activity was launched to log the user out
        if (intent.getBooleanExtra(EXTRA_CLEAR_CREDENTIALS, false)) {
            doLogout()
            return
        }

        credentialsManager?.let {
            if (it.hasValidCredentials()) {
                // Obtain the existing credentials and move to the next activity
                showNextActivity()
            }
        }

    }

    /** NOT USED
     * Override required when setting up Local Authentication in the Credential Manager
     * Refer to SecureCredentialsManager#requireAuthentication method for more information.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        credentialsManager?.let {
            if (it.checkAuthenticationResult(requestCode, resultCode)) {
                return
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showNextActivity() {
        credentialsManager?.let {
            it.getCredentials(object :
                BaseCallback<Credentials, CredentialsManagerException> {
                override fun onSuccess(credentials: Credentials) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.accessToken)
                    intent.putExtra(EXTRA_ID_TOKEN, credentials.idToken)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(error: CredentialsManagerException) {
                    //Authentication cancelled by the user. Exit the app
                    finish()
                }
            })
        }
    }

    private fun doLogin() {
        auth0?.let {
            WebAuthProvider.login(it)
                .withScheme(getString(R.string.auth0_scheme))
                .withAudience(getString(R.string.auth0_audience))
                .withScope("openid offline_access")
                .start(this, loginCallback)
        }
    }

    private fun doLogout() {
        auth0?.let {
            WebAuthProvider.logout(it)
                .withScheme(getString(R.string.auth0_scheme))
                .start(this, logoutCallback)
        }
    }

    private val loginCallback = object : AuthCallback {
        override fun onFailure(dialog: Dialog) {
            runOnUiThread { dialog.show() }
        }

        override fun onFailure(exception: AuthenticationException?) {
            runOnUiThread {
                Toast.makeText(
                    this@LoginActivity,
                    "Log In - Error Occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onSuccess(credentials: Credentials) {
            credentialsManager?.let {
                it.saveCredentials(credentials)
                showNextActivity()

                Log.d(TAG, "Succesfully logged in!")
            }
        }
    }

    private val logoutCallback = object : VoidCallback {
        override fun onSuccess(payload: Void?) {
            credentialsManager?.let {
                it.clearCredentials()
                Log.d(TAG, "Succesfully logged out!")
            }
        }

        override fun onFailure(error: Auth0Exception?) {
            //Log out canceled, keep the user logged in
            showNextActivity()
        }

    }
}
