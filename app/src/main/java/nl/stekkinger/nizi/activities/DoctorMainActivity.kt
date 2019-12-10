package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_doctor_main.*
import nl.stekkinger.nizi.adapters.PatientAdapter
import nl.stekkinger.nizi.adapters.PatientAdapterListener
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class DoctorMainActivity : AppCompatActivity() {

    private var TAG = "DoctorMain"

    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()

    private lateinit var model: DoctorLogin

    private lateinit var progressBar: View
    private lateinit var accessToken: String
    val patientList = ArrayList<PatientItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_main)
        progressBar = activity_doctor_main_progressbar
        activity_doctor_main_btn_logout.setOnClickListener { logout() }

        // Get accesstoken
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)

        // Login as doctor for doctor data
        loginDoctorAsyncTask().execute()

        activity_doctor_main_btn_addPatient.setOnClickListener {
            val intent: Intent = Intent(this@DoctorMainActivity, AddPatientActivity::class.java)
            intent.putExtra(EXTRA_ACCESS_TOKEN, accessToken)
            intent.putExtra(EXTRA_DOCTOR_ID, model.doctor.doctorId)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {

        // Listener for recycleview
        val listener = object: PatientAdapterListener
        {
            override fun onItemClick(position: Int) {
                // Open detail page when clickec
                val intent: Intent = Intent(this@DoctorMainActivity, PatientDetailActivity::class.java)

                intent.putExtra("NAME", "${patientList[position].name}")
                startActivity(intent)
            }
        }

        // Create adapter (data)
        activity_doctor_main_rv.adapter = PatientAdapter(this, patientList, listener)

        // Create Linear Layout Manager
        activity_doctor_main_rv.layoutManager = LinearLayoutManager(this)
    }

    // Dubbel
    private fun logout() {
        val intent = Intent(this@DoctorMainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }


    inner class loginDoctorAsyncTask() : AsyncTask<Void, Void, DoctorLogin>()
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
                // Save doctor model
                model = result
                // Start get patients from doc
                getPatientsFromDoctorAsyncTask().execute()
            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class getPatientsFromDoctorAsyncTask() : AsyncTask<Void, Void, List<Patient>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): List<Patient>? {
            model?.let {
                return patientRepository.getPatientsFromDoctor(accessToken, it.doctor.doctorId)
            }
            return null
        }

        override fun onPostExecute(result: List<Patient>?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.get_patients_success, Toast.LENGTH_SHORT).show()

                // Clear
                patientList.clear()

                // Fill
                (0..result.count()-1).forEach {
                    val pi = PatientItem(it+1, "${result[it].firstName} ${result[it].lastName}")
                    patientList.add(pi)
                }

                setupRecyclerView()
            } else {
                Toast.makeText(baseContext, R.string.get_patients_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
