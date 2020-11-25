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
        return service.fetchFeedbacks(authHeader = authHeader, patientId = patientId, sortProp = "date:ASC").execute().body()
    }
}
