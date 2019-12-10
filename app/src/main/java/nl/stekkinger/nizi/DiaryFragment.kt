package nl.stekkinger.nizi

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DiaryFragment: Fragment() {
    private lateinit var mAddFood: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)
        mAddFood = view.findViewById(R.id.activity_add_food) as TextView
        mAddFood.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.activity_main_fragment_container, AddFoodFragment())
                .commit()
        }
        return view
    }
}