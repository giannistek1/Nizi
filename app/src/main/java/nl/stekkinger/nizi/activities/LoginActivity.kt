package nl.stekkinger.nizi.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.d
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
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DoctorLogin
import nl.stekkinger.nizi.classes.PatientLogin

class LoginActivity : AppCompatActivity() {

    private var TAG = "Login"

    //Shared preferences/extras
    private val CODE_DEVICE_AUTHENTICATION = 22
    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    val EXTRA_ID_TOKEN = "com.auth0.ID_TOKEN"
    val PREF_ISDOCTOR = "IS_DOCTOR"

    private var auth0: Auth0? = null
    private var credentialsManager: SecureCredentialsManager? = null

    private var isDoctor = false
    private lateinit var patientLogin: PatientLogin
    private lateinit var doctorLogin: DoctorLogin
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        //Setup the UI
        setContentView(R.layout.activity_login)
        activity_login_btn_loginAsPatient.setOnClickListener {
            isDoctor = false
            prefs.edit().putBoolean(PREF_ISDOCTOR, isDoctor).commit()
            doLogin()
        }
        activity_login_btn_loginAsDoctor.setOnClickListener {
            isDoctor = true
            prefs.edit().putBoolean(PREF_ISDOCTOR, isDoctor).commit()
            doLogin()
        }

        //Setup CredentialsManager
        auth0 = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))
        auth0?.let {
            // needed for universal login
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

        // if already logged in
        credentialsManager?.let {
            if (it.hasValidCredentials()) {
                // Obtain the existing credentials and move to the next activity
                isDoctor = prefs.getBoolean(PREF_ISDOCTOR, false)
                showNextActivity()
            }
        }

    }

    private fun showNextActivity() {
        credentialsManager?.let {
            it.getCredentials(object :
                BaseCallback<Credentials, CredentialsManagerException> {
                override fun onSuccess(credentials: Credentials) {
                    var intent: Intent

                    if (isDoctor)
                        intent = Intent(this@LoginActivity, DoctorMainActivity::class.java)
                    else
                        intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.accessToken)
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
            d("cr", credentials.accessToken)
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
                prefs.edit().remove(PREF_ISDOCTOR).commit()
                Log.d(TAG, "Succesfully logged out!")
            }
        }

        override fun onFailure(error: Auth0Exception?) {
            //Log out canceled, keep the user logged in
            showNextActivity()
        }

    }
}
