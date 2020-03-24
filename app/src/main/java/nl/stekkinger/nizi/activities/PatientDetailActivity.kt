package nl.stekkinger.nizi.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_patient_detail.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryGuideline
import nl.stekkinger.nizi.classes.DietaryView
import nl.stekkinger.nizi.classes.PatientItem
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass
import nl.stekkinger.nizi.repositories.DietaryRepository

class PatientDetailActivity : AppCompatActivity() {

    private val dietaryRepository: DietaryRepository = DietaryRepository()

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    // for activity result
    private val REQUEST_CODE = 0

    private lateinit var patient: PatientItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_patient_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        patient = intent.extras?.get("PATIENT") as PatientItem

        activity_patient_detail_average_of_patient.text = "${getString(R.string.average_of)} ${patient.name}"

        activity_patient_detail_btn_edit.setOnClickListener {
            val intent = Intent(this@PatientDetailActivity, EditPatientActivity::class.java)
            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("PATIENT", patient)
            intent.putExtra(EXTRA_DOCTOR_ID, intent.getIntExtra("DOCTOR_ID", 3))
            startActivityForResult(intent, REQUEST_CODE)
        }

        getDietaryAsyncTask().execute()
    }

    //region Get Dietary
    inner class getDietaryAsyncTask() : AsyncTask<Void, Void, DietaryView>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            //progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryView? {
            return dietaryRepository.getDietary(intent.getIntExtra("PATIENT_ID", 56))
        }

        override fun onPostExecute(result: DietaryView?) {
            super.onPostExecute(result)

            val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

            if (result != null) {
                result.Dietarymanagement.forEachIndexed { index, resultDietary ->

                    lateinit var dietaryGuideline: DietaryGuideline

                    dietaryGuideline = DietaryGuideline(
                        resultDietary.Description,
                        0, 0, 0
                    )

                    var alreadyExists = false

                    // Check if dietaryGuideLines already has the food supplement type (e.g. calories)
                    dietaryGuidelines.forEachIndexed loop@{ i, dietary ->
                        if (dietary.description.contains(resultDietary.Description.take(3))) {
                            dietaryGuideline = dietary
                            alreadyExists = true
                            return@loop
                        }
                    }

                    if (resultDietary.Description.contains("beperking")) {
                        dietaryGuideline.minimum = resultDietary.Amount
                    } else if (resultDietary.Description.contains("verrijking")) {
                        dietaryGuideline.description =
                            dietaryGuideline.description.replace("beperking", "")
                        dietaryGuideline.maximum = resultDietary.Amount
                    }

                    if (!alreadyExists) // create new
                        dietaryGuidelines.add(dietaryGuideline)
                    else // update
                        dietaryGuidelines[index - 1] = dietaryGuideline
                }

                val helperClass = GuidelinesHelperClass()
                helperClass.initializeGuidelines(
                    this@PatientDetailActivity,
                    activity_patient_detail_ll_guidelines,
                    dietaryGuidelines
                )

                // Progressbar
                //progressBar.visibility = View.GONE
            }
        }
    }
    //endregion
}
