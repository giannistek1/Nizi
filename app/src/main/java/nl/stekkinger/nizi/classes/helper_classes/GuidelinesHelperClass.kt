package nl.stekkinger.nizi.classes.helper_classes

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.marginBottom
import androidx.fragment.app.FragmentActivity
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryGuideline

class GuidelinesHelperClass {

    // This Class is for displaying the guidelines
    //lateinit var dietaryGuidelineList: ArrayList<DietaryGuideline>

    fun initializeGuidelines(cont: FragmentActivity?, layout: LinearLayout, dietaryGuidelineList: ArrayList<DietaryGuideline>?)
    {
        //val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)

        //var count: Int = preferences.getInt("dietaryCount", 0)

        lateinit var descriptionKey: String
        lateinit var amountKey: String

        dietaryGuidelineList!!.forEachIndexed { index, dietaryGuideline ->
            // Description                  // Amount
            // Min
            // Max
            // Feedback

            //descriptionKey = "dietaryDescription" + i.toString()
            //amountKey = "dietaryAmount" + i.toString()

            // Create elements
            // Layouts
            var horizontalLayout = LinearLayout(cont)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLayout.setBackgroundColor(getColor(cont!!.baseContext, R.color.gray))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20,0,20,20)
            horizontalLayout.layoutParams = params

            var verticalLayout1 = LinearLayout(cont)
            verticalLayout1.orientation = LinearLayout.VERTICAL
            var verticalLayout2 = LinearLayout(cont)
            verticalLayout2.orientation = LinearLayout.VERTICAL

            var descriptionTextView = TextView(cont)
            var minimumTextView = TextView(cont)
            var maximumTextView = TextView(cont)
            var feedbackTextView = TextView(cont)
            var amountTextView = TextView(cont)

            descriptionTextView.text = dietaryGuidelineList[index].description
            descriptionTextView.width = 400
            minimumTextView.text = dietaryGuidelineList[index].minimum.toString()
            maximumTextView.text = dietaryGuidelineList[index].maximum.toString()
            feedbackTextView.text = "Goedzo!"

            verticalLayout1.addView(descriptionTextView)
            verticalLayout1.addView(minimumTextView)
            verticalLayout1.addView(maximumTextView)
            verticalLayout1.addView(feedbackTextView)

            verticalLayout2.addView(amountTextView)

            horizontalLayout.addView(verticalLayout1)
            horizontalLayout.addView(verticalLayout2)

            layout.addView(horizontalLayout)
        }
    }
}