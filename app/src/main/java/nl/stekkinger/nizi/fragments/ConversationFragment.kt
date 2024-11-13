package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.databinding.FragmentConversationBinding
import nl.stekkinger.nizi.repositories.FeedbackRepository

//TODO: no fragment arguments, make a bundle!!
class ConversationFragment(private val user: UserLogin, private val doctor: Doctor): BaseFragment() {
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    private val mRepository: FeedbackRepository = FeedbackRepository()
    private lateinit var adapter: ConversationAdapter

    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Setup UI
        _binding = FragmentConversationBinding.inflate(layoutInflater)
        loader = binding.fragmentConversationLoader

        // Setup custom toast
        val parent: RelativeLayout = binding.fragmentConversationRl
        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
        parent.addView(toastView)

        val doctorName = "${doctor.user.first_name.first()}. ${doctor.user.last_name}"
        binding.fragmentConversationTxtAdviceFrom.text = getString(R.string.advice_from, doctorName)

        // Setup RV
        val recyclerView: RecyclerView = binding.fragmentConversationRv
        // TODO: change to recycler view (scrollable)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ConversationAdapter()
        recyclerView.adapter = adapter

        // Todo: Setup as interface so there is no dependency injection, just call getConversations and replace the class at start if Admin
        // Get feedback
//        if (GeneralHelper.isAdmin()) { getConversationsMockup(); return view }
        getConversationsMockup()

        // Check internet connection
//        if (!GeneralHelper.hasInternetConnection(requireContext(), toastView, toastAnimation)) return view
//        else { getConversationsAsyncTask().execute() }

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
                mRepository.getFeedbacks(user.patient!!.id)
            }  catch(e: Exception) {
                GeneralHelper.apiIsDown = true
                print("Server offline!"); print(e.message)
                return null
            }
        }

        override fun onPostExecute(result: ArrayList<Feedback>) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
//            if (GeneralHelper.apiIsDown) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.api_is_down)); return }
//            if (result == null) { GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.get_feedbacks_fail))
//                return }

            // Feedback
//            GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_feedbacks))

            d("CONVO", result.toString())
            adapter.setConversationList(result)
        }
    }

    //region Mockup Login
    private fun getConversationsMockup() {
        // Feedback
//        GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.fetched_feedbacks))

        d("CONVO", "Conversation mockup loaded.")
        adapter.setConversationList(LocalDb.feedbacks)
    }
    //endregion
}