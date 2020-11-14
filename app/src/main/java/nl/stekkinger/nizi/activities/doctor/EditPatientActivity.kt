package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_patient.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.patient.UpdatePatientViewModel

class EditPatientActivity : AppCompatActivity() {

    private var TAG = "EditPatient"

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    // for activity result
    private val REQUEST_CODE = 0

    private lateinit var progressBar: View

    private var doctorId: Int? = null
    private lateinit var model: UpdatePatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_add_patient)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        progressBar = activity_add_patient_progressbar

        // Fill patient

        model = intent.extras?.get("PATIENT") as UpdatePatientViewModel

        activity_add_patient_et_firstName.setText(model.patient.firstName)
        activity_add_patient_et_lastName.setText(model.patient.lastName)
        activity_add_patient_et_dob.setText(model.patient.dateOfBirth)
        //activity_add_patient_et_weight.setText(model.patient.weight.toString())
        activity_add_patient_et_email.setText("***@***.**")
        activity_add_patient_et_password.setText("******")
        activity_add_patient_et_password_confirm.setText("******")

        activity_add_patient_btn_to_guidelines.setOnClickListener {
            // Checks
            if (activity_add_patient_et_firstName.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_first_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_lastName.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_last_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_dob.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_date_of_birth, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_email.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_email, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_password.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_password, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_password_confirm.text.toString() == "") {
                Toast.makeText(baseContext, R.string.empty_password_confirm, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else if (activity_add_patient_et_password.text.toString() != activity_add_patient_et_password_confirm.text.toString())
            {
                Toast.makeText(baseContext, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this@EditPatientActivity, EditPatientDietaryActivity::class.java)
            // Prevents duplicating activivity
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            // Patientdata
            /*val newPatient = AddPatientViewModel(
                activity_add_patient_et_firstName.text.toString().trim(),
                activity_add_patient_et_lastName.text.toString().trim(),
                activity_add_patient_et_dob.text.toString().trim(),
                activity_add_patient_et_weight.text.toString().toFloat(),
                "Man"
            )*/
            // Update fields with new inputs once you click btn
            model.patient.firstName = activity_add_patient_et_firstName.text.toString().trim()
            model.patient.lastName = activity_add_patient_et_lastName.text.toString().trim()
            model.patient.dateOfBirth = activity_add_patient_et_dob.text.toString().trim()
            //model.patient.weight = activity_add_patient_et_weight.text.toString().toFloat()

            // Give patient and doctor ID
            intent.putExtra("PATIENT", model)
            intent.putExtra(EXTRA_DOCTOR_ID, doctorId)
            // Start intent
            startActivityForResult(intent, REQUEST_CODE)
        }

        // Get doctorId
        doctorId = intent.getIntExtra(EXTRA_DOCTOR_ID, 0)
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
