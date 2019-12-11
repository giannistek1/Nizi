package nl.stekkinger.nizi.repositories

import android.content.Context
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

// Patient:
//HttpResponse<String> response = Unirest.post("https://appnizi.eu.auth0.com/oauth/token")
//  .header("content-type", "application/json")
//  .body("{\"client_id\":\"dVYtmSw5m819mX2nS2raMZwo5lXcwDg6\",\"client_secret\":\"vN6N5HNG25MP-gjBPsVhf01dzuIPqAixFFImtGUU4vy4RuJwFEYcPnJg4r6EOOdr\",\"audience\":\"appnizi.nl/api\",\"grant_type\":\"client_credentials\"}")
//  .asString();

class AuthRepository {

    private val TAG = "AuthRepository"

    val client_id = "dVYtmSw5m819mX2nS2raMZwo5lXcwDg6"
    val client_secret = "vN6N5HNG25MP-gjBPsVhf01dzuIPqAixFFImtGUU4vy4RuJwFEYcPnJg4r6EOOdr"
    val audience = "appnizi.nl/api"
    val grant_type = "client_credentials"
    val test_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5ERkdPRFUxTnpJNFJEZ3lNakkxUmtFMU5EZ3dRMEUxTkVJM05UTTBSRGRFUTBFNE5FWkdNZyJ9.eyJpc3MiOiJodHRwczovL2FwcG5pemkuZXUuYXV0aDAuY29tLyIsInN1YiI6ImRWWXRtU3c1bTgxOW1YMm5TMnJhTVp3bzVsWGN3RGc2QGNsaWVudHMiLCJhdWQiOiJhcHBuaXppLm5sL2FwaSIsImlhdCI6MTU3NTc0OTM4MywiZXhwIjoxNTc1ODM1NzgzLCJhenAiOiJkVll0bVN3NW04MTltWDJuUzJyYU1ad281bFhjd0RnNiIsImd0eSI6ImNsaWVudC1jcmVkZW50aWFscyJ9.B5SrwGFKykFMmae9zJi6e3TIpnWTIWycjT6IbOl2_2DLDrsbCzQv6Ny3jBdo-9qiJ4J8A1oeMe6qrsslKFfQDSz77hedsJRoTWNCHWnPle1gCgB4rbNvG1cnHo0vJZ2npSx0qJIzSEza0TZGVQ4soGzYiSADuBxLsws_bLKN3aXfk8ECoOKEiPeNo-5ZFmUprzhp9haxOahH5IrLXgMmRwPNKYKYgiavMhoiYDXkJIZoHmUABIrfQAwJ54XPLu6FfXQKJcMxCdr_Yd5G_0qxFVtNyZOBGHV1mPYlf2f0BoEE_ua7sWsjIJLsu8rhyzL8D15HNwY4RAtJRfB0D-F4Vw"

    var preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    var accessToken = preferences.getString("TOKEN", null)

    fun loginAsPatient() : PatientLogin?
    {
        var authHeader = "Bearer " + accessToken

        return service.loginAsPatient(authHeader).execute().body()//.enqueue(loginAsPatientCallback)
    }

    fun loginAsDoctor() : DoctorLogin?
    {
        var authHeader = "Bearer " + accessToken

        return service.loginAsDoctor(authHeader).execute().body()//.enqueue(loginAsPatientCallback)
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