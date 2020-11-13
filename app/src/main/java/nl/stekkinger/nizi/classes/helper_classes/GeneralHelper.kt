package nl.stekkinger.nizi.classes.helper_classes

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.user.Role
import nl.stekkinger.nizi.classes.user.User

object GeneralHelper {
    const val PREF_IS_DOCTOR = "IS_DOCTOR"
    const val PREF_TOKEN = "TOKEN"

    val prefs: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

    fun getUser() : User
    {
        val gson = Gson()
        return when(prefs.contains(PREF_TOKEN)) {
            true -> {val json: String = prefs.getString("USER", "")!!
                    gson.fromJson(json, User::class.java)}
            false -> User(
                id = 0, username = "", email = "", provider = "",
                confirmed = false, blocked = false,
                role = Role(0, "", "", ""),
                created_at = "", updated_at = ""
            )
        }
    }
}