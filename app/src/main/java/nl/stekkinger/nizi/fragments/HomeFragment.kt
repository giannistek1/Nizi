package nl.stekkinger.nizi.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.*
import nl.stekkinger.nizi.NiziApplication
import java.lang.Math.round
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelperClass
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.DietaryGuideline
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment(var cont: AppCompatActivity, private val dietaryGuidelines: ArrayList<DietaryGuideline>?): Fragment() {
    private lateinit var mCurrentDate: String
    private lateinit var model: DiaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // setting date for diary
        mCurrentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        var startDate: String = getDay(mCurrentDate, 0)
        var endDate: String = getDay(mCurrentDate, 1)
        model.setDiaryDate(startDate + "/" + endDate)



//        view?.diary_prev_date.setOnClickListener {
//            endDate = mCurrentDate
//            startDate = getDay(mCurrentDate, -1)
//            model.setDiaryDate(startDate + "/" + endDate)
//            mCurrentDate = startDate
//            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == endDate) {
//                activity_main_txt_header.text = "Gisteren"
//            } else {
//                activity_main_txt_header.text = startDate
//            }
//        }
//
//        view?.diary_next_date.setOnClickListener {
//            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) != mCurrentDate) {
//                startDate= getDay(mCurrentDate, 1)
//                endDate = getDay(mCurrentDate, 2)
//                model.setDiaryDate(startDate + "/" + endDate)
//                mCurrentDate = startDate
//                if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == startDate) {
//                    activity_main_txt_header.text = "Vandaag"
//                } else if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == endDate) {
//                    activity_main_txt_header.text = "Gisteren"
//                } else {
//                    activity_main_txt_header.text = startDate
//                }
//            }
//        }

        return view
    }

    override fun onStart() {
        super.onStart()
        /*model.getDiary().observe(viewLifecycleOwner, Observer { result ->
            // update total intake values of the day (guidelines)
            dietaryGuidelines?.forEachIndexed { index, element ->
                if (element.description.contains("Calorie"))
                    dietaryGuidelines[index].amount = result.KCalTotal.toInt()
                else if (element.description.contains("Vocht"))
                    dietaryGuidelines[index].amount = (round(result.WaterTotal * 100) / 100)
                else if (element.description.contains("Natrium"))
                    dietaryGuidelines[index].amount = (round(result.SodiumTotal * 100) / 100)
                else if (element.description.contains("Kalium"))
                    dietaryGuidelines[index].amount = (round(result.CaliumTotal * 100) / 100)
                else if (element.description.contains("Eiwit"))
                    dietaryGuidelines[index].amount = (round(result.ProteinTotal * 100) / 100)
                else if (element.description.contains("Vezel"))
                    dietaryGuidelines[index].amount = (round(result.FiberTotal * 100) / 100)
            }


        })*/
        if (fragment_home_btn_yesterday != null)
            fragment_home_btn_yesterday.setOnClickListener {
                refreshGuidelines()
            }

        if (fragment_home_btn_tomorrow != null)
            fragment_home_btn_tomorrow.setOnClickListener {
                refreshGuidelines()
            }

        if (fragment_home_txt_day != null)
            fragment_home_txt_day.text = mCurrentDate

        refreshGuidelines()
    }

    fun refreshGuidelines()
    {
        if (dietaryGuidelines != null) {
            val helperClass = GuidelinesHelperClass()
            helperClass.initializeGuidelines(cont, fragment_home_ll_guidelines, dietaryGuidelines)
        }
    }

    fun getDay(date: String, daysAdded: Int): String {
        Log.d("AAAAAA", "BBBBB")
        var newDate = date
        var sdf = SimpleDateFormat("yyyy-MM-dd")
        val c = Calendar.getInstance()
        c.time = sdf.parse(newDate)
        c.add(Calendar.DATE, daysAdded)
        val resultdate = Date(c.timeInMillis)
        newDate = sdf.format(resultdate)
        return newDate
    }
}