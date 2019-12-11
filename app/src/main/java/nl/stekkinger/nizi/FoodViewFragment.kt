package nl.stekkinger.nizi

import android.os.Bundle
import android.util.Log.d
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_food_view.*
import nl.stekkinger.nizi.classes.Food

class FoodViewFragment : Fragment() {

    private lateinit var model: DiaryViewModel

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

        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_add_food -> {
                Toast.makeText(this.context, "adding", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}