package nl.stekkinger.nizi.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import nl.stekkinger.nizi.NiziApplication
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DietaryGuideline
import nl.stekkinger.nizi.classes.DietaryView
import nl.stekkinger.nizi.repositories.DietaryRepository

class HomeFragment(var cont: AppCompatActivity, var dietaryGuidelines: ArrayList<DietaryGuideline>?): Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (dietaryGuidelines != null) {
            val helperClass = GuidelinesHelperClass()
            helperClass.initializeGuidelines(cont, fragment_home_ll_guidelines, dietaryGuidelines)
        }
    }
}