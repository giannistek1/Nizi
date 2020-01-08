package nl.stekkinger.nizi.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.R

class HomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val preferences = NiziApplication.instance.getSharedPreferences("NIZI", Context.MODE_PRIVATE)
        fragment_home_txt_guideline1.text = preferences.getString("dietaryDescription", "")
        fragment_home_txt_guideline1_amount.text = preferences.getInt("dietaryAmount", 0).toString()
    }
}