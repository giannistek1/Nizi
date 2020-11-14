package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_patient_detail.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.patient.UpdatePatientViewModel
import nl.stekkinger.nizi.repositories.DietaryRepository

class PatientDetailActivity : AppCompatActivity() {

    private val dietaryRepository: DietaryRepository = DietaryRepository()

    val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    // for activity result
    private val REQUEST_CODE = 0

    private lateinit var model: UpdatePatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup UI
        setContentView(R.layout.activity_patient_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        model = UpdatePatientViewModel(
            intent.extras?.get("PATIENT") as PatientItem, arrayListOf()
        )

        activity_patient_detail_average_of_patient.text = "${getString(R.string.average_of)} ${model.patient.name}"


        activity_patient_detail_btn_edit.setOnClickListener {
            val intent = Intent(this@PatientDetailActivity, EditPatientActivity::class.java)
            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            intent.putExtra("PATIENT", model)
            intent.putExtra(EXTRA_DOCTOR_ID, intent.getIntExtra("DOCTOR_ID", 3))
            startActivityForResult(intent, REQUEST_CODE)
        }

        getDietaryAsyncTask().execute()
    }

    // Double in Main but this one is for the week and is an average
    //region Get Dietary
    inner class getDietaryAsyncTask() : AsyncTask<Void, Void, DietaryView>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            // Progressbar
            //progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): DietaryView? {
            return dietaryRepository.getDietary(model.patient.patientId)
        }

        override fun onPostExecute(result: DietaryView?) {
            super.onPostExecute(result)

            val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

            if (result != null) {
                result.Dietarymanagement.forEachIndexed { index, resultDietary ->

                    lateinit var dietaryGuideline: DietaryGuideline

                    // fill in restriction and make dietaryGuideline
                    val restriction = resultDietary.Description.replace("beperking", "").replace("verrijking","")
                    dietaryGuideline = DietaryGuideline(
                        resultDietary.Description, restriction, restriction.toLowerCase(),
                        0, 0, 0, ""
                    )

                    var alreadyExists = false

                    // Check if dietaryGuideLines already has the food supplement type (e.g. calories)
                    dietaryGuidelines.forEachIndexed loop@{ _, dietary ->
                        if (dietary.description.contains(resultDietary.Description.take(4))) {
                            dietaryGuideline = dietary
                            alreadyExists = true
                            return@loop
                        }
                    }

                    // Plurals
                    if (resultDietary.Description.contains("Calorie"))
                        dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "en"
                    else if (resultDietary.Description.contains("Eiwit"))
                        dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "ten"
                    else if (resultDietary.Description.contains("Vezel"))
                        dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "s"

                    // Fill in minimum/maximum
                    if (resultDietary.Description.contains("beperking"))
                        dietaryGuideline.minimum = resultDietary.Amount
                    else if (resultDietary.Description.contains("verrijking"))
                        dietaryGuideline.maximum = resultDietary.Amount

                    // fill in Weight unit & plural
                    if (resultDietary.Description.contains("Calorie"))
                        dietaryGuideline.weightUnit = getString(R.string.kcal)
                    else if (resultDietary.Description.contains("Natrium") || resultDietary.Description.contains("Kalium"))
                        dietaryGuideline.weightUnit = getString(R.string.milligram_short)
                    else if (resultDietary.Description.contains("Eiwit") || resultDietary.Description.contains("Vezel"))
                        dietaryGuideline.weightUnit = getString(R.string.gram)
                    else if (resultDietary.Description.contains("Vocht"))
                        dietaryGuideline.weightUnit = getString(R.string.milliliter_short)

                    // Add to or update list of dietaries
                    if (!alreadyExists) // create new
                        dietaryGuidelines.add(dietaryGuideline)
                    else // update
                        dietaryGuidelines[index - 1] = dietaryGuideline
                }

                // Save dietaries for editPage
                model.diets = dietaryGuidelines

                GuidelinesHelper.initializeGuidelines(
                    this@PatientDetailActivity,
                    activity_patient_detail_ll_guidelines,
                    dietaryGuidelines
                )

                // Progressbar
                //progressBar.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // refresh activity
            recreate()
        }
    }
}
