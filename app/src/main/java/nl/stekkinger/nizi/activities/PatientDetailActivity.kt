package nl.stekkinger.nizi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_patient_detail.*
import nl.stekkinger.nizi.R

class PatientDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setup UI
        setContentView(R.layout.activity_patient_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activity_patient_detail_dataOfPatient.text = "${getString(R.string.data_of)} ${intent.getStringExtra("NAME")}"
    }
}
