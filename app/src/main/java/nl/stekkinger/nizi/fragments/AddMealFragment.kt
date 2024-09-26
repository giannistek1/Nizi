package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.os.AsyncTask
import android.util.Log
import android.util.Log.d
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealAdapter
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.repositories.FoodRepository


class AddMealFragment: NavigationChildFragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: MealAdapter
    private lateinit var aantal: TextView

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_meals, container, false)
        setHasOptionsMenu(true)
        aantal = view.fragment_meals_txt_amount

        val recyclerView: RecyclerView = view.meal_recycler_view
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        when (model.getMealTime()) {
            "Ontbijt" -> activity!!.toolbar_title.text = getString(R.string.add_breakfast)
            "Lunch" -> activity!!.toolbar_title.text = getString(R.string.add_lunch)
            "Avondeten" -> activity!!.toolbar_title.text = getString(R.string.add_dinner)
            "Snack" -> activity!!.toolbar_title.text = getString(R.string.add_snack)
            else -> activity!!.toolbar_title.text = getString(R.string.diary)
        }

        adapter = MealAdapter(model, context = context!!)
        recyclerView.adapter = adapter

        getMealsAsyncTask().execute()

        val searchView = view.findViewById(R.id.search_meal) as SearchView
        val searchManager: SearchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchView != null) {
            // Fixes that only the icon is clickable
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    getMealsByNameAsyncTask(query).execute()
                    d("lo", query)
                    return true
                }
            }
            searchView.setOnQueryTextListener(queryTextListener)
        }

        // click isteners
        view.create_meal.setOnClickListener {
            model.emptySelectedMeal()
            model.setIsMealEdit(false)

            fragmentManager!!.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                CreateMealFragment()
            ).addToBackStack(null).commit()
        }

        view.activity_add_food.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).commit()
        }

        view.activity_favorites.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                FavoritesFragment()
            ).commit()
        }

        return view
    }

    inner class getMealsAsyncTask() : AsyncTask<Void, Void, ArrayList<Meal>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Meal>? {
            var meals = mRepository.getMeals()
            return meals
        }

        override fun onPostExecute(result: ArrayList<Meal>?) {
            super.onPostExecute(result)
            // update UI
            if(result != null) {
                val mealsFound = result.count().toString()
                aantal.text = "Aantal ($mealsFound)"
                adapter.setMealList(result)
            }
        }
    }

    inner class getMealsByNameAsyncTask(val search: String) : AsyncTask<Void, Void, ArrayList<Meal>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Meal>? {
            d("r", search)
            var meals = mRepository.getMealsByName(search)
            return meals
        }

        override fun onPostExecute(result: ArrayList<Meal>?) {
            super.onPostExecute(result)
            // update UI
            if(result != null) {
                d("r", result.toString())
                val mealsFound = result.count().toString()
                aantal.text = "Aantal ($mealsFound)"
                adapter.setMealList(result)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
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