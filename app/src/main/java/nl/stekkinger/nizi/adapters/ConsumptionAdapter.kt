package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.ConsumptionResponse

class ConsumptionAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<ConsumptionResponse> = ArrayList()
) : RecyclerView.Adapter<ConsumptionAdapter.ViewHolder>() {


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
                var consumption = mDataset[pos]
                val activity = view.context as AppCompatActivity
                model.selectEdit(activity, consumption)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var consumption : ConsumptionResponse = mDataset[position]
        holder.amount.text = consumption.amount.toString() + "x"
        holder.title.text = consumption.food_meal_component.name
        holder.portion.text = "%.2f".format((consumption.amount * consumption.food_meal_component.portion_size)) + " " + consumption.weight_unit.short
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount: TextView = itemView.findViewById(R.id.amount)
        val title: TextView = itemView.findViewById(R.id.title)
        val portion: TextView = itemView.findViewById(R.id.portion_size)
    }

    fun setConsumptionList(consumptionList: ArrayList<ConsumptionResponse>) {
        this.mDataset = consumptionList
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        var consumption : ConsumptionResponse = mDataset[position]
        model.deleteConsumption(consumption.id)
        mDataset.removeAt(position)
        notifyDataSetChanged()
    }
}