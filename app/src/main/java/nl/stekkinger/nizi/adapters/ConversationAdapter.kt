package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.conversation_item.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.old.Conversation

class ConversationAdapter(
    private var mDataset: ArrayList<Conversation> = ArrayList()
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.conversation_item, parent, false)
        return ViewHolder(view)
            .listen { _, _ ->
                if (view.convo_comment.visibility == GONE ) {
                    view.convo_comment.visibility = VISIBLE
                } else {
                    view.convo_comment.visibility = GONE
                }
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var conversation : Conversation = mDataset[position]

        holder.date.text = conversation.Date
        holder.comment.text = conversation.Comment
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.convo_date
        val comment: TextView = itemView.convo_comment
    }

    fun setConversationList(conversations: ArrayList<Conversation>) {
        this.mDataset = conversations
        notifyDataSetChanged()
    }
}