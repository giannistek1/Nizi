package nl.stekkinger.nizi.fragments

import android.app.Activity
import android.app.SearchManager
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.content.Context.SEARCH_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_create_meal_final.*
import kotlinx.android.synthetic.main.fragment_create_meal_final.view.*
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.adapters.MealProductAdapter
import nl.stekkinger.nizi.repositories.FoodRepository


class CreateMealFinalFragment: Fragment() {
    private lateinit var model: DiaryViewModel
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchAdapter: FoodSearchAdapter
    private lateinit var mealProductAdapter: MealProductAdapter
    private lateinit var mInputMealName: TextInputLayout
    private var mMealName: String = ""
    val CAMERA_REQUEST_CODE = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_meal_final, container, false)
        setHasOptionsMenu(true)

        mInputMealName = view.findViewById(R.id.input_meal_name)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        view.meal_camera_btn.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = activity!!.packageManager
            if(callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        }

        return view
    }

    private fun createMeal(){
        if (!validateMealName()) {
            // failed
            return
        }
        // validate success
        model.createMeal(mMealName)
        (activity)!!.supportFragmentManager.beginTransaction().replace(
            R.id.activity_main_fragment_container,
            AddMealFragment()
        ).commit()
    }

    private fun validateMealName(): Boolean {
        mMealName = mInputMealName.editText?.text.toString().trim()
        // validate input for errors
        when {
            mMealName.isEmpty() -> { mInputMealName.error = getString(R.string.meal_name_empty)}
            mMealName.length > 30 -> { mInputMealName.error = getString(R.string.meal_name_too_long) }
            else -> { mInputMealName.error = null }
        }
        return true
    }



    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
        inflater?.inflate(R.menu.menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.confirm_btn -> {
                // TODO: Check if meal has atleast 2 items
                createMeal()
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    AddMealFragment()
                ).commit()
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
                }
            }
            else -> {
                Toast.makeText(this.activity, "Foto mislukt", Toast.LENGTH_SHORT)
            }
        }
    }
}