package nl.stekkinger.nizi.activities

import android.content.Intent
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
import nl.stekkinger.nizi.classes.PatientViewModel
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

        activity_add_patient_btn_to_guidelines.setOnClickListener {
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
            val intent = Intent(this@AddPatientActivity, AddPatientDietaryActivity::class.java)
            // Give patientdata with intent
            val newPatient = PatientViewModel(
                activity_add_patient_et_firstName.text.toString().trim(),
                activity_add_patient_et_lastName.text.toString().trim(),
                activity_add_patient_et_dob.text.toString().trim(),
                activity_add_patient_et_weight.text.toString().toFloat(),
                "Man"
            )

            intent.putExtra("PATIENT", newPatient)
            intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
            // Start intent
            startActivity(intent)
            //registerPatientAsyncTask().execute()
            }

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)
    }
}
