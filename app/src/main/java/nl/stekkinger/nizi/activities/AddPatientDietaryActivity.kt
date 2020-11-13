package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient_dietary.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryManagementModel
import nl.stekkinger.nizi.classes.PatientRegisterResponse
import nl.stekkinger.nizi.classes.AddPatientViewModel
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class AddPatientDietaryActivity : AppCompatActivity() {

    private var TAG = "AddPatientDietary"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var progressBar: View

    private var doctorId: Int? = null
    private var patientId: Int? = null
    private lateinit var patient: AddPatientViewModel

    private lateinit var dietaryList: ArrayList<DietaryManagementModel>
    private lateinit var textViewList: ArrayList<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient_dietary)

        // Add inputs to list1
        textViewList = arrayListOf(activity_add_patient_dietary_et_cal_min, activity_add_patient_dietary_et_cal_max,
            activity_add_patient_dietary_et_water_min, activity_add_patient_dietary_et_water_max,
            activity_add_patient_dietary_et_sodium_min, activity_add_patient_dietary_et_sodium_max,
            activity_add_patient_dietary_et_potassium_max, activity_add_patient_dietary_et_potassium_max,
            activity_add_patient_dietary_et_protein_min, activity_add_patient_dietary_et_protein_max,
            activity_add_patient_dietary_et_fiber_min, activity_add_patient_dietary_et_fiber_max
        )

        progressBar = activity_add_patient_dietary_loader

        activity_add_patient_dietary_btn_save.setOnClickListener {
            //registerPatientAsyncTask().execute()
        }

        // Fill in Patient
        patient = intent.extras?.get("PATIENT") as AddPatientViewModel

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)
    }

    //region RegisterPatient
    /*inner class registerPatientAsyncTask() : AsyncTask<Void, Void, PatientRegisterResponse>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): PatientRegisterResponse? {
            return patientRepository.registerPatient(patient.firstName, patient.lastName, patient.dateOfBirth, patient.weight, doctorId!!)
        }

        override fun onPostExecute(result: PatientRegisterResponse?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            // Feedback
            Toast.makeText(baseContext, R.string.patient_added, Toast.LENGTH_SHORT).show()

            //val restrictionsList = arrayOf(R.array.guideline_array)
            val restrictionsList = arrayOf("Caloriebeperking", "Calorieverrijking",
            "Vochtbeperking", "Vochtverrijking",
            "Natriumbeperking", "Natriumverrijking",
            "Kaliumbeperking", "Kaliumverrijking",
            "Eiwitbeperking", "Eiwitverrijking",
            "Vezelbeperking", "Vezelverrijking")

            dietaryList = arrayListOf()

            // Add dietaries by looping through each editText View
            restrictionsList.forEachIndexed { index, element ->
                if (textViewList[index].text.toString() != "") {
                    dietaryList.add(
                        DietaryManagementModel(
                            0,
                            restrictionsList[index],
                            textViewList[index].text.toString().toInt(),
                            true,
                            result!!.Patient.PatientId
                        )
                    )
                }
            }

            // Add the dietaries that were added (filled in)
            dietaryList.forEachIndexed { index, element ->
                addDietaryToPatientAsyncTask(element).execute()
            }
        }
    }*/
    //endregion

    //region AddDietaryToPatient
    inner class addDietaryToPatientAsyncTask(val dietary: DietaryManagementModel) : AsyncTask<Void, Void, Void>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            dietaryRepository.addDietary(dietary)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            // Feedback
            Toast.makeText(baseContext, R.string.dietary_added, Toast.LENGTH_SHORT).show()

            // Remove from list
            dietaryList.removeAt(0)
            if (dietaryList.isEmpty())
                finish()
        }
    }
    //endregion

    override fun finish() {
        // In case we wanna return something
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }
}
