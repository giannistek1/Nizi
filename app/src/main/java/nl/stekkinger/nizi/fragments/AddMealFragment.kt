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
import kotlinx.android.synthetic.main.fragment_meals.*
import kotlinx.android.synthetic.main.fragment_meals.view.*
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_meals, container, false)
        setHasOptionsMenu(true)

        val recyclerView: RecyclerView = view.meal_recycler_view
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = MealAdapter(model)
        recyclerView.adapter = adapter

        getMealsAsyncTask().execute()

        // listeners
        view.create_meal.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFragment()
                )
                .commit()
        }

        view.activity_add_food.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.activity_favorites.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    FavoritesFragment()
                )
                .commit()
        }

        return view
    }

    inner class getMealsAsyncTask() : AsyncTask<Void, Void, ArrayList<Meal>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Meal>? {
            var meals = mRepository.getMeals()
            return meals
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.back_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}