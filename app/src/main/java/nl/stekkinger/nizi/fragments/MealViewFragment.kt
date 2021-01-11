package nl.stekkinger.nizi.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.text.TextWatcher
import android.util.Base64
import android.util.Log.d
import android.view.View.GONE
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_create_meal.view.*
import kotlinx.android.synthetic.main.fragment_diary.view.*
import kotlinx.android.synthetic.main.fragment_food_view.*
import kotlinx.android.synthetic.main.fragment_food_view.view.*
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.ArrayList


class MealViewFragment : NavigationChildFragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mMeal: Meal
    private lateinit var mServingInput: TextInputEditText
    private lateinit var mDecreaseBtn: ImageButton
    private lateinit var mSaveBtn: ImageButton
    private lateinit var mealProductAdapter: MealProductAdapter
    private var mEdit = true // edit or delete
    private val amounts: ArrayList<Float> = arrayListOf()

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
        setHasOptionsMenu(true)

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.emptyMealProducts()

        model.selectedMeal.observe(this, Observer<Meal> { meal ->
            // store food product
            mMeal = meal
            // collects mealproducts belonging to meal
            model.getMeal(meal.id)

            // Update the UI
            title_food_view.text = meal.food_meal_component.name
            val image = meal.food_meal_component.image_url
            if (URLUtil.isValidUrl(image)) {
                Picasso.get().load(image).into(image_food_view)
            }
            else if(image != null && image !="") { // bitmap img
                val decodedString: ByteArray =
                    Base64.decode(image, Base64.DEFAULT)
                val decodedByte: Bitmap? =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                if (decodedByte != null) {
                    image_food_view.setImageBitmap(decodedByte)
                }
            }
            updateUI()
        })

        // rv's
        val mealProductRV: RecyclerView = view.findViewById(R.id.food_view_recycler_view)
        mealProductRV.layoutManager = LinearLayoutManager(activity)

        // adapters
        mealProductAdapter = model.getMealProductAdapter()
        mealProductRV.adapter = mealProductAdapter
        val mealProducts: ArrayList<Food> = model.getMealProducts()



        lifecycleScope.launchWhenStarted {
            model.mealState.collect {
                when(it) {
                    is FoodRepository.MealState.Success -> {
                        // get meal products
                        model.getFoods(it.data)
                        // store amounts (API doesnt retrieve amount from products)

                        if (it.data.meal_foods != null) {
                            for (food in it.data.meal_foods) {
                                amounts.add(food.amount)
                            }
                        }
                        model.emptyMealState()
                    } else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            model.foodsState.collect {
                when(it) {
                    is FoodRepository.FoodsState.Success -> {
                        // update amounts
                        for (i in 1..amounts.count()) {
                            it.data[i-1].amount = amounts[i-1]
                        }
                        // fill the adapter
                        model.setMealProducts(it.data)
                        mealProductAdapter.setMealProductList(it.data)
                        model.emptyFoodsState()
                    } else -> {}
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            model.deleteMealState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        // todo: toast
                        model.emptyDeleteMealState()
                        (activity)!!.supportFragmentManager.beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            AddMealFragment()
                        ).commit()
                    }
                    is FoodRepository.State.Error -> {
                        // todo: toast
                    } else -> {}
                }
            }
        }

        view.food_view_meal_products_title.visibility = View.VISIBLE
        view.food_view_recycler_view.visibility = View.VISIBLE

        view.heart_food_view.visibility = GONE
        mSaveBtn = view.save_btn
        mDecreaseBtn = view.decrease_portion
        mServingInput = view.findViewById(R.id.serving_input) as TextInputEditText

        mServingInput.addTextChangedListener(textWatcher)

        // click listeners
        view.increase_portion.setOnClickListener {
            if (mServingInput.text.toString() != "") {
                var portion: Float = mServingInput.text.toString().toFloat() + 0.5f
                mServingInput.setText(portion.toString())
            } else {
                mServingInput.setText("0.5")
            }
        }

        view.decrease_portion.setOnClickListener {
            if (mServingInput.text.toString() != "") {
                var portion: Float = mServingInput.text.toString().toFloat()
                if(portion > 0.5) {
                    mServingInput.setText((portion - 0.5f).toString())
                }
            }
        }

        view.save_btn.setOnClickListener {
            val amount: Float = mServingInput.text.toString().trim().toFloat()
            // add meal
            model.addMeal(mMeal, amount)

            // Send text with fragment for toast
            val fragment = DiaryFragment()
            val bundle = Bundle()
            bundle.putString(GeneralHelper.TOAST_TEXT, getString(R.string.add_meal_success))
            fragment.arguments = bundle

            (activity)!!.supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                fragment
            ).commit()
        }

        view.edit_food_view.setOnClickListener {
            (activity)!!.supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                CreateMealFragment()
            ).commit()
        }

        view.delete_food_view.setOnClickListener {
            model.deleteMeal(mMeal.id)
        }

        mServingInput.setOnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                updateUI()
            }
            false
        }

        lifecycleScope.launchWhenStarted {
            model.consumptionState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        // Send text with fragment for toast
                        val fragment = DiaryFragment()
                        val bundle = Bundle()

                        if(mEdit)
                            bundle.putString(GeneralHelper.TOAST_TEXT, getString(R.string.update_food_success))
                        else
                            bundle.putString(GeneralHelper.TOAST_TEXT, getString(R.string.delete_food_success))

                        fragment.arguments = bundle
                        (activity)!!.supportFragmentManager.beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            fragment
                        ).commit()
                    }
                    is FoodRepository.State.Error -> {
                        // TODO: handle events below
//                        wat gaan we hier doen?
//                        Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                    is FoodRepository.State.Loading -> {
//                        spinner toevoegen aan consumptionview?
//                        Toast.makeText(activity, "LOADING", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
            }
        }

        return view
    }

    private fun updateUI() {
        var amount: Float = 1f
        if (mServingInput.text.toString() != "") {
            amount = mServingInput.text.toString().toFloat()
        }

        // enabling/disabeling save or decrease btn
        if(amount <= 0) {
            // TODO: make btn grey
            mSaveBtn.isEnabled = false
            mSaveBtn.isClickable = false
        } else {
            mSaveBtn.isEnabled = true
            mSaveBtn.isClickable = true
        }
        if(amount <= 0.5) {
            mDecreaseBtn.isEnabled = false
            mDecreaseBtn.isClickable = false
        } else {
            mDecreaseBtn.isEnabled = true
            mDecreaseBtn.isClickable = true
        }

        // updating nutrition values
        val food: FoodMealComponent = mMeal.food_meal_component
        serving_size_value.text = "%.0f".format(food.portion_size * amount) + " " + mMeal.weight_unit!!.unit
        calories_value_food_view.text = "%.2f".format(food.kcal * amount) + " Kcal"
        fiber_value_food_view.text = "%.2f".format(food.fiber * amount) + " g"
        protein_value_food_view.text = "%.2f".format(food.protein * amount) + " g"
        water_value_food_view.text = "%.2f".format(food.water * amount) + "ml"
        sodium_value_food_view.text = "%.2f".format(food.sodium * 1000 * amount) + " mg"
        potassium_value_food_view.text = "%.2f".format(food.potassium * 1000 * amount) + " mg"
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // first char cannot be a dot
            if (mServingInput.text.toString() != "") {
                var input: String = mServingInput.text.toString()
                if (input[0] == '.') {
                    mServingInput.setText(input.drop(0))
                }
            }
            updateUI()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                model.resetMealValues()
                model.emptyMealProducts()

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