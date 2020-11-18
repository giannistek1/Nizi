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
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.classes.helper_classes.GuidelinesHelper
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment(private val dietaryGuidelines: ArrayList<DietaryGuideline>?): Fragment() {
    private lateinit var mCurrentDate: String
    private lateinit var model: DiaryViewModel
    private val sdf = GeneralHelper.getDateFormat()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Setting date for diary
        mCurrentDate = sdf.format(Date())
        val startDate: String = getDay(mCurrentDate, 0) // Wat is startDate?
        val endDate: String = getDay(mCurrentDate, 1) // Wat is endDate?
        model.setDiaryDate(startDate + "/" + endDate)

        // Gianni
        val today: String = getDay(mCurrentDate, 0)
        val yesterday: String = getDay(mCurrentDate, -1)

        view.fragment_home_btn_yesterday.setOnClickListener {
            val newDate = sdf.parse(getDay(mCurrentDate, -1))
            mCurrentDate = sdf.format(newDate!!)

            if(sdf.format(newDate) == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = mCurrentDate
            }

            refreshGuidelines()

            // Update UI
            view.fragment_home_btn_tomorrow.imageAlpha = 255
        }

        view.fragment_home_btn_tomorrow.setOnClickListener {
            val newDate = sdf.parse(getDay(mCurrentDate, 1))

            // if new date after today
            if (newDate!!.after(Date())) return@setOnClickListener

            mCurrentDate = sdf.format(newDate)

            if (sdf.format(newDate) == today) {
                // Update UI
                view.fragment_home_txt_day.text = getString(R.string.today)
                view.fragment_home_btn_tomorrow.imageAlpha = 20
            } else if(sdf.format(newDate) == yesterday) {
                view.fragment_home_txt_day.text = getString(R.string.yesterday)
            } else {
                view.fragment_home_txt_day.text = mCurrentDate
            }

            refreshGuidelines()
        }

        // Update UI
        view.fragment_home_txt_day.text = getString(R.string.today)
        view.fragment_home_btn_tomorrow.imageAlpha = 20

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
        val c = Calendar.getInstance()
        c.time = sdf.parse(newDate)!!
        c.add(Calendar.DATE, daysAdded)
        val resultdate = Date(c.timeInMillis)
        newDate = sdf.format(resultdate)
        return newDate
    }
}