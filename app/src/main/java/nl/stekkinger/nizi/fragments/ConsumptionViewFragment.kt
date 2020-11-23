package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import kotlinx.android.synthetic.main.fragment_food_view.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse


class ConsumptionViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mConsumption: ConsumptionResponse
    private lateinit var mServingInput: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
        setHasOptionsMenu(true)

        mServingInput = view.findViewById(R.id.serving_input_value)

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.selectedEdit.observe(this, Observer<ConsumptionResponse> { food ->
            // store food product
            mConsumption = food

            // Update the UI
            view.title_food_view.text = mConsumption.food_meal_component[0].name
            Picasso.get().load(mConsumption.food_meal_component[0].image_url).into(image_food_view)
            serving_input.setText(mConsumption.amount.toString(), TextView.BufferType.EDITABLE)
            serving_size_value.text = mConsumption.food_meal_component[0].portion_size.toString() + " " + mConsumption.weight_unit.unit
            calories_value_food_view.text = mConsumption.food_meal_component[0].kcal.toString() + " Kcal"
            protein_value_food_view.text = mConsumption.food_meal_component[0].protein.toString() + " g"
            potassium_value_food_view.text = mConsumption.food_meal_component[0].potassium.toString() + " g"
            sodium_value_food_view.text = (mConsumption.food_meal_component[0].sodium * 1000).toString() + " mg"
            fiber_value_food_view.text = mConsumption.food_meal_component[0].fiber.toString() + " g"
            water_value_food_view.text = mConsumption.food_meal_component[0].water.toString() + "ml"
        })


        //calcium_value_food_view.text = (mConsumption.food_meal_component[0].water * 1000).toString() + " mg"

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                Toast.makeText(this.context, "toegevoegd", Toast.LENGTH_LONG).show()

                val portion = mServingInput.editText?.text.toString().trim().toFloat()
//                mConsumption.amount = portion
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