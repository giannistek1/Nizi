package nl.stekkinger.nizi.classes.helper_classes

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.login.Role
import nl.stekkinger.nizi.classes.login.UserLogin
import java.text.SimpleDateFormat
import java.util.*

object GeneralHelper {
    const val PREF_TOKEN = "TOKEN"
    const val PREF_USER = "USER"
    const val PREF_IS_DOCTOR = "IS_DOCTOR"
    const val PREF_DOCTOR_ID = "DOCTOR_ID"
    const val PREF_WEIGHT_UNIT = "WEIGHT_UNITS"

    const val EXTRA_PATIENT = "PATIENT"
    const val EXTRA_DOCTOR_ID = "DOCTOR_ID"
    const val EXTRA_DIETARY = "DIETARY"

    var apiIsDown = false

    val prefs: SharedPreferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getUser() : UserLogin
    {
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

    fun getCreateDateFormat() : SimpleDateFormat
    {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    fun getFeedbackDateFormat() : SimpleDateFormat
    {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    }

    @SuppressLint("NewApi")
    fun hasInternetConnection(context: Context) : Boolean
    {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        return false
    }
}