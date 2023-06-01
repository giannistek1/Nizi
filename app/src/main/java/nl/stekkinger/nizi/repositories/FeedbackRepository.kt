package nl.stekkinger.nizi.repositories

import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList

class FeedbackRepository : Repository() {

    private val TAG = "FeedbackRepository"

    fun addFeedback(feedback: FeedbackShort): Feedback? {
        return service.addFeedback(authHeader, feedback).execute().body()
    }

    fun getFeedbacks(patientId: Int): ArrayList<Feedback>? {
        return service.fetchFeedbacks(authHeader = authHeader, patientId = patientId, sortProp = "date:ASC").execute().body()
    }

    //region Mockup
    fun addFeedbackLocally(feedbackShort: FeedbackShort) : Feedback {
        val patientShort = LocalDb.patientsShort.firstOrNull() { it.id == feedbackShort.patient }
        val doctorShort = LocalDb.doctorsShort.firstOrNull() { it.id == feedbackShort.doctor }


        // TODO: Could be in a converter
        val feedback = Feedback(
            id = feedbackShort.id,
            title = feedbackShort.title,
            comment = feedbackShort.comment,
            date = SimpleDateFormat("yyyy-MM-dd").parse(feedbackShort.date)!!,
            is_read = feedbackShort.is_read,
            patient = patientShort!!,
        doctorShort = doctorShort!!)

        LocalDb.feedbacks.add(feedback)

        return feedback;
    }
    //endregion
}
