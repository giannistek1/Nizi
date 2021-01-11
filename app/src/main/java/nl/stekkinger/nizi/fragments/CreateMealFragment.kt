package nl.stekkinger.nizi.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_create_meal.image_food_view
import kotlinx.android.synthetic.main.fragment_create_meal.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.repositories.FoodRepository
import java.io.ByteArrayOutputStream


class CreateMealFragment: NavigationChildFragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var mealProductAdapter: MealProductAdapter
    private lateinit var mInputMealName: EditText
    private var mMealId: Int? = null
    private var mMealName: String = ""
    private var mPhoto: String = ""
    val CAMERA_REQUEST_CODE = 0

    override fun onCreateChildView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal, container, false)
        setHasOptionsMenu(true)

        activity!!.toolbar_title.text = getString(R.string.create_meal)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // update UI with current data
        mInputMealName = view.findViewById(R.id.input_meal_name)
        mInputMealName.setText(model.getMealName())
        if (model.getMealPhoto() != null) {
            val decodedString: ByteArray = Base64.decode(model.getMealPhoto(), Base64.DEFAULT)
            val decodedByte: Bitmap? = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if(decodedByte != null) {
                view.image_food_view.setImageBitmap(decodedByte)
            }
        }
        updateUI(view)



        // rv
        val mealProductRV: RecyclerView = view.findViewById(R.id.create_meal_list_recycler_view)
        mealProductRV.layoutManager = LinearLayoutManager(activity)
        // adapter
        mealProductAdapter = model.getMealProductAdapter()
        mealProductRV.adapter = mealProductAdapter
        // collect mealproducts
        val mealProducts: ArrayList<Food> = model.getMealProducts()
        if (mealProducts.count() > 0) {
            view.create_meal_empty_list_text.visibility = View.GONE
        }
        // fill the adapter
        mealProductAdapter.setMealProductList(model.getMealProducts())

        // stateflows/observers
        // triggered when a meal is saved
        lifecycleScope.launchWhenStarted {
            model.mealState.collect {
                when(it) {
                    is FoodRepository.MealState.Success -> {

                        if (model.getIsMealEdit()) {
                            model.deleteMealFoods(it.data.id)
                            model.createMealFoods(it.data.id)
                        } else {
                            model.createMealFoods(it.data.id)
                        }
                        model.emptyMealState()

                        view.fragment_create_meal_loader.visibility = View.GONE

                        fragmentManager!!
                            .beginTransaction()
                            .replace(
                                R.id.activity_main_fragment_container,
                                AddMealFragment()
                            )
                            .commit()
                    }
                    is FoodRepository.MealState.Error -> {
                        // Todo: add toast for failure
                        view.fragment_create_meal_loader.visibility = View.GONE
                    }
                    is FoodRepository.MealState.Loading -> {
                        view.fragment_create_meal_loader.visibility = View.VISIBLE
                    }
                    else -> {
                        view.fragment_create_meal_loader.visibility = View.GONE
                    }
                }
            }
        }

        // Triggered if new mealfoods are collected (when editing a meal)
        lifecycleScope.launchWhenStarted {
            model.foodsState.collect {
                when(it) {
                    is FoodRepository.FoodsState.Success -> {
                        if (it.data.count() > 0 ) {
                            view.create_meal_empty_list_text.visibility = View.GONE
                        }
                        mealProductAdapter.setMealProductList(it.data)
                        model.setMealProducts(it.data)
                        view.fragment_create_meal_loader.visibility = View.GONE
                        model.emptyFoodsState()
                    }
                    is FoodRepository.FoodsState.Error -> {
                        // TODO: add Error msg toast
                        view.fragment_create_meal_loader.visibility = View.GONE
                    }
                    is FoodRepository.FoodsState.Loading -> {
                        view.fragment_create_meal_loader.visibility = View.VISIBLE
                    }
                    else -> {
                        view.fragment_create_meal_loader.visibility = View.GONE
                    }
                }
            }
        }

        // Update UI with incoming data
        model.selectedMeal.observe(this, Observer<Meal> { meal ->
            if (meal != null) {
                // set meal values in the model
                model.setIsMealEdit(true)
                model.setMealId(meal.id)
                model.setMealComponentId(meal.food_meal_component.id)
                mMealId = meal.id
                mInputMealName.setText(meal.food_meal_component.name)
                if (meal.food_meal_component.image_url != "" && meal.food_meal_component.image_url != null) {
                    model.setMealPhoto(meal.food_meal_component.image_url)
                    mPhoto = meal.food_meal_component.image_url
                    val decodedString: ByteArray =
                        Base64.decode(meal.food_meal_component.image_url, Base64.DEFAULT)
                    val decodedByte: Bitmap? =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    if (decodedByte != null) {
                        view.image_food_view.setImageBitmap(decodedByte)
                    }
                } else {
                    view.image_food_view.setImageResource(R.drawable.ic_culinary)
                }
                updateUI(view)
                model.emptySelectedMeal()
            }
        })

        // click events
        view.meal_camera_btn.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = activity!!.packageManager
            if(callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        }

        view.create_meal_add_food.setOnClickListener {
            // save input data when switching fragments
            model.setMealName(mInputMealName.text.toString().trim())
            if (mPhoto != "") model.setMealPhoto(mPhoto)

            (activity)!!.supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                CreateMealFoodFragment()
            ).commit()
        }

        view.create_meal_save_btn.setOnClickListener {
            saveMeal()
        }

        return view
    }

    private fun updateUI(view: View) {
        // get total nutrition values of the meal
        var totalKcal = 0f
        var totalProtein = 0f
        var totalPotassium = 0f
        var totalSodium = 0f
        var totalWater = 0f
        var totalFiber = 0f
        val products: ArrayList<Food> = model.getMealProducts()
        for (p: Food in products) {
            totalKcal += (p.kcal * p.amount)
            totalProtein += (p.protein * p.amount)
            totalPotassium += (p.potassium * p.amount)
            totalSodium += (p.sodium * p.amount)
            totalWater += (p.water * p.amount)
            totalFiber += (p.fiber * p.amount)
        }

        // update UI
        view.calories_value_meal_view.text = "%.2f".format(totalKcal) + " Kcal"
        view.fiber_value_meal_view.text = "%.2f".format(totalFiber) + " g"
        view.protein_value_meal_view.text = "%.2f".format(totalProtein) + " g"
        view.water_value_meal_view.text = "%.2f".format(totalWater) + "ml"
        view.sodium_value_meal_view.text = "%.2f".format(totalSodium) + " mg"
        view.potassium_value_meal_view.text = "%.2f".format(totalPotassium) + " mg"
    }

    private fun saveMeal(){
        if (!validateMealName()) {
            // validate failed
            return
        } else { // validate success
            // prep photo
            var photoString:String = ""
            var savedPhoto: String? = model.getMealPhoto()
            if(savedPhoto != null && savedPhoto != "") {
                photoString = savedPhoto
            }

            // get total nutrition values of the meal
            var totalKcal = 0f
            var totalProtein = 0f
            var totalPotassium = 0f
            var totalSodium = 0f
            var totalWater = 0f
            var totalFiber = 0f
            var totalPortionSize = 1f
            val products: ArrayList<Food> = model.getMealProducts()
            for (p: Food in products) {
                totalKcal += (p.kcal * p.amount)
                totalProtein += (p.protein * p.amount)
                totalPotassium += (p.potassium * p.amount)
                totalSodium += (p.sodium * p.amount)
                totalWater += (p.water * p.amount)
                totalFiber += (p.fiber * p.amount)
            }

            // create meal object
            val foodMealComponent = FoodMealComponent(
                id = model.getMealComponentId(),
                name = mMealName,
                description = "beschrijving",
                kcal = totalKcal,
                protein = totalProtein,
                potassium = totalPotassium,
                sodium = totalSodium,
                water = totalWater,
                fiber = totalFiber,
                portion_size = totalPortionSize,
                image_url = photoString,
                foodId = 0
            )

            if (model.getIsMealEdit()) {
                val meal = Meal(
                    id = model.getMealId(),
                    food_meal_component = foodMealComponent,
                    patient = GeneralHelper.getUser().patient!!,
                    weight_unit = GeneralHelper.getWeightUnitHolder()!!.weightUnits[7]
                )
                model.updateMeal(meal)
            } else {
                val meal = Meal(
                    name = mMealName,
                    food_meal_component = foodMealComponent,
                    patient = GeneralHelper.getUser().patient!!,
                    weight_unit = GeneralHelper.getWeightUnitHolder()!!.weightUnits[7]
                )
                model.createMeal(meal)
            }
        }
    }

    private fun validateMealName(): Boolean {
        mMealName = mInputMealName.text.toString().trim()
        // validate input for errors
        var succes = true
        when {
            mMealName.isEmpty() -> {
                mInputMealName.error = getString(R.string.meal_name_empty)
                succes = false }
            mMealName.length > 30 -> {
                mInputMealName.error = getString(R.string.meal_name_too_long)
                succes = false }
            else -> { mInputMealName.error = null }
        }
        return succes
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null) {
                    image_food_view.setImageBitmap(data.extras?.get("data") as Bitmap)
                    val bm: Bitmap = data.extras?.get("data") as Bitmap
                    val baos = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.JPEG, 50, baos) //bm is the bitmap object
                    val b: ByteArray = baos.toByteArray()

                    val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)
                    model.setMealPhoto(encodedImage)
                }
            }
            else -> {
                Toast.makeText(this.activity, R.string.photo_error, Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}