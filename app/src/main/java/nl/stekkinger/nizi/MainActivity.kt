package nl.stekkinger.nizi

import android.accounts.Account
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val account = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        activity_main_btn_login.setOnClickListener {

            // Checks
            /*if (activity_main_et_username.text.toString() == "") {
                return@setOnClickListener
            }

            if (activity_main_et_password.text.toString() == "") {
                return@setOnClickListener
            }*/

            // Login using AuthRepository
            // ...

            login()
        }

    }
    private fun login() {
        account.isOIDCConformant = true

        WebAuthProvider.login(account)
            .withScheme("demo")
            .withAudience("appnizi.nl/api")
            .start(this, object : AuthCallback {
                override fun onFailure(dialog: Dialog) {
                    runOnUiThread { dialog.show() }
                }

                override fun onFailure(exception: AuthenticationException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity, "Oops, something went wrong!",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSuccess(credentials: Credentials) {
                    CredentialsManager.saveCredentials(this@MainActivity, credentials)
                }
            })
    }

    private fun logout() {
           WebAuthProvider.logout(account)
                    .start(this, logoutCallback)
    }
}
