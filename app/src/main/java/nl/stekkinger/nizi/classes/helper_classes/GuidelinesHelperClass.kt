package nl.stekkinger.nizi.classes.helper_classes

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import nl.stekkinger.nizi.NiziApplication

class GuidelinesHelperClass {

    fun initializeGuidelines(cont: FragmentActivity?, layout: LinearLayout)
    {
        val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

        var count: Int = preferences.getInt("dietaryCount", 0)

        lateinit var descriptionKey: String
        lateinit var amountKey: String

        for (i in 0..count-1)
        {
            // Description                  // Amount
            // Min
            // Max
            // Feedback
            descriptionKey = "dietaryDescription" + i.toString()
            amountKey = "dietaryAmount" + i.toString()

            // Create elements
            var horizontalLayout = LinearLayout(cont)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            var descriptionTextView = TextView(cont)
            var amountTextView = TextView(cont)

            descriptionTextView.text = preferences.getString(descriptionKey, "")
            descriptionTextView.width = 400
            amountTextView.text = preferences.getInt(amountKey, 0).toString()

            horizontalLayout.addView(descriptionTextView)
            horizontalLayout.addView(amountTextView)

            layout.addView(horizontalLayout)
        }
    }
}