package nl.stekkinger.nizi

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import nl.stekkinger.nizi.classes.PatientLogin
import nl.stekkinger.nizi.repositories.AuthRepository

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"

    private val authRepository: AuthRepository = AuthRepository()
    private var model: PatientLogin? = null
    private lateinit var progressBar: View

    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = activity_main_progressbar

        //model = ViewModelProviders.of(this)[PatientLogin::class.java]

        activity_main_btn_register.setOnClickListener { registerPatient() }
        activity_main_btn_login.setOnClickListener { loginPatient() }
        activity_main_btn_logout.setOnClickListener { logout() }

        //Obtain the token from the Intent's extras
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
        //activity_main_txt_credentials.text = accessToken


    }

    private fun registerPatient() {
        registerPatientAsyncTask(accessToken).execute()
    }

    private fun loginPatient() {

        loginPatientAsyncTask(accessToken).execute()


        /*val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()*/
    }

    private fun logout() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }

    inner class registerPatientAsyncTask(accessToken: String) : AsyncTask<Void, Void, Void>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            authRepository.registerPatient(accessToken)
            return null
        }

    }

    inner class loginPatientAsyncTask(accessToken: String) : AsyncTask<Void, Void, PatientLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): PatientLogin? {
            return authRepository.loginAsPatient(accessToken)
        }

        override fun onPostExecute(result: PatientLogin?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                activity_main_txt_nierstichtingPhonenumber.text = result.account.role
            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }

        }

    }
 }
