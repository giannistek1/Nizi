package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.dietary.DietaryGuideline
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment(private val dietaryGuidelines: ArrayList<DietaryGuideline>?): Fragment() {
    private lateinit var mCurrentDate: String
    private lateinit var model: DiaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Setting date for diary
        mCurrentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val startDate: String = getDay(mCurrentDate, 0)
        val endDate: String = getDay(mCurrentDate, 1)
        model.setDiaryDate(startDate + "/" + endDate)


        view.fragment_home_btn_yesterday.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val newDate = sdf.parse(getDay(mCurrentDate, -1))
            mCurrentDate = sdf.format(newDate)

            fragment_home_txt_day.text = mCurrentDate
            refreshGuidelines()
        }

        view.fragment_home_btn_tomorrow.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val newDate = sdf.parse(getDay(mCurrentDate, 1))
            // if new date is NOT after Today
            if (!newDate.after(Date())) {
                mCurrentDate = sdf.format(newDate)

                fragment_home_txt_day.text = mCurrentDate
                refreshGuidelines()
            }
        }

        view.fragment_home_txt_day.text = mCurrentDate


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

        refreshGuidelines()
    }

    fun refreshGuidelines()
    {
        // Guard
        if (dietaryGuidelines == null) { return }

        GuidelinesHelper.initializeGuidelines(activity, fragment_home_ll_guidelines, dietaryGuidelines)
    }

    fun getDay(date: String, daysAdded: Int): String {
        Log.d("AAAAAA", "BBBBB")
        var newDate = date
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val c = Calendar.getInstance()
        c.time = sdf.parse(newDate)!!
        c.add(Calendar.DATE, daysAdded)
        val resultdate = Date(c.timeInMillis)
        newDate = sdf.format(resultdate)
        return newDate
    }
}