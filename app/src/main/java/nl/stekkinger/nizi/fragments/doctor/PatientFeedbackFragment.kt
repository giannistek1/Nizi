package nl.stekkinger.nizi.fragments.doctor


import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_patient_feedback.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.InputHelper
import nl.stekkinger.nizi.classes.patient.PatientData
import nl.stekkinger.nizi.fragments.BaseFragment
import nl.stekkinger.nizi.repositories.FeedbackRepository
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class PatientFeedbackFragment : BaseFragment() {

    private val feedbackRepository: FeedbackRepository = FeedbackRepository()

    private var mContext: Context? = null

    private lateinit var patientData: PatientData
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

        // Setup custom toast
        val parent: RelativeLayout = view.fragment_patient_feedback_rl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        // Sets custom toast animation for every fragment
        toastAnimation = AnimationUtils.loadAnimation(
            activity,
            R.anim.move_up_fade_out_bottom_nav
        )

        // Get patient data from bundle
        val bundle: Bundle = this.arguments!!
        patientData = bundle.getSerializable(GeneralHelper.EXTRA_PATIENT) as PatientData

        val patientName = "${patientData.user.first_name.first()}. ${patientData.user.last_name}"
        view.fragment_patient_feedback_txt_adviceFor.text = getString(R.string.advice_from, patientName)

        view.fragment_patient_feedback_btn_addAdvice.setOnClickListener {
            hideKeyboard()

            // Guard
            if (InputHelper.inputIsEmpty(mContext!!, mNewFeedbackET, toastView, toastAnimation, getString(R.string.feedback_is_empty)))
                return@setOnClickListener

            val sdf = GeneralHelper.getCreateDateFormat()

            // Create feedback
            newFeedback = FeedbackShort(title = getString(R.string.conversation_summary),
                comment = mNewFeedbackET.text.toString(), patient = patientData.patient.id,
                doctor = patientData.patient.doctor, date = sdf.format(Date())
            )

            // Admin guard
            if (GeneralHelper.isAdmin()) { addFeedbackMockup(); return@setOnClickListener }

            // Check internet connection
            if (!GeneralHelper.hasInternetConnection(context!!, toastView, toastAnimation)) return@setOnClickListener

            if (addFeedbackAsyncTask().status != AsyncTask.Status.RUNNING)
                addFeedbackAsyncTask().execute()
        }

        // Setup RV
        mFeedbackRV = view.fragment_patient_feedback_rv
        mFeedbackRV.layoutManager = LinearLayoutManager(activity)
        adapter = ConversationAdapter()
        mFeedbackRV.adapter = adapter

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(context!!, toastView, toastAnimation)) return view

        // Get feedback
        if (GeneralHelper.isAdmin()) {
            getConversationsMockup()
        } else {
            getConversationsAsyncTask().execute()
        }

        return view
    }

    inner class getConversationsAsyncTask : AsyncTask<Void, Void, ArrayList<Feedback>>() {
        override fun onPreExecute() {
            super.onPreExecute()

            // Loader
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): ArrayList<Feedback>? {
            return try {
                feedbackRepository.getFeedbacks(patientData.patient.id)
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<Feedback>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_feedbacks_fail))
                return }

            // Feedback
            GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_feedbacks))

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
            } catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: Feedback?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.add_feedback_fail))
                return }

            // Feedback
            GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.added_feedback))

            // Clean input
            mNewFeedbackET.setText("")

            // Add result to current feedback list
            feedbackList.add(0, result)

            // Update RV
            mFeedbackRV.adapter!!.notifyDataSetChanged()
        }
    }

    //region HideKeyboard
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    //endregion

    // Todo: Wordt dit gebruikt door de view?
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    // For Bundle
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    //region Mockups
    private fun getConversationsMockup() {
        // Feedback
        GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_feedbacks))

        // Save result
        feedbackList = LocalDb.feedbacks.asReversed()
        adapter.setConversationList(feedbackList)
    }

    private fun addFeedbackMockup() {
        val newFeedback = feedbackRepository.addFeedbackLocally(newFeedback)

        // Feedback
        GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.added_feedback))

        // Clean input
        mNewFeedbackET.setText("")

        // Update RV
        mFeedbackRV.adapter!!.notifyDataSetChanged()
    }

    //endregion
}
