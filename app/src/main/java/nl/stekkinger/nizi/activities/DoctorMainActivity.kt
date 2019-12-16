package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.auth0.android.provider.WebAuthProvider.logout
import com.google.android.material.navigation.NavigationView
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
    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()

    private lateinit var model: DoctorLogin

    private lateinit var progressBar: View
    val patientList = ArrayList<PatientItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_main)
        progressBar = activity_doctor_main_progressbar

        // Login as doctor for doctor data
        loginDoctorAsyncTask().execute()

        activity_doctor_main_btn_addPatient.setOnClickListener {
            val intent: Intent = Intent(this@DoctorMainActivity, AddPatientActivity::class.java)
            intent.putExtra(EXTRA_DOCTOR_ID, model.doctor.doctorId)
            startActivity(intent)
        }
    }

    //region Toolbar
    // Inflates toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.menu_toolbar_logout -> {
                authRepository.logout(this,this)
            }
        }
        return true
    }

    //endregion

    private fun setupRecyclerView() {

        // Listener for recycleview
        val listener = object: PatientAdapterListener
        {
            override fun onItemClick(position: Int) {
                // Open detail page when clicked
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


    inner class loginDoctorAsyncTask() : AsyncTask<Void, Void, DoctorLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DoctorLogin? {
            return authRepository.loginAsDoctor()
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
                return patientRepository.getPatientsFromDoctor(model.doctor.doctorId)
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
