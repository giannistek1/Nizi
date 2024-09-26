package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.repositories.FoodRepository

class AddFoodFragment: NavigationChildFragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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

        adapter = FoodSearchAdapter(model, fragment = "food", context = context!!)
        recyclerView.adapter = adapter

        if (searchView != null) {
            // Fixes that only the icon is clickable
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
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

        // observers
        // get the results of food search
        model.getFoodSearch().observe(viewLifecycleOwner, Observer { foodList ->
            val amount = foodList.size
            view.fragment_add_food_txt_amount.text = getString(R.string.amount_of_products, amount)
            adapter.setFoodList(foodList)
        })
        // when a food is found by barcode scan
        lifecycleScope.launchWhenStarted {
            model.foodByBarcodeState.collect {
                when(it) {
                    is FoodRepository.FoodState.Success -> {
                        model.selected.value = it.data
                        model.emptyFoodBarcodeState()
                        fragmentManager!!.beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            FoodViewFragment()
                        ).commit()
                    }
                    else -> {}
                }
            }
        }

        // click listeners
        view.activity_add_meal.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
        }

        view.activity_favorites.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    FavoritesFragment()
                ).commit()
        }

        view.zxing_barcode_scanner.setOnClickListener {
            zxing_barcode_scanner()
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
    private fun zxing_barcode_scanner() {
        val intentIntegrator = IntentIntegrator.forSupportFragment(this)
        intentIntegrator.setOrientationLocked(true)
        intentIntegrator.setBeepEnabled(true)
        intentIntegrator.initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null){
            if (intentResult.contents != null){
                model.getFoodByBarcode(intentResult.contents)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}