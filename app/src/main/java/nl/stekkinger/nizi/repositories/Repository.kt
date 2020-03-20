package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class Repository
{
    val service: ApiService = getApiService()

    private fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://appniziapi.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}