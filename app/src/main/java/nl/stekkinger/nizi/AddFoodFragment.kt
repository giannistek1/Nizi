package nl.stekkinger.nizi

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import java.util.zip.Inflater
import android.content.Context.SEARCH_SERVICE
import android.content.SharedPreferences
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.Food
import nl.stekkinger.nizi.classes.FoodSearchAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_food, container, false)

        val searchView = view.findViewById(R.id.search_food) as SearchView
        val searchManager: SearchManager = activity!!.getSystemService(SEARCH_SERVICE) as SearchManager

        val recyclerView: RecyclerView = view.findViewById(R.id.food_search_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = FoodSearchAdapter()
        recyclerView.adapter = adapter

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        if (searchView != null) {
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
        }

        // get the results of food search
        model.getFoodSearch().observe(viewLifecycleOwner, Observer { foodList ->
            d("LOGLIST", foodList.toString())
            adapter.setFoodList(foodList)
        })

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}