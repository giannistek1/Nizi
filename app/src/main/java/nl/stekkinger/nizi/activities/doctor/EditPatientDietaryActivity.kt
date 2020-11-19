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
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class EditPatientDietaryActivity : AppCompatActivity() {

    private var TAG = "EditPatientDietary"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var loader: View

    private var doctorId: Int? = null
    private lateinit var patientData: PatientData

    private lateinit var newDietaryList: ArrayList<DietaryManagementShort>
    private lateinit var textViewList: ArrayList<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient_dietary)

        // Add inputs to list
        textViewList = arrayListOf(activity_add_patient_dietary_et_cal_min, activity_add_patient_dietary_et_cal_max,
            activity_add_patient_dietary_et_water_min, activity_add_patient_dietary_et_water_max,
            activity_add_patient_dietary_et_sodium_min, activity_add_patient_dietary_et_sodium_max,
            activity_add_patient_dietary_et_potassium_max, activity_add_patient_dietary_et_potassium_max,
            activity_add_patient_dietary_et_protein_min, activity_add_patient_dietary_et_protein_max,
            activity_add_patient_dietary_et_fiber_min, activity_add_patient_dietary_et_fiber_max
        )

        patientData = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientData

        patientData.diets.forEachIndexed { _, diet ->
            if (diet.description.contains("Calorie")) {
                activity_add_patient_dietary_et_cal_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_cal_max.setText(diet.maximum.toString())
            } else if (diet.description.contains("Vocht")) {
                activity_add_patient_dietary_et_water_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_water_max.setText(diet.maximum.toString())
            } else if (diet.description.contains("Natrium")) {
                activity_add_patient_dietary_et_sodium_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_sodium_max.setText(diet.maximum.toString())
            } else if (diet.description.contains("Kalium")) {
                activity_add_patient_dietary_et_potassium_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_potassium_max.setText(diet.maximum.toString())
            } else if (diet.description.contains("Eiwit")) {
                activity_add_patient_dietary_et_protein_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_protein_max.setText(diet.maximum.toString())
            } else if (diet.description.contains("Vezel")) {
                activity_add_patient_dietary_et_protein_min.setText(diet.minimum.toString())
                activity_add_patient_dietary_et_protein_max.setText(diet.maximum.toString())
            }
        }

        loader = activity_add_patient_dietary_loader

        activity_add_patient_dietary_btn_save.setOnClickListener {
                updatePatientAsyncTask().execute()
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)
    }

    //region step 1. updatePatient
    inner class updatePatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.updatePatient(patientData.patient)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.patient_edit_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.patient_edited, Toast.LENGTH_SHORT).show()

            updateUserAsyncTask().execute()
        }
    }
    //endregion

    //region step 2. updateUser
    inner class updateUserAsyncTask() : AsyncTask<Void, Void, UserLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): UserLogin? {
            return authRepository.updateUser(patientData.user)
        }

        override fun onPostExecute(result: UserLogin?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Feedback
            Toast.makeText(baseContext, R.string.patient_edited, Toast.LENGTH_SHORT).show()

            //val restrictionsList = arrayOf(R.array.guideline_array)
            val restrictionsList = arrayOf("Caloriebeperking", "Calorieverrijking",
                "Vochtbeperking", "Vochtverrijking",
                "Natriumbeperking", "Natriumverrijking",
                "Kaliumbeperking", "Kaliumverrijking",
                "Eiwitbeperking", "Eiwitverrijking",
                "Vezelbeperking", "Vezelverrijking")

            newDietaryList = arrayListOf()

            // Add dietaries by looping through each editText View
            restrictionsList.forEachIndexed { index, _ ->
                if (textViewList[index].text.toString() != "") {
                    newDietaryList.add(
                        DietaryManagementShort(
                            dietary_restriction = index+1,
                            amount = textViewList[index].text.toString().toInt(),
                            is_active = true,
                            patient = patientData.patient.id
                        )
                    )
                }
            }



            // Add the dietaries that were added (filled in)
            if (patientData.diets.count() == 0) {
                newDietaryList.forEachIndexed { _, dietary ->
                    addDietaryToPatientAsyncTask(dietary).execute()
                }
            }
            else
            {
                newDietaryList.forEachIndexed { _, dietary ->
                    updateDietaryAsyncTask(dietary).execute()
                }
            }
        }
    }
    //endregion

    //region step 3. updateDietaryToPatient
    inner class updateDietaryAsyncTask(val dietaryManagement: DietaryManagementShort) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return dietaryRepository.updateDietary(dietaryManagement)
        }

        override fun onPostExecute(result: DietaryManagement?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.dietary_change_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.dietary_changed, Toast.LENGTH_SHORT).show()

            // Remove from list
            newDietaryList.removeAt(0)
            if (newDietaryList.isEmpty())
                finish()
        }
    }
    //endregion

    //region step 3.5 addDietaryToPatient
    inner class addDietaryToPatientAsyncTask(val dietary: DietaryManagementShort) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return dietaryRepository.addDietary(dietary)
        }

        override fun onPostExecute(result: DietaryManagement?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.dietary_add_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.dietary_added, Toast.LENGTH_SHORT).show()

            // Remove from list
            newDietaryList.removeAt(0)
            if (newDietaryList.isEmpty())
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
