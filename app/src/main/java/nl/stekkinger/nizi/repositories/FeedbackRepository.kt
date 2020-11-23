package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import kotlin.collections.ArrayList

class FeedbackRepository : Repository() {

    private val TAG = "FeedbackRepository"

    fun addFeedback(feedback: FeedbackShort): Feedback? {
        return service.addFeedback(authHeader, feedback).execute().body()
    }

    fun getFeedbacks(patientId: Int): ArrayList<Feedback>? {
        return service.fetchFeedbacks(authHeader = authHeader, patientId = patientId).execute().body()
    }

    /*fun getConversations(): ArrayList<Conversation>? {
        // TODO: temp, untill api fix, will remove dates from call
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val date = calendar.time
        val format = SimpleDateFormat("yyyy MM dd")
        val dateOutput = format.format(date)
        val endDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        d("Call", endDate)
        return service.fetchConversations(authHeader = authHeader, patientId = preferences.getInt("patient", 0), beginDate = dateOutput, endDate = endDate).execute().body()
    }*/

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
}
