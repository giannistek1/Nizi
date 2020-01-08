package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.activities.LoginActivity
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class DietaryRepository : Repository() {

    private val TAG = "DietaryRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    fun getDietary(patientId: Int) : DietaryView? {
        return service.getDietary(authHeader, patientId).execute().body()

        //var result = DietaryView(listOf(Restrictions(0, "")), listOf(DietaryManagementModel(id = 0, )))

        /*service.getDietary(authHeader = authHeader, patientId = patientId).enqueue(object: Callback<DietaryView> {
            override fun onResponse(call: Call<DietaryView>, response: Response<DietaryView>) {
                if (response.isSuccessful && response.body() != null) {
                    result = response.body()
                } else {
                    d(TAG, "response, not successful: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<DietaryView>, t: Throwable) {
                d(TAG, "onFailure")
            }
        })
        return result*/
    }
}