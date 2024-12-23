package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.MyFoodResponse
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.databinding.FragmentFavoritesBinding
import nl.stekkinger.nizi.repositories.FoodRepository


class FavoritesFragment: NavigationChildFragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        _binding = FragmentFavoritesBinding.inflate(layoutInflater)

        setHasOptionsMenu(true)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // rc
        val recyclerView: RecyclerView = binding.favoritesRv
        recyclerView.layoutManager = LinearLayoutManager(activity)
        // adapter
        adapter = FoodSearchAdapter(model, fragment = "favorites", context = requireContext())
        recyclerView.adapter = adapter

        // swipe funct
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        model.fetchFavorites()

        // stateflows
        lifecycleScope.launchWhenStarted {
            model.favoritesState.collect {
                when(it) {
                    is FoodRepository.FavoritesState.Success -> {
                        var foodList: ArrayList<Food> = arrayListOf()

                        // getting weight units
                        val gson = Gson()
                        val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
                        val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)

                        for (foodResponse: MyFoodResponse in it.data) {
                            Log.i("onResponse", foodResponse.toString())

                            val weightUnit = WeightUnit(
                                id = foodResponse.food.weight_unit,
                                unit = weightUnitHolder.weightUnits[foodResponse.food.weight_unit -1].unit,
                                short = weightUnitHolder.weightUnits[foodResponse.food.weight_unit -1].short
                            )

                            val food = Food(
                                id = foodResponse.food.id,
                                my_food = foodResponse.id,
                                name = foodResponse.food.name,
                                description = foodResponse.food.food_meal_component.description,
                                kcal = foodResponse.food.food_meal_component.kcal,
                                protein = foodResponse.food.food_meal_component.protein,
                                potassium = foodResponse.food.food_meal_component.potassium,
                                sodium = foodResponse.food.food_meal_component.sodium,
                                water = foodResponse.food.food_meal_component.water,
                                fiber = foodResponse.food.food_meal_component.fiber,
                                portion_size = foodResponse.food.food_meal_component.portion_size,
                                weight_unit = weightUnit,
                                weight_amount = foodResponse.food.food_meal_component.portion_size,
                                image_url = foodResponse.food.food_meal_component.image_url,
                                foodId = foodResponse.food.food_meal_component.foodId
                            )
                            foodList.add(food)
                        }
                        val foodAmount = foodList.count().toString()
                        binding.fragmentAddFoodTxtAmount.text = "Aantal ($foodAmount)"
                        adapter.setFoodList(foodList)
                    }
                    else -> {}
                }
            }
        }

        // click listeners
        binding.activityAddFood.setOnClickListener {
            requireFragmentManager()
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        binding.activityAddMeal.setOnClickListener {
            requireFragmentManager()
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                )
                .commit()
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
                adapter.removeItem(viewHolder.adapterPosition)
            }
        }
}