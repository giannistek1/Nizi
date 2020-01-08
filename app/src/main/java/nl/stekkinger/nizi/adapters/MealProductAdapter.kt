package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.diary_food_item.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Food
import nl.stekkinger.nizi.classes.Meal
import nl.stekkinger.nizi.classes.MealProduct

class MealProductAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<MealProduct> = ArrayList()
) : RecyclerView.Adapter<MealProductAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.diary_food_item, parent, false)
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
        holder.title.text = meal.Name
        holder.summary.text = meal.PortionSize.toString() + " " + meal.WeightUnit

        holder.itemView.btn_delete.setOnClickListener {
            model.deleteMealProduct(meal)
            notifyDataSetChanged()
        }
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