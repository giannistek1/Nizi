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
import nl.stekkinger.nizi.classes.login.UserLogin

class AuthRepository : Repository() {

    private val TAG = "AuthRepository"

    private val prefs = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = prefs.getString(GeneralHelper.PREF_TOKEN, null)
    private val authHeader = "Bearer " + accessToken

    // Code 400 gives body = null back
    fun login(loginRequest: LoginRequest) : LoginResponse? {
        return service.login(loginRequest).execute().body()
    }

    fun logout(context: Context, activity: AppCompatActivity) {
        val newIntent = Intent(context, LoginActivity::class.java)
        prefs.edit().remove(GeneralHelper.PREF_TOKEN).apply()
        prefs.edit().remove(GeneralHelper.PREF_USER).apply()
        prefs.edit().remove(GeneralHelper.PREF_IS_DOCTOR).apply()
        prefs.edit().remove(GeneralHelper.PREF_DOCTOR_ID).apply()

        activity.startActivity(newIntent)
        activity.finish()
    }

    fun registerUser(user: User) : UserLogin? {
        return service.registerUser(authHeader, user).execute().body()
    }

    fun updateUser(user: User) : UserLogin? {
        return service.updateUser(authHeader, user).execute().body()
    }
}