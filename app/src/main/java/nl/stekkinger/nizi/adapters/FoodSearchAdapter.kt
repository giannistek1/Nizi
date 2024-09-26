package nl.stekkinger.nizi.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.classes.diary.Food
import nl.stekkinger.nizi.classes.helper_classes.GeneralHelper
import nl.stekkinger.nizi.databinding.ItemFoodBinding
import nl.stekkinger.nizi.fragments.DiaryFragment

class FoodSearchAdapter(
    private var model: DiaryViewModel = DiaryViewModel(),
    private var mDataset: ArrayList<Food> = ArrayList(),
    private var fragment: String,
    private val context: Context
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {

    private lateinit var activity: AppCompatActivity

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: ItemFoodBinding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        activity = view.root.context as AppCompatActivity
        return ViewHolder(view)
            .listen { pos, _ ->
                var food = mDataset[pos]
                if(fragment == "food") { model.select(activity, food) }
                if(fragment == "favorites") { model.select(activity, food) }
                if(fragment == "meal") { model.select(activity, food, fragment) }
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var food: Food = mDataset[position]
        Picasso.get().load(food.image_url).resize(40, 40).into(holder.image)
        holder.title.text = food.name
        // Todo: change into string with template
        //holder.summary.text = Resources.getSystem().getString(R.string.portion_with_weightunit, food.portion_size.toString() + " " + food.weight_unit.unit)
        holder.summary.text = "Portie " + food.portion_size.toString() + " " + food.weight_unit.unit

        // TODO: clean up, maybe per button to avoid dubble code
        if (fragment == "food") {
            holder.addBtn.setOnClickListener {
                model.addConsumption(food)

                // Send boolean with fragment which gives a sign to show the toast in diary
                val fragment = DiaryFragment()
                val bundle = Bundle()
                bundle.putString(GeneralHelper.TOAST_TEXT, context.getString(R.string.add_food))
                fragment.arguments = bundle

                (activity).supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    fragment
                ).commit()
            }
            holder.favBtn.setOnClickListener {
                // do somthing in model
                model.addFavorite(food.id)
                // show toast of success (there is no way to get visual representation of liked foods)
                Toast.makeText(activity, R.string.added_favorite, Toast.LENGTH_SHORT).show()
            }
        } else if (fragment == "meal") {
            holder.addBtn.setOnClickListener {
                model.addMealProduct(food)
                notifyDataSetChanged()
            }
            holder.favBtn.isVisible = false
        } else if(fragment == "favorites") {
            holder.addBtn.setOnClickListener {
                model.addConsumption(food)

                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
            }
            // hide btns
            holder.favBtn.isVisible = false
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mDataset.size

    class ViewHolder(itemView: ItemFoodBinding) : RecyclerView.ViewHolder(itemView.root) {
        val image: ImageView = itemView.foodImage
        val title: TextView = itemView.title
        val summary: TextView = itemView.summary
        val addBtn: ImageButton = itemView.addBtn
        val favBtn: ImageButton = itemView.favBtn
    }

    fun setFoodList(foodList: ArrayList<Food>) {
        this.mDataset = foodList
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        var food : Food = mDataset[position]
        model.deleteFavorite(food.my_food)
        this.mDataset.removeAt(position)
        notifyDataSetChanged()
    }
}