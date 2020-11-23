package nl.stekkinger.nizi.classes

import android.content.Context
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.fragments.FoodViewFragment
import nl.stekkinger.nizi.repositories.FoodRepository
import java.text.SimpleDateFormat
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.diary.WeightUnit
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.MealFoodViewFragment
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
    private var mDiary: LiveData<ArrayList<ConsumptionResponse>> = Transformations.switchMap<String, ArrayList<ConsumptionResponse>>(
        mDate
    ) { date ->  diary(date) }

    private fun diary(date: String): MutableLiveData<ArrayList<ConsumptionResponse>> {
        d("t", date)
        val endDate: String = date.substringAfter("/")
        val startDate: String = date.substringBefore("/")
        return mRepository.getDiary(startDate, endDate)
    }

    fun setDiaryDate(date: String) {
        mDate.value = date
        mCurrentDay = date.substringBefore("/")
    }

    fun getDiary(): LiveData<ArrayList<ConsumptionResponse>> {
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

        d("log mt", mMealTime)

        val foodItem = FoodMealComponent(
            id = food.id,
            name = food.name,
            description = food.description,
            kcal = (food.kcal * portion).toFloat(),
            protein = (food.protein * portion).toFloat(),
            potassium = (food.potassium * portion).toFloat(),
            sodium = (food.sodium * portion).toFloat(),
            water = (food.water * portion).toFloat(),
            fiber = (food.fiber * portion).toFloat(),
            portion_size = food.portion_size,
            image_url = food.image_url
        )
        val weightUnit = WeightUnit(
            id = food.weight_unit.id,
            unit = food.weight_unit.unit,
            short = food.weight_unit.short
        )
        d("conunit", weightUnit.toString())
        // the foodMealComponent has to be in array in strapi
        val foodMealArray: ArrayList<FoodMealComponent> = arrayListOf()
        foodMealArray.add(foodItem)
        val consumption = Consumption(
            amount = (food.portion_size * portion).toFloat(),
            date = mCurrentDay+"T11:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodMealArray
        )
        d("conmod", consumption.toString())
        mRepository.addConsumption(consumption)
    }

    // meals
    private var adapter: MealProductAdapter = MealProductAdapter(this)
    private var mealProducts: ArrayList<MealProduct> = ArrayList()
    fun addMealProduct(food: Food, portion: Double = 1.toDouble()) {
        val mealProduct = MealProduct (
            Name = food.name,
            KCal = (food.kcal * portion).toFloat(),
            Protein = (food.protein * portion).toFloat(),
            Fiber = (food.fiber * portion).toFloat(),
            Calcium = (food.fiber * portion).toFloat(),
            Sodium = (food.fiber * portion).toFloat(),
            Water = (food.fiber * portion).toFloat(),
            PortionSize = (food.portion_size * portion).toInt(),
            WeightUnit = food.weight_unit.unit
        )
        mealProducts.add(mealProduct)
        adapter.setMealProductList(mealProducts)
    }

    fun createMeal(name: String, photo: String?) {
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
        var photoVal:String = ""
        if(photo != null) {
            // dit hier beneden werkt gewoon
            photoVal = "/:1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" //""D/foto:" + photo

            // dit niet??? :C
            photoVal = photo.toString()

            d("string foto", photoVal)
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
            Picture = photoVal
        )
        d("MEALMEAL", meal.toString())
        // create meal
        mRepository.createMeal(meal)
        mealProducts = ArrayList()
    }

    // adding meal to diary
    fun addMeal(meal: Meal) {
        // TODO: add overview for portions later
        val portion = 1.toFloat()
//        val consumption = Consumption(
//            FoodName = meal.Name,
//            KCal = (meal.KCal * portion).toFloat(),
//            Protein = (meal.Protein * portion).toFloat(),
//            Fiber = (meal.Fiber * portion).toFloat(),
//            Calium = (meal.Calcium * portion).toFloat(),
//            Sodium = (meal.Sodium * portion).toFloat(),
//            Amount = (meal.PortionSize * portion).toInt(),
//            MealTime = mMealTime,
//            Water = (meal.Water * portion).toFloat(),
//            WeightUnitId = 1,
//            Date = mCurrentDay,
//            PatientId = preferences.getInt("patient", 0),
//            ConsumptionId = 0
//        )
//        mRepository.addConsumption(consumption)
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