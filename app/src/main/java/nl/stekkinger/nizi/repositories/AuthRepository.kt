package nl.stekkinger.nizi.repositories

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import nl.stekkinger.nizi.activities.ForgotPasswordActivity
import nl.stekkinger.nizi.activities.LoginActivity
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.*
import nl.stekkinger.nizi.classes.password.ForgotPasswordRequest
import nl.stekkinger.nizi.classes.password.ForgotPasswordResponse
import nl.stekkinger.nizi.classes.password.ResetPasswordRequest
import nl.stekkinger.nizi.classes.password.ResetPasswordResponse

class AuthRepository : Repository() {

    private val TAG = "AuthRepository"

    // Code 400 gives body = null back
    fun login(loginRequest: LoginRequest) : LoginResponse? {
        return service.login(loginRequest).execute().body()
    }

    fun logout(context: Context, activity: AppCompatActivity) {
        val newIntent = Intent(context, LoginActivity::class.java)
        preferences.edit().remove(GeneralHelper.PREF_TOKEN).apply()
        preferences.edit().remove(GeneralHelper.PREF_USER).apply()
        preferences.edit().remove(GeneralHelper.PREF_IS_DOCTOR).apply()
        preferences.edit().remove(GeneralHelper.PREF_DOCTOR_ID).apply()

        activity.startActivity(newIntent)
        activity.finish()
    }

    fun getUsers() : ArrayList<UserLogin>? {
        return service.getUsers(authHeader).execute().body()
    }

    fun registerUser(user: User) : UserLogin? {
        return service.registerUser(authHeader, user).execute().body()
    }

    fun updateUser(user: User) : UserLogin? {
        return service.updateUser(authHeader, user.id, user).execute().body()
    }

    // Forgot password
    fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse? {
        return service.forgotPassword(forgotPasswordRequest).execute().body()
    }

    // Reset password
    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse? {
        return service.resetPassword(resetPasswordRequest).execute().body()
    }

}