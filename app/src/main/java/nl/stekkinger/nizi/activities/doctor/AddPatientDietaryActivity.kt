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
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.AddPatientRequest
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class AddPatientDietaryActivity : AppCompatActivity() {

    private var TAG = "AddPatientDietary"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private var doctorId: Int? = null
    private lateinit var addPatientViewModel: AddPatientViewModel

    private lateinit var dietaryRestrictionList: ArrayList<DietaryRestriction>
    private lateinit var dietaryManagementList: ArrayList<DietaryManagement>
    private lateinit var textViewList: ArrayList<EditText>
    private lateinit var loader: View

    private var userId: Int? = null

    // Adding patient is 5 steps:
    // 1. GET DietaryRestrictions
    // 2. POST Patient
    // 3. POST DietaryManagements
    // 4. POST User
    // 5. PUT Patient with UserId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient_dietary)

        // Ideally dynamic inputs
        // Add inputs to list
        textViewList = arrayListOf(activity_add_patient_dietary_et_cal_min, activity_add_patient_dietary_et_cal_max,
            activity_add_patient_dietary_et_water_min, activity_add_patient_dietary_et_water_max,
            activity_add_patient_dietary_et_sodium_min, activity_add_patient_dietary_et_sodium_max,
            activity_add_patient_dietary_et_potassium_max, activity_add_patient_dietary_et_potassium_max,
            activity_add_patient_dietary_et_protein_min, activity_add_patient_dietary_et_protein_max,
            activity_add_patient_dietary_et_fiber_min, activity_add_patient_dietary_et_fiber_max
        )

        loader = activity_add_patient_dietary_loader

        activity_add_patient_dietary_btn_save.setOnClickListener {

            // (Guard) Check connection
            if (!GeneralHelper.hasInternetConnection(this)) return@setOnClickListener

            // TODO: Check if anything is notEmpty first
            // if all empty, toast: dietaries_empty, return

            registerPatientAsyncTask().execute()
        }

        // Get patient data
        addPatientViewModel = intent.extras?.get("PATIENT") as AddPatientViewModel

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)

        // Testing
        activity_add_patient_dietary_et_water_min.setText("6969")
        activity_add_patient_dietary_et_water_max.setText("9696")

        // Check connection
        if (!GeneralHelper.hasInternetConnection(this)) return

        // Get DietaryRestrictions
        getDietaryRestrictionsAsyncTask().execute()
    }

    //region Step 1. getDietaryRestrictions
    inner class getDietaryRestrictionsAsyncTask() : AsyncTask<Void, Void, ArrayList<DietaryRestriction>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<DietaryRestriction>? {
            return dietaryRepository.getDietaries()
        }

        override fun onPostExecute(result: ArrayList<DietaryRestriction>?) {
            super.onPostExecute(result)
            // Progressbar
            loader.visibility = View.GONE

            // Guard
            if (result == null) {  Toast.makeText(baseContext, R.string.fetch_dietary_restrictions_fail,
                Toast.LENGTH_SHORT).show(); return }

            // Feedback
            Toast.makeText(baseContext, R.string.fetched_dietary_restrictions, Toast.LENGTH_SHORT).show()

            dietaryRestrictionList = result
        }
    }
    //endregion

    //region Step 2. registerPatient
    inner class registerPatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.registerPatient(addPatientViewModel.patient)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)
            // Progressbar
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.patient_add_failed, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.patient_added, Toast.LENGTH_SHORT).show()

            // Update patientId which you get after making patient
            addPatientViewModel.patient.id = result.id!!
            addPatientViewModel.user.patient = result.id!!


            //val restrictionsList = arrayOf(R.array.guideline_array)
            val restrictionsList = arrayOf("Caloriebeperking", "Calorieverrijking",
            "Vochtbeperking", "Vochtverrijking",
            "Natriumbeperking", "Natriumverrijking",
            "Kaliumbeperking", "Kaliumverrijking",
            "Eiwitbeperking", "Eiwitverrijking",
            "Vezelbeperking", "Vezelverrijking")

            dietaryManagementList = arrayListOf()

            // Add dietaries by looping through each editText View
            restrictionsList.forEachIndexed { index, element ->
                if (textViewList[index].text.isNotEmpty()) {
                    dietaryManagementList.add(
                        DietaryManagement(
                            dietary_restriction = index+1,
                            amount = textViewList[index].text.toString().toInt(),
                            is_active = true,
                            patient = addPatientViewModel.patient.id
                        )
                    )
                }
            }

            // Add the dietaryManagements that were added (filled in)
            dietaryManagementList.forEachIndexed { _, dietaryManagement ->
                addDietaryToPatientAsyncTask(dietaryManagement).execute()
            }
            // Register user
            registerUserAsyncTask().execute()
        }
    }
    //endregion

    //region Step 3. AddDietaryToPatient
    inner class addDietaryToPatientAsyncTask(val dietary: DietaryManagement) : AsyncTask<Void, Void, DietaryManagement>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryManagement? {
            return dietaryRepository.addDietary(dietary)
        }

        override fun onPostExecute(result: DietaryManagement?) {
            super.onPostExecute(result)
            // Progressbar
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.dietary_add_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.dietary_added, Toast.LENGTH_SHORT).show()

            // Remove from list
            dietaryManagementList.removeAt(0)
            if (dietaryManagementList.isEmpty())
                registerUserAsyncTask().execute()
        }
    }
    //endregion

    //region Step 4. registerUser
    inner class registerUserAsyncTask() : AsyncTask<Void, Void, UserLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): UserLogin? {
            return authRepository.registerUser(addPatientViewModel.user)
        }

        override fun onPostExecute(result: UserLogin?) {
            super.onPostExecute(result)
            // Progressbar
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.user_add_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.user_added, Toast.LENGTH_SHORT).show()

            // Save userId for the patient that needs to be updated
            userId = result.id

            addPatientUserIdAsyncTask().execute()
        }
    }
    //endregion

    //region Step 5. updatePatientUserId
    inner class addPatientUserIdAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.updatePatientUserId(addPatientViewModel.patient.id, addPatientViewModel.user.id)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)
            // Progressbar
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.patient_update_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.patient_updated, Toast.LENGTH_SHORT).show()

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
