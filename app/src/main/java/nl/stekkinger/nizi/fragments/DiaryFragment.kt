package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.stekkinger.nizi.R

class DiaryFragment: Fragment() {
    private lateinit var mAddFood: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)
        mAddFood = view.findViewById(R.id.activity_add_food) as TextView
        mAddFood.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }
        return view
    }
}