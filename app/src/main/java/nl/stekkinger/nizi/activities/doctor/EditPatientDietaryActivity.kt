package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_patient_dietary.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import java.lang.Exception

class EditPatientDietaryActivity : BaseActivity() {

    private var TAG = "EditPatientDietary"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var loader: View

    private var doctorId: Int? = null
    private lateinit var patientData: PatientData                                   // User, Patient, Doctor, Current DietaryManagements
    private lateinit var weightUnitHolder: WeightUnitHolder                         // WeightUnits
    private lateinit var dietaryRestrictionList: ArrayList<DietaryRestriction>      // Dietary Restrictions
    private lateinit var currentDietaryList: ArrayList<DietaryManagementShort>      // All Current DietaryManagements
    private lateinit var newDietaryList: ArrayList<DietaryManagementShort>          // All New DietaryManagements
    private lateinit var checkList: ArrayList<DietaryManagementShort>               // Checklist to go off

    private lateinit var textViewList: ArrayList<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient_dietary)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_txt_back.text = getString(R.string.personal_info_short)

        // Add inputs to list
        textViewList = arrayListOf(activity_add_patient_dietary_et_cal_min, activity_add_patient_dietary_et_cal_max,
            activity_add_patient_dietary_et_water_min, activity_add_patient_dietary_et_water_max,
            activity_add_patient_dietary_et_sodium_min, activity_add_patient_dietary_et_sodium_max,
            activity_add_patient_dietary_et_potassium_min, activity_add_patient_dietary_et_potassium_max,
            activity_add_patient_dietary_et_protein_min, activity_add_patient_dietary_et_protein_max,
            activity_add_patient_dietary_et_fiber_min, activity_add_patient_dietary_et_fiber_max
        )

        // Get patient data
        patientData = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientData

        // Get WeightUnits
        val gson = Gson()
        val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
        weightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)

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

            // (Guard) Check internet connection
            if (!GeneralHelper.hasInternetConnection(this)) return@setOnClickListener

            if (updatePatientAsyncTask().status != AsyncTask.Status.RUNNING)
                updatePatientAsyncTask().execute()
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Standard on canceled
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)

        // Get DietaryRestrictions
        getDietaryRestrictionsAsyncTask().execute()
    }

    //region Step 1. getDietaryRestrictions
    inner class getDietaryRestrictionsAsyncTask() : AsyncTask<Void, Void, ArrayList<DietaryRestriction>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<DietaryRestriction>? {
            return try {
                dietaryRepository.getDietaryRestrictions()
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<DietaryRestriction>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) {  Toast.makeText(baseContext, R.string.fetch_dietary_restrictions_fail,
                Toast.LENGTH_SHORT).show(); return }

            // Feedback
            Toast.makeText(baseContext, R.string.fetched_dietary_restrictions, Toast.LENGTH_SHORT).show()

            // Save restrictions
            dietaryRestrictionList = result

            currentDietaryList = arrayListOf()

            // Save currentDietaryManagementList
            dietaryRestrictionList.forEachIndexed { i, restriction ->
                val minIndex = i*2      // 0 2 4 6 8 10
                val maxIndex = i*2+1    // 1 3 5 7 9 11
                val dietaryRestriction = dietaryRestrictionList.find { it.id == restriction.id }

                // Check empty, fill in with 0
                if (textViewList[minIndex].text.isBlank())
                    textViewList[minIndex].setText("0")
                if (textViewList[maxIndex].text.isBlank())
                    textViewList[maxIndex].setText("0")

                // Remove unnecessary 0's
                textViewList[minIndex].setText(textViewList[minIndex].text.toString()
                    .replaceFirst("^0+(?!$)", ""))
                textViewList[maxIndex].setText(textViewList[maxIndex].text.toString()
                    .replaceFirst("^0+(?!$)", ""))

                val dietaryManagementShort = DietaryManagementShort(
                    dietary_restriction = dietaryRestriction!!.id,
                    is_active = true, // Since there is no way to deactivate it anyways
                    minimum = textViewList[minIndex].text.toString().toInt(),
                    maximum = textViewList[maxIndex].text.toString().toInt(),
                    patient = patientData.patient.id
                )
                dietaryManagementShort.id = patientData.patient.dietary_managements?.find { it.dietary_restriction == dietaryManagementShort.dietary_restriction}?.id
                currentDietaryList.add(dietaryManagementShort)
            }
        }
    }
    //endregion

    //region step 2. updatePatient
    inner class updatePatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return try {
                patientRepository.updatePatient(patientData.patient)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(baseContext, R.string.patient_edit_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.patient_edited, Toast.LENGTH_SHORT).show()

            updateUserAsyncTask().execute()
        }
    }
    //endregion

    //region step 3. updateUser
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

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.user_edit_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.user_edited, Toast.LENGTH_SHORT).show()

            newDietaryList = arrayListOf()

            // Save newDietaryManagementList
            dietaryRestrictionList.forEachIndexed { i, restriction ->
                val minIndex = i*2      // 0 2 4 6 8 10
                val maxIndex = i*2+1    // 1 3 5 7 9 11
                val dietaryRestriction = dietaryRestrictionList.find { it.id == restriction.id }

                // Check empty, fill in with 0
                if (textViewList[minIndex].text.isBlank())
                    textViewList[minIndex].setText("0")
                if (textViewList[maxIndex].text.isBlank())
                    textViewList[maxIndex].setText("0")

                // Remove unnecessary 0's
                textViewList[minIndex].setText(textViewList[minIndex].text.toString()
                    .replaceFirst("^0+(?!$)", ""))
                textViewList[maxIndex].setText(textViewList[maxIndex].text.toString()
                    .replaceFirst("^0+(?!$)", ""))

                val dietaryManagementShort = DietaryManagementShort(
                    dietary_restriction = dietaryRestriction!!.id,
                    is_active = true, // Since there is no way to deactivate it anyways
                    minimum = textViewList[minIndex].text.toString().toInt(),
                    maximum = textViewList[maxIndex].text.toString().toInt(),
                    patient = patientData.patient.id
                )

                dietaryManagementShort.id = patientData.patient.dietary_managements?.find { it.dietary_restriction == dietaryManagementShort.dietary_restriction}?.id
                newDietaryList.add(dietaryManagementShort)
            }

            checkList = arrayListOf()
            checkList.addAll(newDietaryList)

            newDietaryList.forEachIndexed { i, _ ->
                // If values not changed -> NEXT ITEM
                if (newDietaryList[i].minimum == currentDietaryList[i].minimum
                    && newDietaryList[i].maximum == currentDietaryList[i].maximum) {

                    checkList.removeAt(0)
                    return@forEachIndexed // Continue
                }

                // Values changed
                // Check if empty now -> REMOVE
                if (newDietaryList[i].minimum == 0 && newDietaryList[i].maximum == 0)
                    deleteDietaryAsyncTask(newDietaryList[i].id!!).execute()

                // If not added yet (both are 0) -> ADD
                else if (currentDietaryList[i].minimum == 0 && currentDietaryList[i].maximum == 0)
                    addDietaryToPatientAsyncTask(newDietaryList[i]).execute()

                else // Values are just different --> UPDATE
                    updateDietaryAsyncTask(newDietaryList[i]).execute()
            }

            // Finish if nothing is changed
            if (checkList.isEmpty()) {
                // In case we wanna return something
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }
    //endregion

    //region step 3.1 deleteDietaryAsyncTask
    inner class deleteDietaryAsyncTask(val id: Int) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return dietaryRepository.deleteDietary(id)
        }

        override fun onPostExecute(result: DietaryManagement?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.dietary_delete_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.dietary_deleted, Toast.LENGTH_SHORT).show()

            // Remove from list
            checkList.removeAt(0)

            if (checkList.isEmpty()) {
                // In case we wanna return something
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }
    //endregion

    //region step 3.2 updateDietaryToPatient
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
            checkList.removeAt(0)

            if (checkList.isEmpty()) {
                // In case we wanna return something
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }
    //endregion

    //region step 3.3 addDietaryToPatient
    inner class addDietaryToPatientAsyncTask(val dietary: DietaryManagementShort) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return try {
                dietaryRepository.addDietary(dietary)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
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
            checkList.removeAt(0)

            if (checkList.isEmpty()) {
                // In case we wanna return something
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }
    //endregion
}
