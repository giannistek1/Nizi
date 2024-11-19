package nl.stekkinger.nizi.activities.doctor

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.databinding.ActivityPatientDetailBinding
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.doctor.PatientFeedbackFragment
import nl.stekkinger.nizi.fragments.doctor.PatientHomeFragment
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import nl.stekkinger.nizi.repositories.WeightUnitRepository


class PatientDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientDetailBinding

    private val authRepository = AuthRepository()
    private val patientRepository = PatientRepository()
    private val weightUnitRepository = WeightUnitRepository()

    private lateinit var weightUnits: ArrayList<WeightUnit>             // WeightUnits
    private lateinit var patientData: PatientData                       // User, Patient, Doctor, Dietary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI.
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(findViewById(R.id.toolbar))
        // Back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.patient_overview)
        loader = binding.activityPatientDetailLoader
        binding.activityPatientDetailBottomNavigation.setOnNavigationItemSelectedListener(navListener)

        // Setup custom toast
        val parent: RelativeLayout = binding.activityPatientDetailRl
        //toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(customToastLayout)

        // Overrides custom toast animation for with bottom navigation
        toastAnimation = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.move_up_fade_out_bottom_nav
        )

        val patientItem = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientItem

        patientData = PatientData(
            patient = PatientShort(id = patientItem.patient_id, date_of_birth = patientItem.date_of_birth,
                gender = patientItem.gender, doctor = patientItem.doctor_id),

            user = User(first_name = patientItem.first_name, last_name = patientItem.last_name,
                username = patientItem.username, email = patientItem.email, role = patientItem.role),

            diets = arrayListOf()
        )

        // Update user with patientId (because its used for getting Diary)
        val doctorUser = GeneralHelper.getUser()
        doctorUser.patient = PatientShort(id = patientItem.patient_id, gender = "", date_of_birth = "", doctor = 0)
        val gson = Gson()
        val json = gson.toJson(doctorUser)
        GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_USER, json).apply()

        if (GeneralHelper.isAdmin()) { getPatientMockup(); return }

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return

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

                supportFragmentManager.beginTransaction().replace(binding.activityPatientDetailFragmentContainer.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_diary -> {
                val fragment = DiaryFragment()
                supportFragmentManager.beginTransaction().replace(binding.activityPatientDetailFragmentContainer.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_conversation -> {
                val fragment = PatientFeedbackFragment()

                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
                fragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(binding.activityPatientDetailFragmentContainer.id,  fragment, fragment.javaClass.simpleName)
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
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.get_weight_unit_fail))
                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_weight_units))

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
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.get_patient_fail))
                return }

            // Feedback
            GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.fetched_patient))

            // Make PatientData because patient does not have descriptions of dietaryRestrictions
            patientData.user = result.user!!
            patientData.patient = PatientShort(
                id = result.id!!,
                gender = result.gender,
                date_of_birth = result.date_of_birth,
                doctor = result.doctor.id!!,
                user = result.user.id,
                feedbacks = result.feedbacks,
                dietary_managements = result.dietary_managements,
                my_foods = result.my_foods,
                consumptions = result.consumptions
            )

            val fragment = PatientHomeFragment()
            val bundle = Bundle()
            bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction().replace(
                binding.activityPatientDetailFragmentContainer.id,
                fragment,
                fragment.javaClass.simpleName)
                .commit()
        }
    }
    //endregion

    //region Mockups
    private fun getPatientMockup() {
        // Make PatientData because patient does not have descriptions of dietaryRestrictions
        patientData.user = authRepository.getUserLocally(patientData.user.username)!!
        val result = patientRepository.getPatientLocally(patientData.patient.id)!!

        patientData.patient = PatientShort(
            id = result.id!!,
            gender = result.gender,
            date_of_birth = result.date_of_birth,
            doctor = result.doctor.id!!,
            user = patientData.user.id,
            feedbacks = result.feedbacks,
            dietary_managements = result.dietary_managements,
            my_foods = result.my_foods,
            consumptions = result.consumptions
        )

        val fragment = PatientHomeFragment()
        val bundle = Bundle()
        bundle.putSerializable(GeneralHelper.EXTRA_PATIENT, patientData)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(
            binding.activityPatientDetailFragmentContainer.id,
            fragment,
            fragment.javaClass.simpleName)
            .commit()
    }
    //endregion
}
