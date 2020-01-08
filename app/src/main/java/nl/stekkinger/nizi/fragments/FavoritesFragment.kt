package nl.stekkinger.nizi.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.os.AsyncTask
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import nl.stekkinger.nizi.classes.DiaryViewModel
import nl.stekkinger.nizi.R
import nl.stekkinger.nizi.adapters.FoodSearchAdapter
import nl.stekkinger.nizi.classes.Food
import nl.stekkinger.nizi.repositories.FoodRepository


class FavoritesFragment: Fragment() {
    private val mRepository: FoodRepository = FoodRepository()
    private lateinit var model: DiaryViewModel
    private lateinit var adapter: FoodSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        setHasOptionsMenu(true)

        val recyclerView: RecyclerView = view.favorites_rv
        // TODO: change recycler view
        recyclerView.layoutManager = LinearLayoutManager(activity)

        model = activity?.run {
            ViewModelProviders.of(this)[DiaryViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        adapter = FoodSearchAdapter(model, fragment = "favorites")
        recyclerView.adapter = adapter

        getFavoritesAsyncTask().execute()

        return view
    }

    inner class getFavoritesAsyncTask() : AsyncTask<Void, Void, ArrayList<Food>>() {
        override fun doInBackground(vararg params: Void?): ArrayList<Food>? {
            return mRepository.getFavorites()
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: ArrayList<Food>?) {
            super.onPostExecute(result)
            // update UI
            if(result != null) {
                adapter.setFoodList(result)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_back, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.back_btn -> {
                (activity)!!.supportFragmentManager.beginTransaction().replace(
                    R.id.activity_main_fragment_container,
                    DiaryFragment()
                ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}