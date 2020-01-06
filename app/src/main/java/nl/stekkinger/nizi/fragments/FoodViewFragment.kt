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
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Food

class FoodViewFragment : Fragment() {

    private lateinit var model: DiaryViewModel
    private lateinit var mFood: Food
    private lateinit var mServingInput: TextInputLayout
//    private lateinit var mContext: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
        setHasOptionsMenu(true)

//        mContext = Activity()

        mServingInput = view.findViewById(R.id.serving_input_value)

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.selected.observe(this, Observer<Food> { food ->
            // Update the UI
            title_food_view.text = food.Name
            Picasso.get().load(food.Picture).into(image_food_view)
            serving_size_value.text = food.PortionSize.toString() + " " + food.WeightUnit
            calories_value_food_view.text = food.KCal.toString() + " Kcal"
            fiber_value_food_view.text = food.Fiber.toString() + " g"
            protein_value_food_view.text = food.Protein.toString() + " g"
            water_value_food_view.text = "10 g" // TODO update API with water input
            sodium_value_food_view.text = (food.Sodium * 1000).toString() + " mg"
            calcium_value_food_view.text = (food.Calcium * 1000).toString() + " mg"

            // store food product
            mFood = food
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_add_food -> {

                Log.d("hi", "added override")
                Toast.makeText(this.context, "added ovr", Toast.LENGTH_LONG).show()

                val portion = mServingInput.editText?.text.toString().trim().toDouble()
                model.addFood(mFood, portion)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}