package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_conversation.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.feedback.Feedback
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        // Hide comment
        view.convo_comment.visibility = GONE

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
        val conversation: Feedback = mDataset[position]

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = sdf.format(conversation.date)
        holder.date.text = date
        holder.comment.text = conversation.comment
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.convo_date
        val comment: TextView = itemView.convo_comment
    }

    fun setConversationList(conversations: MutableList<Feedback>) {
        this.mDataset = conversations
        notifyDataSetChanged()
    }
}