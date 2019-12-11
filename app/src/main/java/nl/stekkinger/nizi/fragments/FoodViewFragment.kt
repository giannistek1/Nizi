package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.Food

class FoodViewFragment : Fragment() {

    private lateinit var model: DiaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_food_view, container, false)
        //TODO: add food item data

        // get the DiaryViewModel
        model = activity?.run {
            ViewModelProviders.of(this).get(DiaryViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.selected.observe(this, Observer<Food> { food ->
            // Update the UI

        })

        return view
    }
}