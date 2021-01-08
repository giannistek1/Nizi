package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_patient.*
import kotlinx.android.synthetic.main.activity_add_patient_dietary.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.repositories.AuthRepository
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class AddPatientActivity : BaseActivity() {

    private var TAG = "AddPatient"

    // For activity result
    private val REQUEST_CODE = 1

    private val authRepository: AuthRepository = AuthRepository()

    private lateinit var date: Date
    private var doctorId: Int? = null
    private var users: ArrayList<UserLogin> = arrayListOf()

    val sdfDB = GeneralHelper.getCreateDateFormat() // Todo: Refactor getCreateDate to DBDate or something

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_txt_back.text = getString(R.string.patient_overview)
        loader = activity_add_patient_progressbar

        // Setup custom toast
        val parent: RelativeLayout = activity_add_patient_rl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        // Date of Birth
        activity_add_patient_et_dob.setOnClickListener {

            if (activity_add_patient_dp.visibility == View.VISIBLE)
                activity_add_patient_dp.visibility = View.GONE
            else
                activity_add_patient_dp.visibility = View.VISIBLE
        }

        activity_add_patient_dp.updateDate(2000, 1, 1)
        activity_add_patient_dp.setOnDateChangedListener { _, mYear, mMonth, mDay ->

            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay, 0, 0)
            date = calendar.time

            val sdf = GeneralHelper.getFeedbackDateFormat()

            activity_add_patient_et_dob.setText("${sdf.format(date)}")

        }

        activity_add_patient_btn_to_guidelines.setOnClickListener {
            tryCreatePatient(activity_add_patient_et_firstName, activity_add_patient_et_lastName,
                activity_add_patient_et_dob, activity_add_patient_et_email, activity_add_patient_et_password,
            activity_add_patient_et_passwordConfirm)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Standard on canceled
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)

        getUsers().execute()

        //region Testing
        activity_add_patient_et_firstName.setText(getString(R.string.sample_first_name))
        activity_add_patient_et_lastName.setText(R.string.sample_last_name)
        activity_add_patient_et_dob.setText(R.string.sample_dob)
        val calendar = Calendar.getInstance()
        calendar.set(1990, 1, 1, 0, 0)
        date = calendar.time
        activity_add_patient_et_email.setText(R.string.sample_email)
        activity_add_patient_et_password.setText(R.string.sample_password)
        activity_add_patient_et_passwordConfirm.setText(R.string.sample_password)
        activity_add_patient_rg_gender.check(activity_add_patient_rb_male.id)
        //endregion
    }

    private fun tryCreatePatient(firstNameET: EditText, lastNameET: EditText, dobET: EditText, emailET:
    EditText, passwordET: EditText, passwordConfirmET: EditText) {
        firstNameET.setBackgroundColor(Color.TRANSPARENT)
        lastNameET.setBackgroundColor(Color.TRANSPARENT)
        dobET.setBackgroundColor(Color.TRANSPARENT)
        emailET.setBackgroundColor(Color.TRANSPARENT)
        passwordET.setBackgroundColor(Color.TRANSPARENT)
        passwordConfirmET.setBackgroundColor(Color.TRANSPARENT)

        // Guards/Checks
        if (InputHelper.inputIsEmpty(this, firstNameET, R.string.empty_first_name)) return
        if (InputHelper.inputIsEmpty(this, lastNameET, R.string.empty_last_name)) return
        if (InputHelper.inputIsEmpty(this, dobET, R.string.empty_date_of_birth)) return
        if (InputHelper.inputIsEmpty(this, emailET, R.string.empty_email)) return
        if (InputHelper.inputIsEmpty(this, passwordET, R.string.empty_password)) return
        if (InputHelper.inputIsEmpty(this, passwordConfirmET, R.string.empty_password_confirm)) return

        val checkedGenderRadioButtonId: Int = activity_add_patient_rg_gender.checkedRadioButtonId
        if (checkedGenderRadioButtonId == -1) { return }

        // Check if email already exists
        val user = users.find { it.email ==  emailET.text.toString() }
        if (user != null) {
            Toast.makeText(baseContext, R.string.email_already_exists, Toast.LENGTH_SHORT).show()
            emailET.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            return }

        // Check if email is valid
        if (!InputHelper.isValidEmail(emailET.text.toString())) {
            Toast.makeText(baseContext, R.string.email_invalid, Toast.LENGTH_SHORT).show()
            emailET.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            return }

        // Check if not matching passwords
        else if (passwordET.text.toString() != passwordConfirmET.text.toString()) {
            Toast.makeText(baseContext, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show(); return }

        val intent = Intent(this@AddPatientActivity, AddPatientDietaryActivity::class.java)
        // Prevents duplicating activity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        // Patientdata
        var lastNameWithoutSpaces = lastNameET.text.toString().trim()
        lastNameWithoutSpaces = lastNameWithoutSpaces.replace("\\s".toRegex(), "")

        val selectedRadioButton: RadioButton = activity_add_patient_rg_gender.findViewById(checkedGenderRadioButtonId)

        val newPatient = AddPatientViewModel(
            user = User(
                first_name = firstNameET.text.toString().trim(),
                last_name = lastNameET.text.toString().trim(),
                username = firstNameET.text.toString().trim() + lastNameWithoutSpaces,
                email = emailET.text.toString().trim(),
                password = passwordET.text.toString().trim(),
                role = 4 // Patient role
            ),
            // Update corresponding user later when its made
            patient = PatientShort(
                gender = selectedRadioButton.text.toString(),
                date_of_birth = sdfDB.format(date),
                doctor = doctorId!!
            )
        )

        // Give patient and doctor ID
        intent.putExtra(GeneralHelper.EXTRA_PATIENT, newPatient)
        intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, doctorId!!)
        // Start intent
        startActivityForResult(intent, REQUEST_CODE)
    }

    //region getUsers
    inner class getUsers : AsyncTask<Void, Void, ArrayList<UserLogin>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<UserLogin>? {
            return try {
                authRepository.getUsers()
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<UserLogin>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(baseContext, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(baseContext, R.string.get_users_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            //Toast.makeText(baseContext, R.string.fetched_users, Toast.LENGTH_SHORT).show()

            users = result
        }
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // In case we wanna return something
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }
}
