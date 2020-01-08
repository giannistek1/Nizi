package nl.stekkinger.nizi.activities

import android.content.Context
import android.content.Intent
import android.drm.DrmStore
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.auth0.android.provider.WebAuthProvider.logout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.fragments.DashboardFragment
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.PatientLogin
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.repositories.AuthRepository

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    private val authRepository: AuthRepository = AuthRepository()

    private var model: PatientLogin? = null

    private lateinit var diaryModel: DiaryViewModel

    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI
        setContentView(R.layout.activity_main)

        diaryModel = ViewModelProviders.of(this)[DiaryViewModel::class.java]

        // Checks if  fragment state is null, else start with homeFragment
        if (savedInstanceState == null) {
            val fragment = HomeFragment()
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
                val fragment = HomeFragment()
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

            R.id.nav_dashboard -> {
                val fragment = DashboardFragment()
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

            val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
            preferences.edit().putInt("patient", result!!.patient.patientId).commit()
            d("con", preferences.getInt("patient", 0).toString())
            d("login", result.patient.patientId.toString())
            // Progressbar
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }
    //endregion
 }
