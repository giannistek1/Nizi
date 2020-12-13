package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import kotlinx.android.synthetic.main.fragment_food_view.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food

class FoodViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mFood: Food
    private lateinit var mServingInput: TextInputEditText
    private lateinit var mDecreaseBtn: ImageButton

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

        model.selected.observe(this, Observer<Food> { food ->
            // store food product
            mFood = food
            // Update the UI
            title_food_view.text = food.name
            Picasso.get().load(food.image_url).into(image_food_view)
            updateUI()
        })
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
        val food: Food = mFood
        serving_size_value.text = "%.2f".format(food.portion_size * amount) + " " + food.weight_unit.unit
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
            if (count != 0) {
                val firstChar: String? = s?.get(0).toString()
                if(firstChar == ".") {
                    mServingInput.setText("")
                } else {
                    updateUI()
                }
            }

            // disable/enable decrease button if needed
            if (mServingInput.text.toString() != "") {
                var portion: Float = mServingInput.text.toString().toFloat()
                if(portion > 0.5f) {
                    mDecreaseBtn.isEnabled = true
                    mDecreaseBtn.isClickable = true
                } else {
                    mDecreaseBtn.isEnabled = false
                    mDecreaseBtn.isClickable = false
                }
            } else {
                mDecreaseBtn.isEnabled = false
                mDecreaseBtn.isClickable = false
            }
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
                model.addFood(mFood, portion)

                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
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