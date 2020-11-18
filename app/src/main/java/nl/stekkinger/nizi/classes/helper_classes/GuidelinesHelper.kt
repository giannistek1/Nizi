package nl.stekkinger.nizi.classes.helper_classes

import android.R.attr.*
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentActivity
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import kotlin.random.Random


object GuidelinesHelper {

    // This Class is for displaying the guidelines

    // Should be a recyclerview

    fun initializeGuidelines(cont: FragmentActivity?, layout: LinearLayout, dietaryGuidelineList: ArrayList<DietaryGuideline>?)
    {
        layout.removeAllViews()

        // Description                  // Amount
        // Min
        // Max
        // Feedback

        val circleSize = cont!!.resources.getDimensionPixelSize(R.dimen.guidelineCircleSize)
        val iconSize = cont.resources.getDimensionPixelSize(R.dimen.guidelineIconSize)

        // Loop for every dietary restriction/guideline
        dietaryGuidelineList!!.forEachIndexed { _, dietaryGuideline ->

            // Create elements
            // Layouts
            // Root of the item
            val horizontalLayout = LinearLayout(cont)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLayout.setBackgroundColor(getColor(cont.baseContext, R.color.lightGray))
            var params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20,0,20,20)
            horizontalLayout.setPadding(15,10,15,10)
            horizontalLayout.layoutParams = params


            val headerLayout = LinearLayout(cont)
            headerLayout.orientation = LinearLayout.HORIZONTAL

            val verticalLayout1 = LinearLayout(cont)
            verticalLayout1.orientation = LinearLayout.VERTICAL
            params = LinearLayout.LayoutParams(0 ,LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)
            params.setMargins(20, 10,0,10)
            verticalLayout1.layoutParams = params

            /*val verticalLayout2 = LinearLayout(cont)
            verticalLayout2.orientation = LinearLayout.VERTICAL
            verticalLayout2.gravity = Gravity.CENTER_HORIZONTAL
            verticalLayout2.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)*/

            val verticalLayout2 = RelativeLayout(cont)
            verticalLayout2.gravity = Gravity.CENTER_HORIZONTAL
            verticalLayout2.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)

            // Views
            val icon = ImageView(cont)
            val descriptionTextView = TextView(cont)
            val minimumTextView = TextView(cont)
            val maximumTextView = TextView(cont)
            val feedbackTextView = TextView(cont)
            val amountTextView = TextView(cont)
            val progressBar = ProgressBar(cont, null, R.style.Widget_AppCompat_ProgressBar_Horizontal)

            // Icon
            if (dietaryGuideline.description.contains("Calorie")) {
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(getColor(cont, R.color.red)))
                icon.setImageResource(R.drawable.ic_calories)
            }

            else if (dietaryGuideline.description.contains("Vocht")) {
                icon.setImageResource(R.drawable.ic_water)
                icon.setColorFilter(getColor(cont, R.color.blue))
            }
            else if (dietaryGuideline.description.contains("Natrium")) {
                icon.setImageResource(R.drawable.ic_salt)
                icon.setColorFilter(getColor(cont, R.color.darkGray))
            }
            else if (dietaryGuideline.description.contains("Kalium")) {
                icon.setImageResource(R.drawable.ic_k)
                icon.setColorFilter(getColor(cont, R.color.blue))
            }
            else if (dietaryGuideline.description.contains("Eiwit")) {
                icon.setImageResource(R.drawable.ic_protein)
                icon.setColorFilter(getColor(cont, R.color.black))
            }
            else if (dietaryGuideline.description.contains("Vezel"))
            {
                icon.setImageResource(R.drawable.ic_grain)
                icon.setColorFilter(getColor(cont, R.color.yellow))
            }

            val iconParams = LinearLayout.LayoutParams(iconSize, iconSize)
            iconParams.setMargins(0, 8, 15, 0)
            icon.layoutParams = iconParams

            descriptionTextView.textSize = 16f
            descriptionTextView.text = dietaryGuideline.plural.capitalize()
            params = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0,0,0,20)
            descriptionTextView.layoutParams = params

            params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0,0,0,20)
            maximumTextView.layoutParams = params

            minimumTextView.text = "${cont.getString(R.string.min)} ${dietaryGuideline.minimum} ${dietaryGuideline.weightUnit}"
            if (dietaryGuideline.minimum == 0)
                minimumTextView.visibility = View.GONE

            maximumTextView.text = "${cont.getString(R.string.max)} ${dietaryGuideline.maximum} ${dietaryGuideline.weightUnit}"
            if (dietaryGuideline.maximum == 0)
                maximumTextView.visibility = View.INVISIBLE

            // Amount Text View
            // Center align text
            amountTextView.gravity = Gravity.CENTER
            val amountTextViewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            // Center align view
            amountTextViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            amountTextView.layoutParams = amountTextViewParams

            val randomProgress = Random.nextInt(150)
            val progress = randomProgress
            var amount = 0

            if (dietaryGuideline.maximum != 0)
                //progress = dietaryGuideline.amount/dietaryGuideline.maximum * 100
                amount = (progress*dietaryGuideline.maximum/100)
            else
                //progress = dietaryGuideline.amount/dietaryGuideline.minimum * 100
                amount = (progress*dietaryGuideline.minimum/100)

            amountTextView.text = amount.toString() + " " + dietaryGuideline.weightUnit

            // Progressbar
            val progressBarParams = RelativeLayout.LayoutParams(circleSize, circleSize)
            // Center align view
            progressBarParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            progressBar.layoutParams = progressBarParams
            progressBar.isIndeterminate = false

            // if ate too much
            if (progress > 100 && dietaryGuideline.maximum != 0)
                progressBar.progressDrawable = ContextCompat.getDrawable(cont, R.drawable.circular_progress_bar_red)
            else
                progressBar.progressDrawable = ContextCompat.getDrawable(cont, R.drawable.circular_progress_bar)
            if (dietaryGuideline.minimum != 0)
                progressBar.background = ContextCompat.getDrawable(cont, R.drawable.circle_shape_yellow)
            else
                progressBar.background = ContextCompat.getDrawable(cont, R.drawable.circle_shape)
            progressBar.max = 100
            progressBar.progress = progress

            // FEEDBACK
            // if filled && maximum not set
            if (progress >= 100 && dietaryGuideline.maximum == 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_positive)
                feedbackTextView.setTextColor(getColor(cont, R.color.lime))
            }
            else if (progress > 100 && dietaryGuideline.maximum > 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_negative, dietaryGuideline.plural)
                feedbackTextView.setTextColor(getColor(cont, R.color.red))
            }
            else if (progress <= 100 && dietaryGuideline.minimum != 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_encouraging, dietaryGuideline.plural)
                feedbackTextView.setTextColor(getColor(cont, R.color.yellow))
            }


            headerLayout.addView(icon)
            headerLayout.addView(descriptionTextView)

            verticalLayout1.addView(headerLayout)
            verticalLayout1.addView(minimumTextView)
            verticalLayout1.addView(maximumTextView)
            verticalLayout1.addView(feedbackTextView)

            verticalLayout2.addView(amountTextView)
            verticalLayout2.addView(progressBar)

            horizontalLayout.addView(verticalLayout1)
            horizontalLayout.addView(verticalLayout2)

            layout.addView(horizontalLayout)
        }
    }
}