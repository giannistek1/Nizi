package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_diary_food.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.MealProduct

class MealProductAdapterBU(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<MealProduct> = ArrayList()
) : RecyclerView.Adapter<MealProductAdapterBU.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_food, parent, false)
        return ViewHolder(view)
            .listen { pos, _ ->
//                var food = mDataset[pos]
//                val activity = view.context as AppCompatActivity
//                model.select(activity, meal)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var meal : MealProduct = mDataset[position]
        holder.title.text = meal.food_meal_component.name
//        holder.summary.text = meal.PortionSize.toString() + " " + meal.WeightUnit

//        holder.itemView.btn_delete.setOnClickListener {
//            model.deleteMealProduct(meal)
//            notifyDataSetChanged()
//        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
    }

    fun setMealProductList(mealList: ArrayList<MealProduct>) {
        this.mDataset = mealList
        notifyDataSetChanged()
    }
}