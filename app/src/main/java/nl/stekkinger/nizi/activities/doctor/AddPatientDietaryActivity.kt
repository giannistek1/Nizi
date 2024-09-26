package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.dietary.DietaryRestriction
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.databinding.ActivityAddPatientDietaryBinding
import nl.stekkinger.nizi.databinding.ToolbarBinding
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class AddPatientDietaryActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPatientDietaryBinding
    private lateinit var toolbarBinding: ToolbarBinding

    private var TAG = "AddPatientDietary"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private var doctorId: Int? = null
    private lateinit var addPatientViewModel: AddPatientViewModel
    private lateinit var dietaryRestrictionList: ArrayList<DietaryRestriction>
    private lateinit var dietaryManagementList: ArrayList<DietaryManagementShort>
    private lateinit var textViewList: ArrayList<EditText>

    private var userId: Int? = null

    // Adding patient is 5 steps:
    // 1. GET DietaryRestrictions
    // 2. POST Patient
    // 3. POST DietaryManagements
    // 4. POST User
    // 5. PUT Patient with UserId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI.
        binding = ActivityAddPatientDietaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(toolbarBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBinding.toolbarTxtBack.text = getString(R.string.personal_info_short)
        loader = binding.activityAddPatientDietaryLoader

        // Setup custom toast
        val parent: RelativeLayout = binding.activityAddPatientDietaryRl
        parent.addView(customToastLayout)

        // Ideally dynamic inputs
        // Add inputs to list
        textViewList = arrayListOf(binding.activityAddPatientDietaryEtCalMin, binding.activityAddPatientDietaryEtCalMax,
            binding.activityAddPatientDietaryEtWaterMin, binding.activityAddPatientDietaryEtWaterMax,
            binding.activityAddPatientDietaryEtSodiumMin, binding.activityAddPatientDietaryEtSodiumMax,
            binding.activityAddPatientDietaryEtPotassiumMin, binding.activityAddPatientDietaryEtPotassiumMax,
            binding.activityAddPatientDietaryEtProteinMin, binding.activityAddPatientDietaryEtProteinMax,
            binding.activityAddPatientDietaryEtFiberMin, binding.activityAddPatientDietaryEtFiberMax
        )

        binding.activityAddPatientDietaryBtnSave.setOnClickListener {

            // (Guard) Check internet connection
            if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return@setOnClickListener

            // TODO: Check if anything is notEmpty first
            // if all empty, toast: dietaries_empty, return
            //checkValidInputs()

            if (registerPatientAsyncTask().status != AsyncTask.Status.RUNNING)
                registerPatientAsyncTask().execute()
        }

        // Get patient data
        addPatientViewModel = intent.extras?.get("PATIENT") as AddPatientViewModel

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)

        // Standard on canceled
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)

        // Testing
        binding.activityAddPatientDietaryEtWaterMin.setText("1969")
        binding.activityAddPatientDietaryEtWaterMax.setText("3696")

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return

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
            GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.fetched_dietary_restrictions))

            dietaryRestrictionList = result
        }
    }
    //endregion

    //region Step 2. registerPatient
    inner class registerPatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.registerPatient(addPatientViewModel.patient)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.patient_add_failed, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.patient_added))

            // Update patientId which you get after making patient (two places)
            addPatientViewModel.patient.id = result.id!!
            addPatientViewModel.user.patient = result.id

            dietaryManagementList = arrayListOf()

            var dietaryManagement: DietaryManagementShort
            var minimumText: String
            var maximumText: String

            // Loop through each restriction and check if filled in
            for (i in 0 until dietaryRestrictionList.count()) {

                val minIndex = i*2      // 0 2 4 6 8 10
                val maxIndex = i*2+1    // 1 3 5 7 9 11

                // Check empty, fill in with 0
                if (textViewList[minIndex].text.isBlank())
                    textViewList[minIndex].setText("0")
                if (textViewList[maxIndex].text.isBlank())
                    textViewList[maxIndex].setText("0")

                // Remove unnecessary 0's
                minimumText = textViewList[minIndex].text.toString().replaceFirst("^0+(?!$)", "")
                maximumText = textViewList[maxIndex].text.toString().replaceFirst("^0+(?!$)", "")

                if (minimumText == "0" && maximumText == "0") continue

                dietaryManagement = DietaryManagementShort(
                    dietary_restriction = i+1,
                    minimum = textViewList[minIndex].text.toString().toInt(),
                    maximum = textViewList[maxIndex].text.toString().toInt(),
                    is_active = true,
                    patient = addPatientViewModel.patient.id
                )

                dietaryManagementList.add(dietaryManagement)
            }

            // Add the dietaryManagements that were added (filled in)
            dietaryManagementList.forEachIndexed { _, dietaryManagementElement ->
                addDietaryToPatientAsyncTask(dietaryManagementElement).execute()
            }
        }
    }
    //endregion

    //region Step 3. AddDietaryToPatient
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
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.dietary_add_fail))
                return }

            // Feedback
            //GeneralHelper.makeToast(baseContext, customToastLayout, getString(R.string.dietary_added))

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

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): UserLogin? {
            return authRepository.registerUser(addPatientViewModel.user)
        }

        override fun onPostExecute(result: UserLogin?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.user_add_fail))
                return }

            // Feedback
            //GeneralHelper.makeToast(baseContext, customToastLayout, getString(R.string.user_added))

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

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.updatePatientUserId(addPatientViewModel.patient.id, addPatientViewModel.user.id)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.patient_update_fail))
                return }

            // Feedback
            //GeneralHelper.makeToast(baseContext, customToastLayout, getString(R.string.patient_updated))

            // In case we wanna return something
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }
    //endregion
}
