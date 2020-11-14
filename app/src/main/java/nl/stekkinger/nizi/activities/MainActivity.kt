package nl.stekkinger.nizi.activities

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.user.User
import nl.stekkinger.nizi.classes.user.UserLogin
import nl.stekkinger.nizi.fragments.ConversationFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.repositories.AuthRepository
import nl.stekkinger.nizi.repositories.DietaryRepository

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    private val authRepository: AuthRepository = AuthRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    private lateinit var diaryModel: DiaryViewModel
    private var list: ArrayList<DietaryGuideline>? = null
    private var patientId: Int = 0
    private lateinit var user: UserLogin

    private lateinit var progressBar: View

    val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI
        setContentView(R.layout.activity_main)

        diaryModel = ViewModelProviders.of(this)[DiaryViewModel::class.java]

        diaryModel.getDiary().observe(this, Observer { result ->

        })

        // Checks if  fragment state is null, else start with homeFragment
        if (savedInstanceState == null) {
            val fragment = HomeFragment(this, dietaryGuidelines)
            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
        progressBar = activity_main_progressbar

        // Get User
        user = GeneralHelper.getUser()

        activity_main_bottom_navigation.setOnNavigationItemSelectedListener(navListener)
    }

    //region Bottom Nav
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {  menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val fragment = HomeFragment(this, list)
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

                val restriction = resultDietary.Description.replace("beperking", "").replace("verrijking","")
                dietaryGuideline = DietaryGuideline(resultDietary.Description, restriction, restriction.toLowerCase(),
                    0, 0, 0, "")

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

                // Plurals
                if (resultDietary.Description.contains("Calorie"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "en"
                else if (resultDietary.Description.contains("Eiwit"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "ten"
                else if (resultDietary.Description.contains("Vezel"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "s"

                // minimum/maximum
                if (resultDietary.Description.contains("beperking"))
                {
                    dietaryGuideline.minimum = resultDietary.Amount
                }
                else if (resultDietary.Description.contains("verrijking"))
                {
                    dietaryGuideline.description = dietaryGuideline.description.replace("beperking", "")
                    dietaryGuideline.maximum = resultDietary.Amount
                }

                // Fill in weight unit
                if (resultDietary.Description.contains("Calorie"))
                    dietaryGuideline.weightUnit = getString(R.string.kcal)
                else if (resultDietary.Description.contains("Natrium") || resultDietary.Description.contains("Kalium"))
                    dietaryGuideline.weightUnit = getString(R.string.milligram_short)
                else if (resultDietary.Description.contains("Eiwit") || resultDietary.Description.contains("Vezel"))
                    dietaryGuideline.weightUnit = getString(R.string.gram)
                else if (resultDietary.Description.contains("Vocht"))
                    dietaryGuideline.weightUnit = getString(R.string.milliliter_short)

                // Add to dietary list or update list
                if (!alreadyExists) // create new
                    dietaryGuidelines.add(dietaryGuideline)
                else // update
                    dietaryGuidelines[index-1] = dietaryGuideline
            }

            // Progressbar
            progressBar.visibility = View.GONE

            list = dietaryGuidelines

            val fragment = HomeFragment(this@MainActivity, dietaryGuidelines)
            supportFragmentManager.beginTransaction().replace(activity_main_fragment_container.id,  fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
    }
    //endregion
 }
