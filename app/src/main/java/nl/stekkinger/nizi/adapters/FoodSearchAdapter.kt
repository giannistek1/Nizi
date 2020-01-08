package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Food
import nl.stekkinger.nizi.fragments.DiaryFragment

class FoodSearchAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var dataset: ArrayList<Food> = ArrayList(),
    private var fragment: String
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {

    private lateinit var activity: AppCompatActivity

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        activity = view.context as AppCompatActivity
        return ViewHolder(view)
            .listen { pos, _ ->
                var food = dataset[pos]
                if(fragment == "food") { model.select(activity, food) }
                if(fragment == "meal") { model.selectMealProduct(activity, food) }
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var food : Food = dataset[position]
        Picasso.get().load(food.Picture).resize(40, 40).into(holder.image)
//        holder.image.setImageURI(uri)
        holder.title.text = food.Name
        holder.summary.text = food.PortionSize.toString() + " " + food.WeightUnit

        if(fragment == "food") {
            holder.addBtn.setOnClickListener {
                model.addFood(food)
                notifyDataSetChanged()
                (activity).supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
            }
            holder.favBtn.setOnClickListener {
                // do somthing in model
                // show toast of success
            }

        } else if(fragment == "meal") {
            holder.addBtn.setOnClickListener {
                model.addMealProduct(food)
                notifyDataSetChanged()
            }
            // hide favorite btn
            holder.favBtn.isVisible = false
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.food_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val summary: TextView = itemView.findViewById(R.id.summary)
        val addBtn: ImageButton = itemView.findViewById(R.id.btn_add)
        val favBtn: ImageButton = itemView.findViewById(R.id.fav_btn)
    }

    fun setFoodList(foodList: ArrayList<Food>) {
        this.dataset = foodList
        notifyDataSetChanged()
    }
}