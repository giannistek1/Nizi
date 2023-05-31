package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.Mockup
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HomeFragment: BaseFragment() {
    private val dietaryRepository: DietaryRepository = DietaryRepository()
    private val foodRepository: FoodRepository = FoodRepository()

    private lateinit var user: UserLogin
    private lateinit var weightUnits: ArrayList<WeightUnit>
    private lateinit var consumptions: ArrayList<ConsumptionResponse>
    private var dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()
    private var supplements: ArrayList<Int> = arrayListOf()
    private lateinit var mCurrentDate: Date
    private lateinit var mEndDate: Date

    private val sdf = GeneralHelper.getDateFormat()
    private val sdfDB = GeneralHelper.getCreateDateFormat()

    private lateinit var loader: View
    private lateinit var linearLayoutGuidelines: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        loader = view.fragment_home_loader
        linearLayoutGuidelines = view.fragment_home_ll_guidelines

        // Setup custom toast
        val parent: RelativeLayout = view.fragment_home_rl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        // WeightUnits
        weightUnits = if (GeneralHelper.isAdmin()) {
            Mockup.weightUnits;
        } else {
            val gson = Gson()
            val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
            val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)
            weightUnitHolder.weightUnits
        }

        user = GeneralHelper.getUser()

        val calendar: Calendar = Calendar.getInstance()
        // set the calendar to start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        mCurrentDate = calendar.time
        val today = calendar.time
        calendar.add(Calendar.DATE, 1)
        mEndDate = calendar.time

        calendar.time = today
        calendar.add(Calendar.DATE, -1)
        val yesterday = calendar.time

        view.fragment_home_btn_yesterday.setOnClickListener {
            calendar.time = mCurrentDate
            calendar.add(Calendar.DATE, -1)
            val newDate = calendar.time

            mCurrentDate = newDate
            calendar.add(Calendar.DATE, 1)
            mEndDate = calendar.time

            if(newDate == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = sdf.format(mCurrentDate)
            }

            getConsumptions();

            // Update UI
            view.fragment_home_btn_tomorrow.isEnabled = true
            view.fragment_home_btn_tomorrow.isClickable = true
            view.fragment_home_btn_tomorrow.alpha = 1f
        }

        view.fragment_home_btn_tomorrow.setOnClickListener {
            calendar.time = mCurrentDate
            calendar.add(Calendar.DATE, 1)
            val newDate = calendar.time

            // Guard: if new date after today
            if (newDate.after(today)) return@setOnClickListener

            mCurrentDate = newDate
            calendar.add(Calendar.DATE, 1)
            mEndDate = calendar.time

            if (newDate == today) {
                // Update UI
                view.fragment_home_txt_day.text = getString(R.string.today)
                view.fragment_home_btn_tomorrow.isEnabled = false
                view.fragment_home_btn_tomorrow.isClickable = false
                view.fragment_home_btn_tomorrow.alpha = 0.2f
            } else if(newDate == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = sdf.format(mCurrentDate)
            }

            getConsumptions()
        }

        // Update UI
        view.fragment_home_txt_day.text = getString(R.string.today)
        view.fragment_home_btn_tomorrow.isEnabled = false
        view.fragment_home_btn_tomorrow.isClickable = false
        view.fragment_home_btn_tomorrow.alpha = 0.2f

        getConsumptions()

        return view
    }

    fun refreshGuidelines()
    {
        // Guard
        if (dietaryGuidelines == null) { return }

        GuidelinesHelper.initializeGuidelines(activity, linearLayoutGuidelines, dietaryGuidelines)
    }

    //region Get Consumptions
    private fun getConsumptions() {
        if (GeneralHelper.isAdmin()) {
            getConsumptionsMockup()
        } else {
            getConsumptionsAsyncTask().execute()
        }
    }
    inner class getConsumptionsAsyncTask() : AsyncTask<Void, Void, ArrayList<ConsumptionResponse>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<ConsumptionResponse>? {
            return try {
                foodRepository.getConsumptionsByRange(sdfDB.format(mCurrentDate), sdfDB.format(mEndDate),
                    patientId = user.patient!!.id)
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
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_consumptions_fail))
                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_consumptions))

            consumptions = result

            supplements.clear()

            // Should be based on amount of dietaryRestrictions
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

            getDietaryAsyncTask().execute()
        }
    }
    //endregion

    //region Get Dietary
    inner class getDietaryAsyncTask() : AsyncTask<Void, Void, ArrayList<DietaryManagement>>()
    {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<DietaryManagement>? {
            return try {
                dietaryRepository.getDietaryManagements(user.patient!!.id)
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<DietaryManagement>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_dietary_fail))
                return }

            // Feedback
            //GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_dietary))

            dietaryGuidelines.clear()

            result.forEachIndexed { _, resultDietary ->
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

                val dietaryGuideline = DietaryGuideline(
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

            refreshGuidelines()
        }
    }
    //endregion

    //region Mockups
    private fun getConsumptionsMockup() {
        consumptions = Mockup.getRandomConsumptionResponses(5)

        supplements.clear()

        // Should be based on amount of dietaryRestrictions
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

        getDietaryMockup()
    }

    private fun getDietaryMockup() {
        dietaryGuidelines.clear()

        Mockup.dietaryManagements.forEachIndexed { _, resultDietary ->
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

            val dietaryGuideline = DietaryGuideline(
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

        refreshGuidelines()
    }
    //endregion
}