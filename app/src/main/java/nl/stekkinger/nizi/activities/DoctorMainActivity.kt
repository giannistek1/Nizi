package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_add_patient.*
import kotlinx.android.synthetic.main.activity_doctor_main.*
import nl.stekkinger.nizi.adapters.PatientAdapter
import nl.stekkinger.nizi.adapters.PatientAdapterListener
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class DoctorMainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private var TAG = "DoctorMain"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    // for activity result
    private val REQUEST_CODE = 0

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()

    private lateinit var model: DoctorLogin
    private lateinit var patientListViewModel: PatientListViewModel
    var patientList = ArrayList<PatientItem>()
    private var doctorId: Int = 3

    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setup UI
        setContentView(R.layout.activity_doctor_main)
        progressBar = activity_doctor_main_loader

        activity_doctor_main_btn_addPatient.setOnClickListener {
            val intent: Intent = Intent(this@DoctorMainActivity, AddPatientActivity::class.java)
            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(EXTRA_DOCTOR_ID, model.doctor.doctorId)
            startActivityForResult(intent, REQUEST_CODE)
        }

        // Get viewmodel
        //patientListViewModel = ViewModelProviders.of(this).get(PatientListViewModel::class.java)

        // Login as doctor for doctor data
        loginDoctorAsyncTask().execute()

        /*patientListViewModel.setDoctorId(doctorId)
        patientListViewModel.loadPatients().observe(this, Observer { patientList ->
            Log.d("log", patientList.toString())
        })*/
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

    //region Doctor Login
    inner class loginDoctorAsyncTask() : AsyncTask<Void, Void, DoctorLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Loader
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DoctorLogin? {
            return authRepository.loginAsDoctor()
        }

        override fun onPostExecute(result: DoctorLogin) {
            super.onPostExecute(result)
            // Loader
            //progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                // Save doctor model
                model = result

                // update doctorId
                doctorId = model.doctor.doctorId
                // Start get patients from doc
                getPatientsFromDoctorAsyncTask().execute()

            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }
    //endregion

    inner class getPatientsFromDoctorAsyncTask() : AsyncTask<Void, Void, List<Patient>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Loader
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): List<Patient>? {
                return patientRepository.getPatientsFromDoctor(model.doctor.doctorId)
        }

        override fun onPostExecute(result: List<Patient>?) {
            super.onPostExecute(result)
            // Loader
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.get_patients_success, Toast.LENGTH_SHORT).show()

                // Clear
                patientList.clear()

                // Fill
                (0..result.count()-1).forEach {
                    val pi = PatientItem(it+1,
                        result[it].firstName + " " + result[it].lastName,
                        result[it].firstName,
                        result[it].lastName,
                        result[it].dateOfBirth,
                        result[it].weightInKilograms,
                        result[it].patientId,
                        result[it].doctorId)
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
