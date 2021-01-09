package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.util.Log.d
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_diary.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConsumptionAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.diary.MyFoodResponse
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.*

class DiaryFragment: Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var breakfastAdapter: ConsumptionAdapter
    private lateinit var lunchAdapter: ConsumptionAdapter
    private lateinit var dinnerAdapter: ConsumptionAdapter
    private lateinit var snackAdapter: ConsumptionAdapter
    private lateinit var mNextDayBtn: ImageView
    private lateinit var loader: View

    private val sdf = GeneralHelper.getDateFormat()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_diary, container, false)

        activity!!.toolbar_title.text = getString(R.string.diary)

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
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.time = Date()
        model.setDiaryDate(cal)
        model.emptyState()

        lifecycleScope.launchWhenStarted {
            model.diaryState.collect {
                when(it) {
                    is FoodRepository.DiaryState.Success -> {

                        val breakfastList = ArrayList<ConsumptionResponse>()
                        val lunchList = ArrayList<ConsumptionResponse>()
                        val dinnerList = ArrayList<ConsumptionResponse>()
                        val snackList = ArrayList<ConsumptionResponse>()

                        //sorting consumptions
                        for (c: ConsumptionResponse in it.data) {
                            when (c.meal_time) {
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
                        view.fragment_diary_loader.visibility = GONE
                    }
                    is FoodRepository.DiaryState.Error -> {
                        view.fragment_diary_loader.visibility = GONE
                    }
                    is FoodRepository.DiaryState.Loading -> {
                        view.fragment_diary_loader.visibility = VISIBLE
                    }
                    else -> {
                        view.fragment_diary_loader.visibility = GONE
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            model.consumptionState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        // Somthing has been updated. get new diary data
                        model.getData(cal)
                        view.fragment_diary_loader.visibility = GONE
                        model.emptyState()
                    }
                    is FoodRepository.State.Error -> {
                        view.fragment_diary_loader.visibility = GONE
                    }
                    is FoodRepository.State.Loading -> {
                        view.fragment_diary_loader.visibility = VISIBLE
                    }
                    else -> {
                        view.fragment_diary_loader.visibility = GONE
                    }
                }
            }
        }
        model.fetchFavorites()

        lifecycleScope.launchWhenStarted {
            model.favoritesState.collect {
                when(it) {
                    is FoodRepository.FavoritesState.Success -> {

                        val favorites: ArrayList<MyFood> = ArrayList()
                        for (fav in it.data) {
                            val food = MyFood(id = fav.id, food = fav.food.id)
                            favorites.add(food)
                        }
                        model.setFavorites(favorites)
                    }
                    is FoodRepository.FavoritesState.Error -> {
                        // TODO: handle events below
//                        Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                    is FoodRepository.FavoritesState.Loading -> {
//                        Toast.makeText(activity, "LOADING", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
            }
        }
        model.getData(cal)

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(breakfastRv)

        mNextDayBtn = view.diary_next_date

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
            setNewDate(-1, view)
        }

        view.diary_next_date.setOnClickListener {
            setNewDate(1, view)
        }

        view.diary_next_date.isEnabled = false
        view.diary_next_date.isClickable = false
        view.diary_next_date.alpha = 0.2f

        // Hide patient things if isDoctor
        val isDoctor = GeneralHelper.prefs.getBoolean(GeneralHelper.PREF_IS_DOCTOR, false)
        if (isDoctor) {
            view.diary_add_breakfast.visibility = GONE
            view.diary_add_breakfast_btn.visibility = GONE
            view.diary_add_lunch.visibility = GONE
            view.diary_add_lunch_btn.visibility = GONE
            view.diary_add_dinner.visibility = GONE
            view.diary_add_dinner_btn.visibility = GONE
            view.diary_add_snack.visibility = GONE
            view.diary_add_snack_btn.visibility = GONE
        }
        loader = view.fragment_diary_loader

        return view
    }

    // function to set date in model, and return date as string for UI
    private fun setNewDate(dayAdjustment: Int, v: View) {
        var cal: Calendar = model.getSelectedDate()
        cal.add(Calendar.DATE, dayAdjustment)
        model.setDiaryDate(cal)
        model.getData(cal)

        when (model.getDateString()) {
            "today" -> {
                v.fragment_diary_date.text = getString(R.string.today)
                mNextDayBtn.isEnabled = false
                mNextDayBtn.isClickable = false
                mNextDayBtn.alpha = 0.2f
            }
            "yesterday" -> {
                v.fragment_diary_date.text = getString(R.string.yesterday)
                mNextDayBtn.isEnabled = true
                mNextDayBtn.isClickable = true
                mNextDayBtn.alpha = 1f
            }
            else -> {
                v.fragment_diary_date.text = sdf.format(cal.time)
            }
        }
    }

    private val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                breakfastAdapter.removeItem(viewHolder.adapterPosition)
            }
        }
}