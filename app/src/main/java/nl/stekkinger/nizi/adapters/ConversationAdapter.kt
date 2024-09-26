package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ConversationAdapter(
    private var mDataset: MutableList<Feedback> = ArrayList()
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //val binding = ItemConversationBinding.inflate()
        val view: ItemConversationBinding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Hide comment
        view.convoComment.visibility = GONE

        return ViewHolder(view)
            .listen { _, _ ->
                if (view.convoComment.visibility == GONE ) {
                    view.convoComment.visibility = VISIBLE
                } else {
                    view.convoComment.visibility = GONE
                }
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation: Feedback = mDataset[position]

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = sdf.format(conversation.date)
        holder.date.text = date
        holder.comment.text = conversation.comment
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: ItemConversationBinding) : RecyclerView.ViewHolder(itemView.root) {
        val date: TextView = itemView.convoDate
        val comment: TextView = itemView.convoComment
    }

    fun setConversationList(conversations: MutableList<Feedback>) {
        this.mDataset = conversations
        notifyDataSetChanged()
    }
}