package nl.stekkinger.nizi.repositories

import android.util.Log
import android.util.Log.d
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class FoodRepository : Repository() {

    private val TAG = "FoodRepository"

    fun getDiary(startDate: String, endDate: String): MutableLiveData<ArrayList<ConsumptionResponse>> {
        val result = MutableLiveData<ArrayList<ConsumptionResponse>>()
        val date: String = startDate + "T11:00:00.000Z"
        // todo: fix date
//        d("logV", GeneralHelper.getUser().patient?.id.toString() + " " +authHeader + " " + preferences.getInt("patient", 0).toString() + " " + startDate + " " + endDate)
        val gson = Gson()
        val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
        val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)
        d("log weight", "derp" + weightUnitHolder.weightUnits[3].unit)
        service.fetchConsumptions(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id, date = date).enqueue(object: Callback<ArrayList<ConsumptionResponse>> {
            override fun onResponse(call: Call<ArrayList<ConsumptionResponse>>, response: Response<ArrayList<ConsumptionResponse>>) {
                if (response.isSuccessful) {
                    result.value = response.body()
                    d("succ", response.code().toString())
                    d("succ", response.body().toString())
                } else {
                    d("resp", response.code().toString())
                    d("rene", "response, not succesfull: ${response}")
                }
            }
            override fun onFailure(call: Call<ArrayList<ConsumptionResponse>>, t: Throwable) {
                d("rene", t.toString())
            }
        })
        return result
    }

    fun getConsumptionsForDietary(date: String, patientId: Int): ArrayList<ConsumptionResponse>? {
        return service.fetchConsumptions(authHeader = authHeader, patientId = patientId, date = date).execute().body()
    }

    fun getConsumptionsForDietaryByWeek(startDate: String, endDate: String, patientId: Int): ArrayList<ConsumptionResponse>? {
        return service.fetchConsumptionsByWeek(authHeader = authHeader, patientId = patientId, startDate = startDate,
        endDate = endDate).execute().body()
    }

    fun deleteConsumption(id: Int) {
        service.deleteConsumption(authHeader = authHeader, consumptionId = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    d("del", response.code().toString())
                } else {
                    d("del", response.code().toString())
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                d("rene", "onFailure")
            }
        })
    }

    fun searchFood(search: String): MutableLiveData<ArrayList<Food>?> {
        var foodList: ArrayList<Food> = arrayListOf()
        val result = MutableLiveData<ArrayList<Food>?>()

        service.searchFoodDB(authHeader = authHeader, foodName = search).enqueue(object: Callback<ArrayList<FoodResponse>> {
            override fun onResponse(call: Call<ArrayList<FoodResponse>>, response: Response<ArrayList<FoodResponse>>) {
                if (response.isSuccessful && response.body() != null) {

                    for (foodResponse: FoodResponse in response.body()!!) {
                        Log.i("onResponse", foodResponse.toString())

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
                            weight_amount = foodResponse.food_meal_component.portion_size, // Todo: there is no amount yet
                            image_url = foodResponse.food_meal_component.image_url
                        )
                        foodList.add(food)
                    }
                    result.value = foodList
                } else {
                    d("rene", "response, not succesfull: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<ArrayList<FoodResponse>>, t: Throwable) {
                d("rene", "onFailure")
            }
        })
        return result
    }

    fun addConsumption(consumption: Consumption) {
        service.addConsumption(authHeader = authHeader, body = consumption).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                d("con", response.toString())
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }

    fun editConsumption(consumption: Consumption) {
        service.editConsumption(authHeader = authHeader, body = consumption).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                d("con", response.toString())
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }

    fun getMeals(): ArrayList<Meal>? {
        return service.getMeals(authHeader = authHeader, patientId = preferences.getInt("patient", 0)).execute().body()
    }

    fun getFavorites(): ArrayList<MyFoodResponse>? {
        d("tess", "tess")
        return service.getFavoriteFood(authHeader = authHeader, patientId = GeneralHelper.getUser().patient!!.id).execute().body()
    }

    fun addFavorite(id: Int) {
        val myFoodRequest = MyFoodRequest(
            food = id,
            patients_ids = GeneralHelper.getUser().patient!!.id
        )
        service.addFavoriteFood(authHeader = authHeader, body = myFoodRequest).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }

    fun deleteFavorite(id: Int) {
        service.deleteFavoriteFood(authHeader = authHeader, id = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                d("succ", response.toString())
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                d("fail", "onFailure")
            }
        })
    }


    fun createMeal(meal: Meal) {
        service.createMeal(authHeader = authHeader, patientId = preferences.getInt("patient", 0), body = meal).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                d("SMEAL", response.toString())
                d("SMEAL", response.message())
                d("SMEAL", response.isSuccessful.toString())
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                d("SMEAL", "fail")
            }
        })
    }

    fun deleteMeal(id: Int) {
        service.deleteMeal(authHeader = authHeader, patientId = preferences.getInt("patient", 0), mealId = id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }
}
