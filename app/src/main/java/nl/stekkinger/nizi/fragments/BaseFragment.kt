package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.databinding.CustomToastBinding

open class BaseFragment : Fragment() {
    private var _binding: CustomToastBinding? = null
    private val binding get() = _binding!!

//    private lateinit var customToastLayout: View
//    protected lateinit var toastView: View

    protected lateinit var toastAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = CustomToastBinding.inflate(layoutInflater)

        // CustomToastLayout for toast (in case you want a disappearing toast that exists through multiple activities)
//        customToastLayout = layoutInflater.inflate(R.layout.custom_toast, binding.llCustomToastWrapper)

        // Sets custom toast animation for every fragment
        toastAnimation = AnimationUtils.loadAnimation(
            activity,
            R.anim.move_up_fade_out
        )
    }
}