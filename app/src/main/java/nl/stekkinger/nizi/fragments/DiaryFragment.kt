package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.android.synthetic.main.fragment_diary.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConsumptionAdapter
import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.Consumptions
import nl.stekkinger.nizi.classes.DiaryViewModel
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*

class DiaryFragment: Fragment() {
    private lateinit var mCurrentDate: String
    private lateinit var model: DiaryViewModel
    private lateinit var breakfastAdapter: ConsumptionAdapter
    private lateinit var lunchAdapter: ConsumptionAdapter
    private lateinit var dinnerAdapter: ConsumptionAdapter
    private lateinit var snackAdapter: ConsumptionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)

//        val recyclerView: RecyclerView = view.findViewById(R.id.diary_recycler_view)
//        recyclerView.layoutManager = LinearLayoutManager(activity)
        val breakfastRv: RecyclerView = view.findViewById(R.id.diary_breakfast_rv)
        breakfastRv.layoutManager = LinearLayoutManager(activity)
        val lunchRv: RecyclerView = view.findViewById(R.id.diary_lunch_rv)
        lunchRv.layoutManager = LinearLayoutManager(activity)
        val dinnerRv: RecyclerView = view.findViewById(R.id.diary_dinner_rv)
        dinnerRv.layoutManager = LinearLayoutManager(activity)
        val snackRv: RecyclerView = view.findViewById(R.id.diary_snack_rv)
        snackRv.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        breakfastAdapter = ConsumptionAdapter(model)
        lunchAdapter = ConsumptionAdapter(model)
        dinnerAdapter = ConsumptionAdapter(model)
        snackAdapter = ConsumptionAdapter(model)
        breakfastRv.adapter = breakfastAdapter
        lunchRv.adapter = lunchAdapter
        dinnerRv.adapter = dinnerAdapter
        snackRv.adapter = snackAdapter

        // setting date for diary
        mCurrentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        var startDate: String = getDay(mCurrentDate, 0)
        var endDate: String = getDay(mCurrentDate, 1)
        model.setDiaryDate(startDate + "/" + endDate)

        // get the results of food search
        model.getDiary().observe(viewLifecycleOwner, Observer { result ->

            val breakfastList = ArrayList<Consumption>()
            val lunchList = ArrayList<Consumption>()
            val dinnerList = ArrayList<Consumption>()
            val snackList = ArrayList<Consumption>()

            //sorting consumptions
            for (c in result.Consumptions) {
                when (c.MealTime) {
                    "Ontbijt" -> breakfastList.add(c)
                    "Lunch" -> lunchList.add(c)
                    "Avondeten" -> dinnerList.add(c)
                    "Snack" -> snackList.add(c)
                    else -> breakfastList.add(c)
                }
            }

            // pass list of consumptions to adapter
            breakfastAdapter.setConsumptionList(breakfastList)
            lunchAdapter.setConsumptionList(lunchList)
            dinnerAdapter.setConsumptionList(dinnerList)
            snackAdapter.setConsumptionList(snackList)
        })

        // click events
        view.diary_add_breakfast_btn.setOnClickListener {
            model.setMealTime("Ontbijt")
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.diary_add_lunch_btn.setOnClickListener {
            model.setMealTime("Lunch")
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.diary_add_dinner_btn.setOnClickListener {
            model.setMealTime("Avondeten")
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.diary_add_snack_btn.setOnClickListener {
            model.setMealTime("Snack")
            fragmentManager!!
                .beginTransaction()
                .replace(
                    R.id.activity_main_fragment_container,
                    AddFoodFragment()
                )
                .commit()
        }

        view.fragment_diary_breakfast.setOnClickListener {
            if (view.diary_breakfast_rv.visibility == GONE){
                view.diary_breakfast_rv.visibility = VISIBLE
            } else {
                view.diary_breakfast_rv.visibility = GONE
            }
        }

        view.fragment_diary_lunch.setOnClickListener {
            if (view.diary_lunch_rv.visibility == GONE){
                view.diary_lunch_rv.visibility = VISIBLE
            } else {
                view.diary_lunch_rv.visibility = GONE
            }
        }

        view.fragment_diary_dinner.setOnClickListener {
            if (view.diary_dinner_rv.visibility == GONE){
                view.diary_dinner_rv.visibility = VISIBLE
            } else {
                view.diary_dinner_rv.visibility = GONE
            }
        }

        view.fragment_diary_snack.setOnClickListener {
            if (view.diary_snack_rv.visibility == GONE){
                view.diary_snack_rv.visibility = VISIBLE
            } else {
                view.diary_snack_rv.visibility = GONE
            }
        }

        view.diary_prev_date.setOnClickListener {
            endDate = mCurrentDate
            startDate = getDay(mCurrentDate, -1)
            model.setDiaryDate(startDate + "/" + endDate)
            mCurrentDate = startDate
            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) == endDate) {
                activity_main_txt_header.text = "Gisteren"
            } else {
                activity_main_txt_header.text = startDate
            }
        }

        view.diary_next_date.setOnClickListener {
            if(SimpleDateFormat("yyyy-MM-dd").format(Date()) != mCurrentDate) {
                startDate= getDay(mCurrentDate, 1)
                endDate = getDay(mCurrentDate, 2)
                model.setDiaryDate(startDate + "/" + endDate)
                mCurrentDate = startDate
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