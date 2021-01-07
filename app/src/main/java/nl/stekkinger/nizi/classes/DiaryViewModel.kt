package nl.stekkinger.nizi.classes

import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.fragments.FoodViewFragment
import nl.stekkinger.nizi.fragments.MealViewFragment
import nl.stekkinger.nizi.repositories.FoodRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var mDate: MutableLiveData<Calendar> = MutableLiveData(),
//    private var mCurrentDay: String = SimpleDateFormat("yyyy-MM-dd").format(Date()),
    private var mMealTime: String = "Ontbijt",
    private var mFavorites: ArrayList<MyFood> = ArrayList(),
    private var mSearchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    val diaryState = mRepository.diaryState
    fun getData(date: Calendar) { mRepository.getData(sdfDb.format(date.time)) }

    val favoritesState = mRepository.favoritesState
    fun fetchFavorites() { mRepository.getFavorites() }



    // Favorites
    val toggleFavoriteState = mRepository.toggleFavoriteState
    fun resetToggleFavoriteState() { mRepository.resetToggleFavoriteState() }
    fun addFavorite(id: Int) { mRepository.addFavorite(id) }
    fun deleteFavorite(id: Int) { mRepository.deleteFavorite(id) }

    // Easy access for ui to check favorites
    fun getFavorites(): ArrayList<MyFood> { return mFavorites}
    fun setFavorites(favorites: ArrayList<MyFood>) { mFavorites = favorites }

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
    ) { search: String ->  foodSearch(search) }

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
    private var currentFragment = "food"
    fun getCurrentFragment(): String {
        return currentFragment
    }

    // load the food view fragment with the selected food
    fun select(activity: AppCompatActivity, food: Food, fragment: String = "food") {
        currentFragment = fragment
        selected.value = food
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            FoodViewFragment()
        ).commit()
    }

    fun emptyState() {
        mRepository.emptyState()
    }

    val selectedEdit = MutableLiveData<ConsumptionResponse>()
    fun selectEdit(activity: AppCompatActivity, consumption: ConsumptionResponse) {
        // TODO: remove activity?
        selectedEdit.value = consumption
    }

    fun addConsumption(food: Food, portion: Float = 1.0F) {

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
            portion_size = food.portion_size * portion,
            image_url = food.image_url,
            foodId = food.foodId
        )
        val weightUnit = WeightUnit(
            id = food.weight_unit.id,
            unit = food.weight_unit.unit,
            short = food.weight_unit.short
        )

        val consumption = Consumption(
            amount = portion,
            date = date+"T11:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.addConsumption(consumption)
    }

    val consumptionState = mRepository.consumptionState

    fun editConsumption(c: ConsumptionResponse, newPortion: Float) {
        val date: String = sdfDb.format(mDate.value!!.time)
        val oldPortion: Float = c.amount

        val foodItem = FoodMealComponent(
            id = c.food_meal_component.id,
            name = c.food_meal_component.name,
            description = c.food_meal_component.description,
            kcal = (c.food_meal_component.kcal / oldPortion * newPortion),
            protein = (c.food_meal_component.protein / oldPortion * newPortion),
            potassium = (c.food_meal_component.potassium / oldPortion * newPortion),
            sodium = (c.food_meal_component.sodium / oldPortion * newPortion),
            water = (c.food_meal_component.water / oldPortion * newPortion),
            fiber = (c.food_meal_component.fiber / oldPortion * newPortion),
            portion_size = (c.food_meal_component.portion_size / oldPortion * newPortion),
            image_url = c.food_meal_component.image_url,
            foodId = c.food_meal_component.foodId
        )
        val weightUnit = WeightUnit(
            id = c.weight_unit.id,
            unit = c.weight_unit.unit,
            short = c.weight_unit.short
        )

        val consumption = Consumption(
            id = c.id,
            amount = newPortion,
            date = date+"T11:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.editConsumption(consumption)
    }

    // meals
    fun deleteMeal(id: Int){
        mRepository.deleteMeal(id)
    }

    private var adapter: MealProductAdapter = MealProductAdapter(this)
    private var mealProducts: ArrayList<Food> = ArrayList()
    private var mealProductPosition: Int = 0

    fun setMealProductPosition(pos: Int) {
        mealProductPosition = pos
    }
    fun addMealProduct(food: Food, amount: Float = 1f) {
        food.amount = amount
        mealProducts.add(food)
        adapter.setMealProductList(mealProducts)
    }


    fun editMealProduct(amount: Float) {
        mealProducts[mealProductPosition].amount = amount
        adapter.setMealProductList(mealProducts)
    }

    val mealState = mRepository.mealState

    fun createMeal(meal: Meal) {
        mRepository.createMeal(meal)
    }

    fun createMealFoods(mealId: Int) {
        for (food: Food in mealProducts) {
            val mealFood = MealFood(
                food = food.id,
                amount = food.amount,
                meal = mealId
            )
            mRepository.createMealFood(mealFood)
        }
    }

    // load the food view fragment with the selected food
    val selectedMeal = MutableLiveData<Meal>()
    fun selectMeal(activity: AppCompatActivity, meal: Meal) {

        selectedMeal.value = meal
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            MealViewFragment()
        ).commit()
    }

    fun addMeal(meal: Meal, amount: Float = 1f) {
        // TODO: transform into consumption
        // mRepository.addConsumption(consumption)
    }

    fun editMeal(meal: Meal) {
        // get the food items
        if (meal.meal_foods!!.count() > 0)
        for (mealFood: MealFood in meal.meal_foods) {
//            mRepository.getFood
        }

    }

    fun getMealProducts(): ArrayList<Food> {
        return mealProducts
    }

    fun getMealProductAdapter(): MealProductAdapter {
        return adapter
    }
}