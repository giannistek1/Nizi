package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.content.Context.SEARCH_SERVICE
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.repositories.FoodRepository


class CreateMealFragment: Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchAdapter: FoodSearchAdapter
    private lateinit var mealProductAdapter: MealProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal, container, false)
        setHasOptionsMenu(true)

        val searchView = view.findViewById(R.id.search_food) as SearchView
        val searchManager: SearchManager = activity!!.getSystemService(SEARCH_SERVICE) as SearchManager

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // rv's
        val searchRV: RecyclerView = view.findViewById(R.id.food_search_recycler_view)
        searchRV.layoutManager = LinearLayoutManager(activity)
        val mealProductRV: RecyclerView = view.findViewById(R.id.meal_food_list_recycler_view)
        mealProductRV.layoutManager = LinearLayoutManager(activity)

        // adapters
        searchAdapter = FoodSearchAdapter(model, fragment = "meal")
        searchRV.adapter = searchAdapter
        mealProductAdapter = MealProductAdapter(model)
        mealProductRV.adapter = mealProductAdapter

        if (searchView != null) {
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    Log.i("onQueryTextChange", newText)

                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    model.setFoodSearch(query)
                    return true
                }
            }
            searchView.setOnQueryTextListener(queryTextListener)
        } else {
            model.setFoodSearch("")
        }

        mealProductAdapter.setMealProductList(model.getMealProducts())

        // get the results of food search
        model.getFoodSearch().observe(viewLifecycleOwner, Observer { foodList ->
            d("LOGLIST", model.getMealProducts().toString())
            searchAdapter.setFoodList(foodList)
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFinalFragment()
                ).commit()
                true
            }
            R.id.back_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}