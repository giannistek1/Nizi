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
import nl.stekkinger.nizi.fragments.ConsumptionViewFragment
import nl.stekkinger.nizi.fragments.DiaryFragment
import nl.stekkinger.nizi.fragments.MealFoodViewFragment
import java.util.*
import kotlin.collections.ArrayList



class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var mDate: MutableLiveData<Calendar> = MutableLiveData(),
//    private var mCurrentDay: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    private var mMealTime: String = "Ontbijt",
    private var mSearchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    fun setMealTime(time: String){
        mMealTime = time
    }


    // diary/consumption area
    private var mDiary: LiveData<ArrayList<ConsumptionResponse>> = Transformations.switchMap<Calendar, ArrayList<ConsumptionResponse>>(
        mDate
    ) { date: Calendar ->  diary(date) }

    private fun diary(date: Calendar): MutableLiveData<ArrayList<ConsumptionResponse>> {
        return mRepository.getDiary(sdfDb.format(date.time))
    }

    fun setDiaryDate(cal: Calendar) {
        mDate.value = cal
    }

    fun getDiary(): LiveData<ArrayList<ConsumptionResponse>> {
        return mDiary
    }

    // Date format for views
    private val sdf: SimpleDateFormat = GeneralHelper.getDateFormat()
    // Date format for database
    private val sdfDb: SimpleDateFormat = GeneralHelper.getCreateDateFormat()

    fun getSelectedDate(): Calendar {
        return mDate.value!!
    }

    fun getDateString(): String {
        val date: String = sdf.format(mDate.value!!.time)
        // create temporary calendar
        val tmpCalendar: Calendar = Calendar.getInstance()
        // set the calendar to start of today
        tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tmpCalendar.set(Calendar.MINUTE, 0);
        tmpCalendar.set(Calendar.SECOND, 0);
        tmpCalendar.set(Calendar.MILLISECOND, 0);
        // collect today/yesterday date
        val today: String = sdf.format(tmpCalendar.time)
        tmpCalendar.add(Calendar.DATE, -1)
        val yesterday: String = sdf.format(tmpCalendar.time)
        // compare to determine string for UI
        return when (date) {
            today ->  "today"
            yesterday -> "yesterday"
            else -> {
                date
            }
        }
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

    val selectedEdit = MutableLiveData<ConsumptionResponse>()

    // TODO: find fix for editing consumptions (impossible with current API)
    fun selectEdit(activity: AppCompatActivity, consumption: ConsumptionResponse) {
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            ConsumptionViewFragment()
        ).commit()
        selectedEdit.value = consumption
    }

    val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    fun addFood(food: Food, portion: Float = 1.0F) {

        val date: String = sdfDb.format(mDate.value!!.time)

        val foodItem = FoodMealComponent(
            id = food.id,
            name = food.name,
            description = food.description,
            kcal = food.kcal * portion,
            protein = food.protein * portion,
            potassium = food.potassium * portion,
            sodium = food.sodium * portion,
            water = food.water * portion,
            fiber = food.fiber * portion,
            portion_size = food.portion_size,
            image_url = food.image_url
        )
        val weightUnit = WeightUnit(
            id = food.weight_unit.id,
            unit = food.weight_unit.unit,
            short = food.weight_unit.short
        )

        val consumption = Consumption(
            amount = portion.toFloat(),
            date = date+"T11:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.addConsumption(consumption)
    }

    fun editFood(consumptionResponse: ConsumptionResponse, portion: Float) {
        val date: String = sdfDb.format(mDate.value!!.time)

        val foodItem = FoodMealComponent(
            id = consumptionResponse.food_meal_component.id,
            name = consumptionResponse.food_meal_component.name,
            description = consumptionResponse.food_meal_component.description,
            kcal = (consumptionResponse.food_meal_component.kcal * portion).toFloat(),
            protein = (consumptionResponse.food_meal_component.protein * portion).toFloat(),
            potassium = (consumptionResponse.food_meal_component.potassium * portion).toFloat(),
            sodium = (consumptionResponse.food_meal_component.sodium * portion).toFloat(),
            water = (consumptionResponse.food_meal_component.water * portion).toFloat(),
            fiber = (consumptionResponse.food_meal_component.fiber * portion).toFloat(),
            portion_size = consumptionResponse.food_meal_component.portion_size,
            image_url = consumptionResponse.food_meal_component.image_url
        )
        val weightUnit = WeightUnit(
            id = consumptionResponse.weight_unit.id,
            unit = consumptionResponse.weight_unit.unit,
            short = consumptionResponse.weight_unit.short
        )

        val consumption = Consumption(
            amount = portion,
            date = date+"T11:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.editConsumption(consumption)
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