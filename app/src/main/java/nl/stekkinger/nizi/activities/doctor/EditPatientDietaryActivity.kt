package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
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
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.databinding.ActivityAddPatientDietaryBinding
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository

class EditPatientDietaryActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPatientDietaryBinding

    private var TAG = "EditPatientDietary"

    private val authRepository: AuthRepository = AuthRepository()
    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private var doctorId: Int? = null
    private lateinit var patientData: PatientData                                   // User, Patient, Doctor, Current DietaryManagements
    private lateinit var originalEmail: String                                      // Original email
    private lateinit var weightUnitHolder: WeightUnitHolder                         // WeightUnits
    private lateinit var dietaryRestrictionList: ArrayList<DietaryRestriction>      // Dietary Restrictions
    private lateinit var currentDietaryList: ArrayList<DietaryManagementShort>      // All Current DietaryManagements
    private lateinit var newDietaryList: ArrayList<DietaryManagementShort>          // All New DietaryManagements
    private lateinit var checkList: ArrayList<DietaryManagementShort>               // Checklist to go off (could have also been a number until all dietaries were checked)

    private lateinit var textViewList: ArrayList<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI same as AddPatientDietary
        binding = ActivityAddPatientDietaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.personal_info_short)
        loader = binding.activityAddPatientDietaryLoader

        // Setup custom toast
        val parent: RelativeLayout = binding.activityAddPatientDietaryRl
//        parent.addView(customToastLayout)

        // Add inputs to list
        textViewList = arrayListOf(binding.activityAddPatientDietaryEtCalMin, binding.activityAddPatientDietaryEtCalMax,
            binding.activityAddPatientDietaryEtWaterMin, binding.activityAddPatientDietaryEtWaterMax,
            binding.activityAddPatientDietaryEtSodiumMin, binding.activityAddPatientDietaryEtSodiumMax,
            binding.activityAddPatientDietaryEtPotassiumMin, binding.activityAddPatientDietaryEtPotassiumMax,
            binding.activityAddPatientDietaryEtProteinMin, binding.activityAddPatientDietaryEtProteinMax,
            binding.activityAddPatientDietaryEtFiberMin, binding.activityAddPatientDietaryEtFiberMax
        )

        // Get patient data
        patientData = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientData
        originalEmail = intent.extras!!.getString(GeneralHelper.EXTRA_ORIGINAL_EMAIL, "")

        // Get WeightUnits
        weightUnitHolder = GeneralHelper.getWeightUnitHolder()!!

        patientData.diets.forEachIndexed { _, diet ->
            if (diet.description.contains("Calorie")) {
                binding.activityAddPatientDietaryEtCalMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtCalMax.setText(diet.maximum.toString())
            } else if (diet.description.contains("Vocht")) {
                binding.activityAddPatientDietaryEtWaterMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtWaterMax.setText(diet.maximum.toString())
            } else if (diet.description.contains("Natrium")) {
                binding.activityAddPatientDietaryEtSodiumMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtSodiumMax.setText(diet.maximum.toString())
            } else if (diet.description.contains("Kalium")) {
                binding.activityAddPatientDietaryEtPotassiumMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtPotassiumMax.setText(diet.maximum.toString())
            } else if (diet.description.contains("Eiwit")) {
                binding.activityAddPatientDietaryEtProteinMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtProteinMax.setText(diet.maximum.toString())
            } else if (diet.description.contains("Vezel")) {
                binding.activityAddPatientDietaryEtFiberMin.setText(diet.minimum.toString())
                binding.activityAddPatientDietaryEtFiberMax.setText(diet.maximum.toString())
            }
        }

        binding.activityAddPatientDietaryBtnSave.setOnClickListener {

            // (Guard) Check internet connection
            if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return@setOnClickListener

            if (updatePatientAsyncTask().status != AsyncTask.Status.RUNNING)
                updatePatientAsyncTask().execute()
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Standard on canceled and give values back
        val returnIntent = Intent()
        returnIntent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
        returnIntent.putExtra(GeneralHelper.EXTRA_ORIGINAL_EMAIL, originalEmail)
        setResult(Activity.RESULT_CANCELED, returnIntent)

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return

        // Get DietaryRestrictions
        getDietaryRestrictionsAsyncTask().execute()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
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
            //Toast.makeText(baseContext, R.string.fetched_dietary_restrictions, Toast.LENGTH_SHORT).show()

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
            val toast: Toast = Toast.makeText(baseContext, "", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.patient_edited))
            //customToastLayout.toast_text.text = getString(R.string.patient_edited)
//            toast.view = customToastLayout
            toast.show()

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
            //Toast.makeText(baseContext, R.string.user_edited, Toast.LENGTH_SHORT).show()

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
