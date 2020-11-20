package nl.stekkinger.nizi.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_meal.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Meal

class MealAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<Meal> = ArrayList()
) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return ViewHolder(view)
            .listen { pos, _ ->
                var meal = mDataset[pos]
                val activity = view.context as AppCompatActivity
                model.selectMeal(activity, meal)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var meal : Meal = mDataset[position]
        if (meal.Picture != "" && meal.Picture != null) {
            val decodedString: ByteArray = Base64.decode(meal.Picture, Base64.DEFAULT)
            val decodedByte: Bitmap? = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if(decodedByte != null) {
                holder.image.setImageBitmap(decodedByte)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_culinary)
//            Picasso.get().load(meal.Picture).resize(40, 40).into(holder.image)
        }
        holder.title.text = meal.Name
        holder.summary.text = meal.PortionSize.toString() + " " + meal.WeightUnit

        holder.itemView.delete_meal_btn.setOnClickListener {
            model.deleteMeal(meal.MealId)
            mDataset.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
    }

    fun setMealList(mealList: ArrayList<Meal>) {
        this.mDataset = mealList
        notifyDataSetChanged()
    }
}