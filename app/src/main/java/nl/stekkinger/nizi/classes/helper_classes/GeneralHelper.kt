package nl.stekkinger.nizi.classes.helper_classes

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.custom_toast.view.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.login.Role
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
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
    const val EXTRA_ORIGINAL_EMAIL = "ORIGINAL_EMAIL"
    const val EXTRA_DIETARY = "DIETARY"
    const val EXTRA_FOOD_ADDED = "FOOD_ADDED"

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

    fun getWeightUnitHolder() : WeightUnitHolder?
    {
        return if (prefs.contains(PREF_WEIGHT_UNIT)) {
            val json: String = prefs.getString(PREF_WEIGHT_UNIT, "")!!
            val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)

            weightUnitHolder
        } else {
            return null
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

    fun makeToast(cont: Context, customToastLayout: View, message: String)
    {
        // Sadly cant use animations on the android toast
        val toast: Toast = Toast.makeText(cont, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        customToastLayout.toast_text.text = message
        customToastLayout.visibility = View.VISIBLE
        toast.view = customToastLayout

        toast.show()
    }

    fun showAnimatedToast(toastView: View, toastAnimation: Animation, message: String)
    {
        toastView.toast_text.text = message // Could also be context.getString(id) but its easier to give a string instead of context and id, because you can test that quicker
        toastView.visibility = View.VISIBLE
        toastView.startAnimation(toastAnimation)
    }

    @SuppressLint("NewApi")
    fun hasInternetConnection(context: Context, toastView: View, toastAnimation: Animation) : Boolean
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
        showAnimatedToast(toastView, toastAnimation, context.getString(R.string.no_internet_connection))
        return false
    }
}