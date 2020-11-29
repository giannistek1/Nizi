package nl.stekkinger.nizi.adapters

import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_diary_food.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.Consumption
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
        holder.title.text = consumption.food_meal_component.name
        holder.summary.text = (consumption.food_meal_component.portion_size * consumption.amount).toString() + " " + consumption.weight_unit.short

        holder.itemView.btn_delete.setOnClickListener {
            model.deleteConsumption(consumption.id)
            mDataset.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
    }

    fun setConsumptionList(consumptionList: ArrayList<ConsumptionResponse>) {
        this.mDataset = consumptionList
        notifyDataSetChanged()
    }
}