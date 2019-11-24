package repositories

import ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    private val service: ApiService = getApiService()

    private fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://appnizi-api.azurewebsites.net/api")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}