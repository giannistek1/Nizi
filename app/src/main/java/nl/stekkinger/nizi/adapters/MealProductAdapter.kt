package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food

class MealProductAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<Food> = ArrayList()
) : RecyclerView.Adapter<MealProductAdapter.ViewHolder>() {


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
                var food: Food = mDataset[pos]
                val activity = view.context as AppCompatActivity
                model.setMealProductPosition(pos)
                model.select(activity, food, "mealEdit")
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var food : Food = mDataset[position]
        holder.amount.text = food.amount.toString() + "x"
        holder.title.text = food.name
        holder.portionSize.text = "%.2f".format(food.amount * food.portion_size) + " " + food.weight_unit.short
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount: TextView = itemView.findViewById(R.id.amount)
        val title: TextView = itemView.findViewById(R.id.title)
        val portionSize: TextView = itemView.findViewById(R.id.portion_size)
    }

    fun setMealProductList(mealList: ArrayList<Food>) {
        this.mDataset = mealList
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        this.mDataset.removeAt(position)
        notifyDataSetChanged()
    }
}