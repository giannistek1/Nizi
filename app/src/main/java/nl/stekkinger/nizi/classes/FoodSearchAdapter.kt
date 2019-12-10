package nl.stekkinger.nizi.classes

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import nl.stekkinger.nizi.DiaryViewModel
import nl.stekkinger.nizi.R

class FoodSearchAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var dataset: ArrayList<Food> = ArrayList()
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {


    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return ViewHolder(view)
            .listen { pos, _ ->
                var food = dataset[pos]
                val activity = view.context as AppCompatActivity
                model.select(activity, food)
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var food : Food = dataset[position]
        val tmpPicture = "https://vaperztx.com/wp-content/uploads/2016/02/bynytdy6elugfira8nsh.jpg"
        val uri: Uri = Uri.parse(tmpPicture)
        Picasso.get().load(tmpPicture).resize(40, 40).into(holder.image) // TODO: food.Picture
        holder.image.setImageURI(uri)
        holder.title.text = food.Name
        holder.summary.text = food.PortionSize.toString() + " " + food.WeightUnit

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
    }

    fun setFoodList(foodList: ArrayList<Food>) {
        this.dataset = foodList
        notifyDataSetChanged()
    }
}