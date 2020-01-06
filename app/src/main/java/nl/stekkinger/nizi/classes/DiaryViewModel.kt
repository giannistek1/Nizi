package nl.stekkinger.nizi.classes

import android.app.Activity
import android.app.Application
import android.app.PendingIntent.getActivity
import android.content.Context
import android.util.Log
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.MainActivity
import nl.stekkinger.nizi.fragments.FoodViewFragment
import nl.stekkinger.nizi.repositories.FoodRepository
import java.text.SimpleDateFormat
import nl.stekkinger.nizi.classes.Consumption
import java.text.DateFormat.getDateInstance
import java.util.*
import kotlin.collections.ArrayList



class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var mStartDate: MutableLiveData<String> = MutableLiveData(),
    private var mSearchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    // diary
    private var mDiary: LiveData<Consumptions.Result> = Transformations.switchMap<String, Consumptions.Result>(
        mStartDate
    ) { date ->  diary(date) }

    private fun diary(startDate: String): MutableLiveData<Consumptions.Result> {
        d("t", startDate)
//        var endDate = startDate
//        var sdf = SimpleDateFormat("yyyy-MM-dd")
//        val c = Calendar.getInstance()
//        c.time = sdf.parse(endDate)
//        c.add(Calendar.DATE, 1)
//        val resultdate = Date(c.timeInMillis)
//        endDate = sdf.format(resultdate)
//
//        d("date", startDate + " " + endDate)
//        return mRepository.getDiary(startDate, endDate)
        return mRepository.getDiary("2020-01-06", "2020-01-07")
    }

    fun setDiaryDate(date: String) {
        mStartDate.value = date
    }

    fun getDiary(): LiveData<Consumptions.Result> {
        return mDiary
    }

    // foodSearch
    private var mFoodSearch: LiveData<ArrayList<Food>> = Transformations.switchMap<String, ArrayList<Food>>(
        mSearchText
    ) { search ->  foodSearch(search) }

    private fun foodSearch(searchText: String): MutableLiveData<ArrayList<Food>?> {
        return mRepository.searchFood(searchText)
    }

    fun setFoodSearch(text: String) {
        mSearchText.value = text
    }

    fun getFoodSearch(): LiveData<ArrayList<Food>> {
        return mFoodSearch
    }

    // for food view fragment
    val selected = MutableLiveData<Food>()

    // load the food view fragment with the selected food
    fun select(activity: AppCompatActivity, food: Food) {
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            FoodViewFragment()
        ).commit()
        selected.value = food
    }


    fun addFood(food: Food, portion: Double = 1.0) {
        val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date()) + "T00:00:01"

        val consumption = Consumption(
            FoodName = food.Name,
            KCal = (food.KCal * portion).toFloat(),
            Protein = (food.Protein * portion).toFloat(),
            Fiber = (food.Fiber * portion).toFloat(),
            Calium = (food.Calcium * portion).toFloat(),
            Sodium = (food.Sodium * portion).toFloat(),
            Amount = (food.PortionSize * portion).toInt(),
            WeightUnitId = 1,
            Date = date,
            PatientId = preferences.getInt("patient", 0),
            Id = 0
        )

//        val consumption: String = "{ \"FoodName\": \"" + food.Name + "\", " +
//                "\"KCal\": " + (food.KCal * portion).toInt() + ", " +
//                "\"Protein\": " + (food.Protein * portion).toInt() + ", " +
//                "\"Fiber\": " + (food.Fiber * portion).toInt() + ", " +
//                "\"Calium\": " + (food.Calcium * portion).toInt() + ", " +
//                "\"Sodium\": " + (food.Sodium * portion).toInt() + ", " +
//                "\"Amount\": " + (food.PortionSize * portion).toInt() + ", " +
//                "\"WeightUnitId\": " + 1 + ", " +
//                "\"Date\": \"" + date + "\", " +
//                "\"PatientId\": " + preferences.getInt("patient", 0) + ", " +
//                "\"Id\": 0 }"


        mRepository.addConsumption(consumption)

        d("con", consumption.toString())
        d("con", portion.toString())
        d("con", preferences.getInt("patient", 0).toString())
    }
}