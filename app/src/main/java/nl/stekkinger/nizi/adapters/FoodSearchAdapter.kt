package nl.stekkinger.nizi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_food.view.*
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.Food
import nl.stekkinger.nizi.fragments.DiaryFragment

class FoodSearchAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<Food> = ArrayList(),
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
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        activity = view.context as AppCompatActivity
        return ViewHolder(view)
            .listen { pos, _ ->
                var food = mDataset[pos]
                if(fragment == "food") { model.select(activity, food) }
                if(fragment == "meal") { model.selectMealProduct(activity, food) }
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var food: Food = mDataset[position]
        Picasso.get().load(food.Picture).resize(40, 40).into(holder.image)
        holder.title.text = food.Name
        holder.summary.text = food.PortionSize.toString() + " " + food.WeightUnit

        if (fragment == "food") {
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
                model.addFavorite(food.FoodId)
                // show toast of success (there is no way to get visual representation of liked foods)
                Toast.makeText(activity, "Toegevoegd aan favorieten", Toast.LENGTH_SHORT).show()
            }
            // hide btns
            holder.deleteBtn.isVisible = false
        } else if (fragment == "meal") {
            holder.addBtn.setOnClickListener {
                model.addMealProduct(food)
                notifyDataSetChanged()
            }
            holder.favBtn.isVisible = false
            holder.deleteBtn.isVisible = false
        } else if(fragment == "favorites") {
            holder.addBtn.setOnClickListener {
                model.addFood(food)
                notifyDataSetChanged()
                (activity).supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
            }
            holder.deleteBtn.setOnClickListener {
                // do something in model
                model.deleteFavorite(food.FoodId)
                mDataset.removeAt(holder.adapterPosition)
                notifyDataSetChanged()
            }
            // hide btns
            holder.favBtn.isVisible = false
            holder.deleteBtn.isVisible = true
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.food_image
        val title: TextView = itemView.title
        val summary: TextView = itemView.summary
        val addBtn: ImageButton = itemView.add_btn
        val favBtn: ImageButton = itemView.fav_btn
        val deleteBtn: ImageButton = itemView.delete_btn
    }

    fun setFoodList(foodList: ArrayList<Food>) {
        this.mDataset = foodList
        notifyDataSetChanged()
    }
}