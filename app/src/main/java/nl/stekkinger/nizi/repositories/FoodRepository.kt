package nl.stekkinger.nizi.repositories

import android.app.Application
import android.app.PendingIntent.getActivity
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodRepository : Repository() {

    private val TAG = "FoodRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

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
}
