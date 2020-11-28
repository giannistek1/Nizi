package nl.stekkinger.nizi.activities.doctor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_add_patient.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.BaseActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.patient.PatientData
import java.text.SimpleDateFormat
import java.util.*

class EditPatientActivity : BaseActivity() {

    private var TAG = "EditPatient"

    // For activity result
    private val REQUEST_CODE = 0

    private var doctorId: Int? = null
    private lateinit var patientData: PatientData
    private lateinit var date: Date

    val sdf = GeneralHelper.getFeedbackDateFormat()
    val sdfDB = GeneralHelper.getCreateDateFormat()

    private lateinit var loader: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI Same layout as add patient
        setContentView(R.layout.activity_add_patient)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_txt_back.text = getString(R.string.patient_overview)
        loader = activity_add_patient_progressbar

        // Fill patient
        patientData = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientData


        activity_add_patient_et_firstName.setText(patientData.user.first_name)
        activity_add_patient_et_lastName.setText(patientData.user.last_name)

        date = SimpleDateFormat("yyyy-MM-dd").parse(patientData.patient.date_of_birth)
        val calendar = Calendar.getInstance()
        calendar.time = date
        activity_add_patient_et_dob.setText("${sdf.format(date)}")
        activity_add_patient_et_email.setText(patientData.user.email)

        // Date of Birth
        activity_add_patient_et_dob.setOnClickListener {

            if (activity_add_patient_dp.visibility == View.VISIBLE)
                activity_add_patient_dp.visibility = View.GONE
            else
                activity_add_patient_dp.visibility = View.VISIBLE
        }

        // Date of Birth
        activity_add_patient_dp.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        activity_add_patient_dp.setOnDateChangedListener { _, mYear, mMonth, mDay ->

            calendar.set(mYear, mMonth, mDay, 0, 0)
            date = calendar.time

            activity_add_patient_et_dob.setText("${sdf.format(date)}")
        }

        // Hide password
        activity_add_patient_txt_password.visibility = View.GONE
        activity_add_patient_ll_password.visibility = View.GONE
        activity_add_patient_txt_password_confirm.visibility = View.GONE
        activity_add_patient_ll_passwordConfirm.visibility = View.GONE

        if (patientData.patient.gender == activity_add_patient_rb_male.text.toString())
            activity_add_patient_rg_gender.check(activity_add_patient_rb_male.id)
        else
            activity_add_patient_rg_gender.check(activity_add_patient_rb_female.id)

        activity_add_patient_btn_to_guidelines.setOnClickListener {

            tryEditPatient(activity_add_patient_et_firstName, activity_add_patient_et_lastName,
                activity_add_patient_et_dob, activity_add_patient_et_email, activity_add_patient_et_password,
                activity_add_patient_et_passwordConfirm)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 0)

        // Standard on canceled
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
    }

    private fun tryEditPatient(firstNameET: EditText, lastNameET: EditText, dobET: EditText, emailET:
    EditText, passwordET: EditText, passwordConfirmET: EditText) {
        // Guards/Checks
        if (InputHelper.inputIsEmpty(this, firstNameET, R.string.empty_first_name)) return
        if (InputHelper.inputIsEmpty(this, lastNameET, R.string.empty_last_name)) return
        if (InputHelper.inputIsEmpty(this, dobET, R.string.empty_date_of_birth)) return
        if (InputHelper.inputIsEmpty(this, emailET, R.string.empty_email)) return

        val checkedGenderRadioButtonId: Int = activity_add_patient_rg_gender.checkedRadioButtonId
        if (checkedGenderRadioButtonId == -1) { return }

        // Check if not matching passwords
        else if (passwordET.text.toString() != passwordConfirmET.text.toString()) {
            Toast.makeText(baseContext, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show(); return }

        // Update fields with new inputs once you click btn
        patientData.user.first_name = firstNameET.text.toString().trim()
        patientData.user.last_name = lastNameET.text.toString().trim()
        patientData.patient.date_of_birth = dobET.text.toString().trim()
        patientData.user.email = emailET.text.toString().trim()
        patientData.user.password = passwordConfirmET.text.toString().trim()
        //model.patient.weight = activity_add_patient_et_weight.text.toString().toFloat()

        val selectedRadioButton: RadioButton = activity_add_patient_rg_gender.findViewById(checkedGenderRadioButtonId)
        patientData.patient.gender = selectedRadioButton.text.toString()

        val intent = Intent(this@EditPatientActivity, EditPatientDietaryActivity::class.java)

        // Prevents duplicating activity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        // Give patient and doctor ID
        intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
        intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, doctorId)

        // Start intent
        startActivityForResult(intent, REQUEST_CODE)
    }

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
