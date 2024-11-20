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
import android.widget.RelativeLayout
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.DoctorMainActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.password.ForgotPasswordRequest
import nl.stekkinger.nizi.classes.password.ForgotPasswordResponse
import nl.stekkinger.nizi.databinding.ActivityForgotPasswordBinding
import nl.stekkinger.nizi.repositories.AuthRepository


class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private var TAG = "ForgotPassword"

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI.
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(findViewById(R.id.toolbar))
        // Back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Show app title by hiding the bar overlayed on the toolbar
        supportActionBar?.title = getString(R.string.app_name)
        loader = binding.activityForgotPasswordLoader

        // Setup custom toast
        val parent: RelativeLayout = binding.activityForgotPasswordRl
        //toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
//        parent.addView(customToastLayout)

        binding.activityForgotPasswordEtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleSendButtonIfNotEmpty(binding.activityForgotPasswordEtEmail, binding.activityForgotPasswordBtnSend)
            }
        })

        binding.activityForgotPasswordBtnSend.setOnClickListener {
            // Give EditTexts so you can change them
            sendForgetPassword(binding.activityForgotPasswordEtEmail)
        }

        binding.activityForgotPasswordBtnSend.isEnabled = false
        binding.activityForgotPasswordBtnSend.alpha = 0.2f

        // Testing
        //activity_forgot_password_et_email.setText("BramWenting@inholland.nl")
    }

    private fun showNextActivity() {
        Intent(this@ForgotPasswordActivity, DoctorMainActivity::class.java)

        startActivity(intent)
        finish()
    }

    private fun toggleSendButtonIfNotEmpty(emailET: EditText, loginButton: Button) {
        if (emailET.text.toString().length > 3) {
            loginButton.isEnabled = true
            loginButton.alpha = 1f
        } else {
            loginButton.isEnabled = false
            loginButton.alpha = 0.2f
        }
    }

    private fun sendForgetPassword(emailET: EditText) {
        emailET.setBackgroundColor(Color.TRANSPARENT)

        // Checks (Guards)
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return
//        if (InputHelper.inputIsEmpty(this, emailET, customToastLayout, toastAnimation, getString(R.string.email_cant_be_empty))) return

        val forgotPasswordRequest = ForgotPasswordRequest(emailET.text.toString())

        if (forgotPasswordAsyncTask(forgotPasswordRequest).status != AsyncTask.Status.RUNNING)
            forgotPasswordAsyncTask(forgotPasswordRequest).execute()
    }

    //region forgotPassword
    inner class forgotPasswordAsyncTask(private val forgotPasswordRequest: ForgotPasswordRequest) : AsyncTask<Void, Void, ForgotPasswordResponse>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ForgotPasswordResponse? {
            return try {
                 authRepository.forgotPassword(forgotPasswordRequest)
            } catch (e: Exception ){
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        // Result must be nullable
        override fun onPostExecute(result: ForgotPasswordResponse?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.INVISIBLE

            // Guards
            // Since you can't toast in onBackground
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.api_is_down)); return }
            // Result either gives the (token, user, patient/doctor) OR null
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.mail_sent_fail)); return }

            // Feedback
            GeneralHelper.makeToast(baseContext, customToastBinding, getString(R.string.mail_sent))

            showNextActivity()
        }
    }
    //endregion
}
