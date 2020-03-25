package nl.stekkinger.nizi.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Meal
import nl.stekkinger.nizi.repositories.FoodRepository

class ConversationFragment: Fragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: MealAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //val helper = GuidelinesHelperClass()
        //helper.initializeGuidelines(activity, ll_guidelines_content)
    }

//    inner class getConversationsAsyncTask() : AsyncTask<Void, Void, ArrayList<Meal>>() {
//        override fun doInBackground(vararg params: Void?): ArrayList<Meal>? {
//            var conversations = mRepository.getConversations()
//            return conversations
//        }
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//        }
//
//        override fun onPostExecute(result: ArrayList<Meal>?) {
//            super.onPostExecute(result)
//            // update UI
//            if(result != null) {
//                adapter.setMealList(result)
//            }
//        }
//    }
}