package nl.stekkinger.nizi.activities

import android.os.AsyncTask
import android.os.AsyncTask.execute
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DoctorLogin
import nl.stekkinger.nizi.classes.Patient
import nl.stekkinger.nizi.classes.PatientItem
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class AddPatientActivity : AppCompatActivity() {

    private var TAG = "AddPatient"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val patientRepository: PatientRepository = PatientRepository()

    private lateinit var progressBar: View

    private lateinit var mFirstName: String
    private lateinit var mLastName: String
    private lateinit var mDateOfBirth: String


    private var mWeight: Float = 0f
    private var doctorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        progressBar = activity_add_patient_progressbar

        activity_add_patient_btn_save.setOnClickListener {
            // Checks
            if (activity_add_patient_et_firstName.text.toString() == "") {
                return@setOnClickListener
            }

            if (activity_add_patient_et_lastName.text.toString() == "") {
                return@setOnClickListener
            }

            if (activity_add_patient_et_dob.text.toString() == "") {
                return@setOnClickListener
            }

            if (activity_add_patient_et_weight.text.toString() == "") {
                return@setOnClickListener
            }

            registerPatientAsyncTask().execute()  }

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)

        // Spinner array adapter
        ArrayAdapter.createFromResource(
            this,
            R.array.guideline_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Spinner adapter
            activity_add_patient_spr_guidelines.adapter = adapter
        }
    }


    inner class registerPatientAsyncTask() : AsyncTask<Void, Void, Void>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE

            mFirstName = activity_add_patient_et_firstName.text.toString().trim()
            mLastName = activity_add_patient_et_lastName.text.toString().trim()
            mDateOfBirth = activity_add_patient_et_dob.text.toString().trim()
            mWeight = activity_add_patient_et_weight.text.toString().toFloat()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            doctorId?.let {
                patientRepository.registerPatient(mFirstName, mLastName, mDateOfBirth, mWeight, it)
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
