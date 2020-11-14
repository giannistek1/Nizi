package nl.stekkinger.nizi.classes.helper_classes

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.login.Role
import nl.stekkinger.nizi.classes.login.UserLogin

object GeneralHelper {
    const val PREF_TOKEN = "TOKEN"
    const val PREF_USER = "USER"
    const val PREF_IS_DOCTOR = "IS_DOCTOR"
    const val PREF_DOCTOR_ID = "DOCTOR_ID"

    val prefs: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

    fun getUser() : UserLogin
    {
        val gson = Gson()
        return when(prefs.contains(PREF_TOKEN)) {
            true -> {val json: String = prefs.getString(PREF_USER, "")!!
                    gson.fromJson(json, UserLogin::class.java)}
            false -> UserLogin(
                id = 0, username = "", email = "", first_name = "", last_name = "", provider = "",
                confirmed = false, blocked = false,
                //patient = Patient(id = 0, gender = "Male", dateOfBirth = "", ),
                patient = null,
                //doctor = Doctor(),
                doctor = null,
                role = Role(0, "", "", ""),
                created_at = "", updated_at = ""
            )
        }
    }

    fun getDoctorId() : Int
    {
        return prefs.getInt(PREF_DOCTOR_ID, 0)
    }
}