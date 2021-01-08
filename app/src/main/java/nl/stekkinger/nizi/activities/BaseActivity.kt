package nl.stekkinger.nizi.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_doctor_main.*
import kotlinx.android.synthetic.main.custom_toast.*
import nl.stekkinger.nizi.R

open class BaseActivity : AppCompatActivity() {
    protected lateinit var customToastLayout: View
    protected lateinit var toastView: View
    protected lateinit var loader: View

    protected lateinit var toastAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CustomToastLayout for toast (in case you want a disappearing toast that exists through multiple activities)
        customToastLayout = layoutInflater.inflate(R.layout.custom_toast, ll_custom_toast_wrapper)

        // Sets custom toast animation for every activity
        toastAnimation = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.move_up_fade_out
        )
    }

    //region Hides Keyboard on touch
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
    //endregion
}