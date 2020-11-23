package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food

class FoodViewFragment : Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mFood: Food
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

        model.selected.observe(this, Observer<Food> { food ->
            // Update the UI
            title_food_view.text = food.name
            Picasso.get().load(food.image_url).into(image_food_view)
            serving_size_value.text = food.portion_size.toString() + " " + food.weight_unit
            calories_value_food_view.text = food.kcal.toString() + " Kcal"
            fiber_value_food_view.text = food.fiber.toString() + " g"
            protein_value_food_view.text = food.protein.toString() + " g"
            water_value_food_view.text = food.water.toString() + "ml"
            sodium_value_food_view.text = (food.sodium *1000).toString() + " mg"
            potassium_value_food_view.text = (food.potassium * 1000).toString() + " mg"

            // store food product
            mFood = food
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                Toast.makeText(this.context, "toegevoegd", Toast.LENGTH_LONG).show()

                val portion = mServingInput.editText?.text.toString().trim().toDouble()
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