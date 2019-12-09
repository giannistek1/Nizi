package nl.stekkinger.nizi

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_doctor_main.*
import nl.stekkinger.nizi.classes.DoctorLogin
import nl.stekkinger.nizi.repositories.AuthRepository

class DoctorMainActivity : AppCompatActivity() {

    private var TAG = "Patients"

    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"

    private val authRepository: AuthRepository = AuthRepository()

    private lateinit var progressBar: View
    private lateinit var accessToken: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_main)
        progressBar = activity_patients_progressbar
        activity_doctor_main_btn_logout.setOnClickListener { logout() }

        // Get accesstoken
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)

        loginDoctorAsyncTask(accessToken).execute()
    }


    private fun registerPatient() {
        registerPatientAsyncTask(accessToken).execute()
    }

    // Dubbel
    private fun logout() {
        val intent = Intent(this@DoctorMainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }


    inner class loginDoctorAsyncTask(accessToken: String) : AsyncTask<Void, Void, DoctorLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DoctorLogin? {
            return authRepository.loginAsDoctor(accessToken)
        }

        override fun onPostExecute(result: DoctorLogin?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                //activity_main_txt_nierstichtingPhonenumber.text = result.account.role
            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }

        }

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

}
