package nl.stekkinger.nizi.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.d
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import kotlinx.android.synthetic.main.fragment_food_view.view.*
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.ArrayList


class FoodViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mFood: Food
    private lateinit var mServingInput: TextInputEditText
    private lateinit var mDecreaseBtn: ImageButton
    private lateinit var mSaveBtn: ImageButton
    private lateinit var mFavBtn: ImageButton
    private lateinit var mCurrentFragment: String
    private var mIsLiked = false
    private var mFavoriteSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
        setHasOptionsMenu(true)


        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        mCurrentFragment = model.getCurrentFragment()

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
            title_food_view.text = food.name
            Picasso.get().load(food.image_url).into(image_food_view)
            updateUI()
        })

        view.edit_food_view.visibility = GONE

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
                    is FoodRepository.FavoritesState.Error -> {
                        // TODO: handle events below
//                        wat gaan we hier doen?
//                        Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                    is FoodRepository.FavoritesState.Loading -> {
//                        spinner toevoegen aan consumptionview?
//                        Toast.makeText(activity, "LOADING", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
            }
        }

        mFavBtn = view.findViewById(R.id.heart_food_view)
        mSaveBtn = view.save_btn
        mDecreaseBtn = view.decrease_portion
        mServingInput = view.findViewById(R.id.serving_input) as TextInputEditText

        mServingInput.addTextChangedListener(textWatcher)

        // this view does not have a delete button
        view.delete_food_view.visibility = GONE

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
                            model.resetToggleFavoriteState()
                        } else {
                            d("add", "add")
                            // added
                            isLiked(true)
                            Toast.makeText(activity, R.string.added_favorite, Toast.LENGTH_SHORT).show()
                            model.fetchFavorites()
                            model.resetToggleFavoriteState()
                        }
                        view.fragment_food_view_loader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                    is FoodRepository.State.Error -> {
                        if(mIsLiked) Toast.makeText(activity, R.string.error_delete_favorite, Toast.LENGTH_SHORT).show()
                        else Toast.makeText(activity, R.string.error_add_favorite, Toast.LENGTH_SHORT).show()
                        view.fragment_food_view_loader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                    is FoodRepository.State.Loading -> {
                        view.fragment_food_view_loader.visibility = VISIBLE
                    }
                    else -> {
                        view.fragment_food_view_loader.visibility = GONE
                        mFavBtn.isEnabled = true
                        mFavBtn.isClickable = true
                    }
                }
            }
        }

        // click listeners
        view.heart_food_view.setOnClickListener {
            // prevent button spamming until result
            mFavBtn.isEnabled = false
            mFavBtn.isClickable = false

            if(mIsLiked) {
                d("del", "del")
                model.deleteFavorite(mFavoriteSelected)
            } else {
                model.addFavorite(mFood.id)
            }
        }

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

            when (mCurrentFragment) {
                "food" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.addConsumption(mFood, amount)

                    (activity)!!.supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        DiaryFragment()
                    ).commit()
                }
                "meal" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.addMealProduct(mFood, amount)

                    (activity)!!.supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        CreateMealFragment()
                    ).commit()
                }
                "mealEdit" -> {
                    val amount: Float = mServingInput.text.toString().trim().toFloat()
                    model.editMealProduct(amount)

                    (activity)!!.supportFragmentManager.beginTransaction().replace(
                        R.id.activity_main_fragment_container,
                        CreateMealFragment()
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

        return view
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
        serving_size_value.text = "%.2f".format(food.portion_size * amount) + " " + food.weight_unit.unit
        calories_value_food_view.text = "%.2f".format(food.kcal * amount) + " Kcal"
        fiber_value_food_view.text = "%.2f".format(food.fiber * amount) + " g"
        protein_value_food_view.text = "%.2f".format(food.protein * amount) + " g"
        water_value_food_view.text = "%.2f".format(food.water * amount) + "ml"
        sodium_value_food_view.text = "%.2f".format(food.sodium * 1000 * amount) + " mg"
        potassium_value_food_view.text = "%.2f".format(food.potassium * 1000 * amount) + " mg"
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                Toast.makeText(this.context, R.string.add_food_success, Toast.LENGTH_LONG).show()

                val portion = mServingInput.text.toString().trim().toFloat()
                model.addConsumption(mFood, portion)

                val fragment: Fragment = DiaryFragment()
                val bundle = Bundle()
                bundle.putBoolean("refresh", true)
                fragment.arguments = bundle

                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    fragment
                ).commit()
                true
            }
            R.id.back_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}