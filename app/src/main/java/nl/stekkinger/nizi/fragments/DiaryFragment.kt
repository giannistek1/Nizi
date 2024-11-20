package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.ConsumptionAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.LocalDb
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.databinding.FragmentDiaryBinding
import nl.stekkinger.nizi.repositories.FoodRepository
import java.util.Calendar
import java.util.Date

class DiaryFragment: BaseFragment() {
    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentDiaryBinding.inflate(layoutInflater)

        requireFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        mNextDayBtn = binding.diaryNextDate

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Hide patient things if isDoctor
        val isDoctor = GeneralHelper.prefs.getBoolean(GeneralHelper.PREF_IS_DOCTOR, false)

//        if (!isDoctor)
//            requireActivity().toolbar_title.text = getString(R.string.diary)

        // Setup custom toast
        val parent: RelativeLayout = binding.fragmentDiaryRl
//        toastView = layoutInflater.inflate(R.layout.custom_toast, parent, false)
//        parent.addView(toastView)

        // Get foodAdded data from bundle
        val bundle: Bundle? = this.arguments
        if (bundle != null) {
            val toastText = bundle.getString(GeneralHelper.TOAST_TEXT, "")

//            GeneralHelper.showAnimatedToast(toastView, toastAnimation, toastText)
        }

        // rv's
        val breakfastRv: RecyclerView = view.findViewById(R.id.diary_breakfast_rv)
        breakfastRv.layoutManager = LinearLayoutManager(activity)
        val lunchRv: RecyclerView = view.findViewById(R.id.diary_lunch_rv)
        lunchRv.layoutManager = LinearLayoutManager(activity)
        val dinnerRv: RecyclerView = view.findViewById(R.id.diary_dinner_rv)
        dinnerRv.layoutManager = LinearLayoutManager(activity)
        val snackRv: RecyclerView = view.findViewById(R.id.diary_snack_rv)
        snackRv.layoutManager = LinearLayoutManager(activity)

        // adapters
        breakfastAdapter = ConsumptionAdapter(model)
        lunchAdapter = ConsumptionAdapter(model)
        dinnerAdapter = ConsumptionAdapter(model)
        snackAdapter = ConsumptionAdapter(model)
        breakfastRv.adapter = breakfastAdapter
        lunchRv.adapter = lunchAdapter
        dinnerRv.adapter = dinnerAdapter
        snackRv.adapter = snackAdapter

        // adapters swipe functions
        ItemTouchHelper(breakfastTouchHelperCallback).attachToRecyclerView(breakfastRv)
        ItemTouchHelper(lunchTouchHelperCallback).attachToRecyclerView(lunchRv)
        ItemTouchHelper(dinnerTouchHelperCallback).attachToRecyclerView(dinnerRv)
        ItemTouchHelper(snackTouchHelperCallback).attachToRecyclerView(snackRv)

        // empty state when loading page(avoids triggering success response if nothing is done)
        model.emptyState()

        // setting date for diary
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.time = Date()
        // setting the date
        model.setDiaryDate(cal)
        //fetching needed diary information
        model.fetchFavorites()
        model.getConsumptions(cal)

        if (GeneralHelper.isAdmin())
        {
            val breakfastList = ArrayList<ConsumptionResponse>()
            val lunchList = ArrayList<ConsumptionResponse>()
            val dinnerList = ArrayList<ConsumptionResponse>()
            val snackList = ArrayList<ConsumptionResponse>()

            // Sorting consumptions
            for (c: ConsumptionResponse in LocalDb.getRandomConsumptionResponses(4)) {
                when (c.meal_time) {
                    getString(R.string.breakfast) -> breakfastList.add(c)
                    getString(R.string.lunch) -> lunchList.add(c)
                    getString(R.string.dinner) -> dinnerList.add(c)
                    getString(R.string.snack) -> snackList.add(c)
                    else -> breakfastList.add(c)
                }
            }

            // pass list of consumptions to adapter
            breakfastAdapter.setConsumptionList(breakfastList)
            lunchAdapter.setConsumptionList(lunchList)
            dinnerAdapter.setConsumptionList(dinnerList)
            snackAdapter.setConsumptionList(snackList)
        }
        else
        {
            // stateflows
            // collects all consumptions and fills the diary
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
                            binding.fragmentDiaryLoader.visibility = GONE
                        }
                        is FoodRepository.DiaryState.Error -> {
                            binding.fragmentDiaryLoader.visibility = GONE
                        }
                        is FoodRepository.DiaryState.Loading -> {
                            binding.fragmentDiaryLoader.visibility = VISIBLE
                        }
                        else -> {
                            binding.fragmentDiaryLoader.visibility = GONE
                        }
                    }
                }
            }
        }

        // if a consumption has been updated while in diary, get new diary data
        lifecycleScope.launchWhenStarted {
            model.consumptionState.collect {
                when(it) {
                    is FoodRepository.State.Success -> {
                        // Something has been updated. get new diary data
                        model.getConsumptions(cal)
                        binding.fragmentDiaryLoader.visibility = GONE
                        model.emptyState()
                    }
                    is FoodRepository.State.Error -> {
                        binding.fragmentDiaryLoader.visibility = GONE
                    }
                    is FoodRepository.State.Loading -> {
                        binding.fragmentDiaryLoader.visibility = VISIBLE
                    }
                    else -> {
                        binding.fragmentDiaryLoader.visibility = GONE
                    }
                }
            }
        }

        // collect user's favorites info
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
                    else -> {}
                }
            }
        }

        // click listeners
        binding.diaryAddBreakfastBtn.setOnClickListener {
            model.setMealTime("Ontbijt")
            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).addToBackStack(null).commit()
        }

        binding.diaryAddLunchBtn.setOnClickListener {
            model.setMealTime("Lunch")
            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).addToBackStack(null).commit()
        }

        binding.diaryAddDinnerBtn.setOnClickListener {
            model.setMealTime("Avondeten")
            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).addToBackStack(null).commit()
        }

        binding.diaryAddSnackBtn.setOnClickListener {
            model.setMealTime("Snack")
            requireFragmentManager().beginTransaction().replace(
                R.id.activity_main_fragment_container,
                AddFoodFragment()
            ).addToBackStack(null).commit()
        }

        binding.fragmentDiaryBreakfast.setOnClickListener {
            if (binding.diaryBreakfastRv.visibility == GONE){
                binding.diaryBreakfastRv.visibility = VISIBLE
            } else {
                binding.diaryBreakfastRv.visibility = GONE
            }
        }

        binding.fragmentDiaryLunch.setOnClickListener {
            if (binding.diaryLunchRv.visibility == GONE){
                binding.diaryLunchRv.visibility = VISIBLE
            } else {
                binding.diaryLunchRv.visibility = GONE
            }
        }

        binding.fragmentDiaryDinner.setOnClickListener {
            if (binding.diaryDinnerRv.visibility == GONE){
                binding.diaryDinnerRv.visibility = VISIBLE
            } else {
                binding.diaryDinnerRv.visibility = GONE
            }
        }

        binding.fragmentDiarySnack.setOnClickListener {
            if (binding.diarySnackRv.visibility == GONE){
                binding.diarySnackRv.visibility = VISIBLE
            } else {
                binding.diarySnackRv.visibility = GONE
            }
        }

        binding.diaryPrevDate.setOnClickListener {
            setNewDate(-1)
        }

        binding.diaryNextDate.setOnClickListener {
            setNewDate(1)
        }

        // btns starting state
        binding.diaryNextDate.isEnabled = false
        binding.diaryNextDate.isClickable = false
        binding.diaryNextDate.alpha = 0.2f

        // Diary view for doctor's side
        if (isDoctor) {
            binding.diaryAddBreakfast.visibility = GONE
            binding.diaryAddBreakfastBtn.visibility = GONE
            binding.diaryAddLunch.visibility = GONE
            binding.diaryAddLunchBtn.visibility = GONE
            binding.diaryAddDinner.visibility = GONE
            binding.diaryAddDinnerBtn.visibility = GONE
            binding.diaryAddSnack.visibility = GONE
            binding.diaryAddSnackBtn.visibility = GONE
        }
        loader = binding.fragmentDiaryLoader
        return binding.root
    }

    // function to set date in model, and return date as string for UI
    private fun setNewDate(dayAdjustment: Int) {
        var cal: Calendar = model.getSelectedDate()
        cal.add(Calendar.DATE, dayAdjustment)
        model.setDiaryDate(cal)
        model.getConsumptions(cal)

        when (model.getDateString()) {
            "today" -> {
                binding.fragmentDiaryDate.text = getString(R.string.today)
                mNextDayBtn.isEnabled = false
                mNextDayBtn.isClickable = false
                mNextDayBtn.alpha = 0.2f
            }
            "yesterday" -> {
                binding.fragmentDiaryDate.text = getString(R.string.yesterday)
                mNextDayBtn.isEnabled = true
                mNextDayBtn.isClickable = true
                mNextDayBtn.alpha = 1f
            }
            else -> {
                binding.fragmentDiaryDate.text = sdf.format(cal.time)
            }
        }
    }

    // swipe callbacks
    private val breakfastTouchHelperCallback: ItemTouchHelper.SimpleCallback =
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
//                GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.food_deleted))
            }
        }

    private val lunchTouchHelperCallback: ItemTouchHelper.SimpleCallback =
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
                lunchAdapter.removeItem(viewHolder.adapterPosition)
//                GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.food_deleted))
            }
        }

    private val dinnerTouchHelperCallback: ItemTouchHelper.SimpleCallback =
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
                dinnerAdapter.removeItem(viewHolder.adapterPosition)
//                GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.food_deleted))
            }
        }

    private val snackTouchHelperCallback: ItemTouchHelper.SimpleCallback =
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
                snackAdapter.removeItem(viewHolder.adapterPosition)
//                GeneralHelper.showAnimatedToast(toastView, toastAnimation, getString(R.string.food_deleted))
            }
        }
}