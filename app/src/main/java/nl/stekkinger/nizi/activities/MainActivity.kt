package nl.stekkinger.nizi.activities

import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Mockup
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.fragments.ConversationFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DoctorRepository
import nl.stekkinger.nizi.repositories.WeightUnitRepository

class MainActivity : BaseActivity() {

    private var TAG = "Main"

    // Repositories
    private val authRepository: AuthRepository = AuthRepository()
    private val weightUnitRepository: WeightUnitRepository = WeightUnitRepository()
    private val doctorRepository: DoctorRepository = DoctorRepository()

    private lateinit var user: UserLogin                                // Userdata
    private lateinit var weightUnits: ArrayList<WeightUnit>             // WeightUnits
    val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()  // Dietary
    private lateinit var doctor: Doctor                                 // Doctor
    private lateinit var diaryModel: DiaryViewModel                     // Diary

    private var savedInstanceState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toolbar_title.text = getString(R.string.app_name)
        loader = activity_main_loader
        activity_main_bottom_navigation.setOnNavigationItemSelectedListener(navListener)

        // Setup custom toast
        val parent: RelativeLayout = activity_main_rl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        // Overrides custom toast animation for with bottom navigation
        toastAnimation = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.move_up_fade_out_bottom_nav
        )

        diaryModel = ViewModelProviders.of(this)[DiaryViewModel::class.java]

        // Checks if fragment state is null and save it
        //if (savedInstanceState != null)
            //this.savedInstanceState = savedInstanceState

        // Get User
        user = GeneralHelper.getUser()

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, toastView, toastAnimation)) return

        // Get data
        if (GeneralHelper.isAdmin()) { getDoctorMockup(); return }

        getWeightUnits().execute()
        getDoctorAsyncTask().execute()
    }

    //region Toolbar
    // Inflates toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            android.R.id.home -> {
                return false
            }
            R.id.menu_toolbar_logout -> {
                authRepository.logout(this, this)
            }
            R.id.confirm_btn -> {
                return false
            }
        }
        return true
    }
    //endregion

    //region Bottom Nav
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {  menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                toolbar_title.text = getString(R.string.app_name)
                val fragment = HomeFragment()

                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_DIETARY, dietaryGuidelines)
                fragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_diary -> {
                val fragment = DiaryFragment()
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_conversation -> {
                toolbar_title.text = getString(R.string.conversations)
                val fragment = ConversationFragment(user, doctor)
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    //endregion

    //region Get WeightUnits
    inner class getWeightUnits() : AsyncTask<Void, Void, ArrayList<WeightUnit>>()
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
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_weight_unit_fail))
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

            // Checks if fragment state is null, then start with homeFragment
            if (savedInstanceState == null) {
                val fragment = HomeFragment()

                val bundle = Bundle()
                bundle.putSerializable(GeneralHelper.EXTRA_DIETARY, dietaryGuidelines)
                fragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id, fragment,
                    fragment.javaClass.simpleName).commit()
            }
        }
    }
    //endregion

    //region Get Doctor (One time for Feedback)
    inner class getDoctorAsyncTask() : AsyncTask<Void, Void, Doctor>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Doctor? {
            return try {
                doctorRepository.getDoctor(user.patient!!.doctor)
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: Doctor?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_doctor_fail))
                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_doctor))

            doctor = result
        }
    }
    //endregion

    //region Mockups
    private fun getDoctorMockup() {
        doctor = Mockup.doctor
    }
    //endregion

    override fun onBackPressed() {
        val fm: FragmentManager = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
 }
