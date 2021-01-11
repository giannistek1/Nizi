package nl.stekkinger.nizi.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.util.Log.d
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_food.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.diary.Meal
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.fragments.DiaryFragment

class MealAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<Meal> = ArrayList(),
    private val context: Context
) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {

    private lateinit var activity: AppCompatActivity

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        activity = view.context as AppCompatActivity
        return ViewHolder(view)
            .listen { pos, _ ->
                var meal = mDataset[pos]
                model.emptySelectedMeal()
                model.emptyMealProducts()
                model.selectMeal(activity, meal)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var meal : Meal = mDataset[position]
        holder.favBtn.isVisible = false
        if (meal.food_meal_component.image_url != "" && meal.food_meal_component.image_url != null) {
            val decodedString: ByteArray = Base64.decode(meal.food_meal_component.image_url, Base64.DEFAULT)
            val decodedByte: Bitmap? = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if(decodedByte != null) {
                holder.image.setImageBitmap(decodedByte)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_culinary)
        }
        holder.title.text = meal.food_meal_component.name

        holder.itemView.add_btn.setOnClickListener {
            model.addMeal(meal)

            val fragment = DiaryFragment()
            val bundle = Bundle()
            bundle.putString(GeneralHelper.TOAST_TEXT, context.getString(R.string.add_meal_success))
            fragment.arguments = bundle

            (activity).supportFragmentManager.beginTransaction().replace(
                R.id.activity_main_fragment_container,
                fragment
            ).commit()
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val favBtn: ImageButton = itemView.fav_btn
    }

    fun setMealList(mealList: ArrayList<Meal>) {
        this.mDataset = mealList
        notifyDataSetChanged()
    }
}