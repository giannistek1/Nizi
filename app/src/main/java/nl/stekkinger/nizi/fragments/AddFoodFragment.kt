package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.content.Intent
import android.os.Bundle
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
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.databinding.FragmentAddFoodBinding
import nl.stekkinger.nizi.repositories.FoodRepository

class AddFoodFragment: NavigationChildFragment() {
    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add_food, container, false)
        _binding = FragmentAddFoodBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)

        val searchView = view.findViewById(R.id.search_food) as SearchView
        val searchManager: SearchManager = requireActivity().getSystemService(SEARCH_SERVICE) as SearchManager

        val recyclerView: RecyclerView = view.findViewById(R.id.food_search_recycler_view)
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

        adapter = FoodSearchAdapter(model, fragment = "food", context = requireContext())
        recyclerView.adapter = adapter

        if (searchView != null) {
            // Fixes that only the icon is clickable
            searchView.setOnClickListener {
                searchView.isIconified = false
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    model.setFoodSearch(newText)
                    binding.fragmentAddFoodTxtAmount.text = getString(R.string.amount_of_products, recyclerView.adapter!!.itemCount)
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
            val amount = foodList?.size
            binding.fragmentAddFoodTxtAmount.text = getString(R.string.amount_of_products, amount)
            if (foodList != null) {
                adapter.setFoodList(foodList)
            }
        })
        // when a food is found by barcode scan
        lifecycleScope.launchWhenStarted {
            model.foodByBarcodeState.collect {
                when(it) {
                    is FoodRepository.FoodState.Success -> {
                        model.selected.value = it.data
                        model.emptyFoodBarcodeState()
                        requireFragmentManager().beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            FoodViewFragment()
                        ).commit()
                    }
                    else -> {}
                }
            }
        }

        // click listeners
        binding.activityAddMeal.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
        }

        binding.activityFavorites.setOnClickListener {
            requireFragmentManager().beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    FavoritesFragment()
                ).commit()
        }

        binding.zxingBarcodeScanner.setOnClickListener {
            zxing_barcode_scanner()
        }

        return binding.root
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