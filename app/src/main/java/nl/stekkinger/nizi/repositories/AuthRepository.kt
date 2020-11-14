package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.activities.LoginActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.LoginRequest
import nl.stekkinger.nizi.classes.login.LoginResponse
import nl.stekkinger.nizi.classes.login.User

class AuthRepository : Repository() {

    private val TAG = "AuthRepository"

    private val prefs = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = prefs.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    fun registerUser(registerUser: User) {
        service.registerUser(authHeader, registerUser).execute().body()
    }

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