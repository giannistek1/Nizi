package nl.stekkinger.nizi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import nl.stekkinger.nizi.classes.*
import nl.stekkinger.nizi.repositories.FoodRepository

class DiaryViewModel(
    private val mRepository: FoodRepository = FoodRepository(),
    private var searchText: MutableLiveData<String> = MutableLiveData()
) : ViewModel() {

    private var mFoodSearch: LiveData<ArrayList<Food>> = Transformations.switchMap<String, ArrayList<Food>>(
        searchText
    ) { search ->  foodSearch(search) }

    private fun foodSearch(searchText: String): MutableLiveData<ArrayList<Food>?> {
        return mRepository.searchFood(searchText)
    }

    fun setFoodSearch(text: String) {
        searchText.value = text
    }

    fun getFoodSearch(): LiveData<ArrayList<Food>> {
        return mFoodSearch
    }
}