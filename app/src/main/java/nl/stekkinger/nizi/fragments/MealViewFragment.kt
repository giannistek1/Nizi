package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.databinding.FragmentFoodViewBinding
import nl.stekkinger.nizi.repositories.FoodRepository


class MealViewFragment : NavigationChildFragment() {
    private var _binding: FragmentFoodViewBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentFoodViewBinding.inflate(layoutInflater)

        setHasOptionsMenu(true)

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // throw away old mealproducts if saved
        model.emptyMealProducts()

        // rv
        val mealProductRV: RecyclerView = view.findViewById(R.id.food_view_recycler_view)
        mealProductRV.layoutManager = LinearLayoutManager(activity)
        // adapter
        mealProductAdapter = model.getMealProductAdapter()
        mealProductRV.adapter = mealProductAdapter

        // stateflows/observers
//        model.selectedMeal.observe(this, Observer<Meal> { meal ->
//            // store food product
//            mMeal = meal
//            // collects mealproducts belonging to meal
//            model.getMeal(meal!!.id)
//
//            // Update the UI
//            binding.titleFoodView.text = meal.food_meal_component.name
//            val image = meal.food_meal_component.image_url
//            if (URLUtil.isValidUrl(image)) {
//                Picasso.get().load(image).into(binding.imageFoodView)
//            }
//            else if(image != null && image !="") { // bitmap img
//                val decodedString: ByteArray =
//                    Base64.decode(image, Base64.DEFAULT)
//                val decodedByte: Bitmap? =
//                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//                if (decodedByte != null) {
//                    binding.imageFoodView.setImageBitmap(decodedByte)
//                }
//            }
//            updateUI()
//        })

        // meal data
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

        // getting food products from the meal
        lifecycleScope.launchWhenStarted {
            model.foodsState.collect {
                when(it) {
                    is FoodRepository.FoodsState.Success -> {
                        // update amounts
                        for (i in 1..amounts.count()) {
                            it.data[i-1].amount = amounts[i-1]
                        }
                        // fill the model, keeping track of food products
                        model.setMealProducts(it.data)
                        // fill the adapter
                        mealProductAdapter.setMealProductList(it.data)
                        model.emptyFoodsState()
                    } else -> {}
                }
            }
        }

        // when meal deleted
        lifecycleScope.launchWhenStarted {
            model.deleteMealState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        model.emptyDeleteMealState()
                        requireActivity().supportFragmentManager.beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            AddMealFragment()
                        ).commit()
                    } else -> {}
                }
            }
        }

        binding.foodViewMealProductsTitle.visibility = View.VISIBLE
        binding.foodViewRecyclerView.visibility = View.VISIBLE

        binding.heartFoodView.visibility = GONE
        mSaveBtn = binding.saveBtn
        mDecreaseBtn = binding.decreasePortion
        mServingInput = view.findViewById(R.id.serving_input) as TextInputEditText

        mServingInput.addTextChangedListener(textWatcher)

        // click listeners
        binding.increasePortion.setOnClickListener {
            if (mServingInput.text.toString() != "") {
                var portion: Float = mServingInput.text.toString().toFloat() + 0.5f
                mServingInput.setText(portion.toString())
            } else {
                mServingInput.setText("0.5")
            }
        }

        binding.decreasePortion.setOnClickListener {
            if (mServingInput.text.toString() != "") {
                var portion: Float = mServingInput.text.toString().toFloat()
                if(portion > 0.5) {
                    mServingInput.setText((portion - 0.5f).toString())
                }
            }
        }

        binding.saveBtn.setOnClickListener {
            val amount: Float = mServingInput.text.toString().trim().toFloat()
            // add meal
            model.addMeal(mMeal, amount)

            // Send text with fragment for toast
            val fragment = DiaryFragment()
            val bundle = Bundle()
            bundle.putString(GeneralHelper.TOAST_TEXT, getString(R.string.add_meal_success))
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                fragment
            ).commit()
        }

        binding.editFoodView.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                CreateMealFragment()
            ).commit()
        }

        binding.deleteFoodView.setOnClickListener {
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
                        requireActivity().supportFragmentManager.beginTransaction().replace(
                            R.id.activity_main_fragment_container,
                            fragment
                        ).commit()
                    }
                    else -> {}
                }
            }
        }

        return binding.root
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
        binding.servingSizeValue.text = "%.0f".format(food.portion_size * amount) + " " + mMeal.weight_unit!!.unit
        binding.caloriesValueFoodView.text = "%.2f".format(food.kcal * amount) + " Kcal"
        binding.fiberValueFoodView.text = "%.2f".format(food.fiber * amount) + " g"
        binding.proteinValueFoodView.text = "%.2f".format(food.protein * amount) + " g"
        binding.waterValueFoodView.text = "%.2f".format(food.water * amount) + "ml"
        binding.sodiumValueFoodView.text = "%.2f".format(food.sodium * 1000 * amount) + " mg"
        binding.potassiumValueFoodView.text = "%.2f".format(food.potassium * 1000 * amount) + " mg"
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

                requireActivity().supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}