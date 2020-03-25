package nl.stekkinger.nizi.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.Consumptions
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Food

class ConsumptionViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mConsumption: Consumption
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

        // Update the UI
        title_food_view.text = mConsumption.FoodName
        serving_size_value.text = mConsumption.Amount.toString() + " " + mConsumption.WeightUnitId
        calories_value_food_view.text = mConsumption.KCal.toString() + " Kcal"
        fiber_value_food_view.text = mConsumption.Fiber.toString() + " g"
        protein_value_food_view.text = mConsumption.Protein.toString() + " g"
        water_value_food_view.text = "0 ml" // TODO update API with water input
        sodium_value_food_view.text = (mConsumption.Sodium * 1000).toString() + " mg"
        calcium_value_food_view.text = (mConsumption.Calium * 1000).toString() + " mg"

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}