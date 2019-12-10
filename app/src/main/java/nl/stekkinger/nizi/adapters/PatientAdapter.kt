package nl.stekkinger.nizi.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_patient.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.PatientItem

class PatientAdapter(val context: Context, val items: ArrayList<PatientItem>, val listener: PatientAdapterListener) : RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items.get(position)
        holder.id.text = "${item.id.toString()}."
        holder.name.text = item.name
        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.adapterPosition)
        }
    }

    // My View Holder
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.item_patient_id
        val name: TextView = itemView.item_patient_name
    }
}
