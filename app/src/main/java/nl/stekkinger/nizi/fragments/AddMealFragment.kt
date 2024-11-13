package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.databinding.FragmentMealsBinding
import nl.stekkinger.nizi.repositories.FoodRepository


class AddMealFragment: NavigationChildFragment() {
    private var _binding: FragmentMealsBinding? = null
    private val binding get() = _binding!!

    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: MealAdapter
    private lateinit var aantal: TextView

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_meals, container, false)
        _binding = FragmentMealsBinding.inflate(layoutInflater)

        setHasOptionsMenu(true)
        aantal = binding.fragmentMealsTxtAmount

        val recyclerView: RecyclerView = binding.mealRecyclerView
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // TODO: Expose binding here?
        when (model.getMealTime()) {
//            "Ontbijt" -> requireActivity().toolbar_title.text = getString(R.string.add_breakfast)
//            "Lunch" -> requireActivity().toolbar_title.text = getString(R.string.add_lunch)
//            "Avondeten" -> requireActivity().toolbar_title.text = getString(R.string.add_dinner)
//            "Snack" -> requireActivity().toolbar_title.text = getString(R.string.add_snack)
//            else -> requireActivity().toolbar_title.text = getString(R.string.diary)
        }

        adapter = MealAdapter(model, context = requireContext())
        recyclerView.adapter = adapter

        getMealsAsyncTask().execute()

        val searchView = view.findViewById(R.id.search_meal) as SearchView
        val searchManager: SearchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchView != null) {
            // Fixes that only the icon is clickable
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

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
        binding.createMeal.setOnClickListener {
            model.emptySelectedMeal()
            model.setIsMealEdit(false)

            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                CreateMealFragment()
            ).addToBackStack(null).commit()
        }

        binding.activityAddFood.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).commit()
        }

        binding.activityFavorites.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(
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
                requireActivity().supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}