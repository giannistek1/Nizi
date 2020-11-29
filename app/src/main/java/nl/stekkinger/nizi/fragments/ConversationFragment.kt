package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_conversation.view.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.repositories.FeedbackRepository
import java.lang.Exception

class ConversationFragment(private val user: UserLogin, private val doctor: Doctor): Fragment() {
    private val mRepository: FeedbackRepository = FeedbackRepository()
    private lateinit var adapter: ConversationAdapter

    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Setup UI
        val view: View = inflater.inflate(R.layout.fragment_conversation, container, false)
        loader = view.fragment_conversation_loader

        val doctorName = "${doctor.user.first_name.first()}. ${doctor.user.last_name}"
        view.fragment_conversation_txt_advice_from.text = getString(R.string.advice_from, doctorName)

        // Setup RV
        val recyclerView: RecyclerView = view.fragment_conversation_rv
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ConversationAdapter()
        recyclerView.adapter = adapter

        // Check internet connection
        if (!GeneralHelper.hasInternetConnection(context!!)) return view

        // Get feedback
        getConversationsAsyncTask().execute()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

        override fun onPostExecute(result: ArrayList<Feedback>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guards
            if (GeneralHelper.apiIsDown) { Toast.makeText(activity, R.string.api_is_down, Toast.LENGTH_SHORT).show(); return }
            if (result == null) { Toast.makeText(activity, R.string.get_feedbacks_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(activity, R.string.fetched_feedbacks, Toast.LENGTH_SHORT).show()

            d("CONVO", result.toString())
            adapter.setConversationList(result)
        }
    }
}