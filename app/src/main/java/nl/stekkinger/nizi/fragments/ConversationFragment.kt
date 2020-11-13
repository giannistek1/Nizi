package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_conversation.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConversationAdapter
import nl.stekkinger.nizi.classes.Conversation
import nl.stekkinger.nizi.repositories.ConversationRepository

class ConversationFragment: Fragment() {
    private val mRepository: ConversationRepository = ConversationRepository()
    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_conversation, container, false)

        val recyclerView: RecyclerView = view.conversation_recycler_view
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ConversationAdapter()
        recyclerView.adapter = adapter

        getConversationsAsyncTask().execute()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //val helper = GuidelinesHelperClass()
        //helper.initializeGuidelines(activity, ll_guidelines_content)
    }

    inner class getConversationsAsyncTask() : AsyncTask<Void, Void, ArrayList<Conversation>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Conversation>? {
            var conversations = mRepository.getConversations()
            return conversations
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: ArrayList<Conversation>?) {
            super.onPostExecute(result)
            if(result != null) {
                d("CONVO", result.toString())
                adapter.setConversationList(result)
            }
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