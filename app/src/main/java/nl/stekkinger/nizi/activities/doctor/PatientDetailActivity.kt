package nl.stekkinger.nizi.activities.doctor

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_patient_detail.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.PatientRepository
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class PatientDetailActivity : AppCompatActivity() {

    private val patientRepository: PatientRepository = PatientRepository()
    private val dietaryRepository: DietaryRepository = DietaryRepository()

    // For activity result
    private val REQUEST_CODE = 0

    private lateinit var patientData: PatientData
    private var weekNumber by Delegates.observable(0) { property, oldValue, newValue ->
        activity_patient_detail_week.setText("Week ${newValue}")
    }

    private lateinit var loader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup UI
        setContentView(R.layout.activity_patient_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        loader = activity_patient_detail_loader

        // Set week
        val calendar: Calendar = Calendar.getInstance()
        weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        activity_patient_detail_week.setText("Week ${weekNumber}")

        activity_patient_detail_btn_previousWeek.setOnClickListener {
            if (weekNumber > 1)
                weekNumber--
        }

        activity_patient_detail_btn_nextWeek.setOnClickListener {
            if (weekNumber < 53)
                weekNumber++
        }


        val patientItem = intent.extras?.get(GeneralHelper.EXTRA_PATIENT) as PatientItem

        patientData = PatientData(
            patient = PatientLogin(id = patientItem.patient_id, date_of_birth = patientItem.date_of_birth,
                gender = patientItem.gender, doctor = patientItem.doctor_id),

            user = User(first_name = patientItem.first_name, last_name = patientItem.last_name,
                username = patientItem.username, email = patientItem.email, role = patientItem.role),

            diets = arrayListOf()
        )

        val fullName = "${patientData.user.first_name} ${patientData.user.last_name}"
        activity_patient_detail_average_of_patient.text = "${getString(R.string.average_of)} ${fullName}"


        activity_patient_detail_btn_edit.setOnClickListener {
            val intent = Intent(this@PatientDetailActivity, EditPatientActivity::class.java)

            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
            intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 1))
            startActivityForResult(intent, REQUEST_CODE)
        }

        getDietaryManagementsAsyncTask().execute()
    }

    // Double in Main but this one is for the week and is an average
    //region getDietary
    inner class getDietaryManagementsAsyncTask() : AsyncTask<Void, Void, ArrayList<DietaryManagement>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<DietaryManagement>? {
            return dietaryRepository.getDietaryManagements(patientData.patient.id)
        }

        override fun onPostExecute(result: ArrayList<DietaryManagement>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.get_dietary_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(baseContext, R.string.fetched_dietary, Toast.LENGTH_SHORT).show()

            val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

            result.forEachIndexed { index, resultDietary ->

                lateinit var dietaryGuideline: DietaryGuideline

                // fill in restriction and make dietaryGuideline
                val restriction = resultDietary.dietary_restriction.description.replace("beperking", "").replace("verrijking","")
                dietaryGuideline =
                    DietaryGuideline(
                        resultDietary.dietary_restriction.description,
                        restriction,
                        restriction.toLowerCase(),
                        0,
                        0,
                        0,
                        ""
                    )

                var alreadyExists = false

                // Check if dietaryGuideLines already has the food supplement type (e.g. calories)
                // by checking the first four letters
                dietaryGuidelines.forEachIndexed loop@{ _, dietary ->
                    if (dietary.description.contains(resultDietary.dietary_restriction.description.take(4))) {
                        dietaryGuideline = dietary
                        alreadyExists = true
                        return@loop
                    }
                }

                // Plurals
                if (resultDietary.dietary_restriction.description.contains("Calorie"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "en"
                else if (resultDietary.dietary_restriction.description.contains("Eiwit"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "ten"
                else if (resultDietary.dietary_restriction.description.contains("Vezel"))
                    dietaryGuideline.plural = dietaryGuideline.restriction.toLowerCase() + "s"

                // Fill in minimum/maximum
                if (resultDietary.dietary_restriction.description.contains("beperking"))
                    dietaryGuideline.minimum = resultDietary.amount
                else if (resultDietary.dietary_restriction.description.contains("verrijking"))
                    dietaryGuideline.maximum = resultDietary.amount

                // fill in Weight unit & plural
                if (resultDietary.dietary_restriction.description.contains("Calorie"))
                    dietaryGuideline.weightUnit = getString(R.string.kcal)
                else if (resultDietary.dietary_restriction.description.contains("Natrium") || resultDietary.dietary_restriction.description.contains("Kalium"))
                    dietaryGuideline.weightUnit = getString(R.string.milligram_short)
                else if (resultDietary.dietary_restriction.description.contains("Eiwit") || resultDietary.dietary_restriction.description.contains("Vezel"))
                    dietaryGuideline.weightUnit = getString(R.string.gram)
                else if (resultDietary.dietary_restriction.description.contains("Vocht"))
                    dietaryGuideline.weightUnit = getString(R.string.milliliter_short)

                // Add to or update list of dietaries
                if (!alreadyExists) // create new
                    dietaryGuidelines.add(dietaryGuideline)
                else // update
                    dietaryGuidelines[index - 1] = dietaryGuideline
            }

            // Save dietaries for editPage
            patientData.diets = dietaryGuidelines

            GuidelinesHelper.initializeGuidelines(
                this@PatientDetailActivity,
                activity_patient_detail_ll_guidelines,
                dietaryGuidelines
            )

            // get patient
            getPatientAsyncTask().execute()
        }
    }
    //endregion

    //region getPatient
    inner class getPatientAsyncTask() : AsyncTask<Void, Void, Patient>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Patient? {
            return patientRepository.getPatient(patientData.patient.id)
        }

        override fun onPostExecute(result: Patient?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(baseContext, R.string.get_patient_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(baseContext, R.string.fetched_patient, Toast.LENGTH_SHORT).show()

            // Make PatientData because patient does not have descriptions of dietaryRestrictions
            patientData.user = result.user!!
            patientData.patient = PatientLogin(
                id = result.id!!,
                gender = result.gender,
                date_of_birth = result.date_of_birth,
                doctor = result.doctor.id!!,
                user = result.user.id,
                feedbacks = result.feedbacks,
                dietary_managements = arrayListOf(), // Empty because we use a list of DietaryViews for everything
                my_foods = result.my_foods,
                consumptions = result.consumptions
            )
        }
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // TODO: Only do this when you do succesful patient change in editingView
            // refresh activity
            recreate()
        }
    }
}
