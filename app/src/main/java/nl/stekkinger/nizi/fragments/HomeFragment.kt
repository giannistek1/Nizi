package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.repositories.DietaryRepository
import nl.stekkinger.nizi.repositories.FoodRepository
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HomeFragment: Fragment() {
    private val dietaryRepository: DietaryRepository = DietaryRepository()
    private val foodRepository: FoodRepository = FoodRepository()

    private lateinit var user: UserLogin
    private lateinit var weightUnits: ArrayList<WeightUnit>
    private lateinit var consumptions: ArrayList<ConsumptionResponse>
    private var dietaryGuidelines: ArrayList<DietaryGuideline> = arrayListOf()
    private var supplements: ArrayList<Int> = arrayListOf()
    private lateinit var mCurrentDate: String
    private lateinit var mCurrentDateAsCreateDate: String
    private lateinit var model: DiaryViewModel
    private val sdf = GeneralHelper.getDateFormat()

    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)
        loader = view.fragment_home_loader

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Get dietary list from bundle
        //val bundle: Bundle = this.arguments!!
        //dietaryGuidelines = bundle.getSerializable(GeneralHelper.EXTRA_DIETARY) as ArrayList<DietaryGuideline>

        val gson = Gson()
        val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
        val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)
        weightUnits = weightUnitHolder.weightUnits
        user = GeneralHelper.getUser()

        // Setting date for diary
        mCurrentDate = sdf.format(Date())
        val sdfDB = GeneralHelper.getCreateDateFormat()
        mCurrentDateAsCreateDate = sdfDB.format(Date())
        val startDate: String = getDay(mCurrentDate, 0) // Wat is startDate?
        val endDate: String = getDay(mCurrentDate, 1) // Wat is endDate?
        model.setDiaryDate(startDate + "/" + endDate)

        // Gianni
        val today: String = getDay(mCurrentDate, 0)
        val yesterday: String = getDay(mCurrentDate, -1)

        view.fragment_home_btn_yesterday.setOnClickListener {
            val newDate = sdf.parse(getDay(mCurrentDate, -1))
            mCurrentDate = sdf.format(newDate!!)

            if(sdf.format(newDate) == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = mCurrentDate
            }

            refreshGuidelines()

            // Update UI
            view.fragment_home_btn_tomorrow.imageAlpha = 255
        }

        view.fragment_home_btn_tomorrow.setOnClickListener {
            val newDate = sdf.parse(getDay(mCurrentDate, 1))

            // Guard: if new date after today
            if (newDate!!.after(Date())) return@setOnClickListener

            mCurrentDate = sdf.format(newDate)

            if (sdf.format(newDate) == today) {
                // Update UI
                view.fragment_home_txt_day.text = getString(R.string.today)
                view.fragment_home_btn_tomorrow.imageAlpha = 20
            } else if(sdf.format(newDate) == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = mCurrentDate
            }

            refreshGuidelines()
        }

        // Update UI
        view.fragment_home_txt_day.text = getString(R.string.today)
        view.fragment_home_btn_tomorrow.imageAlpha = 20

        getConsumptionsAsyncTask().execute()

        return view
    }

    override fun onStart() {
        super.onStart()

    }

    fun refreshGuidelines()
    {
        // Guard
        if (dietaryGuidelines == null) { return }

        GuidelinesHelper.initializeGuidelines(activity, fragment_home_ll_guidelines, dietaryGuidelines)
    }

    fun getDay(date: String, daysAdded: Int): String {
        Log.d("AAAAAA", "BBBBB")
        var newDate = date
        val c = Calendar.getInstance()
        c.time = sdf.parse(newDate)!!
        c.add(Calendar.DATE, daysAdded)
        val resultdate = Date(c.timeInMillis)
        newDate = sdf.format(resultdate)
        return newDate
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
            return try {
                foodRepository.getConsumptionsForDietary(mCurrentDateAsCreateDate, patientId = user.patient!!.id)
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
            if (GeneralHelper.apiIsDown) { Toast.makeText(activity, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(activity, R.string.get_consumptions_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(activity, R.string.fetched_consumptions, Toast.LENGTH_SHORT).show()

            consumptions = result

            supplements.clear()

            // Should be based on amount of dietaryRestrictions
            /*dietaryGuidelines.forEachIndexed { index, element ->
                supplements.add(0)
            }*/

            for (i in 0..5)
                supplements.add(0)

            consumptions.forEach {
                supplements[0] += (it.food_meal_component.kcal * it.amount).roundToInt()
                supplements[1] += (it.food_meal_component.water * it.amount).roundToInt()
                supplements[2] += (it.food_meal_component.sodium * 1000 * it.amount).roundToInt()
                supplements[3] += (it.food_meal_component.potassium * 1000 * it.amount).roundToInt()
                supplements[4] += (it.food_meal_component.protein * it.amount).roundToInt()
                supplements[5] += (it.food_meal_component.fiber * it.amount).roundToInt()
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
            if (GeneralHelper.apiIsDown) { Toast.makeText(activity, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(activity, R.string.get_dietary_fail, Toast.LENGTH_SHORT).show()
                return }

            // Feedback
            Toast.makeText(activity, R.string.fetched_dietary, Toast.LENGTH_SHORT).show()

            dietaryGuidelines.clear()

            result.forEachIndexed { _, resultDietary ->
                var index = 0
                if (resultDietary.dietary_restriction.description.contains("Calorie"))
                    index = 0
                else if (resultDietary.dietary_restriction.description.contains("Vocht"))
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
}