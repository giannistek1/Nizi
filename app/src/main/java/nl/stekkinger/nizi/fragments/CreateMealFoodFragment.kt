package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.content.Context.SEARCH_SERVICE
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_create_meal_food.view.*
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.repositories.FoodRepository


class CreateMealFoodFragment: NavigationChildFragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchAdapter: FoodSearchAdapter
    private lateinit var mealProductAdapter: MealProductAdapter

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal_food, container, false)
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
        searchAdapter = FoodSearchAdapter(model, fragment = "meal", context = context!!)
        searchRV.adapter = searchAdapter
//        mealProductAdapter = MealProductAdapter(model)
        mealProductAdapter = model.getMealProductAdapter()
        mealProductRV.adapter = mealProductAdapter

        if (searchView != null) {
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    model.setFoodSearch(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }
            }
            searchView.setOnQueryTextListener(queryTextListener)
        } else {
            model.setFoodSearch("")
        }

         //TODO: do i need consumption response or something else to create a meal, what information do i send?
//        mealProductAdapter.setMealProductList(model.getMealProducts())

        // get the results of food search
        model.getFoodSearch().observe(viewLifecycleOwner, Observer { foodList ->
            searchAdapter.setFoodList(foodList)
        })

        mealProductAdapter.setMealProductList(model.getMealProducts())
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mealProductRV)

        lifecycleScope.launchWhenStarted {
            model.foodByBarcodeState.collect {
                when(it) {
                    is FoodRepository.FoodState.Success -> {
                        model.emptyFoodBarcodeState()
                        model.select(activity as AppCompatActivity, it.data, "meal")
                    }
                    is FoodRepository.FoodState.Error -> {
                        // TODO: handle events below
//                        Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                    is FoodRepository.FoodState.Loading -> {
//                        Toast.makeText(activity, "LOADING", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
            }
        }

        // click events
        view.save_btn.setOnClickListener {
            // Remove last fragment from backstack
            fragmentManager!!.popBackStack()

            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFragment()
                )
                .commit()
        }
        view.zxing_barcode_scanner.setOnClickListener {
            zxing_barcode_scanner(view)
        }

        return view
    }

    private val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                mealProductAdapter.removeItem(viewHolder.adapterPosition)
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                fragmentManager!!.popBackStack()
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun zxing_barcode_scanner(view: View?) {
        val intentIntegrator = IntentIntegrator.forSupportFragment(this)
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