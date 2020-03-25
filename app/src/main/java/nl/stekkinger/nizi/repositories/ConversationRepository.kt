package nl.stekkinger.nizi.repositories

import android.content.Context

import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ConversationRepository : Repository() {

    private val TAG = "ConversationRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    // patient calls

    fun getConversations(): ArrayList<Conversation>? {
        // TODO: temp, untill api fix, will remove dates from call
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val date = calendar.time
        val format = SimpleDateFormat("yyyy MM dd")
        val dateOutput = format.format(date)
        val endDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        return service.fetchConversations(authHeader = authHeader, patientId = preferences.getInt("patient", 0), beginDate = dateOutput, endDate = endDate).execute().body()
    }

    // doctor calls
}
