package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_diary.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConsumptionAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*

class DiaryFragment: Fragment() {
    private lateinit var mAddFood: TextView
    private lateinit var mPrevDate: ImageButton
    private lateinit var mNextDate: ImageButton
    private lateinit var mCurrentDate: String
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: ConsumptionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)
        mAddFood = view.findViewById(R.id.activity_add_food) as TextView
        mPrevDate = view.findViewById(R.id.diary_prev_date)
        mNextDate = view.findViewById(R.id.diary_next_date)


        val recyclerView: RecyclerView = view.findViewById(R.id.diary_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = ConsumptionAdapter(model)
        recyclerView.adapter = adapter

        // setting date for diary
        mCurrentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        var startDate: String = getDay(mCurrentDate, 0)
        var endDate: String = getDay(mCurrentDate, 1)
        model.setDiaryDate(startDate + "/" + endDate)

        // get the results of food search
        model.getDiary().observe(viewLifecycleOwner, Observer { result ->
            // update total intake values of the day (guidelines)
            diary_guidelines_kcal_val.text = (round(result.KCalTotal * 100) / 100).toString()
            diary_guidelines_fiber_val.text = (round(result.FiberTotal * 100.0) / 100.0).toString()
            diary_guidelines_protein_val.text = (round(result.ProteinTotal * 100.0) / 100.0).toString()
            //diary_guidelines_water_val.text = (round(result.WaterTotal * 100.0) / 100.0).toString()    // TODO: water needs to be added to food
            diary_guidelines_calcium_val.text = (round(result.CaliumTotal * 100.0) / 100.0).toString()
            diary_guidelines_sodium_val.text = (round(result.SodiumTotal * 100.0) / 100.0).toString()
            // pass a list of consumptions to the adapter
            adapter.setConsumptionList(result.Consumptions)
        })

        // click events
        mAddFood.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        mPrevDate.setOnClickListener {
            endDate = mCurrentDate
            startDate = getDay(mCurrentDate, -1)
            model.setDiaryDate(startDate + "/" + endDate)
            mCurrentDate = startDate
            d("date", startDate + "/" + endDate)
            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == endDate) {
                activity_main_txt_header.text = "Gisteren"
            } else {
                activity_main_txt_header.text = startDate
            }
        }
        mNextDate.setOnClickListener {
            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) != mCurrentDate) {
                startDate= getDay(mCurrentDate, 1)
                endDate = getDay(mCurrentDate, 2)
                model.setDiaryDate(startDate + "/" + endDate)
                mCurrentDate = startDate
                d("date", startDate + "/" + endDate)
                if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == startDate) {
                    activity_main_txt_header.text = "Vandaag"
                } else if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == endDate) {
                    activity_main_txt_header.text = "Gisteren"
                } else {
                    activity_main_txt_header.text = startDate
                }
            }
        }


        return view
    }
    fun getDay(date: String, daysAdded: Int): String {
        d("AAAAAA", "BBBBB")
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