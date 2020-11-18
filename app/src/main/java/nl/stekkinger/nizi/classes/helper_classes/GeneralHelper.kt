package nl.stekkinger.nizi.classes.helper_classes

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.widget.Toast
import com.google.gson.Gson
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.login.Role
import nl.stekkinger.nizi.classes.login.UserLogin
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object GeneralHelper {
    const val PREF_TOKEN = "TOKEN"
    const val PREF_USER = "USER"
    const val PREF_IS_DOCTOR = "IS_DOCTOR"
    const val PREF_DOCTOR_ID = "DOCTOR_ID"

    const val EXTRA_PATIENT = "PATIENT"
    const val EXTRA_DOCTOR_ID = "DOCTOR_ID"

    val prefs: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

    fun getUser() : UserLogin
    {
        val gson = Gson()
        return if (prefs.contains(PREF_TOKEN) && prefs.contains(PREF_USER)) {
            val json: String = prefs.getString(PREF_USER, "")!!
            gson.fromJson(json, UserLogin::class.java)
        } else {
            UserLogin(
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

    fun getDateFormat() : SimpleDateFormat
    {
        //val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }

    fun hasInternetConnection(context: Context) : Boolean
    {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val hasConnection = connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false

        if (!hasConnection)
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()

        return hasConnection
    }

}