package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_patient_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.fragments.doctor.PatientDiaryFragment
import nl.stekkinger.nizi.fragments.doctor.PatientFeedbackFragment
import nl.stekkinger.nizi.fragments.doctor.PatientHomeFragment
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import nl.stekkinger.nizi.repositories.WeightUnitRepository


class PatientDetailActivity : BaseActivity() {

    private val patientRepository: PatientRepository = PatientRepository()
    private val weightUnitRepository: WeightUnitRepository = WeightUnitRepository()

    // For activity result
    private val EDIT_PATIENT_REQUEST_CODE = 0

    private lateinit var weightUnits: ArrayList<WeightUnit>             // WeightUnits
    private lateinit var patientData: PatientData                       // User, Patient, Doctor, Dietary

    private var savedInstanceState: Bundle? = null

    private lateinit var loader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI
        setContentView(R.layout.activity_patient_detail)
        setSupportActionBar(toolbar)
        // Back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_txt_back.text = getString(R.string.patient_overview)
        loader = activity_patient_detail_loader

        // Checks if fragment state is null and save it
        if (savedInstanceState != null)
            this.savedInstanceState = savedInstanceState

        val patientItem = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientItem

        patientData = PatientData(
            patient = PatientShort(id = patientItem.patient_id, date_of_birth = patientItem.date_of_birth,
                gender = patientItem.gender, doctor = patientItem.doctor_id),

            user = User(first_name = patientItem.first_name, last_name = patientItem.last_name,
                username = patientItem.username, email = patientItem.email, role = patientItem.role),

            diets = arrayListOf()
        )

        activity_patient_detail_bottom_navigation.setOnNavigationItemSelectedListener(navListener)

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this)) return

        getWeightUnitsAsyncTask().execute()
    }

    //region Bottom Nav
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val fragment = PatientHomeFragment()

                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
                fragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(activity_patient_detail_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_diary -> {
                val fragment = PatientDiaryFragment()
                supportFragmentManager.beginTransaction().replace(activity_patient_detail_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_conversation -> {
                val fragment = PatientFeedbackFragment()

                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
                fragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(activity_patient_detail_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    //endregion

    // TODO: Double in main
    //region Get WeightUnits
    inner class getWeightUnitsAsyncTask() : AsyncTask<Void, Void, ArrayList<WeightUnit>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<WeightUnit>? {
            return try {
                weightUnitRepository.getWeightUnits()
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<WeightUnit>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(baseContext, R.string.get_weight_unit_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(baseContext, R.string.fetched_weight_units, Toast.LENGTH_SHORT).show()

            // Save weightUnits
            val gson = Gson()
            val weightUnitHolder = WeightUnitHolder()
            weightUnitHolder.weightUnits = result
            val json = gson.toJson(weightUnitHolder)
            GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_WEIGHT_UNIT, json).apply()

            weightUnits = result

            getPatientAsyncTask().execute()
        }
    }
    //endregion

    //region getPatient
    inner class getPatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.getPatient(patientData.patient.id)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.get_patient_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(baseContext, R.string.fetched_patient, Toast.LENGTH_SHORT).show()

            // Make PatientData because patient does not have descriptions of dietaryRestrictions
            patientData.user = result.user!!
            patientData.patient = PatientShort(
                id = result.id!!,
                gender = result.gender,
                date_of_birth = result.date_of_birth,
                doctor = result.doctor.id!!,
                user = result.user.id,
                feedbacks = result.feedbacks,
                dietary_managements = arrayListOf(), // Empty because we use a list of DietaryViews for everything
                my_foods = result.my_foods,
                consumptions = result.consumptions
            )


            // Checks if fragment state is null, then start with homeFragment
            if (savedInstanceState == null) {

                val fragment = PatientHomeFragment()
                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(
                    activity_patient_detail_fragment_container.id,
                    fragment,
                    fragment.javaClass.simpleName)
                    .commit()
            }
        }
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_PATIENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // refresh activity
            recreate()
        }
    }
}
