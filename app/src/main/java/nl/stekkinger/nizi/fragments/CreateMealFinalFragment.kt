package nl.stekkinger.nizi.fragments

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_create_meal_final.image_food_view
import kotlinx.android.synthetic.main.fragment_create_meal_final.view.*
import kotlinx.coroutines.flow.collect
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.classes.diary.*
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.repositories.FoodRepository
import java.io.ByteArrayOutputStream


class CreateMealFinalFragment: Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchAdapter: FoodSearchAdapter
    private lateinit var mealProductAdapter: MealProductAdapter
    private lateinit var mInputMealName: TextInputLayout
    private var mMealName: String = ""
    private var mPhoto: String? = null
    val CAMERA_REQUEST_CODE = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal_final, container, false)
        setHasOptionsMenu(true)

        mInputMealName = view.findViewById(R.id.input_meal_name)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // rv's
        val mealProductRV: RecyclerView = view.findViewById(R.id.create_meal_list_recycler_view)
        mealProductRV.layoutManager = LinearLayoutManager(activity)

        // adapters
        mealProductAdapter = model.getMealProductAdapter()
        mealProductRV.adapter = mealProductAdapter

        // fill the adapter
        mealProductAdapter.setMealProductList(model.getMealProducts())

        view.meal_camera_btn.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = activity!!.packageManager
            if(callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        }

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
        view.sodium_value_meal_view.text = "%.2f".format(totalSodium * 1000) + " mg"
        view.potassium_value_meal_view.text = "%.2f".format(totalPotassium * 1000) + " mg"


        lifecycleScope.launchWhenStarted {
            model.mealState.collect {
                when(it) {
                    is FoodRepository.MealState.Success -> {
                        model.createMealFoods(it.data.id)
                         view.fragment_create_meal_loader.visibility = View.GONE
                    }
                    is FoodRepository.MealState.Error -> {
                        // TODO: add Error msg toast
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

        return view
    }

    private fun createMeal(){
        if (!validateMealName()) {
            // validate failed
            return
        } else { // validate success
            // prep photo
            var photoString:String = ""
            if(mPhoto != null) {
                photoString = mPhoto.toString()
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

            val meal = Meal(
                food_meal_component = foodMealComponent,
                patient = GeneralHelper.getUser().patient!!,
                weight_unit = GeneralHelper.getWeightUnitHolder()!!.weightUnits[7]
            )

            model.createMeal(meal)
//            (activity)!!.supportFragmentManager.beginTransaction().replace(
//                R.id.activity_main_fragment_container,
//                AddMealFragment()
//            ).commit()
        }

    }

    private fun validateMealName(): Boolean {
        mMealName = mInputMealName.editText?.text.toString().trim()
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



    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                createMeal()
                true
            }
            R.id.back_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    CreateMealFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                    mPhoto = encodedImage
                }
            }
            else -> {
                Toast.makeText(this.activity, R.string.photo_error, Toast.LENGTH_SHORT)
            }
        }
    }
}