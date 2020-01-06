package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.FoodSearchAdapter
import java.text.SimpleDateFormat
import java.util.*

class DiaryFragment: Fragment() {
    private lateinit var mAddFood: TextView
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)
        mAddFood = view.findViewById(R.id.activity_add_food) as TextView

        val recyclerView: RecyclerView = view.findViewById(R.id.diary_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = FoodSearchAdapter(model)
        recyclerView.adapter = adapter

        mAddFood.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        model.setDiaryDate(SimpleDateFormat("yyyy-MM-dd").format(Date()))

        // get the results of food search
        model.getDiary().observe(viewLifecycleOwner, Observer { result ->
            Log.d("LOGLIST", result.KCalTotal.toString())
//            adapter.setFoodList(foodList)
        })
        return view
    }
}