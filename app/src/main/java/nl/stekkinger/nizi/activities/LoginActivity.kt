package nl.stekkinger.nizi.activities

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.DoctorMainActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import nl.stekkinger.nizi.repositories.AuthRepository


class LoginActivity : AppCompatActivity() {

    private var TAG = "Login"

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    //endregion

    private var isDoctor = false

    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the UI
        setContentView(R.layout.activity_login)
        progressBar = activity_login_loader

        activity_login_et_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleLoginButtonIfNotEmpty(activity_login_et_username, activity_login_et_password,
                    activity_login_btn_login)
            }
        })

        activity_login_et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleLoginButtonIfNotEmpty(activity_login_et_username, activity_login_et_password,
                    activity_login_btn_login)
            }
        })

        activity_login_btn_login.setOnClickListener {
            // Give EditTexts so you can change them
            doLogin(activity_login_et_username, activity_login_et_password)
        }

        // if logged in, get isDoctor and go to next activity
        if (GeneralHelper.prefs.contains(GeneralHelper.PREF_TOKEN)) {
            isDoctor = GeneralHelper.prefs.getBoolean(GeneralHelper.PREF_IS_DOCTOR, false)
            showNextActivity()
        }

        // Testing
        //activity_login_et_username.setText("BramWenting") // Patient
        activity_login_et_username.setText("HugoBrand") // Doctor
        activity_login_et_password.setText("Welkom123")

    }

    private fun showNextActivity() {
        val intent: Intent = when(isDoctor) {
            true -> Intent(this@LoginActivity, DoctorMainActivity::class.java)
            false ->  Intent(this@LoginActivity, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    private fun toggleLoginButtonIfNotEmpty(usernameET: EditText, passwordET: EditText, loginButton: Button) {
        loginButton.isEnabled = (usernameET.text.toString().length > 3 && passwordET.text.toString().length > 3)
    }

    private fun doLogin(usernameET: EditText, passwordET: EditText) {
        usernameET.setBackgroundColor(Color.TRANSPARENT)
        passwordET.setBackgroundColor(Color.TRANSPARENT)

        // Checks (Guards)
        if (!GeneralHelper.hasInternetConnection(this)) return
        if (InputHelper.inputIsEmpty(this, usernameET, R.string.username_cant_be_empty)) return
        if (InputHelper.inputIsEmpty(this, passwordET, R.string.password_cant_be_empty)) return

        val loginRequest = LoginRequest(usernameET.text.toString(), passwordET.text.toString())

        if (loginAsyncTask(loginRequest).status != AsyncTask.Status.RUNNING)
            loginAsyncTask(loginRequest).execute()
    }

    //region Login
    inner class loginAsyncTask(private val loginRequest: LoginRequest) : AsyncTask<Void, Void, LoginResponse>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Loader
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): LoginResponse? {
            return authRepository.login(loginRequest)
        }

        // Result must be nullable
        override fun onPostExecute(result: LoginResponse?) {
            super.onPostExecute(result)
            // Loader
            progressBar.visibility = View.GONE

            // Guard result either gives the (token, user, patient/doctor) OR null
            if (result == null) { Toast.makeText(baseContext, R.string.credentials_wrong, Toast.LENGTH_SHORT).show(); return }

            // Feedback
            Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()

            // Save token
            GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_TOKEN, result.jwt).apply()

            // Save isDoctor for future reference
            isDoctor = (result.user!!.role.name == "Doctor")
            GeneralHelper.prefs.edit().putBoolean(GeneralHelper.PREF_IS_DOCTOR, isDoctor).apply()

            // Save Patient/Doctor Id
            if (isDoctor)
                GeneralHelper.prefs.edit().putInt(GeneralHelper.PREF_DOCTOR_ID, result.user.doctor!!.id!!).apply()
            else
                GeneralHelper.prefs.edit().putInt(GeneralHelper.PREF_DOCTOR_ID, result.user.patient!!.id).apply()

            // Save user
            val gson = Gson()
            val json = gson.toJson(result.user)
            GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_USER, json).apply()

            showNextActivity()
        }
    }
    //endregion
}
