package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.text.Editable
import android.view.*
import android.text.TextWatcher
import android.util.Log.d
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import kotlinx.android.synthetic.main.fragment_food_view.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.FoodMealComponent


class ConsumptionViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mConsumption: ConsumptionResponse
    private lateinit var mServingInput: TextInputEditText
    private lateinit var mDecreaseBtn: ImageButton
    private lateinit var mSaveBtn: ImageButton

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

        model.selectedEdit.observe(this, Observer<ConsumptionResponse> { food ->
            // store food product
            mConsumption = food

            // Update the UI
            val amount: Float = mConsumption.amount
            view.title_food_view.text = mConsumption.food_meal_component.name
            Picasso.get().load(mConsumption.food_meal_component.image_url).into(image_food_view)
            serving_input.setText(mConsumption.amount.toString(), TextView.BufferType.EDITABLE)
        })

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
            d("-----", "-------------------------")
            Toast.makeText(this.context, R.string.update_food_success, Toast.LENGTH_LONG).show()

            val portion: Float = mServingInput.text.toString().trim().toFloat()
            model.editFood(mConsumption, portion)

            (activity)!!.supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                DiaryFragment()
            ).commit()
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
        d("lo", amount.toString())

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
        val oldAmount: Float = mConsumption.amount
        val food: FoodMealComponent = mConsumption.food_meal_component
        serving_size_value.text = "%.2f".format(food.portion_size / oldAmount * amount) + " " + mConsumption.weight_unit.unit
        calories_value_food_view.text = "%.2f".format(food.kcal / oldAmount * amount) + " Kcal"
        fiber_value_food_view.text = "%.2f".format(food.fiber / oldAmount * amount) + " g"
        protein_value_food_view.text = "%.2f".format(food.protein / oldAmount * amount) + " g"
        water_value_food_view.text = "%.2f".format(food.water / oldAmount * amount) + "ml"
        sodium_value_food_view.text = "%.2f".format(food.sodium / oldAmount * 1000 * amount) + " mg"
        potassium_value_food_view.text = "%.2f".format(food.potassium / oldAmount * 1000 * amount) + " mg"
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
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                Toast.makeText(this.context, R.string.update_food_success, Toast.LENGTH_LONG).show()

                val portion = mServingInput.text.toString().trim().toFloat()
                model.editFood(mConsumption, portion)

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