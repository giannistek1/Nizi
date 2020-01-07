package nl.stekkinger.nizi.repositories

import android.content.Context
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.MutableLiveData
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class FoodRepository : Repository() {

    private val TAG = "FoodRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    fun getDiary(startDate: String, endDate: String): MutableLiveData<Consumptions.Result> {
        val result = MutableLiveData<Consumptions.Result>()
        d("logV", authHeader + " " + preferences.getInt("patient", 0).toString() + " " + startDate + " " + endDate)

        service.fetchConsumptions(authHeader = authHeader, patientId = preferences.getInt("patient", 0), startDate = startDate, endDate = endDate ).enqueue(object: Callback<Consumptions.Result> {
            override fun onResponse(call: Call<Consumptions.Result>, response: Response<Consumptions.Result>) {
                if (response.isSuccessful) {
                    result.value = response.body()
                    d("succ", response.code().toString())
                    d("succ", response.body().toString())
                } else {
                    d("resp", response.code().toString())
                    d("rene", "response, not succesfull: ${response}")
                }
            }
            override fun onFailure(call: Call<Consumptions.Result>, t: Throwable) {
                d("rene", "onFailure")
            }
        })
        return result
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

        service.searchFoodDB(authHeader = authHeader, foodName = search, count = 10).enqueue(object: Callback<ArrayList<Food>> {
            override fun onResponse(call: Call<ArrayList<Food>>, response: Response<ArrayList<Food>>) {
                if (response.isSuccessful && response.body() != null) {
                    for (food: Food in response.body()!!) {
                        Log.i("onResponse", food.toString())
                        foodList.add(food)
                    }
                    result.value = foodList
                } else {
                    d("rene", "response, not succesfull: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<ArrayList<Food>>, t: Throwable) {
                d("rene", "onFailure")
            }
        })
        return result
    }

    fun addConsumption(consumption: Consumption) {

        val con = "{ \"FoodName\": \"brood (bruin)\", \"KCal\": 62, \"Protein\": 1, \"Fiber\": 1, \"Calium\": 1, \"Sodium\": 1, \"Amount\": 1, \"WeightUnitId\": 1, \"Date\": \"2020-01-04T00:00:00\", \"PatientId\": 56, \"Id\": 0 }"
//        val body = RequestBody.create(MediaType.parse("text/plain"), consumption)
        service.addConsumption(authHeader = authHeader, body = consumption).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    d("resp", response.code().toString())
                    d("rene", consumption.toString())
                    d("rene", con)
                } else {
                    d("resp", response.code().toString())
                    d("rene", consumption.toString())
                    d("rene", con)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                d("rene", "onFailure")
            }
        })
    }

    fun getMeals(): ArrayList<Meal>? {
        d("hi", service.getMeals(authHeader = authHeader, patientId = preferences.getInt("patient", 0)).execute().toString())
        return service.getMeals(authHeader = authHeader, patientId = preferences.getInt("patient", 0)).execute().body()
    }
}
