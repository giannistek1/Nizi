package nl.stekkinger.nizi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_patient_detail.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass

class PatientDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_patient_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activity_patient_detail_average_of_patient.text = "${getString(R.string.average_of)} ${intent.getStringExtra("NAME")}"

        //val helperClass = GuidelinesHelperClass()
        //helperClass.initializeGuidelines(this, ll_guidelines_content)
    }
}
