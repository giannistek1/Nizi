package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.patient.AddPatientViewModel
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.classes.login.User

class AddPatientActivity : AppCompatActivity() {

    private var TAG = "AddPatient"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    // for activity result
    private val REQUEST_CODE = 0

    private lateinit var progressBar: View

    private var doctorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        progressBar = activity_add_patient_progressbar

        activity_add_patient_btn_to_guidelines.setOnClickListener {
            tryCreatePatient(activity_add_patient_et_firstName, activity_add_patient_et_lastName,
                activity_add_patient_et_dob, activity_add_patient_et_email, activity_add_patient_et_password,
            activity_add_patient_et_password_confirm)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)

        // Testing
        activity_add_patient_et_firstName.setText("Jaap")
        activity_add_patient_et_lastName.setText("van Steen")
        activity_add_patient_et_dob.setText("01/01/1984")
        activity_add_patient_et_email.setText("jaapvansteen@hotmail.com")
        activity_add_patient_et_password.setText("Welkom123")
        activity_add_patient_et_password_confirm.setText("Welkom123")
    }

    private fun tryCreatePatient(firstNameET: EditText, lastNameET: EditText, dobET: EditText, emailET:
    EditText, passwordET: EditText, passwordConfirmET: EditText) {
        // Checks
        if (InputHelper.inputIsEmpty(this, firstNameET, R.string.empty_first_name)) return
        else if (InputHelper.inputIsEmpty(this, lastNameET, R.string.empty_last_name)) return
        else if (InputHelper.inputIsEmpty(this, dobET, R.string.empty_date_of_birth)) return
        else if (InputHelper.inputIsEmpty(this, emailET, R.string.empty_email)) return
        else if (InputHelper.inputIsEmpty(this, passwordET, R.string.empty_password)) return
        else if (InputHelper.inputIsEmpty(this, passwordConfirmET, R.string.empty_password_confirm)) return

        // Check if not matching passwords
        else if (passwordET.text.toString() != passwordConfirmET.text.toString())
        {
            Toast.makeText(baseContext, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this@AddPatientActivity, AddPatientDietaryActivity::class.java)
        // Prevents duplicating activivity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        // Patientdata
        var lastNameWithoutSpaces = lastNameET.text.toString().trim()
        lastNameWithoutSpaces = lastNameWithoutSpaces.replace("\\s".toRegex(), "")

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
            patient = PatientLogin(
                gender = "Man",
                date_of_birth = dobET.text.toString().trim(),
                doctor = doctorId!!
            )
        )

        // Give patient and doctor ID
        intent.putExtra("PATIENT", newPatient)
        intent.putExtra(EXTRA_DOCTOR_ID, doctorId!!)
        // Start intent
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun finish() {
        // In case we wanna return something
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }
}
