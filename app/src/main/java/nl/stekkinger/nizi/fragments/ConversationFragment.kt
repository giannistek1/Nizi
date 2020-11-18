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
import kotlinx.android.synthetic.main.fragment_conversation.*
import kotlinx.android.synthetic.main.fragment_conversation.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.login.User
import nl.stekkinger.nizi.classes.login.UserLogin
import nl.stekkinger.nizi.classes.old.Conversation
import nl.stekkinger.nizi.repositories.FeedbackRepository

class ConversationFragment(private val user: UserLogin): Fragment() {
    private val mRepository: FeedbackRepository = FeedbackRepository()
    private lateinit var adapter: ConversationAdapter

    private lateinit var loader: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Setup UI
        val view: View = inflater.inflate(R.layout.fragment_conversation, container, false)
        loader = view.fragment_conversation_loader

        //view.fragment_conversation_txt_advice_from.text = "Advies van ${dietist}"

        // Setup RV
        val recyclerView: RecyclerView = view.fragment_rv_conversation
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ConversationAdapter()
        recyclerView.adapter = adapter

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
            return mRepository.getFeedbacks(user.patient!!.id)
        }

        override fun onPostExecute(result: ArrayList<Feedback>?) {
            super.onPostExecute(result)

            // Loader
            loader.visibility = View.GONE

            // Guard
            if (result == null) { Toast.makeText(activity, R.string.get_feedbacks_fail, Toast.LENGTH_SHORT).show()
                return }

            Toast.makeText(activity, R.string.fetched_feedbacks, Toast.LENGTH_SHORT).show()

            d("CONVO", result.toString())
            adapter.setConversationList(result)
        }
    }

//    inner class getConversationsAsyncTask() : AsyncTask<Void, Void, ArrayList<String>>() {
//        override fun doInBackground(vararg params: Void?): ArrayList<String>? {
//            var conversations = mRepository.getConversations()
//            return conversations
//        }
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//        }
//
//        override fun onPostExecute(result: ArrayList<String>?) {
//            super.onPostExecute(result)
//            if(result != null) {
//                d("CONVO", result.toString())
////                adapter.setMealList(result)
//            }
//        }
//    }
}