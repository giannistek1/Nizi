package nl.stekkinger.nizi.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import org.w3c.dom.Text

class HomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

        var count: Int = preferences.getInt("dietaryCount", 0)

        lateinit var descriptionKey: String
        lateinit var amountKey: String

        var horizontalLayouts: ArrayList<LinearLayout> = arrayListOf()

        //var descriptionTextViews: ArrayList<TextView> = arrayListOf()
        //var amountTextViews: ArrayList<TextView> = arrayListOf()

        for (i in 0..count-1)
        {
            descriptionKey = "dietaryDescription" + i.toString()
            amountKey = "dietaryAmount" + i.toString()

            var horizontalLayout = LinearLayout(activity)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            var descriptionTextView = TextView(activity)
            var amountTextView = TextView(activity)

            descriptionTextView.setText(preferences.getString(descriptionKey, ""))
            descriptionTextView.width = 400
            amountTextView.setText(preferences.getInt(amountKey, 0).toString())

            horizontalLayout.addView(descriptionTextView)
            horizontalLayout.addView(amountTextView)

            fragment_home_ll_guidelines_content.addView(horizontalLayout)

        }
        /*fragment_home_txt_guideline1.text = preferences.getString("dietaryDescription1", "")
        fragment_home_txt_guideline1_amount.text = preferences.getInt("dietaryAmount1", 0).toString()*/
    }
}