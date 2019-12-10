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

//    private var mSearchFood: LiveData<Food> = Transformations.switchMap<String, Food>(
//        searchText
//    ) { text ->  getFoodSearch(text) }

//    private fun getFoodSearch(searchText: String): MutableLiveData<Food> {
//        return mRepository.searchFood(searchText)
//    }
}