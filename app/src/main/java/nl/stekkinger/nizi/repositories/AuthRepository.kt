package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.Intent
import android.util.Log.d
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.activities.LoginActivity
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository : Repository() {

    private val TAG = "AuthRepository"

    private val prefs = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = prefs.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken


    // Code 400 gives body = null back
    fun login(loginRequest: LoginRequest) : LoginResponse? {
        return service.login(loginRequest).execute().body()
    }

    fun logout(context: Context, activity: AppCompatActivity) {
        val newIntent = Intent(context, LoginActivity::class.java)
        prefs.edit().remove(GeneralHelper.PREF_IS_DOCTOR).apply()
        prefs.edit().remove(GeneralHelper.PREF_TOKEN).apply()

        activity.startActivity(newIntent)
        activity.finish()
    }
}