package nl.stekkinger.nizi.repositories

import android.content.Context

import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class ConversationRepository : Repository() {

    private val TAG = "ConversationRepository"

    private val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
    private val accessToken = preferences.getString("TOKEN", null)
    private val authHeader = "Bearer " + accessToken

    // patient calls

//    fun getConversations(): ArrayList<Conversation>? {
//        return service.fetchConversations(authHeader = authHeader, patientId = preferences.getInt("patient", 0), beginDate = "", endDate = "").execute().body()
//    }

    // doctor calls
}
