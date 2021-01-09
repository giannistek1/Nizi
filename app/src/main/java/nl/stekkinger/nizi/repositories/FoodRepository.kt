package nl.stekkinger.nizi.repositories

import android.util.Log
import android.util.Log.d
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class FoodRepository : Repository() {

    private val TAG = "FoodRepository"

    private val _diaryState: MutableStateFlow<DiaryState> = MutableStateFlow(DiaryState.Empty)
    val diaryState: StateFlow<DiaryState> = _diaryState

    sealed class DiaryState {
        data class Success(val data: ArrayList<ConsumptionResponse>) : DiaryState()
        data class Error(val message: String) : DiaryState()
        object Loading: DiaryState()
        object Empty: DiaryState()
    }

    fun getData(date: String) {
        _diaryState.value = DiaryState.Loading
        val newDate: String = date + "T11:00:00.000Z"

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

    sealed class State {
        object Success: State()
        data class Error(val message: String) : State()
        object Loading: State()
        object Empty: State()
    }

    private val _consumptionState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val consumptionState: StateFlow<State> = _consumptionState

    fun emptyState() {
        _consumptionState.value = State.Empty
    }

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

    private val _favoritesState: MutableStateFlow<FavoritesState> = MutableStateFlow(FavoritesState.Empty)
    val favoritesState: StateFlow<FavoritesState> = _favoritesState

    sealed class FavoritesState {
        data class Success(val data: ArrayList<MyFoodResponse>) : FavoritesState()
        data class Error(val message: String) : FavoritesState()
        object Loading: FavoritesState()
        object Empty: FavoritesState()
    }

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

    // catches state in added or deleted favorites
    private val _toggleFavoriteState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val toggleFavoriteState: StateFlow<State> = _toggleFavoriteState

    // resets state
    fun resetToggleFavoriteState() { _toggleFavoriteState.value = State.Empty }

    fun addFavorite(id: Int) {
        _toggleFavoriteState.value = State.Loading

        val myFoodRequest = MyFoodRequest(food = id, patients_ids = GeneralHelper.getUser().patient!!.id )
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

    fun getDiary(date: String): MutableLiveData<ArrayList<ConsumptionResponse>> {
        val result = MutableLiveData<ArrayList<ConsumptionResponse>>()
        val newDate: String = date + "T11:00:00.000Z"
        val gson = Gson()
        val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!

        service.fetchConsumptions(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id, date = newDate).enqueue(object: Callback<ArrayList<ConsumptionResponse>> {
            override fun onResponse(call: Call<ArrayList<ConsumptionResponse>>, response: Response<ArrayList<ConsumptionResponse>>) {
                if (response.isSuccessful) {
                    result.value = response.body()
                }
            }
            override fun onFailure(call: Call<ArrayList<ConsumptionResponse>>, t: Throwable) {
            }
        })
        return result
    }

    fun getConsumptionsByRange(startDate: String, endDate: String, patientId: Int): ArrayList<ConsumptionResponse>? {
        return service.fetchConsumptionsByRange(authHeader = authHeader, patientId = patientId, startDate = startDate,
        endDate = endDate).execute().body()
    }

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
                } else {

                }
            }
            override fun onFailure(call: Call<ArrayList<FoodResponse>>, t: Throwable) {

            }
        })
        return result
    }

    private val _foodsState: MutableStateFlow<FoodsState> = MutableStateFlow(FoodsState.Empty)
    val foodsState: StateFlow<FoodsState> = _foodsState

    sealed class FoodsState {
        data class Success(val data: ArrayList<Food>) : FoodsState()
        data class Error(val message: String) : FoodsState()
        object Loading: FoodsState()
        object Empty: FoodsState()
    }

    // used for getting foods products for meals
    fun getFoods(ids: ArrayList<Int>) {
        _foodsState.value = FoodsState.Loading
        var foodList: ArrayList<Food> = arrayListOf()

        service.getFoods(authHeader = authHeader, foodName = ids).enqueue(object: Callback<ArrayList<FoodResponse>> {
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

    fun getMeals(): ArrayList<Meal>? {
        return service.getMeals(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id).execute().body()
    }

    private val _mealState: MutableStateFlow<MealState> = MutableStateFlow(MealState.Empty)
    val mealState: StateFlow<MealState> = _mealState

    sealed class MealState {
        data class Success(val data: Meal) : MealState()
        data class Error(val message: String) : MealState()
        object Loading: MealState()
        object Empty: MealState()
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

//    private val _mealFoodState: MutableStateFlow<State> = MutableStateFlow(State.Empty)
//    val mealFoodState: StateFlow<State> = _mealFoodState

    fun createMealFood(mealFood: MealFood) {
//        _mealFoodState.value = State.Loading
        service.createMealFood(authHeader = authHeader, body = mealFood).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                if (response.isSuccessful && response.body() != null) {
//                    _mealFoodState.value = State.Success
//                } else {
//                    _mealFoodState.value = State.Empty
//                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
//                _mealFoodState.value = State.Error(t.message!!)
            }
        })
    }

    // TODO: update functionality, add stateflow
    fun deleteMeal(id: Int) {
        service.deleteMeal(authHeader = authHeader, patientId = preferences.getInt("patient", 0), mealId = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }
}
