package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_doctor_main.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.PatientAdapter
import nl.stekkinger.nizi.adapters.PatientAdapterListener
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import java.lang.Exception


class DoctorMainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private var TAG = "DoctorMain"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    // for activity result
    private val REQUEST_CODE = 0

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    //endregion

    private lateinit var user: UserLogin

    var patientList = ArrayList<PatientItem>()
    private var doctorId: Int = 1

    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setup UI
        setContentView(R.layout.activity_doctor_main)
        progressBar = activity_doctor_main_loader

        // Get User and doctorId
        user = GeneralHelper.getUser()
        doctorId = GeneralHelper.getDoctorId()

        // Add patient button
        activity_doctor_main_btn_addPatient.setOnClickListener {
            val intent = Intent(this@DoctorMainActivity, AddPatientActivity::class.java)
            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
            startActivityForResult(intent, REQUEST_CODE)
        }

        // Check connection
        if (!GeneralHelper.hasInternetConnection(this)) return

        // Get patients
        getPatientsForDoctorAsyncTask().execute()
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

    //region Spinner?
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
         parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }
    //endregion

    private fun setupRecyclerView() {

        // Listener for recycleview
        val listener = object: PatientAdapterListener
        {
            override fun onItemClick(position: Int) {
                // Open detail page when clicked
                val intent = Intent(this@DoctorMainActivity, PatientDetailActivity::class.java)

                intent.putExtra("PATIENT", patientList[position])
                intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
                startActivity(intent)
            }
        }

        // Create adapter (data)
        activity_doctor_main_rv.adapter = PatientAdapter(this, patientList, listener)

        // Create Linear Layout Manager
        activity_doctor_main_rv.layoutManager = LinearLayoutManager(this)
    }

    inner class getPatientsForDoctorAsyncTask() : AsyncTask<Void, Void, ArrayList<Patient>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Loader
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Patient>? {
            try {
                return patientRepository.getPatientsForDoctor(doctorId)
            }  catch(e: Exception) {
                // Failed to connect to API
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<Patient>?) {
            super.onPostExecute(result)
            // Loader
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.get_patients_success, Toast.LENGTH_SHORT).show()

                // Clear
                patientList.clear()

                // Fill
                (0 until result.count()).forEach {
                    if (result[it].user == null) return@forEach

                    val pi = PatientItem(
                        it + 1,
                        result[it].user!!.first_name + " " + result[it].user!!.last_name,
                        result[it].user!!.first_name,
                        result[it].user!!.last_name,
                        result[it].date_of_birth,
                        //result[it].weightInKilograms,
                        result[it].id!!,
                        result[it].doctor.id!!
                    )
                    patientList.add(pi)
                }

                setupRecyclerView()
            } else {
                Toast.makeText(baseContext, R.string.get_patients_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // refresh activity (recyclerview)
            recreate()
        }
    }
}
