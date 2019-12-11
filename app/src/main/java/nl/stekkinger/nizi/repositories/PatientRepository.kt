package nl.stekkinger.nizi.repositories

import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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

        val account = Account(20, "Patient")
        val patient = Patient(57, 57, doctorId = doctorId, firstName = firstName, lastName = lastName, dateOfBirth = dateOfBirth, weightInKilograms = weight, guid = " ")
        val doctor = Doctor(doctorId, "Dr", "Pepper", "Maastricht")
        val authLogin = AuthLogin(" ", Token(" "," "))

        val newPatientLogin = PatientLogin(account, patient, doctor, authLogin)

        // DoB = "YYYY-MM-DDT10:55:38.738Z"
        service.registerPatient(authHeader, newPatientLogin).execute()//.enqueue(loginAsPatientCallback)
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