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

    val test_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5ERkdPRFUxTnpJNFJEZ3lNakkxUmtFMU5EZ3dRMEUxTkVJM05UTTBSRGRFUTBFNE5FWkdNZyJ9.eyJpc3MiOiJodHRwczovL2FwcG5pemkuZXUuYXV0aDAuY29tLyIsInN1YiI6ImRWWXRtU3c1bTgxOW1YMm5TMnJhTVp3bzVsWGN3RGc2QGNsaWVudHMiLCJhdWQiOiJhcHBuaXppLm5sL2FwaSIsImlhdCI6MTU3NTc0OTM4MywiZXhwIjoxNTc1ODM1NzgzLCJhenAiOiJkVll0bVN3NW04MTltWDJuUzJyYU1ad281bFhjd0RnNiIsImd0eSI6ImNsaWVudC1jcmVkZW50aWFscyJ9.B5SrwGFKykFMmae9zJi6e3TIpnWTIWycjT6IbOl2_2DLDrsbCzQv6Ny3jBdo-9qiJ4J8A1oeMe6qrsslKFfQDSz77hedsJRoTWNCHWnPle1gCgB4rbNvG1cnHo0vJZ2npSx0qJIzSEza0TZGVQ4soGzYiSADuBxLsws_bLKN3aXfk8ECoOKEiPeNo-5ZFmUprzhp9haxOahH5IrLXgMmRwPNKYKYgiavMhoiYDXkJIZoHmUABIrfQAwJ54XPLu6FfXQKJcMxCdr_Yd5G_0qxFVtNyZOBGHV1mPYlf2f0BoEE_ua7sWsjIJLsu8rhyzL8D15HNwY4RAtJRfB0D-F4Vw"

    fun registerPatient(accessToken: String)
    {
        var authHeader = "Bearer " + accessToken

        // DoB = "YYYY-MM-DDT10:55:38.738Z"
        service.registerPatient(authHeader, "Gianni", "Stekkinger", "1998-12-06T10:55:38.738Z", 60f, 1).execute().body()//.enqueue(loginAsPatientCallback)
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