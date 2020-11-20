package nl.stekkinger.nizi.activities

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.doctor.DoctorShort
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.fragments.ConversationFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.DoctorRepository
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    // Repositories
    private val authRepository: AuthRepository = AuthRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()
    private val doctorRepository: DoctorRepository = DoctorRepository()

    private lateinit var user: UserLogin                                // Userdata
    val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()  // Dietary
    private lateinit var doctor: Doctor                                 // Doctor
    private lateinit var diaryModel: DiaryViewModel                     // Diary

    private var savedInstanceState: Bundle? = null

    private lateinit var loader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.app_name)
        loader = activity_main_loader
        activity_main_bottom_navigation.setOnNavigationItemSelectedListener(navListener)

        diaryModel = ViewModelProviders.of(this)[DiaryViewModel::class.java]

        diaryModel.getDiary().observe(this, Observer { result ->

        })

        // Checks if fragment state is null and save it
        if (savedInstanceState != null)
            this.savedInstanceState = savedInstanceState

        // Get User
        user = GeneralHelper.getUser()

        getDietaryAsyncTask().execute()
    }

    //region Bottom Nav
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {  menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                toolbar_title.text = getString(R.string.app_name)
                val fragment = HomeFragment(dietaryGuidelines)
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.simpleName)
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_diary -> {
                toolbar_title.text = getString(R.string.diary)
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

    //region Toolbar
    // Inflates toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.menu_toolbar_logout -> {
                authRepository.logout(this, this)
            }
            R.id.confirm_btn -> {
                return false
            }
            R.id.back_btn -> {
                return false
            }
        }
        return true
    }
    //endregion

    //region Get Dietary
    inner class getDietaryAsyncTask() : AsyncTask<Void, Void, ArrayList<DietaryManagement>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<DietaryManagement>? {
            return try {
                dietaryRepository.getDietaryManagements(user.patient!!.id)
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<DietaryManagement>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(baseContext, R.string.get_dietary_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(baseContext, R.string.fetched_dietary, Toast.LENGTH_SHORT).show()

            result.forEachIndexed { index, resultDietary ->

                lateinit var dietaryGuideline: DietaryGuideline

                val restriction = resultDietary.dietary_restriction.description.replace("beperking", "").replace("verrijking","")
                dietaryGuideline =
                    DietaryGuideline(
                        resultDietary.dietary_restriction.description, restriction, restriction.toLowerCase(Locale.ROOT),
                        0, 0, 0, ""
                    )

                var alreadyExists = false

                // Check if dietaryGuideLines already has the food supplement type (e.g. calories)
                dietaryGuidelines.forEachIndexed loop@ {_, dietary ->
                   if (dietary.description.contains(resultDietary.dietary_restriction.description.take(3)))
                   {
                       dietaryGuideline = dietary
                       alreadyExists = true
                       return@loop
                   }
                }

                // Plurals
                if (resultDietary.dietary_restriction.description.contains("Calorie"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase(Locale.ROOT) + "en"
                else if (resultDietary.dietary_restriction.description.contains("Eiwit"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase(Locale.ROOT) + "ten"
                else if (resultDietary.dietary_restriction.description.contains("Vezel"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase(Locale.ROOT) + "s"

                // minimum/maximum
                if (resultDietary.dietary_restriction.description.contains("beperking"))
                {
                    dietaryGuideline.minimum = resultDietary.amount
                }
                else if (resultDietary.dietary_restriction.description.contains("verrijking"))
                {
                    dietaryGuideline.description = dietaryGuideline.description.replace("beperking", "")
                    dietaryGuideline.maximum = resultDietary.amount
                }

                // Fill in weight unit
                if (resultDietary.dietary_restriction.description.contains("Calorie"))
                    dietaryGuideline.weightUnit = getString(R.string.kcal)
                else if (resultDietary.dietary_restriction.description.contains("Natrium") || resultDietary.dietary_restriction.description.contains("Kalium"))
                    dietaryGuideline.weightUnit = getString(R.string.milligram_short)
                else if (resultDietary.dietary_restriction.description.contains("Eiwit") || resultDietary.dietary_restriction.description.contains("Vezel"))
                    dietaryGuideline.weightUnit = getString(R.string.gram)
                else if (resultDietary.dietary_restriction.description.contains("Vocht"))
                    dietaryGuideline.weightUnit = getString(R.string.milliliter_short)

                // Add to dietary list or update list
                if (!alreadyExists) // create new
                    dietaryGuidelines.add(dietaryGuideline)
                else // update
                    dietaryGuidelines[index-1] = dietaryGuideline
            }

            // Checks if fragment state is null, then start with homeFragment
            if (savedInstanceState == null) {
                val fragment = HomeFragment(dietaryGuidelines)
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id, fragment,
                    fragment.javaClass.simpleName).commit()
            }

            getDoctorAsyncTask().execute()
        }
    }
    //endregion

    //region Get Doctor
    inner class getDoctorAsyncTask() : AsyncTask<Void, Void, Doctor>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Doctor? {
            return doctorRepository.getDoctor(user.patient!!.doctor)
        }

        override fun onPostExecute(result: Doctor?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.get_doctor_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(baseContext, R.string.fetched_doctor, Toast.LENGTH_SHORT).show()

            doctor = result
        }
    }
    //endregion
 }
