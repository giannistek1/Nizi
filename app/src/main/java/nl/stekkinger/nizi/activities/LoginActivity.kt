package nl.stekkinger.nizi.activities

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.gson.Gson
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.DoctorMainActivity
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import nl.stekkinger.nizi.databinding.ActivityLoginBinding
import nl.stekkinger.nizi.databinding.ToolbarBinding
import nl.stekkinger.nizi.repositories.AuthRepository
import java.util.Locale


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var toolbarBinding: ToolbarBinding

    private var TAG = "Login"

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    //endregion

    private var isDoctor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the UI.
        binding = ActivityLoginBinding.inflate(layoutInflater)
        toolbarBinding = ToolbarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(toolbarBinding.toolbar)
        // Show app title by hiding the bar overlayed on the toolbar
        toolbarBinding.toolbar.visibility = View.GONE
        loader = binding.activityLoginLoader
        Linkify.addLinks(binding.activityLoginTxtNierstichtingUrl, Linkify.WEB_URLS)
        Linkify.addLinks(binding.activityLoginTxtNierstichtingPhonenumber, Linkify.PHONE_NUMBERS)
        Linkify.addLinks(binding.activityLoginTxtNierstichtingEmail, Linkify.EMAIL_ADDRESSES)
        binding.activityLoginTxtNierstichtingUrl.removeLinksUnderline()
        binding.activityLoginTxtNierstichtingPhonenumber.removeLinksUnderline()
        binding.activityLoginTxtNierstichtingEmail.removeLinksUnderline()

        // Setup custom toast (any Relative layout will do)
        val parent: RelativeLayout = binding.activityLoginRlHeader
        //toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(customToastLayout)

        binding.activityLoginEtUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleLoginButtonIfNotEmpty(binding.activityLoginEtUsername, binding.activityLoginEtPassword,
                    binding.activityLoginBtnLogin)
            }
        })

        binding.activityLoginEtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleLoginButtonIfNotEmpty(binding.activityLoginEtUsername, binding.activityLoginEtPassword,
                    binding.activityLoginBtnLogin)
            }
        })

        binding.activityLoginTxtPasswordForgotten.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.activityLoginBtnLogin.setOnClickListener {
            // Give EditTexts so you can change them
            doLogin(binding.activityLoginEtUsername, binding.activityLoginEtPassword)
        }

        // if logged in, get isDoctor and go to next activity
        if (GeneralHelper.prefs.contains(GeneralHelper.PREF_TOKEN)) {
            isDoctor = GeneralHelper.prefs.getBoolean(GeneralHelper.PREF_IS_DOCTOR, false)
            showNextActivity()
        }

        // Testing
        //activity_login_et_username.setText("BramWenting") // Patient
        //activity_login_et_username.setText("HugoBrand") // Doctor
        //activity_login_et_password.setText("Welkom123")

        binding.activityLoginEtUsername.setText("patient") // Patient
        binding.activityLoginEtUsername.setText("patient") // Patient
        //activity_login_et_username.setText("doctor") // Doctor
        //activity_login_et_password.setText("doctor") // Doctor

        toggleLoginButtonIfNotEmpty(binding.activityLoginEtUsername, binding.activityLoginEtUsername,
            binding.activityLoginBtnLogin)
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
        if (usernameET.text.toString().length > 3 && passwordET.text.toString().length > 3) {
            loginButton.isEnabled = true
            loginButton.alpha = 1f
        } else {
            loginButton.isEnabled = false
            loginButton.alpha = 0.2f
        }
    }

    private fun doLogin(usernameET: EditText, passwordET: EditText) {
        usernameET.setBackgroundColor(Color.TRANSPARENT)
        passwordET.setBackgroundColor(Color.TRANSPARENT)

        // Checks (Guards)
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return
        if (InputHelper.inputIsEmpty(this, usernameET, customToastLayout, toastAnimation, getString(R.string.username_cant_be_empty))) return
        if (InputHelper.inputIsEmpty(this, passwordET, customToastLayout, toastAnimation, getString(R.string.password_cant_be_empty))) return

        val loginRequest = LoginRequest(usernameET.text.toString(), passwordET.text.toString())

        if (usernameET.text.toString().toLowerCase(Locale.ROOT) == "doctor") { loginDoctorMockup(); return }
        if (usernameET.text.toString().toLowerCase(Locale.ROOT) == "patient") { loginPatientMockup(); return }

        if (loginAsyncTask(loginRequest).status != AsyncTask.Status.RUNNING)
            loginAsyncTask(loginRequest).execute()
    }

    private fun TextView.removeLinksUnderline() {
        val spannable = SpannableString(text)
        for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
            spannable.setSpan(object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
        }
        text = spannable
    }

    //region Login
    inner class loginAsyncTask(private val loginRequest: LoginRequest) : AsyncTask<Void, Void, LoginResponse>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): LoginResponse? {
            return try {
                 authRepository.login(loginRequest)
            } catch (e: Exception ){
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        // Result must be nullable
        override fun onPostExecute(result: LoginResponse?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.INVISIBLE

            // Guards
            // Since you can't toast in onBackground
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.api_is_down)); return }
            // Result either gives the (token, user, patient/doctor) OR null
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.credentials_wrong)); return }

            // Feedback
            GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.login_success))

            // Save token
            GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_TOKEN, result.jwt).apply()

            // Save isDoctor for future reference
            isDoctor = (result.user!!.role.name == "Doctor")
            GeneralHelper.prefs.edit().putBoolean(GeneralHelper.PREF_IS_DOCTOR, isDoctor).apply()


            // Save own Patient/Doctor Id
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

    //region Mockup Login
    private fun loginPatientMockup() {
        // Feedback
        GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.login_success))

        // Save token
        GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_TOKEN, LocalDb.jwt).apply()

        // Save isDoctor for future reference
        isDoctor = false
        GeneralHelper.prefs.edit().putBoolean(GeneralHelper.PREF_IS_DOCTOR, isDoctor).apply()

        // Save own Patient/Doctor Id
        GeneralHelper.prefs.edit().putInt(GeneralHelper.PREF_DOCTOR_ID, 0).apply()

        // Save user
        val gson = Gson()
        val json = gson.toJson(LocalDb.userLogin)
        GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_USER, json).apply()

        showNextActivity()
    }

    private fun loginDoctorMockup() {
        // Feedback
        GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.login_success))

        // Save token
        GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_TOKEN, LocalDb.jwt).apply()

        // Save isDoctor for future reference
        isDoctor = true
        GeneralHelper.prefs.edit().putBoolean(GeneralHelper.PREF_IS_DOCTOR, isDoctor).apply()

        // Save own Patient/Doctor Id
        GeneralHelper.prefs.edit().putInt(GeneralHelper.PREF_DOCTOR_ID, 0).apply()

        // Save user
        val gson = Gson()
        val json = gson.toJson(LocalDb.userLogin)
        GeneralHelper.prefs.edit().putString(GeneralHelper.PREF_USER, json).apply()

        showNextActivity()
    }
    //endregion
}
