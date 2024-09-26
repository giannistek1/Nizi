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
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.databinding.ActivityAddPatientBinding
import nl.stekkinger.nizi.databinding.ToolbarBinding
import nl.stekkinger.nizi.repositories.AuthRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class EditPatientActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPatientBinding
    private lateinit var toolbarBinding: ToolbarBinding

    private var TAG = "EditPatient"

    // For activity result
    private val REQUEST_CODE = 0

    private val authRepository: AuthRepository = AuthRepository()

    private var doctorId: Int? = null
    private lateinit var patientData: PatientData
    private lateinit var date: Date
    private var users: ArrayList<UserLogin> = arrayListOf()
    private lateinit var originalEmail: String

    val sdf = GeneralHelper.getFeedbackDateFormat()
    val sdfDB = GeneralHelper.getCreateDateFormat()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI Same layout as add patient
        binding = ActivityAddPatientBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(toolbarBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBinding.toolbarTxtBack.text = getString(R.string.patient_overview)
        loader = binding.activityAddPatientProgressbar

        // Setup custom toast
        val parent: RelativeLayout = binding.activityAddPatientRl
        //oastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(customToastLayout)

        // Fill patient
        if (intent.extras != null) {
            fillInPatient()
        }

        binding.activityAddPatientBtnToGuidelines.setOnClickListener {
            tryEditPatient(binding.activityAddPatientEtFirstName, binding.activityAddPatientEtLastName,
                binding.activityAddPatientEtDob, binding.activityAddPatientEtEmail, binding.activityAddPatientEtPassword,
                binding.activityAddPatientEtPasswordConfirm)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Save current Email
        originalEmail = binding.activityAddPatientEtEmail.text.toString()

        // Standard on canceled
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(this, customToastBinding, toastAnimation)) return

        getUsers().execute()
    }

    //region tryEditPatient
    private fun tryEditPatient(firstNameET: EditText, lastNameET: EditText, dobET: EditText, emailET:
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

        val checkedGenderRadioButtonId: Int = binding.activityAddPatientRgGender.checkedRadioButtonId
        if (checkedGenderRadioButtonId == -1) { return }

        // Check if email already exists and isnt original email
        val user = users.find { it.email ==  emailET.text.toString() }
        if (user != null && user.email != originalEmail) {
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

        // Update fields with new inputs once you click btn
        patientData.user.first_name = firstNameET.text.toString().trim()
        patientData.user.last_name = lastNameET.text.toString().trim()
        patientData.patient.date_of_birth = sdfDB.format(date)
        patientData.user.email = emailET.text.toString().trim()
        //patientData.user.password = passwordConfirmET.text.toString().trim()
        //model.patient.weight = activity_add_patient_et_weight.text.toString().toFloat()

        val selectedRadioButton: RadioButton = binding.activityAddPatientRgGender.findViewById(checkedGenderRadioButtonId)
        patientData.patient.gender = selectedRadioButton.text.toString()

        val intent = Intent(this@EditPatientActivity, EditPatientDietaryActivity::class.java)

        // Prevents duplicating activity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        // Give patient, original Email and doctor ID
        intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
        intent.putExtra(GeneralHelper.EXTRA_ORIGINAL_EMAIL, originalEmail)
        intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, doctorId)

        // Start intent
        startActivityForResult(intent, REQUEST_CODE)
    }
    //endregion

    //region fillInPatient
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillInPatient() {
        patientData = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientData

        binding.activityAddPatientEtFirstName.setText(patientData.user.first_name)
        binding.activityAddPatientEtLastName.setText(patientData.user.last_name)
        binding.activityAddPatientEtEmail.setText(patientData.user.email)

        // Date of Birth
        date = SimpleDateFormat("yyyy-MM-dd").parse(patientData.patient.date_of_birth)
        val calendar = Calendar.getInstance()
        calendar.time = date
        binding.activityAddPatientEtDob.setText("${sdf.format(date)}")

        binding.activityAddPatientEtDob.setOnClickListener {

            if (binding.activityAddPatientDp.visibility == View.VISIBLE)
                binding.activityAddPatientDp.visibility = View.GONE
            else
                binding.activityAddPatientDp.visibility = View.VISIBLE
        }

        binding.activityAddPatientDp.maxDate = Date().time
        binding.activityAddPatientDp.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        binding.activityAddPatientDp.setOnDateChangedListener { _, mYear, mMonth, mDay ->

            calendar.set(mYear, mMonth, mDay, 0, 0)
            date = calendar.time

            binding.activityAddPatientEtDob.setText("${sdf.format(date)}")
        }

        // Hide password
        binding.activityAddPatientTxtPassword.visibility = View.GONE
        binding.activityAddPatientLlPassword.visibility = View.GONE
        binding.activityAddPatientTxtPasswordConfirm.visibility = View.GONE
        binding.activityAddPatientLlPasswordConfirm.visibility = View.GONE

        if (patientData.patient.gender == binding.activityAddPatientRbMale.text.toString())
            binding.activityAddPatientRgGender.check(binding.activityAddPatientRbMale.id)
        else
            binding.activityAddPatientRgGender.check(binding.activityAddPatientRbFemale.id)
    }
    //endregion

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
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_users));

            users = result
        }
    }
    //endregion

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // In case we wanna return something
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        // If came back from dietary screen
        else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            fillInPatient()
            //originalEmail = intent.extras!!.getString(GeneralHelper.EXTRA_ORIGINAL_EMAIL, "")
        }
    }
}
