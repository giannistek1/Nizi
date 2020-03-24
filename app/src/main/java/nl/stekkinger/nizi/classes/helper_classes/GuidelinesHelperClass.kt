package nl.stekkinger.nizi.classes.helper_classes

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.marginBottom
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentActivity
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryGuideline
import java.net.ConnectException
import kotlin.random.Random

class GuidelinesHelperClass {

    // This Class is for displaying the guidelines
    //lateinit var dietaryGuidelineList: ArrayList<DietaryGuideline>

    fun initializeGuidelines(cont: FragmentActivity?, layout: LinearLayout, dietaryGuidelineList: ArrayList<DietaryGuideline>?)
    {
        dietaryGuidelineList!!.forEachIndexed { index, dietaryGuideline ->
            // Description                  // Amount
            // Min
            // Max
            // Feedback

            // Create elements
            // Layouts
            val horizontalLayout = LinearLayout(cont)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLayout.setBackgroundColor(getColor(cont!!.baseContext, R.color.gray))
            var params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20,0,20,20)
            horizontalLayout.layoutParams = params

            val headerLayout = LinearLayout(cont)
            headerLayout.orientation = LinearLayout.HORIZONTAL
            val verticalLayout1 = LinearLayout(cont)
            verticalLayout1.orientation = LinearLayout.VERTICAL
            val verticalLayout2 = LinearLayout(cont)
            verticalLayout2.orientation = LinearLayout.VERTICAL
            verticalLayout2.gravity = Gravity.CENTER_HORIZONTAL
            verticalLayout2.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

            val icon = ImageView(cont)
            val descriptionTextView = TextView(cont)
            val minimumTextView = TextView(cont)
            val maximumTextView = TextView(cont)
            val feedbackTextView = TextView(cont)
            val amountTextView = TextView(cont)
            val progressBar = ProgressBar(cont, null, R.style.Widget_AppCompat_ProgressBar_Horizontal)

            if (dietaryGuideline.description.contains("calorie")) {
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(getColor(cont, R.color.red)))
                icon.setImageResource(R.drawable.ic_calories)
            }
            else if (dietaryGuideline.description.contains("vocht")) {
                icon.setImageResource(R.drawable.ic_water)
                icon.setColorFilter(getColor(cont, R.color.blue))
            }
            else if (dietaryGuideline.description.contains("natrium")) {
                icon.setImageResource(R.drawable.ic_salt)
                icon.setColorFilter(getColor(cont, R.color.darkGray))
            }
            else if (dietaryGuideline.description.contains("kalium")) {
                icon.setImageResource(R.drawable.ic_salt)
                icon.setColorFilter(getColor(cont, R.color.blue))
            }
            else if (dietaryGuideline.description.contains("eiwit")) {
                icon.setImageResource(R.drawable.ic_protein)
                icon.setColorFilter(getColor(cont, R.color.black))
            }
            else if (dietaryGuideline.description.contains("vezel"))
            {
                icon.setImageResource(R.drawable.ic_grain)
                icon.setColorFilter(getColor(cont, R.color.yellow))
            }

            icon.layoutParams = LinearLayout.LayoutParams(100, 100)
            descriptionTextView.text = dietaryGuideline.restriction
            descriptionTextView.width = 400
            minimumTextView.text = "${cont.getString(R.string.min)} ${dietaryGuideline.minimum.toString()}"
            if (dietaryGuideline.minimum == 0)
                minimumTextView.visibility = View.GONE

            maximumTextView.text = "${cont.getString(R.string.max)} ${dietaryGuideline.maximum.toString()}"
            if (dietaryGuideline.maximum == 0)
                maximumTextView.visibility = View.GONE

            val randomProgress = Random.nextInt(100)

            amountTextView.gravity = Gravity.CENTER
            amountTextView.text = (randomProgress*dietaryGuideline.maximum/100).toString()

            // Progressbar
            progressBar.layoutParams = LinearLayout.LayoutParams(200, 200)
            progressBar.isIndeterminate = false
            progressBar.progressDrawable = ContextCompat.getDrawable(cont, R.drawable.circular_progress_bar)
            progressBar.background = ContextCompat.getDrawable(cont, R.drawable.circle_shape)
            progressBar.max = 100
            progressBar.progress = randomProgress // Amount / maximum * 100 if minimum = 0
            if (progressBar.progress >= 100 && dietaryGuideline.maximum != 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_positive)
                feedbackTextView.setTextColor(getColor(cont, R.color.lime))
            }
            else if (progressBar.progress >= 100 && dietaryGuideline.maximum > 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_negative, dietaryGuideline.restriction)
                feedbackTextView.setTextColor(getColor(cont, R.color.red))
            }
            else if (progressBar.progress <= 100 && dietaryGuideline.minimum != 0) {
                feedbackTextView.text = cont.getString(R.string.feedback_encouraging, dietaryGuideline.restriction)
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