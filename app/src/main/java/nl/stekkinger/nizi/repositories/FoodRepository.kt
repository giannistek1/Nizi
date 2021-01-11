package nl.stekkinger.nizi.repositories

import android.util.Log.d
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class FoodRepository : Repository() {

    // State classes
    sealed class State {
        object Success: State()
        data class Error(val message: String) : State()
        object Loading: State()
        object Empty: State()
    }
    sealed class DiaryState {
        data class Success(val data: ArrayList<ConsumptionResponse>) : DiaryState()
        data class Error(val message: String) : DiaryState()
        object Loading: DiaryState()
        object Empty: DiaryState()
    }
    sealed class FoodsState {
        data class Success(val data: ArrayList<Food>) : FoodsState()
        data class Error(val message: String) : FoodsState()
        object Loading: FoodsState()
        object Empty: FoodsState()
    }
    sealed class FoodState {
        data class Success(val data: Food) : FoodState()
        data class Error(val message: String) : FoodState()
        object Loading: FoodState()
        object Empty: FoodState()
    }
    sealed class FavoritesState {
        data class Success(val data: ArrayList<MyFoodResponse>) : FavoritesState()
        data class Error(val message: String) : FavoritesState()
        object Loading: FavoritesState()
        object Empty: FavoritesState()
    }
    sealed class MealState {
        data class Success(val data: Meal) : MealState()
        data class Error(val message: String) : MealState()
        object Loading: MealState()
        object Empty: MealState()
    }

    // State values, used to observe changes within fragments
    private val _diaryState: MutableStateFlow<DiaryState> = MutableStateFlow(DiaryState.Empty)
    val diaryState: StateFlow<DiaryState> = _diaryState

    private val _consumptionState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val consumptionState: StateFlow<State> = _consumptionState

    private val _favoritesState: MutableStateFlow<FavoritesState> = MutableStateFlow(FavoritesState.Empty)
    val favoritesState: StateFlow<FavoritesState> = _favoritesState

    private val _toggleFavoriteState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val toggleFavoriteState: StateFlow<State> = _toggleFavoriteState

    private val _foodByBarcodeState: MutableStateFlow<FoodState> = MutableStateFlow(FoodState.Empty)
    val foodByBarcodeState: StateFlow<FoodState> = _foodByBarcodeState

    private val _foodsState: MutableStateFlow<FoodsState> = MutableStateFlow(FoodsState.Empty)
    val foodsState: StateFlow<FoodsState> = _foodsState

    private val _mealState: MutableStateFlow<MealState> = MutableStateFlow(MealState.Empty)
    val mealState: StateFlow<MealState> = _mealState

    private val _deleteMealState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val deleteMealState: StateFlow<State> = _deleteMealState

    private val _mealFoodState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val mealFoodState: StateFlow<State> = _mealFoodState

    // functions to reset states (only needed for some if the fragment redirects on success)
    fun emptyState() { _consumptionState.value = State.Empty }
    fun emptyFoodsState() { _foodsState.value = FoodsState.Empty}
    fun emptyFoodBarcodeState() { _foodByBarcodeState.value = FoodState.Empty }
    fun emptyToggleFavoriteState() { _toggleFavoriteState.value = State.Empty }
    fun emptyMealState() { _mealState.value = MealState.Empty }
    fun emptyDeleteMealState() { _deleteMealState.value = State.Empty }


    // Consumption / diary calls
    // search food items based on search string
    fun searchFood(search: String): MutableLiveData<ArrayList<Food>?> {
        var foodList: ArrayList<Food> = arrayListOf()
        val result = MutableLiveData<ArrayList<Food>?>()

        service.searchFoodDB(authHeader = authHeader, foodName = search).enqueue(object: Callback<ArrayList<FoodResponse>> {
            override fun onResponse(call: Call<ArrayList<FoodResponse>>, response: Response<ArrayList<FoodResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    for (foodResponse: FoodResponse in response.body()!!) {
                        val food = Food(
                            id = foodResponse.id,
                            name = foodResponse.name,
                            description = foodResponse.food_meal_component.description,
                            kcal = foodResponse.food_meal_component.kcal,
                            protein = foodResponse.food_meal_component.protein,
                            potassium = foodResponse.food_meal_component.potassium,
                            sodium = foodResponse.food_meal_component.sodium,
                            water = foodResponse.food_meal_component.water,
                            fiber = foodResponse.food_meal_component.fiber,
                            portion_size = foodResponse.food_meal_component.portion_size,
                            weight_unit = foodResponse.weight_unit,
                            weight_amount = foodResponse.food_meal_component.portion_size,
                            image_url = foodResponse.food_meal_component.image_url,
                            foodId = foodResponse.food_meal_component.foodId
                        )
                        foodList.add(food)
                    }
                    result.value = foodList
                }
            }
            override fun onFailure(call: Call<ArrayList<FoodResponse>>, t: Throwable) {

            }
        })
        return result
    }

    fun getFoodByBarcode(barcode: String) {
        _foodByBarcodeState.value = FoodState.Loading
        service.getFoodByBarcode(authHeader = authHeader, barcode = barcode).enqueue(object: Callback<ArrayList<FoodResponse>> {
            override fun onResponse(call: Call<ArrayList<FoodResponse>>, response: Response<ArrayList<FoodResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    if (response.body()!!.count() == 0) {
                        // no food found with this barcode
                        _foodByBarcodeState.value = FoodState.Error("not found")
                    } else {
                        val foodResponse: FoodResponse = response.body()!![0]!!
                        val food = Food(
                            id = foodResponse.id,
                            name = foodResponse.name,
                            description = foodResponse.food_meal_component.description,
                            kcal = foodResponse.food_meal_component.kcal,
                            protein = foodResponse.food_meal_component.protein,
                            potassium = foodResponse.food_meal_component.potassium,
                            sodium = foodResponse.food_meal_component.sodium,
                            water = foodResponse.food_meal_component.water,
                            fiber = foodResponse.food_meal_component.fiber,
                            portion_size = foodResponse.food_meal_component.portion_size,
                            weight_unit = foodResponse.weight_unit,
                            weight_amount = foodResponse.food_meal_component.portion_size,
                            image_url = foodResponse.food_meal_component.image_url,
                            foodId = foodResponse.food_meal_component.foodId
                        )
                        _foodByBarcodeState.value = FoodState.Success(food)
                    }
                } else {
                    d("got", "here")
                    _foodByBarcodeState.value = FoodState.Empty
                }
            }
            override fun onFailure(call: Call<ArrayList<FoodResponse>>, t: Throwable) {
                _foodByBarcodeState.value = FoodState.Error(t.message!!)
            }
        })
    }

    // Get all consumptions by date for a patient
    fun getConsumptions(date: String) {
        _diaryState.value = DiaryState.Loading
        val newDate: String = date + "T00:00:00.000Z"

        service.fetchConsumptions(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id, date = newDate).enqueue(object: Callback<ArrayList<ConsumptionResponse>> {
            override fun onResponse(call: Call<ArrayList<ConsumptionResponse>>, response: Response<ArrayList<ConsumptionResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    _diaryState.value = DiaryState.Success(response.body()!!)
                } else {
                    _diaryState.value = DiaryState.Empty
                }
            }
            override fun onFailure(call: Call<ArrayList<ConsumptionResponse>>, t: Throwable) {
                _diaryState.value = DiaryState.Error(t.message!!)
            }
        })
    }

    // Get consumptions for a patient within a date period
    fun getConsumptionsByRange(startDate: String, endDate: String, patientId: Int): ArrayList<ConsumptionResponse>? {
        return service.fetchConsumptionsByRange(authHeader = authHeader, patientId = patientId, startDate = startDate,
            endDate = endDate).execute().body()
    }

    // create a consumption
    fun addConsumption(consumption: Consumption) {
        _consumptionState.value = State.Loading
        service.addConsumption(authHeader = authHeader, body = consumption).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _consumptionState.value = State.Success
                } else {
                    _consumptionState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _consumptionState.value = State.Error(t.message!!)
            }
        })
    }

    // update a consumption by id
    fun editConsumption(consumption: Consumption) {
        _consumptionState.value = State.Loading
        service.editConsumption(authHeader = authHeader, body = consumption, consumptionId = consumption.id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _consumptionState.value = State.Success
                } else {
                    _consumptionState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _consumptionState.value = State.Error(t.message!!)
            }
        })
    }

    // delete a consumption by id
    fun deleteConsumption(id: Int) {
        _consumptionState.value = State.Loading
        service.deleteConsumption(authHeader = authHeader, consumptionId = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _consumptionState.value = State.Success
                } else {
                    _consumptionState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _consumptionState.value = State.Error(t.message!!)
            }
        })
    }

    // Favorite calls
    // Get all favorite foods of a user
    fun getFavorites(): MutableStateFlow<FavoritesState> {
        _favoritesState.value = FavoritesState.Loading
        service.getFavoriteFood(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id).enqueue(object : Callback<ArrayList<MyFoodResponse>> {
            override fun onResponse(call: Call<ArrayList<MyFoodResponse>>, response: Response<ArrayList<MyFoodResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    _favoritesState.value = FavoritesState.Success(response.body()!!)
                } else {
                    _favoritesState.value = FavoritesState.Empty
                }
            }
            override fun onFailure(call: Call<ArrayList<MyFoodResponse>>, t: Throwable) {
                _favoritesState.value = FavoritesState.Error(t.message!!)
            }
        })
        return _favoritesState
    }

    // Adding a favorite food for a user
    fun addFavorite(id: Int) {
        _toggleFavoriteState.value = State.Loading
        val myFoodRequest = MyFoodRequest(food = id, patients_id = GeneralHelper.getUser().patient!!.id )
        service.addFavoriteFood(authHeader = authHeader, body = myFoodRequest).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _toggleFavoriteState.value = State.Success
                } else {
                    _toggleFavoriteState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _toggleFavoriteState.value = State.Error(t.message!!)
            }
        })
    }

    // Remove a favorite food by id
    fun deleteFavorite(id: Int) {
        _toggleFavoriteState.value = State.Loading
        service.deleteFavoriteFood(authHeader = authHeader, id = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _toggleFavoriteState.value = State.Success
                } else {
                    _toggleFavoriteState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _toggleFavoriteState.value = State.Error(t.message!!)
            }
        })
    }

    // Meal calls
    fun getMeals(): ArrayList<Meal>? {
        return service.getMeals(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id).execute().body()
    }
    fun getMealsByName(search: String): ArrayList<Meal>? {
        return service.getMealsByName(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id, mealName = search).execute().body()
    }

    fun getMeal(id: Int) {
        _mealState.value = MealState.Loading
        service.getMeal(authHeader = authHeader, id = id).enqueue(object : Callback<Meal> {
            override fun onResponse(call: Call<Meal>, response: Response<Meal>) {
                if (response.isSuccessful && response.body() != null) {
                    _mealState.value = MealState.Success(response.body()!!)
                } else {
                    _mealState.value = MealState.Empty
                }
            }
            override fun onFailure(call: Call<Meal>, t: Throwable) {
                _mealState.value = MealState.Error(t.message!!)
            }
        })
    }

    fun createMeal(meal: Meal) {
        _mealState.value = MealState.Loading
        service.createMeal(authHeader = authHeader, body = meal).enqueue(object : Callback<Meal> {
            override fun onResponse(call: Call<Meal>, response: Response<Meal>) {
                if (response.isSuccessful && response.body() != null) {
                    _mealState.value = MealState.Success(response.body()!!)
                } else {
                    _mealState.value = MealState.Empty
                }
            }
            override fun onFailure(call: Call<Meal>, t: Throwable) {
                _mealState.value = MealState.Error(t.message!!)
            }
        })
    }

    fun updateMeal(meal: Meal, mealId: Int) {
        _mealState.value = MealState.Loading
        service.updateMeal(authHeader = authHeader, body = meal, id = mealId).enqueue(object : Callback<Meal> {
            override fun onResponse(call: Call<Meal>, response: Response<Meal>) {
                if (response.isSuccessful && response.body() != null) {
                    _mealState.value = MealState.Success(response.body()!!)
                } else {
                    _mealState.value = MealState.Empty
                }
            }
            override fun onFailure(call: Call<Meal>, t: Throwable) {
                _mealState.value = MealState.Error(t.message!!)
            }
        })
    }

    fun deleteMeal(id: Int) {
        _deleteMealState.value = State.Loading
        service.deleteMeal(authHeader = authHeader, id = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful && response.body() != null) {
                    _deleteMealState.value = State.Success
                } else {
                    _deleteMealState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _deleteMealState.value = State.Error(t.message!!)
            }
        })
    }

    // Meal food calls
    // used for getting foods products for meals
    fun getFoods(ids: ArrayList<Int>) {

        _foodsState.value = FoodsState.Loading
        var foodList: ArrayList<Food> = arrayListOf()

        service.getFoods(authHeader = authHeader, ids = ids).enqueue(object: Callback<ArrayList<FoodResponse>> {
            override fun onResponse(call: Call<ArrayList<FoodResponse>>, response: Response<ArrayList<FoodResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    for (foodResponse: FoodResponse in response.body()!!) {
                        val food = Food(
                            id = foodResponse.id,
                            name = foodResponse.name,
                            description = foodResponse.food_meal_component.description,
                            kcal = foodResponse.food_meal_component.kcal,
                            protein = foodResponse.food_meal_component.protein,
                            potassium = foodResponse.food_meal_component.potassium,
                            sodium = foodResponse.food_meal_component.sodium,
                            water = foodResponse.food_meal_component.water,
                            fiber = foodResponse.food_meal_component.fiber,
                            portion_size = foodResponse.food_meal_component.portion_size,
                            weight_unit = foodResponse.weight_unit,
                            weight_amount = foodResponse.food_meal_component.portion_size,
                            image_url = foodResponse.food_meal_component.image_url,
                            foodId = foodResponse.food_meal_component.foodId
                        )
                        foodList.add(food)
                    }
                    _foodsState.value = FoodsState.Success(foodList)
                } else {
                    _foodsState.value = FoodsState.Empty
                }
            }
            override fun onFailure(call: Call<ArrayList<FoodResponse>>, t: Throwable) {
                _foodsState.value = FoodsState.Error(t.message!!)
            }
        })
    }

    fun createMealFood(mealFood: MealFood) {
        _mealFoodState.value = State.Loading
        service.createMealFood(authHeader = authHeader, body = mealFood).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful && response.body() != null) {
                    _mealFoodState.value = State.Success
                } else {
                    _mealFoodState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _mealFoodState.value = State.Error(t.message!!)
            }
        })
    }

    fun deleteMealFoods(mealId: Int) {
        _mealFoodState.value = State.Loading
        service.deleteMealFoods(authHeader = authHeader, id = mealId).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful && response.body() != null) {
                    _mealFoodState.value = State.Success
                } else {
                    _mealFoodState.value = State.Empty
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _mealFoodState.value = State.Error(t.message!!)
            }
        })
    }
}
