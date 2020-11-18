package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.SharedPreferences
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class Repository
{
    var preferences: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString(GeneralHelper.PREF_TOKEN, null)
    var authHeader = "Bearer " + accessToken

    val service: ApiService = getApiService()

    private fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://104.45.16.76:1337/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}