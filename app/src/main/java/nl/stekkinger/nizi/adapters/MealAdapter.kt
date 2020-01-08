package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Meal

class MealAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var dataset: ArrayList<Meal> = ArrayList()
) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return ViewHolder(view)
            .listen { pos, _ ->
//                var food = dataset[pos]
//                val activity = view.context as AppCompatActivity
//                model.select(activity, meal)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var meal : Meal = dataset[position]
        if (meal.Picture != "") {
            Picasso.get().load(meal.Picture).resize(40, 40).into(holder.image)
        }

//        holder.image.setImageURI(uri)
        holder.title.text = meal.Name
        holder.summary.text = meal.PortionSize.toString() + " " + meal.WeightUnit
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
    }

    fun setMealList(mealList: ArrayList<Meal>) {
        this.dataset = mealList
        notifyDataSetChanged()
    }
}