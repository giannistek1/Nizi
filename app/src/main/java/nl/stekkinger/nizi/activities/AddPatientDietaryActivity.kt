package nl.stekkinger.nizi.activities

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient_dietary.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.Patient
import nl.stekkinger.nizi.classes.PatientViewModel
import nl.stekkinger.nizi.repositories.PatientRepository

class AddPatientDietaryActivity : AppCompatActivity() {

    private var TAG = "AddPatientDietary"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val patientRepository: PatientRepository = PatientRepository()

    private lateinit var progressBar: View

    private var doctorId: Int? = null
    private lateinit var patient: PatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient_dietary)

        progressBar = activity_add_patient_dietary_loader

        activity_add_patient_dietary_btn_save.setOnClickListener {
            // Checks
            //if (activity_add_patient_et_firstName.text.toString() == "") {
            //    return@setOnClickListener
            //}

            registerPatientAsyncTask().execute()
            }

        // Fill in Patient
        patient = intent.extras?.get("PATIENT") as PatientViewModel

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)
    }

    inner class registerPatientAsyncTask() : AsyncTask<Void, Void, Void>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE

            //mFirstName = activity_add_patient_et_firstName.text.toString().trim()
            //mLastName = activity_add_patient_et_lastName.text.toString().trim()
            //mDateOfBirth = activity_add_patient_et_dob.text.toString().trim()
            //mWeight = activity_add_patient_et_weight.text.toString().toFloat()
        }

        override fun doInBackground(vararg p0: Void?): Void? {

            doctorId?.let {
                patientRepository.registerPatient(patient.firstName, patient.lastName, patient.dateOfBirth, patient.weight, it)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            // Feedback
            Toast.makeText(baseContext, R.string.patient_added, Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}
