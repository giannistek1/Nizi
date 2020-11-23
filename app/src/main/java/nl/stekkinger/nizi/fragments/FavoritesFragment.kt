package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.os.AsyncTask
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.FoodResponse
import nl.stekkinger.nizi.classes.diary.MyFoodResponse
import nl.stekkinger.nizi.classes.diary.WeightUnit
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.weight_unit.WeightUnitHolder
import nl.stekkinger.nizi.repositories.FoodRepository


class FavoritesFragment: Fragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        setHasOptionsMenu(true)

        val recyclerView: RecyclerView = view.favorites_rv
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = FoodSearchAdapter(model, fragment = "favorites")
        recyclerView.adapter = adapter

        getFavoritesAsyncTask().execute()

        view.activity_add_food.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.activity_add_meal.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                )
                .commit()
        }

        return view
    }

    inner class getFavoritesAsyncTask() : AsyncTask<Void, Void, ArrayList<MyFoodResponse>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<MyFoodResponse>? {
            return mRepository.getFavorites()
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: ArrayList<MyFoodResponse>?) {
            super.onPostExecute(result)
            // update UI
            d("tess2", result.toString())
            if(result != null) {
                var foodList: ArrayList<Food> = arrayListOf()

                // getting weight units
                val gson = Gson()
                val json: String = GeneralHelper.prefs.getString(GeneralHelper.PREF_WEIGHT_UNIT, "")!!
                val weightUnitHolder: WeightUnitHolder = gson.fromJson(json, WeightUnitHolder::class.java)

                for (foodResponse: MyFoodResponse in result) {
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
                        weight_amount = foodResponse.food.food_meal_component.portion_size, // Todo: there is no amount yet
                        image_url = foodResponse.food.food_meal_component.image_url
                    )
                    foodList.add(food)
                }
                adapter.setFoodList(foodList)
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