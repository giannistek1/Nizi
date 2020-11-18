package nl.stekkinger.nizi.repositories

import android.content.Context
import android.util.Log.d

import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.old.Conversation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FeedbackRepository : Repository() {

    private val TAG = "FeedbackRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString(GeneralHelper.PREF_TOKEN, null)
    private val authHeader = "Bearer " + accessToken

    fun getFeedbacks(patientId: Int): ArrayList<Feedback>? {
        return service.fetchFeedbacks(authHeader = authHeader, patientId = patientId).execute().body()
    }

    fun getConversations(): ArrayList<Conversation>? {
        // TODO: temp, untill api fix, will remove dates from call
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val date = calendar.time
        val format = SimpleDateFormat("yyyy MM dd")
        val dateOutput = format.format(date)
        val endDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        d("Call", endDate)
        return service.fetchConversations(authHeader = authHeader, patientId = preferences.getInt("patient", 0), beginDate = dateOutput, endDate = endDate).execute().body()
    }

//    fun getConversations(): ArrayList<String>? {
//        // TODO: temp, untill api fix, will remove dates from call
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.MONTH, -1)
//        val date = calendar.time
//        val format = SimpleDateFormat("yyyy MM dd")
//        val dateOutput = format.format(date)
//        val endDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
//        d("Call", endDate)
//        return service.fetchConversations(authHeader = authHeader, patientId = preferences.getInt("patient", 0), beginDate = dateOutput, endDate = endDate).execute().body()
//    }

    // doctor calls
}
