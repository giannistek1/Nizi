package nl.stekkinger.nizi.repositories

import android.util.Log
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.classes.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodRepository {

    private val TAG = "AuthRepository"

    val client_id = "dVYtmSw5m819mX2nS2raMZwo5lXcwDg6"
    val client_secret = "vN6N5HNG25MP-gjBPsVhf01dzuIPqAixFFImtGUU4vy4RuJwFEYcPnJg4r6EOOdr"
    val audience = "appnizi.nl/api"
    val grant_type = "client_credentials"
    val test_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5ERkdPRFUxTnpJNFJEZ3lNakkxUmtFMU5EZ3dRMEUxTkVJM05UTTBSRGRFUTBFNE5FWkdNZyJ9.eyJpc3MiOiJodHRwczovL2FwcG5pemkuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDVkZGZiM2I5MjlmOWNkMGUzMjA0MDViZiIsImF1ZCI6WyJhcHBuaXppLm5sL2FwaSIsImh0dHBzOi8vYXBwbml6aS5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNTc1OTY2ODk0LCJleHAiOjE1NzYwNTMyOTQsImF6cCI6InU5MzdvZncwQllQR1VIUGdOVXJmQnBzcElhMFZTQzg2Iiwic2NvcGUiOiJvcGVuaWQifQ.Vd3PdGhs0slRbkhbHkHwB_BM-Ch5Tij497cZJqM29FpOLI8DZMNpG0yeJSS2PJXRWrst8kcHf7VhZvvC22kttRCcGiDum-_1wBvfXT4sB0k5xMh75hRufV5doN47cLR6wXAnt7OzjI5u5huoxcG1afKo1hCkrHOz7v-liSUGqXbbj5sKAA0Krm640_eQKY4bjyU2VkOduZbgyDo0DZ_tzJ9xHutPxhEDA_TkTmyRgSeRey5di3ZUunf0-nw1lN6Xs7lMYHowPe39-u0EIW8f90aY8kmsVC2LM3kUr9w7w0gzd3ebXK00IJhwlwcTMMAXmWZeNUACPg144VS1IfXZCQ"

    fun searchFood(search: String): MutableLiveData<ArrayList<Food>?> {
        var foodList: ArrayList<Food> = arrayListOf()
        val result = MutableLiveData<ArrayList<Food>?>()

        var authHeader = "Bearer " + test_token

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


    private val service: ApiService = getApiService()

    private fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://appnizi-api.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
