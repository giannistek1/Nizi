package nl.stekkinger.nizi.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.patient.PatientItem
import nl.stekkinger.nizi.databinding.ItemPatientBinding

class PatientAdapter(val context: Context, val items: MutableList<PatientItem>, val listener: PatientAdapterListener) : RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: ItemPatientBinding = ItemPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items.get(position)
        holder.id.text = "${item.id}."
        holder.name.text = item.name
        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.adapterPosition)
        }
    }

    // My View Holder
    class MyViewHolder(itemView: ItemPatientBinding) : RecyclerView.ViewHolder(itemView.root) {
        val id: TextView = itemView.itemPatientId
        val name: TextView = itemView.itemPatientName
    }


}
