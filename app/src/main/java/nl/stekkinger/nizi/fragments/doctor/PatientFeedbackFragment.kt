package nl.stekkinger.nizi.fragments.doctor


import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_patient_feedback.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.repositories.FeedbackRepository
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class PatientFeedbackFragment(private val patientData: PatientData) : Fragment() {

    private val feedbackRepository: FeedbackRepository = FeedbackRepository()

    private var mContext: Context? = null

    private lateinit var newFeedback: FeedbackShort
    private lateinit var feedbackList: MutableList<Feedback>

    private lateinit var mNewFeedbackET: EditText
    private lateinit var mFeedbackRV: RecyclerView
    private lateinit var adapter: ConversationAdapter
    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Setup UI
        val view: View = inflater.inflate(R.layout.fragment_patient_feedback, container, false)
        loader = view.fragment_patient_feedback_loader
        mNewFeedbackET = view.fragment_patient_feedback_et_newFeedback as EditText

        val patientName = "${patientData.user.first_name.first()}. ${patientData.user.last_name}"
        view.fragment_patient_feedback_txt_adviceFor.text = getString(R.string.advice_from, patientName)

        view.fragment_patient_feedback_btn_addAdvice.setOnClickListener {

            // Guard
            if (InputHelper.inputIsEmpty(mContext!!, mNewFeedbackET, R.string.feedback_is_empty))
                return@setOnClickListener

            val sdf = GeneralHelper.getCreateDateFormat()

            // Create feedback
            newFeedback = FeedbackShort(title = getString(R.string.conversation_summary),
                comment = mNewFeedbackET.text.toString(), patient = patientData.patient.id,
                doctor = patientData.patient.doctor, date = sdf.format(Date())
            )

            addFeedbackAsyncTask().execute()
        }

        // Setup RV
        mFeedbackRV = view.fragment_patient_feedback_rv
        mFeedbackRV.layoutManager = LinearLayoutManager(activity)
        adapter = ConversationAdapter()
        mFeedbackRV.adapter = adapter

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
            return feedbackRepository.getFeedbacks(patientData.patient.id)
        }

        override fun onPostExecute(result: ArrayList<Feedback>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(activity, R.string.get_feedbacks_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(activity, R.string.fetched_feedbacks, Toast.LENGTH_SHORT).show()

            // Save result
            feedbackList = result.asReversed()
            adapter.setConversationList(feedbackList)
        }
    }

    inner class addFeedbackAsyncTask : AsyncTask<Void, Void, Feedback>() {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): Feedback? {
            return try {
                 feedbackRepository.addFeedback(newFeedback)
            }
            catch(e: Exception) {
                print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: Feedback?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(activity, R.string.add_feedback_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(activity, R.string.added_feedback, Toast.LENGTH_SHORT).show()

            // Clean input
            mNewFeedbackET.setText("")

            // Add result to current feedback list
            feedbackList.add(0, result)

            // Update RV
            mFeedbackRV.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }
}
