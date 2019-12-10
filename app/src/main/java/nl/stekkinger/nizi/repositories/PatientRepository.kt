package nl.stekkinger.nizi.repositories

import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 1e API TEST = Patiënt
// 3e API TEST = Dcotor


class PatientRepository {

    private val TAG = "PatientRepository"

    fun registerPatient(accessToken: String, firstName: String, lastName: String, dateOfBirth: String, weight: Float, doctorId: Int)
    {
        var authHeader = "Bearer " + accessToken

        // DoB = "YYYY-MM-DDT10:55:38.738Z"
        service.registerPatient(authHeader, firstName, lastName, dateOfBirth, weight, doctorId).execute().body()//.enqueue(loginAsPatientCallback)
    }


    fun getPatientsFromDoctor(accessToken: String, doctorId: Int) : List<Patient>?
    {
        var authHeader = "Bearer " + accessToken

        return service.getPatientsFromDoctor(authHeader, doctorId).execute().body()//.enqueue(loginAsPatientCallback)
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