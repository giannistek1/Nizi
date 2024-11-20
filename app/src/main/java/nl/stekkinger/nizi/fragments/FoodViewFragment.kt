package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.d
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.databinding.FragmentFoodViewBinding
import nl.stekkinger.nizi.repositories.FoodRepository


class FoodViewFragment : NavigationChildFragment() {
    private var _binding: FragmentFoodViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: DiaryViewModel
    private lateinit var mServingInput: TextInputEditText
    private lateinit var mDecreaseBtn: ImageButton
    private lateinit var mSaveBtn: ImageButton
    private lateinit var mFavBtn: ImageButton
    private lateinit var mCurrentFragment: String
    private var mFood: Food = Food(weight_unit = GeneralHelper.getWeightUnitHolder()!!.weightUnits[1])
    private var mIsLiked = false
    private var mFavoriteSelected = 0

     override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
         _binding = FragmentFoodViewBinding.inflate(layoutInflater)

        this.setHasOptionsMenu(true)

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        mCurrentFragment = model.getCurrentFragment()
        mFavBtn = view.findViewById(R.id.heart_food_view)
        mSaveBtn = binding.saveBtn
        mDecreaseBtn = binding.decreasePortion
        mServingInput = view.findViewById(R.id.serving_input) as TextInputEditText
        // this view does not have a delete button
        binding.deleteFoodView.visibility = GONE

        mServingInput.addTextChangedListener(textWatcher)

        binding.editFoodView.visibility = GONE

        // stateflows/observers
        model.selected.observe(this, Observer<Food> { food ->
            // store food product
            mFood = food
            val favorites = model.getFavorites()
            mFavoriteSelected = 0
            loop@ for (fav in favorites) {
                if (fav.food == food.id) {
                    mFavoriteSelected = fav.id
                    break@loop
                }
            }
            // set fav btns
            if (mFavoriteSelected != 0) {
                isLiked(true)
            } else {
                isLiked(false)
            }

            // Update the UI
            binding.titleFoodView.text = food.name
            Picasso.get().load(food.image_url).into(binding.imageFoodView)
            updateUI()
        })

        lifecycleScope.launchWhenStarted {
            model.favoritesState.collect {
                when(it) {
                    is FoodRepository.FavoritesState.Success -> {
                        val favorites: java.util.ArrayList<MyFood> = java.util.ArrayList()
                        for (fav in it.data) {
                            val food = MyFood(id = fav.id, food = fav.food.id)
                            favorites.add(food)
                            if (mFood.id == fav.food.id) mFavoriteSelected = fav.id
                        }
                        model.setFavorites(favorites)
                    }
                    else -> {}
                }
            }
        }



        lifecycleScope.launchWhenStarted {
            model.toggleFavoriteState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        if(mIsLiked) {
                            d("del", "del")
                            // deleted
                            isLiked(false)
                            Toast.makeText(activity, R.string.deleted_favorite, Toast.LENGTH_SHORT).show()
                            model.fetchFavorites()
                            model.emptyToggleFavoriteState()
                        } else {
                            d("add", "add")
                            // added
                            isLiked(true)
                            Toast.makeText(activity, R.string.added_favorite, Toast.LENGTH_SHORT).show()
                            model.fetchFavorites()
                            model.emptyToggleFavoriteState()
                        }
                        binding.fragmentFoodViewLoader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                    is FoodRepository.State.Error -> {
                        if(mIsLiked) Toast.makeText(activity, R.string.error_delete_favorite, Toast.LENGTH_SHORT).show()
                        else Toast.makeText(activity, R.string.error_add_favorite, Toast.LENGTH_SHORT).show()
                        binding.fragmentFoodViewLoader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                    is FoodRepository.State.Loading -> {
                        binding.fragmentFoodViewLoader.visibility = VISIBLE
                    }
                    else -> {
                        binding.fragmentFoodViewLoader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                }
            }
        }

        // click listeners
        binding.heartFoodView.setOnClickListener {
            // prevent button spamming until result
            mFavBtn.isEnabled = false
            mFavBtn.isClickable = false

            if(mIsLiked) {
                model.deleteFavorite(mFavoriteSelected)
            } else {
                model.addFavorite(mFood.id)
            }
        }

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
            when (mCurrentFragment) {
                "food" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.addConsumption(mFood, amount)

                    // Send text with fragment for toast
                    val fragment = DiaryFragment()
                    val bundle = Bundle()
                    bundle.putString(GeneralHelper.TOAST_TEXT, getString(R.string.food_added))
                    fragment.arguments = bundle

                    requireActivity().supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        fragment
                    ).commit()
                }
                "meal" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.addMealProduct(mFood, amount)

                    requireActivity().supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        CreateMealFoodFragment()
                    ).commit()
                }
                "mealEdit" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.editMealProduct(amount)

                    requireActivity().supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        CreateMealFoodFragment()
                    ).commit()
                }
            }
        }

        mServingInput.setOnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                updateUI()
            }
            false
        }

        return binding.root
    }

    private fun updateUI() {
        var amount: Float = 0f
        if (mServingInput.text.toString() != "") {
            amount = mServingInput.text.toString().toFloat()
        }

        // enabling/disabeling save or decrease btn
        if(amount <= 0) {
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
        val food: Food = mFood
        binding.servingSizeValue.text = "%.2f".format(food.portion_size * amount) + " " + food.weight_unit.unit
        binding.caloriesValueFoodView.text = "%.2f".format(food.kcal * amount) + " Kcal"
        binding.fiberValueFoodView.text = "%.2f".format(food.fiber * amount) + " g"
        binding.proteinValueFoodView.text = "%.2f".format(food.protein * amount) + " g"
        binding.waterValueFoodView.text = "%.2f".format(food.water * amount) + "ml"
        binding.sodiumValueFoodView.text = "%.2f".format(food.sodium * 1000 * amount) + " mg"
        binding.potassiumValueFoodView.text = "%.2f".format(food.potassium * 1000 * amount) + " mg"
    }

    private fun isLiked(isLiked: Boolean) {
        mIsLiked = isLiked
        if (isLiked) mFavBtn.setImageResource(R.drawable.ic_heart_filled)
        else mFavBtn.setImageResource(R.drawable.ic_heart)
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
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
