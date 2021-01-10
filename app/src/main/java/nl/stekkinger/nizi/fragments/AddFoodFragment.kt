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
import kotlinx.android.synthetic.main.fragment_add_food.*
import kotlinx.android.synthetic.main.fragment_add_food.view.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.repositories.FoodRepository


class AddFoodFragment: Fragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_food, container, false)
        setHasOptionsMenu(true)

        val searchView = view.findViewById(R.id.search_food) as SearchView
        val searchManager: SearchManager = activity!!.getSystemService(SEARCH_SERVICE) as SearchManager

        val recyclerView: RecyclerView = view.findViewById(R.id.food_search_recycler_view)
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

        adapter = FoodSearchAdapter(model, fragment = "food")
        recyclerView.adapter = adapter

        if (searchView != null) {
            // Fixes that only the icon is clickable
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    Log.i("onQueryTextChange", newText)
                    model.setFoodSearch(newText)
                    fragment_add_food_txt_amount.text = getString(R.string.amount_of_products, recyclerView.adapter!!.itemCount)
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

        // get the results of food search
        model.getFoodSearch().observe(viewLifecycleOwner, Observer { foodList ->
            val amount = foodList.size
            view.fragment_add_food_txt_amount.text = getString(R.string.amount_of_products, amount)
            adapter.setFoodList(foodList)
        })

        view.activity_add_meal.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
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