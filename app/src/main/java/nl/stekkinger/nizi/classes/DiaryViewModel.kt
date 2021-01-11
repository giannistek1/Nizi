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

    // State values, used to observe changes within fragments
    val diaryState = mRepository.diaryState
    val favoritesState = mRepository.favoritesState
    val toggleFavoriteState = mRepository.toggleFavoriteState
    val consumptionState = mRepository.consumptionState
    val foodsState = mRepository.foodsState
    val foodByBarcodeState = mRepository.foodByBarcodeState
    val mealState = mRepository.mealState
    val deleteMealState = mRepository.deleteMealState

    // functions to reset states (only needed for some if the fragment redirects on success)
    fun emptyState() { mRepository.emptyState() }
    fun emptyMealState() { mRepository.emptyMealState() }
    fun emptyFoodsState() { mRepository.emptyFoodsState() }
    fun emptyFoodBarcodeState() { mRepository.emptyFoodBarcodeState() }
    fun emptyToggleFavoriteState() { mRepository.emptyToggleFavoriteState() }
    fun emptyDeleteMealState() { mRepository.emptyDeleteMealState() }


    // foodSearch functions
    private var mFoodSearch: LiveData<ArrayList<Food>> = Transformations.switchMap<String, ArrayList<Food>>(
        mSearchText
    ) { search: String ->  foodSearch(search) }

    private fun foodSearch(searchText: String): MutableLiveData<ArrayList<Food>?> {
        return mRepository.searchFood(searchText)
    }
    fun getFoodSearch(): LiveData<ArrayList<Food>> { return mFoodSearch }
    fun setFoodSearch(text: String) { mSearchText.value = text }

    fun getFoodByBarcode(barcode: String) { mRepository.getFoodByBarcode(barcode) }

    // diary functions
    // For tracking if the user is adding consumptions to breakfast, lunch etc.
    fun getMealTime(): String { return mMealTime }
    fun setMealTime(time: String){ mMealTime = time }

    // For tracking on what date diary functions must execute
    fun setDiaryDate(cal: Calendar) { mDate.value = cal }
    fun getSelectedDate(): Calendar { return mDate.value!! }

    // Date format for views
    private val sdf: SimpleDateFormat = GeneralHelper.getDateFormat()
    // Date format for database
    private val sdfDb: SimpleDateFormat = GeneralHelper.getCreateDateFormat()

    // Getting date string for views
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


    // Holds food item for food view
    val selected = MutableLiveData<Food>()
    fun getSelected(): Food? { return selected.value }

    // load the food view fragment with the selected food
    fun select(activity: AppCompatActivity, food: Food, fragment: String = "food") {
        currentFragment = fragment
        selected.value = food
        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            FoodViewFragment()
        ).commit()
    }

    // For tracking what type of food view is being used (food, meal, mealEdit)
    private var currentFragment = "food"
    fun getCurrentFragment(): String { return currentFragment }

    // consumption functions
    fun getConsumptions(date: Calendar) { mRepository.getConsumptions(sdfDb.format(date.time)) }
    fun deleteConsumption(id: Int) { mRepository.deleteConsumption(id) }

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
            date = date+"T00:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.addConsumption(consumption)
    }

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
            date = date+"T00:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = weightUnit,
            food_meal_component = foodItem
        )
        mRepository.editConsumption(consumption)
    }

    // favorites
    fun fetchFavorites() { mRepository.getFavorites() }
    fun addFavorite(id: Int) { mRepository.addFavorite(id) }
    fun deleteFavorite(id: Int) { mRepository.deleteFavorite(id) }

    // Easy access for UI to check favorites
    fun getFavorites(): ArrayList<MyFood> { return mFavorites}
    fun setFavorites(favorites: ArrayList<MyFood>) { mFavorites = favorites }


    // Holds consumption for consumption view
    val selectedEdit = MutableLiveData<ConsumptionResponse>()
    fun selectEdit(consumption: ConsumptionResponse) { selectedEdit.value = consumption }

    // meals
    // storing values for meal fragments
    private var adapter: MealProductAdapter = MealProductAdapter(this)
    private var mealProducts: ArrayList<Food> = arrayListOf()
    private var mealProductPosition: Int = 0
    private var mealId: Int = 0
    private var mealComponentId: Int = 0
    private var mealName = ""
    private var mealPhoto: String? = null
    private var isMealEdit: Boolean = false

    // getters and setters for meal values
    fun getMealProductAdapter(): MealProductAdapter { return adapter }

    fun getMealProducts(): ArrayList<Food> { return mealProducts }
    fun setMealProducts(foods: ArrayList<Food>) { mealProducts = foods }

    fun setMealProductPosition(pos: Int) { mealProductPosition = pos }

    fun getMealId(): Int { return mealId }
    fun setMealId(id: Int) { mealId = id }

    fun getMealComponentId(): Int { return mealComponentId }
    fun setMealComponentId(id: Int) { mealComponentId = id}

    fun getMealName(): String { return mealName }
    fun setMealName(name: String) { mealName = name }

    fun getMealPhoto(): String? { return mealPhoto }
    fun setMealPhoto(photo: String) { mealPhoto = photo }

    fun getIsMealEdit(): Boolean { return isMealEdit }
    fun setIsMealEdit(isEdit: Boolean) { isMealEdit = isEdit }

    // reset all meal values
    fun resetMealValues() {
        mealProductPosition = 0
        mealId = 0
        mealComponentId = 0
        mealName = ""
        mealPhoto = null
        isMealEdit = false
    }

    // meal product list functions (keeps the list intact when swapping various meal fragments)
    fun addMealProduct(food: Food, amount: Float = 1f) {
        food.amount = amount
        mealProducts.add(food)
        adapter.setMealProductList(mealProducts)
    }

    fun editMealProduct(amount: Float) {
        mealProducts[mealProductPosition].amount = amount
        adapter.setMealProductList(mealProducts)
    }

    fun emptyMealProducts() { mealProducts.clear() }

    // load the food view fragment with the selected meal
    val selectedMeal = MutableLiveData<Meal>()
    fun emptySelectedMeal() { selectedMeal.value = null}
    fun selectMeal(activity: AppCompatActivity, meal: Meal) {
        selectedMeal.value = meal

        (activity).supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            MealViewFragment()
        ).commit()
    }

    fun getMeal(id: Int) { mRepository.getMeal(id) }

    fun createMeal(meal: Meal) { mRepository.createMeal(meal) }
    fun deleteMeal(id: Int){ mRepository.deleteMeal(id) }

    // Meal food functions
    fun createMealFoods(mealId: Int) {
        // dont create duplicates
        val ids: ArrayList<Int> = arrayListOf()
        for (food: Food in mealProducts) {
            if (!ids.contains(food.id)) {
                ids.add(food.id)
                val mealFood = MealFood(
                    food = food.id,
                    amount = food.amount,
                    meal = mealId
                )
                mRepository.createMealFood(mealFood)
            }
        }
    }

    // Adding a meal item as consumption
    fun addMeal(meal: Meal, amount: Float = 1f) {
        val date: String = sdfDb.format(mDate.value!!.time)

        val foodItem = FoodMealComponent(
            id = meal.id,
            name = meal.food_meal_component.name,
            description = meal.food_meal_component.description,
            kcal = meal.food_meal_component.kcal * amount,
            protein = meal.food_meal_component.protein * amount,
            potassium = meal.food_meal_component.potassium * amount,
            sodium = meal.food_meal_component.sodium * amount,
            water = meal.food_meal_component.water * amount,
            fiber = meal.food_meal_component.fiber * amount,
            portion_size = meal.food_meal_component.portion_size * amount,
            image_url = meal.food_meal_component.image_url,
            foodId = meal.food_meal_component.foodId
        )

        val consumption = Consumption(
            amount = amount,
            date = date+"T00:00:00.000Z",
            meal_time = mMealTime,
            patient = GeneralHelper.getUser().patient!!.id,
            weight_unit = GeneralHelper.getWeightUnitHolder()!!.weightUnits[7],
            food_meal_component = foodItem
        )
         mRepository.addConsumption(consumption)
    }

    fun getFoods(meal: Meal) {
        // empty meal products
        mealProducts.clear()
        // get the food items
        val foodIds: ArrayList<Int> = arrayListOf()
        if (meal.meal_foods!!.count() > 0) {
            for (mealFood: MealFood in meal.meal_foods) {
                foodIds.add(mealFood.food)
            }
            mRepository.getFoods(foodIds)
        }
    }

    fun updateMeal(meal: Meal) { mRepository.updateMeal(meal, mealId) }
    fun deleteMealFoods(mealId: Int) { mRepository.deleteMealFoods(mealId) }

}