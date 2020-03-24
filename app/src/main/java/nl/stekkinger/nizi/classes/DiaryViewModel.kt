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
import nl.stekkinger.nizi.adapters.MealAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.fragments.FoodViewFragment
import nl.stekkinger.nizi.repositories.FoodRepository
import java.text.SimpleDateFormat
import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.fragments.ConsumptionViewFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.MealFoodViewFragment
import java.text.DateFormat.getDateInstance
import java.util.*
import kotlin.collections.ArrayList



class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var mDate: MutableLiveData<String> = MutableLiveData(),
    private var mCurrentDay: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    private var mMealTime: String = "Ontbijt",
    private var mSearchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    fun setMealTime(time: String){
        mMealTime = time
    }

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

    // TODO: find fix for editing consumptions (impossible with current API)
//    fun selectEdit(activity: AppCompatActivity, consumption: Consumptions.Consumption) {
//        (activity).supportFragmentManager.beginTransaction().replace(
//            R.id.activity_main_fragment_container,
//            ConsumptionViewFragment()
//        ).commit()
//    }

    val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    fun addFood(food: Food, portion: Double = 1.0) {


        val consumption = Consumption(
            FoodName = food.Name,
            KCal = (food.KCal * portion).toFloat(),
            Protein = (food.Protein * portion).toFloat(),
            Fiber = (food.Fiber * portion).toFloat(),
            Calium = (food.Calcium * portion).toFloat(),
            Sodium = (food.Sodium * portion).toFloat(),
            Water = (food.Water * portion).toFloat(),
            Amount = (food.PortionSize * portion).toInt(),
            MealTime = mMealTime,
            WeightUnitId = 1,
            Date = mCurrentDay,
            PatientId = preferences.getInt("patient", 0),
            Id = 0
        )
        d("conmod", consumption.toString())
        mRepository.addConsumption(consumption)
    }

    // meals
    private var adapter: MealProductAdapter = MealProductAdapter(this)
    private var mealProducts: ArrayList<MealProduct> = ArrayList()
    fun addMealProduct(food: Food, portion: Double = 1.toDouble()) {
        val mealProduct = MealProduct (
            Name = food.Name,
            KCal = (food.KCal * portion).toFloat(),
            Protein = (food.Protein * portion).toFloat(),
            Fiber = (food.Fiber * portion).toFloat(),
            Calcium = (food.Calcium * portion).toFloat(),
            Sodium = (food.Sodium * portion).toFloat(),
            Water = (food.Water * portion).toFloat(),
            PortionSize = (food.PortionSize * portion).toInt(),
            WeightUnit = food.WeightUnit
        )
        mealProducts.add(mealProduct)
        adapter.setMealProductList(mealProducts)
    }

    fun createMeal(name: String) {
        // prep values for meal
        var totalKCal: Double = 0.toDouble()
        var totalProtein: Double = 0.toDouble()
        var totalFiber: Double = 0.toDouble()
        var totalCalcium: Double = 0.toDouble()
        var totalSodium: Double = 0.toDouble()
        var totalWater: Double = 0.toDouble()
        var totalSize = 0
        // fill in total values for meal
        for (p in mealProducts) {
            totalKCal += p.KCal
            totalProtein += p.Protein
            totalFiber += p.Fiber
            totalCalcium += p.Calcium
            totalSodium += p.Sodium
            totalWater += p.Water
            totalSize += p.PortionSize
        }
        // create meal object
        val meal = Meal(
            MealId = 0,
            Name = name,
            PatientId = preferences.getInt("patient", 0),
            KCal = totalKCal,
            Protein = totalProtein,
            Fiber = totalFiber,
            Calcium = totalCalcium,
            Sodium = totalSodium,
            Water = totalWater,
            PortionSize = totalSize,
            WeightUnit = "g",
            Picture = ""
        )
        // create meal
        mRepository.createMeal(meal)
        mealProducts = ArrayList()
    }

    // adding meal to diary
    fun addMeal(meal: Meal) {
        // TODO: add overview for portions later
        val portion = 1.toFloat()
        val consumption = Consumption(
            FoodName = meal.Name,
            KCal = (meal.KCal * portion).toFloat(),
            Protein = (meal.Protein * portion).toFloat(),
            Fiber = (meal.Fiber * portion).toFloat(),
            Calium = (meal.Calcium * portion).toFloat(),
            Sodium = (meal.Sodium * portion).toFloat(),
            Amount = (meal.PortionSize * portion).toInt(),
            MealTime = mMealTime,
            Water = (meal.Water * portion).toFloat(),
            WeightUnitId = 1,
            Date = mCurrentDay,
            PatientId = preferences.getInt("patient", 0),
            Id = 0
        )
        mRepository.addConsumption(consumption)
    }

    fun deleteMeal(id: Int){
        mRepository.deleteMeal(id)
    }

    fun selectMealProduct(activity: AppCompatActivity, food: Food) {
        // TODO: combine with select
//        addMeal(meal)
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            MealFoodViewFragment()
        ).commit()
        selected.value = food
    }

    fun selectMeal(activity: AppCompatActivity, meal: Meal) {
        addMeal(meal)
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            DiaryFragment()
        ).commit()
    }

    fun getMealProducts(): ArrayList<MealProduct> {
        d("meal", mealProducts.count().toString())
        return mealProducts
    }

    fun deleteMealProduct(mealProduct: MealProduct) {
        mealProducts.remove(mealProduct)
    }

    fun addFavorite(id: Int) {
        mRepository.addFavorite(id)
    }

    fun deleteFavorite(id: Int) {
        mRepository.deleteFavorite(id)
    }
}