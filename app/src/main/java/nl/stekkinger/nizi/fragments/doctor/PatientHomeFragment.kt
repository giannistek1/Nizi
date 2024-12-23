package nl.stekkinger.nizi.fragments.doctor


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.doctor.EditPatientActivity
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.databinding.FragmentPatientHomeBinding
import nl.stekkinger.nizi.fragments.BaseFragment
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

class PatientHomeFragment : BaseFragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!

    private val dietaryRepository: DietaryRepository = DietaryRepository()
    private val foodRepository: FoodRepository = FoodRepository()

    // For activity result
    private val EDIT_PATIENT_REQUEST_CODE = 0

    private lateinit var selectedFirstDayOfWeek: Date
    private lateinit var selectedLastDayOfWeek: Date
    private lateinit var patientData: PatientData
    private lateinit var weightUnits: ArrayList<WeightUnit>
    private lateinit var consumptions: ArrayList<ConsumptionResponse>
    private var supplements: ArrayList<Int> = arrayListOf()

    private val sdf = GeneralHelper.getDateFormat()
    private val sdfDB = GeneralHelper.getCreateDateFormat()

    private lateinit var loader: View

    // Gets called one time, you CANT use view references in here
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //val view: View = inflater.inflate(R.layout.fragment_patient_home, container, false)
        loader = binding.fragmentPatientHomeLoader

//        // Setup custom toast (gives error leak)
//        val parent: RelativeLayout = binding.fragmentPatientHomeRl
//        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
//        parent.addView(toastView)

        // Get patient data from bundle
        val bundle: Bundle? = this.arguments

        if (bundle != null) {
            patientData = bundle.getSerializable(GeneralHelper.EXTRA_PATIENT) as PatientData

//            if (GeneralHelper.isAdmin())
                weightUnits = LocalDb.weightUnits
//            else
//                weightUnits = GeneralHelper.getWeightUnitHolder()!!.weightUnits

            // Header
            val fullName = "${patientData.user.first_name} ${patientData.user.last_name}"
            binding.fragmentPatientHomeAverageOfPatient.text =
                getString(R.string.average_of, fullName)
        }

        // Edit Button
        binding.fragmentPatientHomeBtnEdit.setOnClickListener {
            val intent = Intent(activity, EditPatientActivity::class.java)

            // Prevents multiple activities
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            intent.putExtra(GeneralHelper.EXTRA_PATIENT, patientData)
            intent.putExtra(GeneralHelper.EXTRA_DOCTOR_ID, intent.getIntExtra(GeneralHelper.EXTRA_DOCTOR_ID, 1))
            startActivityForResult(intent, EDIT_PATIENT_REQUEST_CODE)
        }

        // Preparation
        // Get current date
        val calendar: Calendar = Calendar.getInstance()
        // Clear time from date
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // Get first day of week and save it
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val currentFirstDayOfWeek = calendar.time
        selectedFirstDayOfWeek = currentFirstDayOfWeek

        // Get last day of week and save it
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek+6);
        val currentLastDayOfWeek = calendar.time
        selectedLastDayOfWeek = currentLastDayOfWeek

        binding.fragmentPatientHomeBtnPreviousWeek.setOnClickListener {
            // Set calendar date to first day of selected week,  substract 7, get date
            calendar.time = selectedFirstDayOfWeek
            calendar.add(Calendar.DATE, -7)
            selectedFirstDayOfWeek = calendar.time

            calendar.time = selectedLastDayOfWeek
            calendar.add(Calendar.DATE, -7)
            selectedLastDayOfWeek = calendar.time

            // Update UI
            binding.fragmentPatientHomeWeek.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
            binding.fragmentPatientHomeBtnNextWeek.isEnabled = true
            binding.fragmentPatientHomeBtnNextWeek.isClickable = true
            binding.fragmentPatientHomeBtnNextWeek.alpha = 1f

            getConsumptions()
        }

        binding.fragmentPatientHomeBtnNextWeek.setOnClickListener {
            // Guard
            if (selectedFirstDayOfWeek == currentFirstDayOfWeek) { return@setOnClickListener }

            calendar.time = selectedFirstDayOfWeek
            calendar.add(Calendar.DATE, 7)
            selectedFirstDayOfWeek = calendar.time

            calendar.time = selectedLastDayOfWeek
            calendar.add(Calendar.DATE, 7)
            selectedLastDayOfWeek = calendar.time

            if (selectedFirstDayOfWeek == currentFirstDayOfWeek) {
                // Update UI
                binding.fragmentPatientHomeWeek.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
                binding.fragmentPatientHomeBtnNextWeek.isEnabled = false
                binding.fragmentPatientHomeBtnNextWeek.isClickable = false
                binding.fragmentPatientHomeBtnNextWeek.alpha = 0.2f
            }

            getConsumptions()
        }

        binding.fragmentPatientHomeWeek.text = "${sdf.format(selectedFirstDayOfWeek)} - ${sdf.format(selectedLastDayOfWeek)}"
        binding.fragmentPatientHomeBtnNextWeek.isEnabled = false
        binding.fragmentPatientHomeBtnNextWeek.isClickable = false
        binding.fragmentPatientHomeBtnNextWeek.alpha = 0.2f

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        getConsumptions()
    }

    fun refreshGuidelines()
    {
        // Fragment patient home ll guidelines might be null because activity + fragment refresh after editing patient
        if (patientData != null && binding.fragmentPatientHomeLlGuidelines != null) {
            GuidelinesHelper.initializeGuidelines(activity, binding.fragmentPatientHomeLlGuidelines, patientData.diets)
        }
    }

    //region Get Consumptions
    inner class getConsumptionsAsyncTask() : AsyncTask<Void, Void, ArrayList<ConsumptionResponse>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<ConsumptionResponse>? {
            try {
                val calendar = Calendar.getInstance()
                calendar.time = selectedFirstDayOfWeek
                calendar.add(Calendar.DATE, 7)
                val firstDayOfNextWeek = calendar.time
                return foodRepository.getConsumptionsByRange(startDate = sdfDB.format(selectedFirstDayOfWeek),
                    endDate = sdfDB.format(firstDayOfNextWeek), patientId = patientData.patient.id)
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<ConsumptionResponse>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
//            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
//            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_consumptions_fail))
//                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_consumptions));

            if (result != null) {
                consumptions = result
            }

            supplements.clear()

            // TODO: Should be based on amount of dietaryRestrictions
            for (i in 0..5)
                supplements.add(0)

            consumptions.forEach {
                supplements[0] += (it.food_meal_component.kcal).roundToInt()
                supplements[1] += (it.food_meal_component.water).roundToInt()
                supplements[2] += (it.food_meal_component.sodium * 1000).roundToInt()
                supplements[3] += (it.food_meal_component.potassium * 1000).roundToInt()
                supplements[4] += (it.food_meal_component.protein).roundToInt()
                supplements[5] += (it.food_meal_component.fiber).roundToInt()
            }

            val calendar: Calendar = Calendar.getInstance()
            //val dayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK) // Makes Sunday day 1
            val dayOfWeek: Int = Calendar.DAY_OF_WEEK

            supplements.forEachIndexed { index, element ->
                supplements[index] = (element.toFloat()/dayOfWeek).roundToInt()
            }

            getDietaryManagementsAsyncTask().execute()
        }
    }
    //endregion

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
//            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_dietary_fail))
//                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_dietary))

            val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

            result?.forEachIndexed { _, resultDietary ->
                if (!resultDietary.is_active) return@forEachIndexed

                var index = 0 // Kcal
                if (resultDietary.dietary_restriction.description.contains("Vocht"))
                    index = 1
                else if (resultDietary.dietary_restriction.description.contains("Natrium"))
                    index = 2
                else if (resultDietary.dietary_restriction.description.contains("Kalium"))
                    index = 3
                else if (resultDietary.dietary_restriction.description.contains("Eiwit"))
                    index = 4
                else if (resultDietary.dietary_restriction.description.contains("Vezel"))
                    index = 5

                val dietaryGuideline =
                    DietaryGuideline(
                        id = resultDietary.id!!,
                        description = resultDietary.dietary_restriction.description,
                        plural = resultDietary.dietary_restriction.plural,
                        minimum = resultDietary.minimum, maximum = resultDietary.maximum, amount = supplements[index],
                        weightUnit = ""
                    )

                val weightUnit = weightUnits.find { it.id == resultDietary.dietary_restriction.weight_unit }
                dietaryGuideline.weightUnit = weightUnit!!.short

                dietaryGuidelines.add(dietaryGuideline)
            }

            // Save dietaries for editPage
            patientData.diets = dietaryGuidelines

            refreshGuidelines()
        }
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_PATIENT_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            // refresh activity
            requireActivity().recreate()

        }
    }

    //region Get Consumptions
    private fun getConsumptions() {
        if (GeneralHelper.isAdmin()) {
            getConsumptionsMockup()
        } else {
            // Check internet connection
//            if (!GeneralHelper.hasInternetConnection(requireContext(), toastView, toastAnimation)) return

            getConsumptionsAsyncTask().execute()
        }
    }

    //region Mockups
    private fun getConsumptionsMockup() {
        consumptions = LocalDb.getRandomConsumptionResponses(5)

        supplements.clear()

        // TODO: Should be based on amount of dietaryRestrictions
        for (i in 0..5)
            supplements.add(0)

        consumptions.forEach {
            supplements[0] += (it.food_meal_component.kcal).roundToInt()
            supplements[1] += (it.food_meal_component.water).roundToInt()
            supplements[2] += (it.food_meal_component.sodium * 1000).roundToInt()
            supplements[3] += (it.food_meal_component.potassium * 1000).roundToInt()
            supplements[4] += (it.food_meal_component.protein).roundToInt()
            supplements[5] += (it.food_meal_component.fiber).roundToInt()
        }

        val calendar: Calendar = Calendar.getInstance()
        //val dayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK) // Makes Sunday day 1
        val dayOfWeek: Int = Calendar.DAY_OF_WEEK

        supplements.forEachIndexed { index, element ->
            supplements[index] = (element.toFloat()/dayOfWeek).roundToInt()
        }

        getDietaryMockup()
    }

    private fun getDietaryMockup() {
        val dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()

        LocalDb.dietaryManagements.forEachIndexed { _, resultDietary ->
            if (!resultDietary.is_active) return@forEachIndexed

            var index = 0 // Kcal
            if (resultDietary.dietary_restriction.description.contains("Vocht"))
                index = 1
            else if (resultDietary.dietary_restriction.description.contains("Natrium"))
                index = 2
            else if (resultDietary.dietary_restriction.description.contains("Kalium"))
                index = 3
            else if (resultDietary.dietary_restriction.description.contains("Eiwit"))
                index = 4
            else if (resultDietary.dietary_restriction.description.contains("Vezel"))
                index = 5

            val dietaryGuideline =
                DietaryGuideline(
                    id = resultDietary.id!!,
                    description = resultDietary.dietary_restriction.description,
                    plural = resultDietary.dietary_restriction.plural,
                    minimum = resultDietary.minimum, maximum = resultDietary.maximum, amount = supplements[index],
                    weightUnit = ""
                )

            val weightUnit = weightUnits.find { it.id == resultDietary.dietary_restriction.weight_unit }
            dietaryGuideline.weightUnit = weightUnit!!.short

            dietaryGuidelines.add(dietaryGuideline)
        }

        // Save dietaries for editPage
        patientData.diets = dietaryGuidelines

        refreshGuidelines()
    }
    //endregion
}
