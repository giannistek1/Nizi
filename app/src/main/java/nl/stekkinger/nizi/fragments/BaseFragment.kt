package nl.stekkinger.nizi.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.custom_toast.*
import nl.stekkinger.nizi.R

/**
 * A simple [Fragment] subclass.
 */
open class BaseFragment : Fragment() {
    private lateinit var customToastLayout: View
    protected lateinit var toastView: View

    protected lateinit var toastAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CustomToastLayout for toast (in case you want a disappearing toast that exists through multiple activities)
        customToastLayout = layoutInflater.inflate(R.layout.custom_toast, ll_custom_toast_wrapper)

        // Sets custom toast animation for every fragment
        toastAnimation = AnimationUtils.loadAnimation(
            activity,
            R.anim.move_up_fade_out
        )
    }
}