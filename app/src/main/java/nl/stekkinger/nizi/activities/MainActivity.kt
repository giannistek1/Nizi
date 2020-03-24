package nl.stekkinger.nizi.activities

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryGuideline
import nl.stekkinger.nizi.classes.DietaryView
import nl.stekkinger.nizi.classes.PatientLogin
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass
import nl.stekkinger.nizi.fragments.ConversationFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    private val authRepository: AuthRepository = AuthRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var diaryModel: DiaryViewModel
    private var patientId: Int = 0

    private lateinit var progressBar: View

    val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI
        setContentView(R.layout.activity_main)

        diaryModel = ViewModelProviders.of(this)[DiaryViewModel::class.java]

        // Checks if  fragment state is null, else start with homeFragment
        if (savedInstanceState == null) {
            val fragment = HomeFragment(this, null)
            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
        progressBar = activity_main_progressbar

        // Login
        loginPatientAsyncTask().execute()

        activity_main_bottom_navigation.setOnNavigationItemSelectedListener(navListener)
    }

    //region Bottom Nav
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {  menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                //getDietaryAsyncTask().execute()
                val fragment = HomeFragment(this, null)
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_diary -> {
                val fragment = DiaryFragment()
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_conversation -> {
                val fragment = ConversationFragment()
                supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.getSimpleName())
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

    //region Patient Login
    inner class loginPatientAsyncTask() : AsyncTask<Void, Void, PatientLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): PatientLogin? {
            return authRepository.loginAsPatient()
        }

        override fun onPostExecute(result: PatientLogin?) {
            super.onPostExecute(result)

            try {
                patientId = result!!.patient.patientId

                val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
                preferences.edit().putInt("patient", result!!.patient.patientId).apply()
                d("con", preferences.getInt("patient", 0).toString())
                d("login", result.patient.patientId.toString())

                getDietaryAsyncTask().execute()
                // Progressbar
                progressBar.visibility = View.GONE
                if (result != null) {
                    Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
                }
            }
            catch(e: Exception)
            {
                d(TAG, e.cause.toString())
            }
        }
    }
    //endregion

    //region Get Dietary
    inner class getDietaryAsyncTask() : AsyncTask<Void, Void, DietaryView>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryView? {
            return dietaryRepository.getDietary(patientId)
        }

        override fun onPostExecute(result: DietaryView?) {
            super.onPostExecute(result)

            result!!.Dietarymanagement.forEachIndexed { index, resultDietary ->

                lateinit var dietaryGuideline: DietaryGuideline

                dietaryGuideline = DietaryGuideline(resultDietary.Description,
                    0, 0, 0)

                var alreadyExists = false

                // Check if dietaryGuideLines already has the food supplement type (e.g. calories)
                dietaryGuidelines.forEachIndexed loop@ {i, dietary ->
                   if (dietary.description.contains(resultDietary.Description.take(3)))
                   {
                       dietaryGuideline = dietary
                       alreadyExists = true
                       return@loop
                   }
                }

                if (resultDietary.Description.contains("beperking"))
                {
                    dietaryGuideline.minimum = resultDietary.Amount
                }
                else if (resultDietary.Description.contains("verrijking"))
                {
                    dietaryGuideline.description = dietaryGuideline.description.replace("beperking", "")
                    dietaryGuideline.maximum = resultDietary.Amount
                }

                if (!alreadyExists) // create new
                    dietaryGuidelines.add(dietaryGuideline)
                else // update
                    dietaryGuidelines[index-1] = dietaryGuideline
            }

            // Progressbar
            progressBar.visibility = View.GONE

            val fragment = HomeFragment(this@MainActivity, dietaryGuidelines)
            supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
    }
    //endregion
 }
