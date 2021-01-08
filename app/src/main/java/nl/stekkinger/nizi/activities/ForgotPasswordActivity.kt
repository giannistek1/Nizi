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
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.DoctorMainActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.password.ForgotPasswordRequest
import nl.stekkinger.nizi.classes.password.ForgotPasswordResponse
import nl.stekkinger.nizi.repositories.AuthRepository


class ForgotPasswordActivity : BaseActivity() {

    private var TAG = "ForgotPassword"

    //region Repositories
    private val authRepository: AuthRepository = AuthRepository()
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the UI
        setContentView(R.layout.activity_forgot_password)
        setSupportActionBar(toolbar)
        // Back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        // Show app title by hiding the bar overlayed on the toolbar
        toolbar_bar.visibility = View.GONE
        loader = activity_forgot_password_loader

        // Setup custom toast
        val parent: FrameLayout = activity_login_fl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        activity_forgot_password_et_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleSendButtonIfNotEmpty(activity_forgot_password_et_email, activity_forgot_password_btn_send)
            }
        })

        activity_forgot_password_btn_send.setOnClickListener {
            // Give EditTexts so you can change them
            sendForgetPassword(activity_forgot_password_et_email)
        }

        activity_forgot_password_btn_send.isEnabled = false
        activity_forgot_password_btn_send.alpha = 0.2f

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
        if (!GeneralHelper.hasInternetConnection(this)) return
        if (InputHelper.inputIsEmpty(this, emailET, R.string.email_cant_be_empty)) return

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
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            // Result either gives the (token, user, patient/doctor) OR null
            if (result == null) { Toast.makeText(baseContext, R.string.mail_sent_fail, Toast.LENGTH_SHORT).show(); return }

            // Feedback
            Toast.makeText(baseContext, R.string.mail_sent, Toast.LENGTH_SHORT).show()

            showNextActivity()
        }
    }
    //endregion
}
