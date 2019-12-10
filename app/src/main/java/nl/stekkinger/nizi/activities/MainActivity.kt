package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import nl.stekkinger.nizi.DiaryViewModel
import nl.stekkinger.nizi.fragments.DashboardFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.HomeFragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.PatientLogin
import nl.stekkinger.nizi.repositories.AuthRepository

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    // Shared preferences/extras
    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"

    private val authRepository: AuthRepository = AuthRepository()

    private var model: PatientLogin? = null

    private lateinit var diaryModel: DiaryViewModel

    private lateinit var progressBar: View

    private lateinit var accessToken: String

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
        activity_main_btn_logout.setOnClickListener { logout() }

        // Obtain the token from the Intent's extras
        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)

        // Login
        loginPatientAsyncTask().execute()

        activity_main_bottom_navigation.setOnNavigationItemSelectedListener(navListener)
    }

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


    private fun logout() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }

    inner class loginPatientAsyncTask() : AsyncTask<Void, Void, PatientLogin>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): PatientLogin? {
            return authRepository.loginAsPatient(accessToken)
        }

        override fun onPostExecute(result: PatientLogin?) {
            super.onPostExecute(result)
            // Progressbar
            progressBar.visibility = View.GONE
            if (result != null) {
                Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
            }

        }
    }
 }
