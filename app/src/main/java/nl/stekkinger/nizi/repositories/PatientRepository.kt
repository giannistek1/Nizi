package nl.stekkinger.nizi.repositories

import android.content.Context
import android.util.Log
import android.util.Log.d
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import nl.stekkinger.nizi.ApiService
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 1e API TEST = Patiënt
// 3e API TEST = Dcotor


class PatientRepository : Repository(){

    private val TAG = "PatientRepository"

    var preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString("TOKEN", null)
    var authHeader = "Bearer " + accessToken

    fun registerPatient(firstName: String, lastName: String, dateOfBirth: String, weight: Float, doctorId: Int)
    {
        val account = Account(20, "Patient")
        val patient = Patient(57, 57, doctorId = doctorId, firstName = firstName, lastName = lastName, dateOfBirth = dateOfBirth, weightInKilograms = weight, guid = " ")
        val doctor = Doctor(doctorId, "Dr", "Pepper", "Maastricht")
        val authLogin = AuthLogin(" ", Token(" "," "))

        val newPatientLogin = PatientLogin(account, patient, doctor, authLogin)

        // DoB = "YYYY-MM-DDT10:55:38.738Z"
        service.registerPatient(authHeader, newPatientLogin).execute()//.enqueue(loginAsPatientCallback)
    }

    fun getPatientsFromDoctor(doctorId: Int) : List<Patient>?
    {
        return service.getPatientsFromDoctor(authHeader, doctorId).execute().body()//.enqueue(loginAsPatientCallback)
    }

    fun getPatientsFromDoctor2(doctorId: Int) : MutableLiveData<ArrayList<Patient>?> {
        var patientList: ArrayList<Patient> = arrayListOf()
        val result = MutableLiveData<ArrayList<Patient>?>()

        service.getPatientsFromDoctor(authHeader = authHeader, doctorId = doctorId).enqueue(object: Callback<ArrayList<Patient>> {
            override fun onResponse(call: Call<ArrayList<Patient>>, response: Response<ArrayList<Patient>>) {
                if (response.isSuccessful && response.body() != null) {
                    for (patient: Patient in response.body()!!) {
                        Log.i(TAG, patient.toString())
                        patientList.add(patient)
                    }
                    result.value = patientList
                } else {
                    d(TAG, "response, not successful: ${response.body()}")
                }
            }
            override fun onFailure(call: Call<ArrayList<Patient>>, t: Throwable) {
                d(TAG, "onFailure")
            }
        })
        return result
    }
}