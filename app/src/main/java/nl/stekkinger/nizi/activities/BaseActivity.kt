package nl.stekkinger.nizi.activities

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.databinding.CustomToastBinding

open class BaseActivity : AppCompatActivity() {

    protected lateinit var customToastBinding: CustomToastBinding

    protected lateinit var customToastLayout: View
    protected lateinit var loader: View

    protected lateinit var toastAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customToastBinding = CustomToastBinding.inflate(layoutInflater)
        //val view = customToastBinding.root
        //setContentView(view)

        // CustomToastLayout for toast (in case you want a disappearing toast that exists through multiple activities)
        customToastLayout = layoutInflater.inflate(R.layout.custom_toast, customToastBinding.llCustomToastWrapper)

        // Sets custom toast animation for every activity
        toastAnimation = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.move_up_fade_out
        )
    }

    //region Hides Keyboard on touch
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
    //endregion
}