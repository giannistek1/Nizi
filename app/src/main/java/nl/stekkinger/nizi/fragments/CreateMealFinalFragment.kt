package nl.stekkinger.nizi.fragments

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.content.Context.SEARCH_SERVICE
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_create_meal_final.*
import kotlinx.android.synthetic.main.fragment_create_meal_final.view.*
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.repositories.FoodRepository


class CreateMealFinalFragment: Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchAdapter: FoodSearchAdapter
    private lateinit var mealProductAdapter: MealProductAdapter
    private lateinit var mInputMealName: TextInputLayout
    private var mMealName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal_final, container, false)

        mInputMealName = view.findViewById(R.id.input_meal_name)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.create_meal_form_btn.setOnClickListener { createMeal() }

        return view
    }

    private fun createMeal(){
        if (!validateMealName()) {
            // failed
            return
        }
        // validate success
        model.createMeal(mMealName)
        (activity)!!.supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            AddMealFragment()
        ).commit()
    }

    private fun validateMealName(): Boolean {
        mMealName = mInputMealName.editText?.text.toString().trim()
        // validate input for errors
        when {
            mMealName.isEmpty() -> { mInputMealName.error = getString(R.string.meal_name_empty)}
            mMealName.length > 30 -> { mInputMealName.error = getString(R.string.meal_name_too_long) }
            else -> { mInputMealName.error = null }
        }
        return true
    }
}