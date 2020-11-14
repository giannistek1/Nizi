package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient_dietary.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.patient.UpdatePatientViewModel
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class EditPatientDietaryActivity : AppCompatActivity() {

    private var TAG = "EditPatientDietary"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var progressBar: View

    private var doctorId: Int? = null
    private lateinit var updatePatientViewMode: UpdatePatientViewModel
    private lateinit var addPatientViewModel: AddPatientViewModel

    private lateinit var dietaryList: ArrayList<DietaryManagement>
    private lateinit var textViewList: ArrayList<EditText>

    private var hasNoDietary = true

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

        updatePatientViewMode = intent.extras?.get("PATIENT") as UpdatePatientViewModel

            updatePatientViewMode.diets.forEachIndexed { _, element ->
                if (element.description.contains("Calorie")) {
                    activity_add_patient_dietary_et_cal_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_cal_max.setText(element.maximum.toString())
                } else if (element.description.contains("Vocht")) {
                    activity_add_patient_dietary_et_water_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_water_max.setText(element.maximum.toString())
                } else if (element.description.contains("Natrium")) {
                    activity_add_patient_dietary_et_sodium_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_sodium_max.setText(element.maximum.toString())
                } else if (element.description.contains("Kalium")) {
                    activity_add_patient_dietary_et_potassium_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_potassium_max.setText(element.maximum.toString())
                } else if (element.description.contains("Eiwit")) {
                    activity_add_patient_dietary_et_protein_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_protein_max.setText(element.maximum.toString())
                } else if (element.description.contains("Vezel")) {
                    activity_add_patient_dietary_et_protein_min.setText(element.minimum.toString())
                    activity_add_patient_dietary_et_protein_max.setText(element.maximum.toString())
                }
            }




        progressBar = activity_add_patient_dietary_loader

        activity_add_patient_dietary_btn_save.setOnClickListener {
                updatePatientAsyncTask().execute()
        }

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)
    }

    //region RegisterPatient
    inner class updatePatientAsyncTask() : AsyncTask<Void, Void, Void>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            patientRepository.updatePatient(updatePatientViewMode.patient.patientId, doctorId, updatePatientViewMode.patient.firstName, updatePatientViewMode.patient.lastName, updatePatientViewMode.patient.dateOfBirth)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            // Feedback
            Toast.makeText(baseContext, R.string.patient_edited, Toast.LENGTH_SHORT).show()

            //val restrictionsList = arrayOf(R.array.guideline_array)
            val restrictionsList = arrayOf("Caloriebeperking", "Calorieverrijking",
            "Vochtbeperking", "Vochtverrijking",
            "Natriumbeperking", "Natriumverrijking",
            "Kaliumbeperking", "Kaliumverrijking",
            "Eiwitbeperking", "Eiwitverrijking",
            "Vezelbeperking", "Vezelverrijking")

            dietaryList = arrayListOf()

            // Add dietaries by looping through each editText View
            restrictionsList.forEachIndexed { index, _ ->
                if (textViewList[index].text.toString() != "") {
                    dietaryList.add(
                        DietaryManagement(
                            dietary_restriction = index+1,
                            amount = textViewList[index].text.toString().toInt(),
                            is_active =  true,
                            patient = updatePatientViewMode.patient.id
                        )
                    )
                }
            }

            // Add the dietaries that were added (filled in)
            if (updatePatientViewMode.diets.count() == 0) {
                dietaryList.forEachIndexed { _, element ->
                    addDietaryToPatientAsyncTask(element).execute()
                }
            }
            else
            {
                dietaryList.forEachIndexed { _, element ->
                    updateDietaryAsyncTask(element.id!!).execute()
                }
            }
        }
    }
    //endregion

    //region AddDietaryToPatient
    inner class addDietaryToPatientAsyncTask(val dietary: DietaryManagement) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            dietaryRepository.addDietary(dietary)
            return null
        }

        override fun onPostExecute(result: DietaryManagement?) {
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

    //region AddDietaryToPatient
    inner class updateDietaryAsyncTask(val dietaryManagementId: Int) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return dietaryRepository.updateDietary(dietaryManagementId)
        }

        override fun onPostExecute(result: DietaryManagement?) {
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
