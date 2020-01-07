package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.content.Context.SEARCH_SERVICE
import android.os.AsyncTask
import android.util.Log
import android.util.Log.d
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealAdapter
import nl.stekkinger.nizi.classes.Meal
import nl.stekkinger.nizi.repositories.FoodRepository


class AddMealFragment: Fragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: MealAdapter
    private lateinit var mCreateMeal: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_meals, container, false)
        mCreateMeal = view.findViewById(R.id.create_meal) as TextView

        val recyclerView: RecyclerView = view.findViewById(R.id.meal_recycler_view)
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = MealAdapter(model)
        recyclerView.adapter = adapter

        getMealsAsyncTask().execute()

        // listeners
        mCreateMeal.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFragment()
                )
                .commit()
        }

        return view
    }

    inner class getMealsAsyncTask() : AsyncTask<Void, Void, ArrayList<Meal>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Meal>? {
            var jaap = mRepository.getMeals()
            d("j", jaap.toString())
            return jaap
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: ArrayList<Meal>?) {
            super.onPostExecute(result)
            // update UI
            if(result != null) {
                adapter.setMealList(result)
            }
        }
    }
}