package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.databinding.ActivityAddPatientBinding
import nl.stekkinger.nizi.repositories.AuthRepository
import java.util.Calendar
import java.util.Date


class AddPatientActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPatientBinding

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

        // Setup UI.
        binding = ActivityAddPatientBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.patient_overview)
        loader = binding.activityAddPatientProgressbar

        // Setup custom toast
        val parent: RelativeLayout = binding.activityAddPatientRl
        //customToastBinding = CustomToastBinding.inflate(layoutInflater)
        parent.addView(customToastLayout)

        // Date of Birth
        binding.activityAddPatientEtDob.setOnClickListener {

            if (binding.activityAddPatientDp.visibility == View.VISIBLE)
                binding.activityAddPatientDp.visibility = View.GONE
            else
                binding.activityAddPatientDp.visibility = View.VISIBLE
        }

        binding.activityAddPatientDp.maxDate = Date().time
        binding.activityAddPatientDp.updateDate(2000, 1, 1)
        binding.activityAddPatientDp.setOnDateChangedListener { _, mYear, mMonth, mDay ->

            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay, 0, 0)
            date = calendar.time

            val sdf = GeneralHelper.getFeedbackDateFormat()

            binding.activityAddPatientEtDob.setText("${sdf.format(date)}")

        }

        binding.activityAddPatientBtnToGuidelines.setOnClickListener {
            tryCreatePatient(binding.activityAddPatientEtFirstName, binding.activityAddPatientEtLastName,
                binding.activityAddPatientEtDob, binding.activityAddPatientEtEmail, binding.activityAddPatientEtPassword,
                binding.activityAddPatientEtPasswordConfirm)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Standard on canceled
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return

        getUsers().execute()

        //region Testing
        binding.activityAddPatientEtFirstName.setText(getString(R.string.sample_first_name))
        binding.activityAddPatientEtLastName.setText(R.string.sample_last_name)
        binding.activityAddPatientEtDob.setText(R.string.sample_dob)
        val calendar = Calendar.getInstance()
        calendar.set(1990, 1, 1, 0, 0)
        date = calendar.time
        binding.activityAddPatientEtEmail.setText(R.string.sample_email)
        binding.activityAddPatientEtPassword.setText(R.string.sample_password)
        binding.activityAddPatientEtPasswordConfirm.setText(R.string.sample_password)
        binding.activityAddPatientRgGender.check(binding.activityAddPatientRbMale.id)
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
        if (InputHelper.inputIsEmpty(this, firstNameET, customToastLayout, toastAnimation, getString(R.string.empty_first_name))) return
        if (InputHelper.inputIsEmpty(this, lastNameET, customToastLayout, toastAnimation, getString(R.string.empty_last_name))) return
        if (InputHelper.inputIsEmpty(this, dobET, customToastLayout, toastAnimation, getString(R.string.empty_date_of_birth))) return
        if (InputHelper.inputIsEmpty(this, emailET, customToastLayout, toastAnimation, getString(R.string.empty_email))) return
        if (InputHelper.inputIsEmpty(this, passwordET, customToastLayout, toastAnimation, getString(R.string.empty_password))) return
        if (InputHelper.inputIsEmpty(this, passwordConfirmET, customToastLayout, toastAnimation, getString(R.string.empty_password_confirm))) return

        val checkedGenderRadioButtonId: Int = binding.activityAddPatientRgGender.checkedRadioButtonId
        if (checkedGenderRadioButtonId == -1) { return }

        // Check if email already exists
        val user = users.find { it.email ==  emailET.text.toString() }
        if (user != null) {
            GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.email_already_exists))
            emailET.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            return }

        // Check if email is valid
        if (!InputHelper.isValidEmail(emailET.text.toString())) {
            GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.email_invalid))
            emailET.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            return }

        // Check if not matching passwords
        else if (passwordET.text.toString() != passwordConfirmET.text.toString()) {
            GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.passwords_dont_match))
            return }

        val intent = Intent(this@AddPatientActivity, AddPatientDietaryActivity::class.java)
        // Prevents duplicating activity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        // Patientdata
        var lastNameWithoutSpaces = lastNameET.text.toString().trim()
        lastNameWithoutSpaces = lastNameWithoutSpaces.replace("\\s".toRegex(), "")

        val selectedRadioButton: RadioButton = binding.activityAddPatientRgGender.findViewById(checkedGenderRadioButtonId)

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
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(customToastBinding, toastAnimation, getString(R.string.get_users_fail))
                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_users))

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
