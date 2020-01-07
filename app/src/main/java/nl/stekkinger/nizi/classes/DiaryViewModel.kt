package nl.stekkinger.nizi.classes

import android.app.Activity
import android.app.Application
import android.app.PendingIntent.getActivity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.activities.MainActivity
import nl.stekkinger.nizi.fragments.FoodViewFragment
import nl.stekkinger.nizi.repositories.FoodRepository
import java.text.SimpleDateFormat
import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.fragments.ConsumptionViewFragment
import java.text.DateFormat.getDateInstance
import java.util.*
import kotlin.collections.ArrayList



class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var mDate: MutableLiveData<String> = MutableLiveData(),
    private var mCurrentDay: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    private var mSearchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    // diary/consumption area
    private var mDiary: LiveData<Consumptions.Result> = Transformations.switchMap<String, Consumptions.Result>(
        mDate
    ) { date ->  diary(date) }

    private fun diary(date: String): MutableLiveData<Consumptions.Result> {
        d("t", date)
        val endDate: String = date.substringAfter("/")
        val startDate: String = date.substringBefore("/")
        return mRepository.getDiary(startDate, endDate)
    }

    fun setDiaryDate(date: String) {
        mDate.value = date
        mCurrentDay = date.substringBefore("/")
    }

    fun getDiary(): LiveData<Consumptions.Result> {
        return mDiary
    }

    fun deleteConsumption(id: Int) {
        mRepository.deleteConsumption(id)
    }

    fun updateConsumption(consumption: Consumptions.Consumption) {

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

    fun selectEdit(activity: AppCompatActivity, consumption: Consumptions.Consumption) {
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            ConsumptionViewFragment()
        ).commit()
    }


    fun addFood(food: Food, portion: Double = 1.0) {
        val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

        val consumption = Consumption(
            FoodName = food.Name,
            KCal = (food.KCal * portion).toFloat(),
            Protein = (food.Protein * portion).toFloat(),
            Fiber = (food.Fiber * portion).toFloat(),
            Calium = (food.Calcium * portion).toFloat(),
            Sodium = (food.Sodium * portion).toFloat(),
            Amount = (food.PortionSize * portion).toInt(),
            WeightUnitId = 1,
            Date = mCurrentDay,
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
        d("AF", "---------")
        d("AF", mCurrentDay)
        d("AF", "---------")

    }
}