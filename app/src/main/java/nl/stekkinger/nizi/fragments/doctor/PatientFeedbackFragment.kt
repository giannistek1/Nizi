package nl.stekkinger.nizi.fragments.doctor


import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_conversation.view.*
import kotlinx.android.synthetic.main.fragment_patient_feedback.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.repositories.FeedbackRepository

/**
 * A simple [Fragment] subclass.
 */
class PatientFeedbackFragment(private val patientData: PatientData) : Fragment() {

    private val mRepository: FeedbackRepository = FeedbackRepository()
    private lateinit var adapter: ConversationAdapter

    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Setup UI
        val view: View = inflater.inflate(R.layout.fragment_patient_feedback, container, false)
        loader = view.fragment_patient_feedback_loader

        val patientName = "${patientData.user.first_name.first()}. ${patientData.user.last_name}"
        view.fragment_patient_feedback_txt_adviceFor.text = getString(R.string.advice_from, patientName)

        // Setup RV
        val recyclerView: RecyclerView = view.fragment_patient_feedback_rv
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ConversationAdapter()
        recyclerView.adapter = adapter

        // Get feedback
        getConversationsAsyncTask().execute()

        return view
    }

    inner class getConversationsAsyncTask : AsyncTask<Void, Void, ArrayList<Feedback>>() {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): ArrayList<Feedback>? {
            return mRepository.getFeedbacks(patientData.patient.id)
        }

        override fun onPostExecute(result: ArrayList<Feedback>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(activity, R.string.get_feedbacks_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(activity, R.string.fetched_feedbacks, Toast.LENGTH_SHORT).show()

            adapter.setConversationList(result)
        }
    }
}
